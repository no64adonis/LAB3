package com.lottery;

import com.lottery.model.LotteryTicket;
import com.lottery.model.TicketHistorySummary;
import com.lottery.model.User;
import com.lottery.model.UserTicketHistory;
import com.lottery.db.RoundDAO;
import com.lottery.db.UserTicketHistoryDAO;
import com.lottery.service.SearchResult;
import com.lottery.service.TicketService;
import com.lottery.service.UserService;
import com.lottery.service.exception.ServiceException;
import com.lottery.service.exception.UserNotFoundException;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("17. MyTicketsServlet (/myTickets)")
class MyTicketsServletTest {

    private MyTicketsServlet servlet;

    @Mock private TicketService ticketService;
    @Mock private UserService userService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;

    private User regularUser;
    private MockedStatic<UserTicketHistoryDAO> mockedHistoryDAO;
    private MockedStatic<RoundDAO> mockedRoundDAO;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new MyTicketsServlet();
        setField(servlet, "ticketService", ticketService);
        setField(servlet, "userService", userService);
        regularUser = new User();
        regularUser.setUserID(1);

        mockedHistoryDAO = mockStatic(UserTicketHistoryDAO.class);
        mockedRoundDAO = mockStatic(RoundDAO.class);

        mockedHistoryDAO.when(() -> UserTicketHistoryDAO.getAllHistoryForUser(anyInt(), anyInt(), anyInt()))
                .thenReturn(new ArrayList<>());
        mockedHistoryDAO.when(() -> UserTicketHistoryDAO.countAllHistoryForUser(anyInt()))
                .thenReturn(0);
        mockedHistoryDAO.when(() -> UserTicketHistoryDAO.getFinancialSummaryByTimePeriod(anyInt(), anyString()))
                .thenReturn(new TicketHistorySummary(java.math.BigDecimal.ZERO, java.math.BigDecimal.ZERO));
        mockedHistoryDAO.when(() -> UserTicketHistoryDAO.getHistoryByRound(anyInt(), anyInt()))
                .thenReturn(new ArrayList<>());
        mockedHistoryDAO.when(() -> UserTicketHistoryDAO.getFinancialSummary(anyInt(), anyInt()))
                .thenReturn(new TicketHistorySummary(java.math.BigDecimal.ZERO, java.math.BigDecimal.ZERO));
        mockedRoundDAO.when(() -> RoundDAO.getPastRoundsForUser(anyInt()))
                .thenReturn(new ArrayList<>());
    }

    @AfterEach
    void tearDown() {
        mockedHistoryDAO.close();
        mockedRoundDAO.close();
    }

    private void setupLoggedInUser() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("user@test.com");
        when(userService.getUserByEmail("user@test.com")).thenReturn(regularUser);
    }

    private void setupDefaultParams() {
        when(request.getParameter("page")).thenReturn(null);
        when(request.getParameter("historyPage")).thenReturn(null);
        when(request.getParameter("timePeriod")).thenReturn(null);
        when(request.getParameter("roundId")).thenReturn(null);
    }

    @Test @DisplayName("#1 GET - Not logged in")
    void testDoGet_notLoggedIn() throws Exception {
        when(request.getSession(false)).thenReturn(null);
        servlet.doGet(request, response);
        verify(response).sendRedirect("login.jsp");
    }

    @Test @DisplayName("#2 GET - User not found")
    void testDoGet_userNotFound() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("user@test.com");
        when(userService.getUserByEmail("user@test.com")).thenReturn(null);
        servlet.doGet(request, response);
        verify(response).sendRedirect("login.jsp");
    }

    @Test @DisplayName("#3 GET - Default load")
    void testDoGet_defaultLoad() throws Exception {
        setupLoggedInUser(); setupDefaultParams();
        SearchResult sr = buildSearchResult(Collections.emptyList(), 1, 0, 0, null);
        when(ticketService.processUserTickets(1, null)).thenReturn(sr);
        when(request.getRequestDispatcher("myTickets.jsp")).thenReturn(dispatcher);
        servlet.doGet(request, response);
        verify(dispatcher).forward(request, response);
    }

    @Test @DisplayName("#4 GET - Ticket pagination (page=2)")
    void testDoGet_ticketPagination() throws Exception {
        setupLoggedInUser(); setupDefaultParams();
        when(request.getParameter("page")).thenReturn("2");
        SearchResult sr = buildSearchResult(Collections.emptyList(), 2, 3, 25, null);
        when(ticketService.processUserTickets(1, "2")).thenReturn(sr);
        when(request.getRequestDispatcher("myTickets.jsp")).thenReturn(dispatcher);
        servlet.doGet(request, response);
        verify(dispatcher).forward(request, response);
    }

    @Test @DisplayName("#5 GET - History pagination")
    void testDoGet_historyPagination() throws Exception {
        setupLoggedInUser(); setupDefaultParams();
        when(request.getParameter("historyPage")).thenReturn("2");
        SearchResult sr = buildSearchResult(Collections.emptyList(), 1, 0, 0, null);
        when(ticketService.processUserTickets(1, null)).thenReturn(sr);
        when(request.getRequestDispatcher("myTickets.jsp")).thenReturn(dispatcher);
        servlet.doGet(request, response);
        verify(request).setAttribute("historyCurrentPage", 2);
    }

    @Test @DisplayName("#6 GET - Invalid historyPage (abc)")
    void testDoGet_invalidHistoryPage() throws Exception {
        setupLoggedInUser(); setupDefaultParams();
        when(request.getParameter("historyPage")).thenReturn("abc");
        SearchResult sr = buildSearchResult(Collections.emptyList(), 1, 0, 0, null);
        when(ticketService.processUserTickets(1, null)).thenReturn(sr);
        when(request.getRequestDispatcher("myTickets.jsp")).thenReturn(dispatcher);
        servlet.doGet(request, response);
        verify(request).setAttribute("historyCurrentPage", 1);
    }

    @Test @DisplayName("#7 GET - Time period summary")
    void testDoGet_timePeriod() throws Exception {
        setupLoggedInUser(); setupDefaultParams();
        when(request.getParameter("timePeriod")).thenReturn("30days");
        SearchResult sr = buildSearchResult(Collections.emptyList(), 1, 0, 0, null);
        when(ticketService.processUserTickets(1, null)).thenReturn(sr);
        when(request.getRequestDispatcher("myTickets.jsp")).thenReturn(dispatcher);
        servlet.doGet(request, response);
        verify(request).setAttribute("selectedTimePeriod", "30days");
    }

    @Test @DisplayName("#8 GET - Past rounds loaded")
    void testDoGet_pastRounds() throws Exception {
        setupLoggedInUser(); setupDefaultParams();
        SearchResult sr = buildSearchResult(Collections.emptyList(), 1, 0, 0, null);
        when(ticketService.processUserTickets(1, null)).thenReturn(sr);
        when(request.getRequestDispatcher("myTickets.jsp")).thenReturn(dispatcher);
        servlet.doGet(request, response);
        verify(dispatcher).forward(request, response);
    }

    @Test @DisplayName("#9 GET - View round detail")
    void testDoGet_roundDetail() throws Exception {
        setupLoggedInUser(); setupDefaultParams();
        when(request.getParameter("roundId")).thenReturn("5");
        SearchResult sr = buildSearchResult(Collections.emptyList(), 1, 0, 0, null);
        when(ticketService.processUserTickets(1, null)).thenReturn(sr);
        when(request.getRequestDispatcher("myTickets.jsp")).thenReturn(dispatcher);
        servlet.doGet(request, response);
        verify(request).setAttribute("selectedRoundId", 5);
    }

    @Test @DisplayName("#10 GET - Invalid roundId")
    void testDoGet_invalidRoundId() throws Exception {
        setupLoggedInUser(); setupDefaultParams();
        when(request.getParameter("roundId")).thenReturn("abc");
        SearchResult sr = buildSearchResult(Collections.emptyList(), 1, 0, 0, null);
        when(ticketService.processUserTickets(1, null)).thenReturn(sr);
        when(request.getRequestDispatcher("myTickets.jsp")).thenReturn(dispatcher);
        servlet.doGet(request, response);
        verify(dispatcher).forward(request, response);
    }

    @Test @DisplayName("#11 GET - UserNotFoundException")
    void testDoGet_userNotFoundException() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("u@t.com");
        when(userService.getUserByEmail("u@t.com")).thenThrow(new UserNotFoundException("Not found"));
        servlet.doGet(request, response);
        verify(response).sendRedirect("login.jsp");
    }

    @Test @DisplayName("#12 GET - ServiceException")
    void testDoGet_serviceException() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("u@t.com");
        when(userService.getUserByEmail("u@t.com")).thenThrow(new ServiceException("DB error"));
        servlet.doGet(request, response);
        verify(response).sendRedirect("welcome.jsp");
    }

    private SearchResult buildSearchResult(List<?> tickets, int page, int totalPages, int totalTickets, String company) {
        SearchResult sr = new SearchResult();
        sr.setTickets((List<LotteryTicket>) (List<?>) tickets);
        sr.setCurrentPage(page);
        sr.setTotalPages(totalPages);
        sr.setTotalTickets(totalTickets);
        sr.setCompany(company);
        return sr;
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
