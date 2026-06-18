package com.lottery;

import com.lottery.model.LotteryTicket;
import com.lottery.model.User;
import com.lottery.service.SearchResult;
import com.lottery.service.TicketService;
import com.lottery.service.UserService;
import com.lottery.service.exception.InsufficientBalanceException;
import com.lottery.service.exception.ServiceException;
import com.lottery.service.exception.TicketNotFoundException;
import com.lottery.service.exception.UserNotFoundException;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("18. TicketPurchaseServlet (/ticketPurchase)")
class TicketPurchaseServletTest {

    private TicketPurchaseServlet servlet;

    @Mock private TicketService ticketService;
    @Mock private UserService userService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;

    private User regularUser;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new TicketPurchaseServlet();
        setField(servlet, "ticketService", ticketService);
        setField(servlet, "userService", userService);
        regularUser = new User();
        regularUser.setUserID(1);
        regularUser.setBalance(BigDecimal.valueOf(1000));
    }

    private void setupLoggedInUser() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("user@test.com");
        when(userService.getUserByEmail("user@test.com")).thenReturn(regularUser);
    }

    private void setupDefaultSearchParams() {
        when(request.getParameter("company")).thenReturn(null);
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
    }

    private void setupDefaultPostParams() {
        setupDefaultSearchParams();
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

    @Test @DisplayName("#3 GET - No search (show form)")
    void testDoGet_noSearch() throws Exception {
        setupLoggedInUser(); setupDefaultSearchParams();
        when(ticketService.getAllPublishedCompanies()).thenReturn(Collections.emptyList());
        when(request.getRequestDispatcher("ticketPurchase.jsp")).thenReturn(dispatcher);
        servlet.doGet(request, response);
        verify(dispatcher).forward(request, response);
    }

    @Test @DisplayName("#4 GET - With search (company)")
    void testDoGet_withSearch() throws Exception {
        setupLoggedInUser(); setupDefaultSearchParams();
        when(request.getParameter("company")).thenReturn("TestCo");
        SearchResult sr = buildSearchResult(Collections.emptyList(), 1, 0, 0, "TestCo");
        when(ticketService.searchTicketsForPurchase("TestCo", null, null, null, null, null, null,
                null, null, null, null)).thenReturn(sr);
        when(ticketService.getAllPublishedCompanies()).thenReturn(Collections.emptyList());
        when(request.getRequestDispatcher("ticketPurchase.jsp")).thenReturn(dispatcher);
        servlet.doGet(request, response);
        verify(request).setAttribute("searchPerformed", true);
    }

    @Test @DisplayName("#5 POST - Not logged in")
    void testDoPost_notLoggedIn() throws Exception {
        when(request.getSession(false)).thenReturn(null);
        servlet.doPost(request, response);
        verify(response).sendRedirect("login.jsp");
    }

    @Test @DisplayName("#6 POST - User not found")
    void testDoPost_userNotFound() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("user@test.com");
        when(userService.getUserByEmail("user@test.com")).thenReturn(null);
        servlet.doPost(request, response);
        verify(response).sendRedirect("login.jsp");
    }

    @Test @DisplayName("#7 POST - Missing ticketId")
    void testDoPost_missingTicketId() throws Exception {
        setupLoggedInUser(); setupDefaultPostParams();
        when(ticketService.getAllPublishedCompanies()).thenReturn(Collections.emptyList());
        when(request.getRequestDispatcher("ticketPurchase.jsp")).thenReturn(dispatcher);
        servlet.doPost(request, response);
        verify(request).setAttribute(eq("errorMessage"), contains("Ticket ID is required"));
    }

    @Test @DisplayName("#8 POST - Ticket not published")
    void testDoPost_ticketNotPublished() throws Exception {
        setupLoggedInUser(); setupDefaultPostParams();
        when(request.getParameter("ticketId")).thenReturn("T001");
        LotteryTicket ticket = new LotteryTicket();
        ticket.setTicketID("T001");
        ticket.setPublished(false);
        when(ticketService.getTicketById("T001")).thenReturn(ticket);
        when(ticketService.getAllPublishedCompanies()).thenReturn(Collections.emptyList());
        when(request.getRequestDispatcher("ticketPurchase.jsp")).thenReturn(dispatcher);
        servlet.doPost(request, response);
        verify(request).setAttribute(eq("errorMessage"), contains("published"));
    }

    @Test @DisplayName("#9 POST - Ticket already owned")
    void testDoPost_ticketAlreadyOwned() throws Exception {
        setupLoggedInUser(); setupDefaultPostParams();
        when(request.getParameter("ticketId")).thenReturn("T001");
        LotteryTicket ticket = new LotteryTicket();
        ticket.setTicketID("T001");
        ticket.setPublished(true);
        ticket.setOwnerId("99");
        when(ticketService.getTicketById("T001")).thenReturn(ticket);
        when(ticketService.getAllPublishedCompanies()).thenReturn(Collections.emptyList());
        when(request.getRequestDispatcher("ticketPurchase.jsp")).thenReturn(dispatcher);
        servlet.doPost(request, response);
        verify(request).setAttribute(eq("errorMessage"), contains("no longer available"));
    }

    @Test @DisplayName("#10 POST - Purchase success")
    void testDoPost_purchaseSuccess() throws Exception {
        setupLoggedInUser(); setupDefaultPostParams();
        when(request.getParameter("ticketId")).thenReturn("T001");
        LotteryTicket ticket = new LotteryTicket();
        ticket.setTicketID("T001");
        ticket.setPublished(true);
        ticket.setPrice(50.0);
        when(ticketService.getTicketById("T001")).thenReturn(ticket);
        when(ticketService.purchaseTicket(ticket, regularUser)).thenReturn(true);
        when(ticketService.getAllPublishedCompanies()).thenReturn(Collections.emptyList());
        when(request.getRequestDispatcher("ticketPurchase.jsp")).thenReturn(dispatcher);
        servlet.doPost(request, response);
        verify(request).setAttribute(eq("successMessage"), contains("Ticket purchased successfully"));
    }

    @Test @DisplayName("#11 POST - Insufficient balance")
    void testDoPost_insufficientBalance() throws Exception {
        setupLoggedInUser(); setupDefaultPostParams();
        when(request.getParameter("ticketId")).thenReturn("T001");
        LotteryTicket ticket = new LotteryTicket();
        ticket.setTicketID("T001");
        ticket.setPublished(true);
        ticket.setPrice(50.0);
        when(ticketService.getTicketById("T001")).thenReturn(ticket);
        when(ticketService.purchaseTicket(ticket, regularUser))
                .thenThrow(new InsufficientBalanceException(BigDecimal.valueOf(10), BigDecimal.valueOf(50)));
        when(ticketService.getAllPublishedCompanies()).thenReturn(Collections.emptyList());
        when(request.getRequestDispatcher("ticketPurchase.jsp")).thenReturn(dispatcher);
        servlet.doPost(request, response);
        verify(request).setAttribute(eq("errorMessage"), contains("Insufficient"));
    }

    @Test @DisplayName("#12 POST - Bulk purchase success")
    void testDoPost_bulkPurchaseSuccess() throws Exception {
        setupLoggedInUser(); setupDefaultPostParams();
        when(request.getParameter("ticketIds")).thenReturn("T001,T002");
        when(request.getParameter("action")).thenReturn("bulkPurchase");
        when(ticketService.purchaseMultipleTickets(Arrays.asList("T001", "T002"), regularUser))
                .thenReturn(Arrays.asList("T001", "T002"));
        LotteryTicket t1 = new LotteryTicket(); t1.setTicketID("T001"); t1.setPrice(50.0);
        LotteryTicket t2 = new LotteryTicket(); t2.setTicketID("T002"); t2.setPrice(50.0);
        when(ticketService.getTicketById("T001")).thenReturn(t1);
        when(ticketService.getTicketById("T002")).thenReturn(t2);
        when(ticketService.getAllPublishedCompanies()).thenReturn(Collections.emptyList());
        when(request.getRequestDispatcher("ticketPurchase.jsp")).thenReturn(dispatcher);
        servlet.doPost(request, response);
        verify(request).setAttribute(eq("successMessage"), contains("2 ticket(s)"));
    }

    @Test @DisplayName("#13 POST - Ticket not found exception")
    void testDoPost_ticketNotFound() throws Exception {
        setupLoggedInUser(); setupDefaultPostParams();
        when(request.getParameter("ticketId")).thenReturn("INVALID");
        when(ticketService.getTicketById("INVALID")).thenThrow(new TicketNotFoundException("Not found"));
        when(ticketService.getAllPublishedCompanies()).thenReturn(Collections.emptyList());
        when(request.getRequestDispatcher("ticketPurchase.jsp")).thenReturn(dispatcher);
        servlet.doPost(request, response);
        verify(request).setAttribute(eq("errorMessage"), contains("no longer available"));
    }

    @Test @DisplayName("#14 POST - ServiceException")
    void testDoPost_serviceException() throws Exception {
        setupLoggedInUser(); setupDefaultPostParams();
        when(request.getParameter("ticketId")).thenReturn("T001");
        when(ticketService.getTicketById("T001")).thenThrow(new ServiceException("DB error"));
        when(ticketService.getAllPublishedCompanies()).thenReturn(Collections.emptyList());
        when(request.getRequestDispatcher("ticketPurchase.jsp")).thenReturn(dispatcher);
        servlet.doPost(request, response);
        verify(request).setAttribute(eq("errorMessage"), eq("DB error"));
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
