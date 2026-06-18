package com.lottery;

import com.lottery.model.Transaction;
import com.lottery.model.User;
import com.lottery.service.TransactionService;
import com.lottery.service.UserService;
import com.lottery.service.exception.ServiceException;
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

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("14. AdminTransactionServlet (/adminTransactions)")
class AdminTransactionServletTest {

    private AdminTransactionServlet servlet;

    @Mock private TransactionService transactionService;
    @Mock private UserService userService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new AdminTransactionServlet();
        setField(servlet, "transactionService", transactionService);
        setField(servlet, "userService", userService);
    }

    @Test
    @DisplayName("#1 GET - Not logged in")
    void testDoGet_notLoggedIn() throws Exception {
        when(request.getSession(false)).thenReturn(null);
        servlet.doGet(request, response);
        verify(response).sendRedirect("login.jsp");
    }

    @Test
    @DisplayName("#2 GET - Non-admin user")
    void testDoGet_nonAdmin() throws Exception {
        User user = new User();
        user.setRole("user");

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("user@test.com");
        when(userService.getUserByEmail("user@test.com")).thenReturn(user);

        servlet.doGet(request, response);

        verify(response).sendRedirect("welcome.jsp");
    }

    @Test
    @DisplayName("#3 GET - Default load (all transactions)")
    void testDoGet_defaultLoad() throws Exception {
        User admin = new User();
        admin.setRole("admin");

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("admin@test.com");
        when(userService.getUserByEmail("admin@test.com")).thenReturn(admin);
        when(request.getParameter("email")).thenReturn(null);
        when(request.getParameter("startDate")).thenReturn(null);
        when(request.getParameter("endDate")).thenReturn(null);
        when(request.getParameter("page")).thenReturn(null);
        when(transactionService.getAllTransactions(0, 10)).thenReturn(Collections.emptyList());
        when(transactionService.getAllTransactionCount()).thenReturn(0);
        when(request.getRequestDispatcher("adminTransactions.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute("transactions", Collections.emptyList());
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("#4 GET - Search with email filter")
    void testDoGet_searchByEmail() throws Exception {
        User admin = new User();
        admin.setRole("admin");

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("admin@test.com");
        when(userService.getUserByEmail("admin@test.com")).thenReturn(admin);
        when(request.getParameter("email")).thenReturn("user@test.com");
        when(request.getParameter("startDate")).thenReturn(null);
        when(request.getParameter("endDate")).thenReturn(null);
        when(request.getParameter("page")).thenReturn(null);
        when(transactionService.searchTransactions("user@test.com", null, null, 0, 10))
                .thenReturn(Collections.emptyList());
        when(transactionService.searchTransactionCount("user@test.com", null, null)).thenReturn(0);
        when(request.getRequestDispatcher("adminTransactions.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute("searchEmail", "user@test.com");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("#5 GET - SQL injection in email (sanitized)")
    void testDoGet_sqlInjectionEmail() throws Exception {
        User admin = new User();
        admin.setRole("admin");

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("admin@test.com");
        when(userService.getUserByEmail("admin@test.com")).thenReturn(admin);
        when(request.getParameter("email")).thenReturn("'; DROP TABLE--");
        when(request.getParameter("startDate")).thenReturn(null);
        when(request.getParameter("endDate")).thenReturn(null);
        when(request.getParameter("page")).thenReturn(null);
        when(transactionService.getAllTransactions(0, 10)).thenReturn(Collections.emptyList());
        when(transactionService.getAllTransactionCount()).thenReturn(0);
        when(request.getRequestDispatcher("adminTransactions.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("#6 GET - Search with date range")
    void testDoGet_searchByDateRange() throws Exception {
        User admin = new User();
        admin.setRole("admin");

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("admin@test.com");
        when(userService.getUserByEmail("admin@test.com")).thenReturn(admin);
        when(request.getParameter("email")).thenReturn(null);
        when(request.getParameter("startDate")).thenReturn("2024-01-01");
        when(request.getParameter("endDate")).thenReturn("2024-12-31");
        when(request.getParameter("page")).thenReturn(null);
        when(transactionService.searchTransactions(null, "2024-01-01", "2024-12-31", 0, 10))
                .thenReturn(Collections.emptyList());
        when(transactionService.searchTransactionCount(null, "2024-01-01", "2024-12-31")).thenReturn(0);
        when(request.getRequestDispatcher("adminTransactions.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute("startDate", "2024-01-01");
        verify(request).setAttribute("endDate", "2024-12-31");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("#7 GET - Pagination page 2")
    void testDoGet_pagination() throws Exception {
        User admin = new User();
        admin.setRole("admin");

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("admin@test.com");
        when(userService.getUserByEmail("admin@test.com")).thenReturn(admin);
        when(request.getParameter("email")).thenReturn(null);
        when(request.getParameter("startDate")).thenReturn(null);
        when(request.getParameter("endDate")).thenReturn(null);
        when(request.getParameter("page")).thenReturn("2");
        when(transactionService.getAllTransactions(10, 10)).thenReturn(Collections.emptyList());
        when(transactionService.getAllTransactionCount()).thenReturn(15);
        when(request.getRequestDispatcher("adminTransactions.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute("currentPage", 2);
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("#8 GET - User not found")
    void testDoGet_userNotFound() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("admin@test.com");
        when(userService.getUserByEmail("admin@test.com")).thenThrow(new UserNotFoundException("Not found"));

        servlet.doGet(request, response);

        verify(session).setAttribute("error", "User not found.");
        verify(response).sendRedirect("login.jsp");
    }

    @Test
    @DisplayName("#9 GET - Service exception")
    void testDoGet_serviceException() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("admin@test.com");
        when(userService.getUserByEmail("admin@test.com")).thenThrow(new ServiceException("DB error"));

        servlet.doGet(request, response);

        verify(session).setAttribute("error", "Error retrieving transactions: DB error");
        verify(response).sendRedirect("welcome.jsp");
    }

    @Test
    @DisplayName("#10 GET - Invalid date format")
    void testDoGet_invalidDateFormat() throws Exception {
        User admin = new User();
        admin.setRole("admin");

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("admin@test.com");
        when(userService.getUserByEmail("admin@test.com")).thenReturn(admin);
        when(request.getParameter("email")).thenReturn(null);
        when(request.getParameter("startDate")).thenReturn("not-a-date");
        when(request.getParameter("endDate")).thenReturn(null);
        when(request.getParameter("page")).thenReturn(null);
        when(transactionService.getAllTransactions(0, 10)).thenReturn(Collections.emptyList());
        when(transactionService.getAllTransactionCount()).thenReturn(0);
        when(request.getRequestDispatcher("adminTransactions.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(dispatcher).forward(request, response);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
