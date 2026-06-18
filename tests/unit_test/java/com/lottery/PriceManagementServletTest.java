package com.lottery;

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

import java.lang.reflect.Field;
import java.util.*;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("23. PriceManagementServlet (/priceManagement)")
class PriceManagementServletTest {

    private PriceManagementServlet servlet;

    @Mock private AdminService adminService;
    @Mock private UserService userService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;

    private User adminUser;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new PriceManagementServlet();
        setField(servlet, "adminService", adminService);
        setField(servlet, "userService", userService);
        adminUser = new User();
        adminUser.setRole("admin");
    }

    private void setupAdminGet() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("admin@test.com");
        when(userService.getUserByEmail("admin@test.com")).thenReturn(adminUser);
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
        when(adminService.getAllCompanies()).thenReturn(Arrays.asList("CompA", "CompB"));
        when(adminService.getPriceForCompany("CompA")).thenReturn(10000.0);
        when(adminService.getPriceForCompany("CompB")).thenReturn(20000.0);
        when(request.getRequestDispatcher("/priceManagement.jsp")).thenReturn(dispatcher);
        servlet.doGet(request, response);
        verify(request).setAttribute(eq("companies"), any());
        verify(request).setAttribute(eq("companyPrices"), any());
        verify(dispatcher).forward(request, response);
    }

    @Test @DisplayName("#4 GET - ServiceException")
    void testDoGet_serviceException() throws Exception {
        setupAdminGet();
        when(adminService.getAllCompanies()).thenThrow(new ServiceException("DB error"));
        servlet.doGet(request, response);
        verify(session).setAttribute(eq("message"), contains("Error"));
        verify(response).sendRedirect("welcome.jsp");
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

    @Test @DisplayName("#7 POST - SQL injection in action")
    void testDoPost_sqlInjection() throws Exception {
        setupAdminGet();
        when(request.getParameter("action")).thenReturn("'; DROP TABLE--");
        when(request.getSession()).thenReturn(session);
        servlet.doPost(request, response);
        verify(session).setAttribute("message", "Invalid request.");
        verify(response).sendRedirect("priceManagement");
    }

    @Test @DisplayName("#8 POST - Update prices success")
    void testDoPost_updateSuccess() throws Exception {
        setupAdminGet();
        when(request.getParameter("action")).thenReturn("updatePrices");
        when(adminService.getAllCompanies()).thenReturn(Arrays.asList("CompA"));
        when(request.getParameter("price_CompA")).thenReturn("15000");
        when(adminService.processPriceUpdates(any())).thenReturn(true);
        when(request.getSession()).thenReturn(session);
        servlet.doPost(request, response);
        verify(session).setAttribute("message", "Prices updated successfully!");
        verify(response).sendRedirect("priceManagement");
    }

    @Test @DisplayName("#9 POST - Update prices failure")
    void testDoPost_updateFailure() throws Exception {
        setupAdminGet();
        when(request.getParameter("action")).thenReturn("updatePrices");
        when(adminService.getAllCompanies()).thenReturn(Arrays.asList("CompA"));
        when(request.getParameter("price_CompA")).thenReturn("15000");
        when(adminService.processPriceUpdates(any())).thenReturn(false);
        when(request.getSession()).thenReturn(session);
        servlet.doPost(request, response);
        verify(session).setAttribute("message", "Failed to update prices.");
        verify(response).sendRedirect("priceManagement");
    }

    @Test @DisplayName("#10 POST - Update prices service error")
    void testDoPost_updateServiceError() throws Exception {
        setupAdminGet();
        when(request.getParameter("action")).thenReturn("updatePrices");
        when(adminService.getAllCompanies()).thenReturn(Arrays.asList("CompA"));
        when(request.getParameter("price_CompA")).thenReturn("15000");
        when(adminService.processPriceUpdates(any())).thenThrow(new ServiceException("Error"));
        when(request.getSession()).thenReturn(session);
        servlet.doPost(request, response);
        verify(session).setAttribute("message", "Error");
        verify(response).sendRedirect("priceManagement");
    }

    @Test @DisplayName("#11 POST - Unknown action")
    void testDoPost_unknownAction() throws Exception {
        setupAdminGet();
        when(request.getParameter("action")).thenReturn("unknown");
        servlet.doPost(request, response);
        verify(response).sendRedirect("priceManagement");
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
