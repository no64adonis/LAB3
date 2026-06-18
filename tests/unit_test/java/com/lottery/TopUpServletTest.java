package com.lottery;

import com.lottery.model.User;
import com.lottery.service.PaymentService;
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
import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("20. TopUpServlet (/topup)")
class TopUpServletTest {

    private TopUpServlet servlet;

    @Mock private PaymentService paymentService;
    @Mock private UserService userService;
    @Mock private TransactionService transactionService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;

    private User regularUser;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new TopUpServlet();
        setField(servlet, "paymentService", paymentService);
        setField(servlet, "userService", userService);
        setField(servlet, "transactionService", transactionService);
        regularUser = new User();
        regularUser.setUserID(1);
        regularUser.setBalance(BigDecimal.valueOf(500));
    }

    private void setupLoggedInUser() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("user@test.com");
        when(userService.getUserByEmail("user@test.com")).thenReturn(regularUser);
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
        setupLoggedInUser();
        when(request.getParameter("page")).thenReturn(null);
        when(paymentService.getUserPaymentMethods(1)).thenReturn(Collections.emptyList());
        when(transactionService.getUserTransactions(1, 0, 10)).thenReturn(Collections.emptyList());
        when(transactionService.getUserTransactionCount(1)).thenReturn(0);
        when(request.getRequestDispatcher("/topup.jsp")).thenReturn(dispatcher);
        servlet.doGet(request, response);
        verify(request).setAttribute("user", regularUser);
        verify(dispatcher).forward(request, response);
    }

    @Test @DisplayName("#4 GET - Transaction pagination")
    void testDoGet_pagination() throws Exception {
        setupLoggedInUser();
        when(request.getParameter("page")).thenReturn("2");
        when(paymentService.getUserPaymentMethods(1)).thenReturn(Collections.emptyList());
        when(transactionService.getUserTransactions(1, 10, 10)).thenReturn(Collections.emptyList());
        when(transactionService.getUserTransactionCount(1)).thenReturn(15);
        when(request.getRequestDispatcher("/topup.jsp")).thenReturn(dispatcher);
        servlet.doGet(request, response);
        verify(request).setAttribute("currentPage", 2);
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

    @Test @DisplayName("#7 POST - SQL injection in action")
    void testDoPost_sqlInjection() throws Exception {
        setupLoggedInUser();
        when(request.getParameter("action")).thenReturn("'; DROP TABLE--");
        servlet.doPost(request, response);
        verify(session).setAttribute("message", "Invalid request.");
        verify(response).sendRedirect("topup");
    }

    @Test @DisplayName("#8 POST - topup valid amount")
    void testDoPost_topupValid() throws Exception {
        setupLoggedInUser();
        when(request.getParameter("action")).thenReturn("topup");
        when(request.getParameter("amount")).thenReturn("100");
        when(request.getParameter("paymentMethod")).thenReturn("1");
        when(paymentService.processTopUpRequest(regularUser, "100", "1")).thenReturn(true);
        servlet.doPost(request, response);
        verify(session).setAttribute(eq("message"), contains("100"));
        verify(response).sendRedirect("topup");
    }

    @Test @DisplayName("#9 POST - topup invalid amount")
    void testDoPost_topupInvalidAmount() throws Exception {
        setupLoggedInUser();
        when(request.getParameter("action")).thenReturn("topup");
        when(request.getParameter("amount")).thenReturn("abc");
        when(request.getParameter("paymentMethod")).thenReturn("1");
        servlet.doPost(request, response);
        verify(session).setAttribute("message", "Invalid amount specified.");
        verify(response).sendRedirect("topup");
    }

    @Test @DisplayName("#10 POST - topup service error")
    void testDoPost_topupServiceError() throws Exception {
        setupLoggedInUser();
        when(request.getParameter("action")).thenReturn("topup");
        when(request.getParameter("amount")).thenReturn("100");
        when(request.getParameter("paymentMethod")).thenReturn("1");
        when(paymentService.processTopUpRequest(regularUser, "100", "1"))
                .thenThrow(new ServiceException("Processing error"));
        servlet.doPost(request, response);
        verify(session).setAttribute("message", "Processing error");
    }

    @Test @DisplayName("#11 POST - topup returns false")
    void testDoPost_topupReturnsFalse() throws Exception {
        setupLoggedInUser();
        when(request.getParameter("action")).thenReturn("topup");
        when(request.getParameter("amount")).thenReturn("100");
        when(request.getParameter("paymentMethod")).thenReturn("1");
        when(paymentService.processTopUpRequest(regularUser, "100", "1")).thenReturn(false);
        servlet.doPost(request, response);
        verify(session).setAttribute("message", "Failed to update balance. Please try again.");
    }

    @Test @DisplayName("#12 POST - topupCustom valid")
    void testDoPost_topupCustomValid() throws Exception {
        setupLoggedInUser();
        when(request.getParameter("action")).thenReturn("topupCustom");
        when(request.getParameter("amount")).thenReturn("50");
        when(request.getParameter("paymentMethod")).thenReturn("1");
        when(paymentService.processTopUpRequest(regularUser, "50", "1")).thenReturn(true);
        servlet.doPost(request, response);
        verify(session).setAttribute(eq("message"), contains("50"));
    }

    @Test @DisplayName("#13 POST - topupCustom invalid amount")
    void testDoPost_topupCustomInvalid() throws Exception {
        setupLoggedInUser();
        when(request.getParameter("action")).thenReturn("topupCustom");
        when(request.getParameter("amount")).thenReturn(null);
        when(request.getParameter("paymentMethod")).thenReturn("1");
        servlet.doPost(request, response);
        verify(session).setAttribute("message", "Invalid amount specified.");
    }

    @Test @DisplayName("#14 POST - Unknown action")
    void testDoPost_unknownAction() throws Exception {
        setupLoggedInUser();
        when(request.getParameter("action")).thenReturn("unknown");
        servlet.doPost(request, response);
        verify(response).sendRedirect("topup");
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
