package com.lottery;

import com.lottery.config.ServiceFactory;
import com.lottery.model.LotteryTicket;
import com.lottery.service.SearchResult;
import com.lottery.service.TicketService;
import com.lottery.service.UserService;
import com.lottery.service.exception.ServiceException;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("7. HomepageServlet (/homepage)")
class HomepageServletTest {

    private HomepageServlet servlet;

    @Mock private TicketService ticketService;
    @Mock private UserService userService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new HomepageServlet();
        setField(servlet, "ticketService", ticketService);
        setField(servlet, "userService", userService);
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
        when(request.getSession(false)).thenReturn(null);
    }

    @Test
    @DisplayName("#1 GET - Default load (no params)")
    void testDoGet_defaultLoad() throws Exception {
        setupDefaultParams();
        SearchResult sr = buildSearchResult(Collections.emptyList(), 1, 0, 0, null);
        when(ticketService.getAllPublishedCompanies()).thenReturn(Collections.emptyList());
        when(ticketService.getAllDistinctCompanies()).thenReturn(Collections.emptyList());
        when(ticketService.processUserLotterySearch(any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any())).thenReturn(sr);
        when(request.getRequestDispatcher("/index.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute("companies", Collections.emptyList());
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("#2 GET - Service exception on company list")
    void testDoGet_companyListError() throws Exception {
        setupDefaultParams();
        when(ticketService.getAllPublishedCompanies()).thenThrow(new ServiceException("DB error"));
        SearchResult sr = buildSearchResult(Collections.emptyList(), 1, 0, 0, null);
        when(ticketService.processUserLotterySearch(any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any())).thenReturn(sr);
        when(request.getRequestDispatcher("/index.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute("companies", Collections.emptyList());
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("#3 GET - Search with company filter")
    void testDoGet_searchWithCompany() throws Exception {
        setupDefaultParams();
        when(request.getParameterValues("company")).thenReturn(new String[]{"CompanyA"});
        when(ticketService.getAllPublishedCompanies()).thenReturn(Arrays.asList("CompanyA", "CompanyB"));
        when(ticketService.getAllDistinctCompanies()).thenReturn(Arrays.asList("CompanyA", "CompanyB"));

        LotteryTicket ticket = new LotteryTicket();
        ticket.setTicketID("T1");
        SearchResult sr = buildSearchResult(Arrays.asList(ticket), 1, 1, 1, "CompanyA");
        when(ticketService.processUserLotterySearch(any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any())).thenReturn(sr);
        when(request.getRequestDispatcher("/index.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute("company", "CompanyA");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("#7 GET - Pagination")
    void testDoGet_pagination() throws Exception {
        setupDefaultParams();
        when(request.getParameter("page")).thenReturn("2");
        when(ticketService.getAllPublishedCompanies()).thenReturn(Collections.emptyList());
        when(ticketService.getAllDistinctCompanies()).thenReturn(Collections.emptyList());
        SearchResult sr = buildSearchResult(Collections.emptyList(), 2, 3, 25, null);
        when(ticketService.processUserLotterySearch(any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), eq("2"))).thenReturn(sr);
        when(request.getRequestDispatcher("/index.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute("currentPage", 2);
        verify(request).setAttribute("totalPages", 3);
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("#12 GET - Clear history (logged in)")
    void testDoGet_clearHistoryLoggedIn() throws Exception {
        setupDefaultParams();
        when(request.getParameter("action")).thenReturn("clearHistory");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("user@test.com");

        servlet.doGet(request, response);

        verify(response).sendRedirect("homepage");
    }

    @Test
    @DisplayName("#13 GET - Clear history (guest)")
    void testDoGet_clearHistoryGuest() throws Exception {
        setupDefaultParams();
        when(request.getParameter("action")).thenReturn("clearHistory");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn(null);

        servlet.doGet(request, response);

        verify(session).removeAttribute("guestSearchHistory");
        verify(response).sendRedirect("homepage");
    }

    @Test
    @DisplayName("#14 GET - Service exception during search")
    void testDoGet_searchException() throws Exception {
        setupDefaultParams();
        when(ticketService.getAllPublishedCompanies()).thenReturn(Collections.emptyList());
        when(ticketService.getAllDistinctCompanies()).thenReturn(Collections.emptyList());
        when(ticketService.processUserLotterySearch(any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any())).thenThrow(new ServiceException("error"));
        when(request.getRequestDispatcher("/index.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute("tickets", Collections.emptyList());
        verify(request).setAttribute("currentPage", 1);
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("#15 POST - Delegates to doGet")
    void testDoPost_delegatesToGet() throws Exception {
        setupDefaultParams();
        when(ticketService.getAllPublishedCompanies()).thenReturn(Collections.emptyList());
        when(ticketService.getAllDistinctCompanies()).thenReturn(Collections.emptyList());
        SearchResult sr = buildSearchResult(Collections.emptyList(), 1, 0, 0, null);
        when(ticketService.processUserLotterySearch(any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any())).thenReturn(sr);
        when(request.getRequestDispatcher("/index.jsp")).thenReturn(dispatcher);

        servlet.doPost(request, response);

        verify(dispatcher).forward(request, response);
    }

    private SearchResult buildSearchResult(java.util.List<LotteryTicket> tickets, int page, int totalPages, int totalTickets, String company) {
        SearchResult sr = new SearchResult();
        sr.setTickets(tickets);
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
