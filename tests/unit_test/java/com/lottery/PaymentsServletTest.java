package com.lottery;

import com.lottery.model.PaymentMethod;
import com.lottery.model.User;
import com.lottery.service.PaymentService;
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

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("21. PaymentsServlet (/payments)")
class PaymentsServletTest {

    private PaymentsServlet servlet;

    @Mock private PaymentService paymentService;
    @Mock private UserService userService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;

    private User regularUser;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new PaymentsServlet();
        setField(servlet, "paymentService", paymentService);
        setField(servlet, "userService", userService);
        regularUser = new User();
        regularUser.setUserID(1);
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
        when(paymentService.getUserPaymentMethods(1)).thenReturn(Collections.emptyList());
        when(request.getRequestDispatcher("/payments.jsp")).thenReturn(dispatcher);
        servlet.doGet(request, response);
        verify(request).setAttribute("paymentMethods", Collections.emptyList());
        verify(dispatcher).forward(request, response);
    }

    @Test @DisplayName("#4 POST - Not logged in")
    void testDoPost_notLoggedIn() throws Exception {
        when(request.getSession(false)).thenReturn(null);
        servlet.doPost(request, response);
        verify(response).sendRedirect("login.jsp");
    }

    @Test @DisplayName("#5 POST - User not found")
    void testDoPost_userNotFound() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("user@test.com");
        when(userService.getUserByEmail("user@test.com")).thenReturn(null);
        servlet.doPost(request, response);
        verify(response).sendRedirect("login.jsp");
    }

    @Test @DisplayName("#6 POST - addPayment success")
    void testDoPost_addPaymentSuccess() throws Exception {
        setupLoggedInUser();
        when(request.getParameter("action")).thenReturn("addPayment");
        when(request.getParameter("cardNumber")).thenReturn("4111111111111111");
        when(request.getParameter("cardHolder")).thenReturn("John Doe");
        when(request.getParameter("expiryMonth")).thenReturn("12");
        when(request.getParameter("expiryYear")).thenReturn("25");
        when(request.getParameter("cvv")).thenReturn("123");
        when(paymentService.processAddPaymentMethod(regularUser, "4111111111111111", "12", "25", "123", "John Doe"))
                .thenReturn(true);
        servlet.doPost(request, response);
        verify(session).setAttribute("message", "Payment method added successfully.");
        verify(response).sendRedirect("payments");
    }

    @Test @DisplayName("#7 POST - addPayment failure")
    void testDoPost_addPaymentFailure() throws Exception {
        setupLoggedInUser();
        when(request.getParameter("action")).thenReturn("addPayment");
        when(request.getParameter("cardNumber")).thenReturn("4111111111111111");
        when(request.getParameter("cardHolder")).thenReturn("John Doe");
        when(request.getParameter("expiryMonth")).thenReturn("12");
        when(request.getParameter("expiryYear")).thenReturn("25");
        when(request.getParameter("cvv")).thenReturn("123");
        when(paymentService.processAddPaymentMethod(regularUser, "4111111111111111", "12", "25", "123", "John Doe"))
                .thenReturn(false);
        servlet.doPost(request, response);
        verify(session).setAttribute("message", "Failed to add payment method. Please try again.");
    }

    @Test @DisplayName("#8 POST - addPayment service error")
    void testDoPost_addPaymentServiceError() throws Exception {
        setupLoggedInUser();
        when(request.getParameter("action")).thenReturn("addPayment");
        when(request.getParameter("cardNumber")).thenReturn("4111111111111111");
        when(request.getParameter("cardHolder")).thenReturn("John Doe");
        when(request.getParameter("expiryMonth")).thenReturn("12");
        when(request.getParameter("expiryYear")).thenReturn("25");
        when(request.getParameter("cvv")).thenReturn("123");
        when(paymentService.processAddPaymentMethod(regularUser, "4111111111111111", "12", "25", "123", "John Doe"))
                .thenThrow(new ServiceException("Card invalid"));
        servlet.doPost(request, response);
        verify(session).setAttribute("message", "Card invalid");
    }

    @Test @DisplayName("#9 POST - deletePayment success")
    void testDoPost_deletePaymentSuccess() throws Exception {
        setupLoggedInUser();
        when(request.getParameter("action")).thenReturn("deletePayment");
        when(request.getParameter("paymentId")).thenReturn("1");
        when(paymentService.processDeletePaymentMethod("1")).thenReturn(true);
        servlet.doPost(request, response);
        verify(session).setAttribute("message", "Payment method deleted successfully.");
    }

    @Test @DisplayName("#10 POST - deletePayment failure")
    void testDoPost_deletePaymentFailure() throws Exception {
        setupLoggedInUser();
        when(request.getParameter("action")).thenReturn("deletePayment");
        when(request.getParameter("paymentId")).thenReturn("1");
        when(paymentService.processDeletePaymentMethod("1")).thenReturn(false);
        servlet.doPost(request, response);
        verify(session).setAttribute("message", "Failed to delete payment method.");
    }

    @Test @DisplayName("#11 POST - deletePayment service error")
    void testDoPost_deletePaymentServiceError() throws Exception {
        setupLoggedInUser();
        when(request.getParameter("action")).thenReturn("deletePayment");
        when(request.getParameter("paymentId")).thenReturn("1");
        when(paymentService.processDeletePaymentMethod("1")).thenThrow(new ServiceException("DB error"));
        servlet.doPost(request, response);
        verify(session).setAttribute("message", "DB error");
    }

    @Test @DisplayName("#12 POST - Unknown action")
    void testDoPost_unknownAction() throws Exception {
        setupLoggedInUser();
        when(request.getParameter("action")).thenReturn(null);
        servlet.doPost(request, response);
        verify(response).sendRedirect("payments");
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
