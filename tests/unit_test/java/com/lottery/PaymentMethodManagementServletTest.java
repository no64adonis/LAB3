package com.lottery;

import com.lottery.model.PaymentMethod;
import com.lottery.model.User;
import com.lottery.service.AdminService;
import com.lottery.service.UserService;
import com.lottery.service.exception.ServiceException;

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
import java.util.Collections;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("22. PaymentMethodManagementServlet (/paymentMethodManagement)")
class PaymentMethodManagementServletTest {

    private PaymentMethodManagementServlet servlet;

    @Mock private UserService userService;
    @Mock private AdminService adminService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;

    private User adminUser;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new PaymentMethodManagementServlet();
        setField(servlet, "userService", userService);
        setField(servlet, "adminService", adminService);
        adminUser = new User();
        adminUser.setRole("admin");
    }

    private void setupAdminGet() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("admin@test.com");
        when(userService.getUserByEmail("admin@test.com")).thenReturn(adminUser);
    }

    private void setupAdminPost() throws Exception {
        setupAdminGet();
        when(request.getSession()).thenReturn(session);
    }

    @Test @DisplayName("#1 GET - Not logged in")
    void testDoGet_notLoggedIn() throws Exception {
        when(request.getSession(false)).thenReturn(null);
        servlet.doGet(request, response);
        verify(response).sendRedirect("login.jsp");
    }

    @Test @DisplayName("#2 GET - Non-admin")
    void testDoGet_nonAdmin() throws Exception {
        User user = new User(); user.setRole("user");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("user@test.com");
        when(userService.getUserByEmail("user@test.com")).thenReturn(user);
        servlet.doGet(request, response);
        verify(response).sendRedirect("welcome.jsp");
    }

    @Test @DisplayName("#3 GET - Default load")
    void testDoGet_defaultLoad() throws Exception {
        setupAdminGet();
        when(request.getParameter("page")).thenReturn(null);
        when(adminService.getAllPaymentMethodsWithUsers(0, 20)).thenReturn(Collections.emptyList());
        when(adminService.getTotalPaymentMethodCount()).thenReturn(0);
        when(request.getRequestDispatcher("/paymentMethodManagement.jsp")).thenReturn(dispatcher);
        servlet.doGet(request, response);
        verify(dispatcher).forward(request, response);
    }

    @Test @DisplayName("#4 GET - Pagination")
    void testDoGet_pagination() throws Exception {
        setupAdminGet();
        when(request.getParameter("page")).thenReturn("2");
        when(adminService.getAllPaymentMethodsWithUsers(20, 20)).thenReturn(Collections.emptyList());
        when(adminService.getTotalPaymentMethodCount()).thenReturn(25);
        when(request.getRequestDispatcher("/paymentMethodManagement.jsp")).thenReturn(dispatcher);
        servlet.doGet(request, response);
        verify(request).setAttribute("currentPage", 2);
    }

    @Test @DisplayName("#5 POST - Not logged in")
    void testDoPost_notLoggedIn() throws Exception {
        when(request.getSession(false)).thenReturn(null);
        servlet.doPost(request, response);
        verify(response).sendRedirect("login.jsp");
    }

    @Test @DisplayName("#6 POST - Non-admin")
    void testDoPost_nonAdmin() throws Exception {
        User user = new User(); user.setRole("user");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("user@test.com");
        when(userService.getUserByEmail("user@test.com")).thenReturn(user);
        servlet.doPost(request, response);
        verify(response).sendRedirect("welcome.jsp");
    }

    @Test @DisplayName("#7 POST - Update payment method success")
    void testDoPost_updateSuccess() throws Exception {
        setupAdminPost();
        when(request.getParameter("action")).thenReturn("updatePaymentMethod");
        when(request.getParameter("paymentMethodId")).thenReturn("1");
        when(request.getParameter("lastFourDigits")).thenReturn("1234");
        when(request.getParameter("cardHolder")).thenReturn("John");
        when(request.getParameter("expiryDate")).thenReturn("12/25");
        when(adminService.updatePaymentMethod(any(PaymentMethod.class))).thenReturn(true);
        servlet.doPost(request, response);
        verify(session).setAttribute("message", "Payment method updated successfully.");
    }

    @Test @DisplayName("#8 POST - Update payment method - invalid ID")
    void testDoPost_updateInvalidId() throws Exception {
        setupAdminPost();
        when(request.getParameter("action")).thenReturn("updatePaymentMethod");
        when(request.getParameter("paymentMethodId")).thenReturn("abc");
        servlet.doPost(request, response);
        verify(session).setAttribute(eq("error"), contains("Invalid"));
    }

    @Test @DisplayName("#9 POST - Update payment method - null ID")
    void testDoPost_updateNullId() throws Exception {
        setupAdminPost();
        when(request.getParameter("action")).thenReturn("updatePaymentMethod");
        when(request.getParameter("paymentMethodId")).thenReturn(null);
        servlet.doPost(request, response);
        verify(session).setAttribute(eq("error"), any(String.class));
    }

    @Test @DisplayName("#10 POST - Delete payment method success")
    void testDoPost_deleteSuccess() throws Exception {
        setupAdminPost();
        when(request.getParameter("action")).thenReturn("deletePaymentMethod");
        when(request.getParameter("paymentMethodId")).thenReturn("1");
        when(adminService.deletePaymentMethod(1)).thenReturn(true);
        servlet.doPost(request, response);
        verify(session).setAttribute("message", "Payment method deleted successfully.");
    }

    @Test @DisplayName("#11 POST - Delete payment method - invalid ID")
    void testDoPost_deleteInvalidId() throws Exception {
        setupAdminPost();
        when(request.getParameter("action")).thenReturn("deletePaymentMethod");
        when(request.getParameter("paymentMethodId")).thenReturn("0");
        servlet.doPost(request, response);
        verify(session).setAttribute(eq("error"), any(String.class));
    }

    @Test @DisplayName("#12 POST - Delete payment method - null ID")
    void testDoPost_deleteNullId() throws Exception {
        setupAdminPost();
        when(request.getParameter("action")).thenReturn("deletePaymentMethod");
        when(request.getParameter("paymentMethodId")).thenReturn(null);
        servlet.doPost(request, response);
        verify(session).setAttribute(eq("error"), any(String.class));
    }

    @Test @DisplayName("#13 POST - Bulk delete success")
    void testDoPost_bulkDeleteSuccess() throws Exception {
        setupAdminPost();
        when(request.getParameter("action")).thenReturn("bulkDelete");
        when(request.getParameterValues("selectedPaymentMethods")).thenReturn(new String[]{"1", "2"});
        when(adminService.bulkDeletePaymentMethods(new String[]{"1", "2"})).thenReturn(true);
        servlet.doPost(request, response);
        verify(session).setAttribute("message", "Payment methods deleted successfully.");
    }

    @Test @DisplayName("#14 POST - Bulk delete failure")
    void testDoPost_bulkDeleteFailure() throws Exception {
        setupAdminPost();
        when(request.getParameter("action")).thenReturn("bulkDelete");
        when(request.getParameterValues("selectedPaymentMethods")).thenReturn(new String[]{"1", "2"});
        when(adminService.bulkDeletePaymentMethods(new String[]{"1", "2"})).thenReturn(false);
        servlet.doPost(request, response);
        verify(session).setAttribute(eq("error"), contains("Failed"));
    }

    @Test @DisplayName("#15 POST - Unknown action")
    void testDoPost_unknownAction() throws Exception {
        setupAdminGet();
        when(request.getParameter("action")).thenReturn("unknown");
        servlet.doPost(request, response);
        verify(response).sendRedirect("paymentMethodManagement");
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
