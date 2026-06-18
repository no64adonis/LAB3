package com.lottery;

import com.lottery.model.LotteryTicket;
import com.lottery.model.Round;
import com.lottery.model.User;
import com.lottery.db.RoundDAO;
import com.lottery.db.UserSearchHistoryDAO;
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
@DisplayName("19. UserLotteryServlet (/userLottery)")
class UserLotteryServletTest {

    private UserLotteryServlet servlet;

    @Mock private TicketService ticketService;
    @Mock private UserService userService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;

    private User regularUser;
    private MockedStatic<UserSearchHistoryDAO> mockedSearchHistoryDAO;
    private MockedStatic<RoundDAO> mockedRoundDAO;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new UserLotteryServlet();
        setField(servlet, "ticketService", ticketService);
        setField(servlet, "userService", userService);
        regularUser = new User();
        regularUser.setUserID(1);

        mockedSearchHistoryDAO = mockStatic(UserSearchHistoryDAO.class);
        mockedRoundDAO = mockStatic(RoundDAO.class);

        mockedSearchHistoryDAO.when(() -> UserSearchHistoryDAO.getUserSearchHistory(anyInt(), anyInt()))
                .thenReturn(new ArrayList<>());
        mockedSearchHistoryDAO.when(() -> UserSearchHistoryDAO.searchPromptExists(anyInt(), anyString()))
                .thenReturn(false);
        mockedSearchHistoryDAO.when(() -> UserSearchHistoryDAO.createSearchHistory(anyInt(), anyString()))
                .thenReturn(true);
        mockedSearchHistoryDAO.when(() -> UserSearchHistoryDAO.updateSearchHistoryDate(anyInt()))
                .thenReturn(true);
        mockedSearchHistoryDAO.when(() -> UserSearchHistoryDAO.clearUserSearchHistory(anyInt()))
                .thenReturn(true);
        mockedRoundDAO.when(() -> RoundDAO.getAllPastRounds())
                .thenReturn(new ArrayList<>());
    }

    @AfterEach
    void tearDown() {
        mockedSearchHistoryDAO.close();
        mockedRoundDAO.close();
    }

    private void setupLoggedInUser() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("user@test.com");
        when(userService.getUserByEmail("user@test.com")).thenReturn(regularUser);
    }

    private void setupDefaultParams() {
        when(request.getParameterValues("company")).thenReturn(null);
        when(request.getParameter("num1")).thenReturn(null);
        when(request.getParameter("num2")).thenReturn(null);
        when(request.getParameter("num3")).thenReturn(null);
        when(request.getParameter("num4")).thenReturn(null);
        when(request.getParameter("num5")).thenReturn(null);
        when(request.getParameter("num6")).thenReturn(null);
        when(request.getParameter("startDate")).thenReturn(null);
        when(request.getParameter("endDate")).thenReturn(null);
        when(request.getParameter("specificDate")).thenReturn(null);
        when(request.getParameter("page")).thenReturn(null);
        when(request.getParameter("fromHistory")).thenReturn(null);
        when(request.getParameter("searchId")).thenReturn(null);
        when(request.getParameter("action")).thenReturn(null);
        when(request.getParameter("roundsPage")).thenReturn(null);
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
        when(userService.getUserByEmail("user@test.com")).thenThrow(new UserNotFoundException("Not found"));
        servlet.doGet(request, response);
        verify(session).setAttribute("error", "User not found.");
        verify(response).sendRedirect("login.jsp");
    }

    @Test @DisplayName("#3 GET - Default load")
    void testDoGet_defaultLoad() throws Exception {
        setupLoggedInUser(); setupDefaultParams();
        SearchResult sr = buildSearchResult(Collections.emptyList(), 1, 0, 0, null);
        when(ticketService.processUserLotterySearch(any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any())).thenReturn(sr);
        when(ticketService.getAllPublishedCompanies()).thenReturn(Collections.emptyList());
        when(request.getRequestDispatcher("/userLottery.jsp")).thenReturn(dispatcher);
        servlet.doGet(request, response);
        verify(dispatcher).forward(request, response);
    }

    @Test @DisplayName("#4 GET - Search with company")
    void testDoGet_searchWithCompany() throws Exception {
        setupLoggedInUser(); setupDefaultParams();
        when(request.getParameterValues("company")).thenReturn(new String[]{"CompA"});
        SearchResult sr = buildSearchResult(Collections.emptyList(), 1, 0, 0, "CompA");
        when(ticketService.processUserLotterySearch(any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any())).thenReturn(sr);
        when(ticketService.getAllPublishedCompanies()).thenReturn(Collections.singletonList("CompA"));
        when(request.getRequestDispatcher("/userLottery.jsp")).thenReturn(dispatcher);
        servlet.doGet(request, response);
        verify(request).setAttribute("company", "CompA");
    }

    @Test @DisplayName("#5 GET - Search with numbers")
    void testDoGet_searchWithNumbers() throws Exception {
        setupLoggedInUser(); setupDefaultParams();
        when(request.getParameter("num1")).thenReturn("5");
        SearchResult sr = buildSearchResult(Collections.emptyList(), 1, 0, 0, null);
        when(ticketService.processUserLotterySearch(any(), eq("5"), any(), any(), any(), any(), any(),
                any(), any(), any(), any())).thenReturn(sr);
        when(ticketService.getAllPublishedCompanies()).thenReturn(Collections.emptyList());
        when(request.getRequestDispatcher("/userLottery.jsp")).thenReturn(dispatcher);
        servlet.doGet(request, response);
        verify(request).setAttribute("num1", "5");
    }

    @Test @DisplayName("#6 GET - Search with dates")
    void testDoGet_searchWithDates() throws Exception {
        setupLoggedInUser(); setupDefaultParams();
        when(request.getParameter("startDate")).thenReturn("2024-01-01");
        when(request.getParameter("endDate")).thenReturn("2024-12-31");
        SearchResult sr = buildSearchResult(Collections.emptyList(), 1, 0, 0, null);
        when(ticketService.processUserLotterySearch(any(), any(), any(), any(), any(), any(), any(),
                eq("2024-01-01"), eq("2024-12-31"), any(), any())).thenReturn(sr);
        when(ticketService.getAllPublishedCompanies()).thenReturn(Collections.emptyList());
        when(request.getRequestDispatcher("/userLottery.jsp")).thenReturn(dispatcher);
        servlet.doGet(request, response);
        verify(request).setAttribute("startDate", "2024-01-01");
    }

    @Test @DisplayName("#7 GET - Pagination (page=3)")
    void testDoGet_pagination() throws Exception {
        setupLoggedInUser(); setupDefaultParams();
        when(request.getParameter("page")).thenReturn("3");
        SearchResult sr = buildSearchResult(Collections.emptyList(), 3, 5, 50, null);
        when(ticketService.processUserLotterySearch(any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), eq("3"))).thenReturn(sr);
        when(ticketService.getAllPublishedCompanies()).thenReturn(Collections.emptyList());
        when(request.getRequestDispatcher("/userLottery.jsp")).thenReturn(dispatcher);
        servlet.doGet(request, response);
        verify(request).setAttribute("currentPage", 3);
    }

    @Test @DisplayName("#8 GET - View count increment")
    void testDoGet_viewCountIncrement() throws Exception {
        setupLoggedInUser(); setupDefaultParams();
        LotteryTicket ticket = new LotteryTicket(); ticket.setTicketID("T1");
        SearchResult sr = buildSearchResult(Collections.singletonList(ticket), 1, 1, 1, null);
        when(ticketService.processUserLotterySearch(any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any())).thenReturn(sr);
        when(ticketService.getAllPublishedCompanies()).thenReturn(Collections.emptyList());
        when(request.getRequestDispatcher("/userLottery.jsp")).thenReturn(dispatcher);
        servlet.doGet(request, response);
        verify(ticketService).incrementViewCountBatch(any());
    }

    @Test @DisplayName("#9 GET - Clear history")
    void testDoGet_clearHistory() throws Exception {
        setupLoggedInUser(); setupDefaultParams();
        when(request.getParameter("action")).thenReturn("clearHistory");
        SearchResult sr = buildSearchResult(Collections.emptyList(), 1, 0, 0, null);
        when(ticketService.processUserLotterySearch(any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any())).thenReturn(sr);
        servlet.doGet(request, response);
        verify(response).sendRedirect("userLottery");
    }

    @Test @DisplayName("#10 GET - Rounds pagination")
    void testDoGet_roundsPagination() throws Exception {
        setupLoggedInUser(); setupDefaultParams();
        when(request.getParameter("roundsPage")).thenReturn("2");
        SearchResult sr = buildSearchResult(Collections.emptyList(), 1, 0, 0, null);
        when(ticketService.processUserLotterySearch(any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any())).thenReturn(sr);
        when(ticketService.getAllPublishedCompanies()).thenReturn(Collections.emptyList());
        when(request.getRequestDispatcher("/userLottery.jsp")).thenReturn(dispatcher);
        
        List<Round> rounds = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            Round r = new Round();
            r.setRoundID(i + 1);
            rounds.add(r);
        }
        mockedRoundDAO.when(() -> RoundDAO.getAllPastRounds()).thenReturn(rounds);
        servlet.doGet(request, response);
        verify(request).setAttribute("roundsCurrentPage", 2);
    }

    @Test @DisplayName("#11 GET - Invalid roundsPage")
    void testDoGet_invalidRoundsPage() throws Exception {
        setupLoggedInUser(); setupDefaultParams();
        when(request.getParameter("roundsPage")).thenReturn("abc");
        SearchResult sr = buildSearchResult(Collections.emptyList(), 1, 0, 0, null);
        when(ticketService.processUserLotterySearch(any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any())).thenReturn(sr);
        when(ticketService.getAllPublishedCompanies()).thenReturn(Collections.emptyList());
        when(request.getRequestDispatcher("/userLottery.jsp")).thenReturn(dispatcher);
        servlet.doGet(request, response);
        verify(request).setAttribute("roundsCurrentPage", 1);
    }

    @Test @DisplayName("#12 GET - ServiceException")
    void testDoGet_serviceException() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("user@test.com");
        when(userService.getUserByEmail("user@test.com")).thenThrow(new ServiceException("DB error"));
        servlet.doGet(request, response);
        verify(session).setAttribute("error", "Error retrieving data: DB error");
        verify(response).sendRedirect("welcome.jsp");
    }

    @Test @DisplayName("#13 POST - Delegates to doGet")
    void testDoPost_delegatesToGet() throws Exception {
        when(request.getSession(false)).thenReturn(null);
        servlet.doPost(request, response);
        verify(response).sendRedirect("login.jsp");
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
