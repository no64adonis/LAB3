package com.lottery;

import com.lottery.model.User;
import com.lottery.service.AuthService;
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

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("10. SetPasswordServlet (/setPassword)")
class SetPasswordServletTest {

    private SetPasswordServlet servlet;

    @Mock private AuthService authService;
    @Mock private UserService userService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new SetPasswordServlet();
        setField(servlet, "authService", authService);
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
    @DisplayName("#2 GET - User doesn't need password setup (user)")
    void testDoGet_noPasswordSetup_user() throws Exception {
        User user = new User();
        user.setRole("user");

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("user@test.com");
        when(session.getAttribute("needsPasswordSetup")).thenReturn(null);
        when(userService.getUserByEmail("user@test.com")).thenReturn(user);

        servlet.doGet(request, response);

        verify(response).sendRedirect("userLottery");
    }

    @Test
    @DisplayName("#3 GET - User doesn't need password setup (admin)")
    void testDoGet_noPasswordSetup_admin() throws Exception {
        User admin = new User();
        admin.setRole("admin");

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("admin@test.com");
        when(session.getAttribute("needsPasswordSetup")).thenReturn(null);
        when(userService.getUserByEmail("admin@test.com")).thenReturn(admin);

        servlet.doGet(request, response);

        verify(response).sendRedirect("userManagement");
    }

    @Test
    @DisplayName("#4 GET - OAuth user needs password - show form")
    void testDoGet_needsPassword() throws Exception {
        User user = new User();
        user.setRole("user");

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("user@test.com");
        when(session.getAttribute("needsPasswordSetup")).thenReturn(Boolean.TRUE);
        when(userService.getUserByEmail("user@test.com")).thenReturn(user);
        when(request.getRequestDispatcher("/setPassword.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("#5 GET - User not found")
    void testDoGet_userNotFound() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("user@test.com");
        when(userService.getUserByEmail("user@test.com")).thenThrow(new UserNotFoundException("Not found"));

        servlet.doGet(request, response);

        verify(response).sendRedirect("login.jsp");
    }

    @Test
    @DisplayName("#6 POST - Not logged in")
    void testDoPost_notLoggedIn() throws Exception {
        when(request.getSession(false)).thenReturn(null);
        servlet.doPost(request, response);
        verify(response).sendRedirect("login.jsp");
    }

    @Test
    @DisplayName("#7 POST - SQL injection in password")
    void testDoPost_sqlInjection() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("user@test.com");
        when(session.getAttribute("needsPasswordSetup")).thenReturn(Boolean.TRUE);
        when(request.getParameter("newPassword")).thenReturn("'; DROP TABLE--");
        when(request.getParameter("confirmPassword")).thenReturn("'; DROP TABLE--");
        when(request.getSession()).thenReturn(session);

        servlet.doPost(request, response);

        verify(session).setAttribute("errorMessage", "Invalid input values.");
        verify(response).sendRedirect("setPassword");
    }

    @Test
    @DisplayName("#8 POST - Empty fields")
    void testDoPost_emptyFields() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("user@test.com");
        when(session.getAttribute("needsPasswordSetup")).thenReturn(Boolean.TRUE);
        when(request.getParameter("newPassword")).thenReturn(null);
        when(request.getParameter("confirmPassword")).thenReturn(null);
        when(request.getSession()).thenReturn(session);

        servlet.doPost(request, response);

        verify(session).setAttribute("errorMessage", "All fields are required.");
        verify(response).sendRedirect("setPassword");
    }

    @Test
    @DisplayName("#9 POST - Weak password")
    void testDoPost_weakPassword() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("user@test.com");
        when(session.getAttribute("needsPasswordSetup")).thenReturn(Boolean.TRUE);
        when(request.getParameter("newPassword")).thenReturn("123");
        when(request.getParameter("confirmPassword")).thenReturn("123");
        when(request.getSession()).thenReturn(session);

        servlet.doPost(request, response);

        verify(session).setAttribute(eq("errorMessage"), anyString());
        verify(response).sendRedirect("setPassword");
    }

    @Test
    @DisplayName("#10 POST - Passwords don't match")
    void testDoPost_dontMatch() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("user@test.com");
        when(session.getAttribute("needsPasswordSetup")).thenReturn(Boolean.TRUE);
        when(request.getParameter("newPassword")).thenReturn("StrongPass1!");
        when(request.getParameter("confirmPassword")).thenReturn("DiffPass1!");
        when(request.getSession()).thenReturn(session);

        servlet.doPost(request, response);

        verify(session).setAttribute("errorMessage", "New password and confirm password do not match.");
        verify(response).sendRedirect("setPassword");
    }

    @Test
    @DisplayName("#11 POST - User not found during save")
    void testDoPost_userNotFound() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("user@test.com");
        when(session.getAttribute("needsPasswordSetup")).thenReturn(Boolean.TRUE);
        when(request.getParameter("newPassword")).thenReturn("StrongPass1!");
        when(request.getParameter("confirmPassword")).thenReturn("StrongPass1!");
        when(userService.getUserByEmail("user@test.com")).thenReturn(null);
        when(request.getSession()).thenReturn(session);

        servlet.doPost(request, response);

        verify(session).setAttribute("errorMessage", "User not found.");
        verify(response).sendRedirect("setPassword");
    }

    @Test
    @DisplayName("#12 POST - Set password failure")
    void testDoPost_setPasswordFailure() throws Exception {
        User user = new User();
        user.setUserID(1);
        user.setRole("user");

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("user@test.com");
        when(session.getAttribute("needsPasswordSetup")).thenReturn(Boolean.TRUE);
        when(request.getParameter("newPassword")).thenReturn("StrongPass1!");
        when(request.getParameter("confirmPassword")).thenReturn("StrongPass1!");
        when(userService.getUserByEmail("user@test.com")).thenReturn(user);
        when(authService.setUserPassword(1, "StrongPass1!")).thenReturn(false);
        when(request.getSession()).thenReturn(session);

        servlet.doPost(request, response);

        verify(session).setAttribute("errorMessage", "Failed to set password. Please try again.");
        verify(response).sendRedirect("setPassword");
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
