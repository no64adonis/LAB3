package com.lottery;

import com.lottery.model.User;
import com.lottery.service.AdminService;
import com.lottery.service.UserService;
import com.lottery.service.exception.ServiceException;
import com.lottery.service.exception.UserNotFoundException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.Collections;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("16. ExportUsersServlet (/exportUsers)")
class ExportUsersServletTest {

    private ExportUsersServlet servlet;

    @Mock private UserService userService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new ExportUsersServlet();
        setField(servlet, "userService", userService);
    }

    private void setupAdminGet() throws Exception {
        User admin = new User(); admin.setRole("admin");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("admin@test.com");
        when(userService.getUserByEmail("admin@test.com")).thenReturn(admin);
    }

    private void setupDefaultParams() {
        when(request.getParameter("action")).thenReturn(null);
        when(request.getParameter("search")).thenReturn(null);
        when(request.getParameter("lastLoginFrom")).thenReturn(null);
        when(request.getParameter("lastLoginTo")).thenReturn(null);
        when(request.getParameter("role")).thenReturn(null);
        when(request.getParameterValues("searchFields")).thenReturn(null);
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

    @Test @DisplayName("#3 GET - Export all users")
    void testDoGet_exportAll() throws Exception {
        setupAdminGet(); setupDefaultParams();
        when(userService.getAllUsers(0, 50000)).thenReturn(Collections.emptyList());
        when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
        servlet.doGet(request, response);
        verify(response).setContentType("application/vnd.ms-excel");
        verify(response).setHeader("Content-Disposition", "attachment; filename=users.xls");
    }

    @Test @DisplayName("#4 GET - Export with search filter")
    void testDoGet_exportWithSearch() throws Exception {
        setupAdminGet(); setupDefaultParams();
        when(request.getParameter("search")).thenReturn("john");
        when(userService.searchUsers(eq("john"), any(), any(), any(), any(), eq(0), eq(50000)))
                .thenReturn(Collections.emptyList());
        when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
        servlet.doGet(request, response);
        verify(response).setContentType("application/vnd.ms-excel");
    }

    @Test @DisplayName("#5 GET - Export inactive users")
    void testDoGet_exportInactive() throws Exception {
        setupAdminGet(); setupDefaultParams();
        when(request.getParameter("action")).thenReturn("inactive");
        when(request.getParameter("period")).thenReturn("30 days");
        
        when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
        servlet.doGet(request, response);
        verify(response).setContentType("application/vnd.ms-excel");
    }

    @Test @DisplayName("#6 GET - User not found")
    void testDoGet_userNotFound() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("admin@test.com");
        when(userService.getUserByEmail("admin@test.com")).thenThrow(new UserNotFoundException("Not found"));
        servlet.doGet(request, response);
        verify(response).sendRedirect("login.jsp");
    }

    @Test @DisplayName("#7 GET - Service exception")
    void testDoGet_serviceException() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("admin@test.com");
        when(userService.getUserByEmail("admin@test.com")).thenThrow(new ServiceException("DB error"));
        servlet.doGet(request, response);
        verify(session).setAttribute("error", "Error exporting users: DB error");
        verify(response).sendRedirect("userManagement");
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
