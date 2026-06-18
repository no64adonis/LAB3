package com.lottery;

import com.lottery.model.User;
import com.lottery.service.AuthService;
import com.lottery.service.UserService;
import com.lottery.service.exception.InvalidCredentialsException;
import com.lottery.service.exception.ServiceException;
import com.lottery.service.exception.UserNotFoundException;
import com.lottery.util.SessionManager;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("1. LoginServlet (/login)")
class LoginServletTest {

    private LoginServlet servlet;

    @Mock private AuthService authService;
    @Mock private UserService userService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new LoginServlet();
        setField(servlet, "authService", authService);
        setField(servlet, "userService", userService);
    }

    @Test
    @DisplayName("#1 GET - Show login page (no params)")
    void testDoGet_showLoginPage() throws Exception {
        when(request.getParameter("registrationSuccess")).thenReturn(null);
        when(request.getRequestDispatcher("/login.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(request, never()).setAttribute(eq("successMessage"), any());
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("#2 GET - Show login with registration success message")
    void testDoGet_registrationSuccess() throws Exception {
        when(request.getParameter("registrationSuccess")).thenReturn("true");
        when(request.getRequestDispatcher("/login.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute("successMessage", "Registration successful! You can now login.");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("#3 POST - Missing email and password")
    void testDoPost_missingEmailAndPassword() throws Exception {
        when(request.getParameter("email")).thenReturn(null);
        when(request.getParameter("password")).thenReturn(null);
        when(request.getRequestDispatcher("/login.jsp")).thenReturn(dispatcher);

        servlet.doPost(request, response);

        verify(request).setAttribute("errorMessage", "Email and password are required");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("#4 POST - Empty email")
    void testDoPost_emptyEmail() throws Exception {
        when(request.getParameter("email")).thenReturn("");
        when(request.getParameter("password")).thenReturn("pass");
        when(request.getRequestDispatcher("/login.jsp")).thenReturn(dispatcher);

        servlet.doPost(request, response);

        verify(request).setAttribute("errorMessage", "Email and password are required");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("#5 POST - Empty password")
    void testDoPost_emptyPassword() throws Exception {
        when(request.getParameter("email")).thenReturn("a@b.com");
        when(request.getParameter("password")).thenReturn("");
        when(request.getRequestDispatcher("/login.jsp")).thenReturn(dispatcher);

        servlet.doPost(request, response);

        verify(request).setAttribute("errorMessage", "Email and password are required");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("#6 POST - Valid admin login")
    void testDoPost_validAdminLogin() throws Exception {
        String email = "admin@test.com";
        when(request.getParameter("email")).thenReturn(email);
        when(request.getParameter("password")).thenReturn("ValidPass1!");

        User adminUser = new User();
        adminUser.setRole("admin");
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");

        try (MockedStatic<SessionManager> sm = mockStatic(SessionManager.class)) {
            sm.when(() -> SessionManager.createSecureSession(any(), any(), eq(email))).thenReturn(session);
            when(authService.validateUser(email, "ValidPass1!")).thenReturn(true);
            when(userService.getUserByEmail(email)).thenReturn(adminUser);

            servlet.doPost(request, response);

            verify(response).sendRedirect("userManagement");
            verify(session).setAttribute("displayName", "Admin User");
        }
    }

    @Test
    @DisplayName("#7 POST - Valid user login")
    void testDoPost_validUserLogin() throws Exception {
        String email = "user@test.com";
        when(request.getParameter("email")).thenReturn(email);
        when(request.getParameter("password")).thenReturn("ValidPass1!");

        User normalUser = new User();
        normalUser.setRole("user");
        normalUser.setFirstName("John");
        normalUser.setLastName("Doe");

        try (MockedStatic<SessionManager> sm = mockStatic(SessionManager.class)) {
            sm.when(() -> SessionManager.createSecureSession(any(), any(), eq(email))).thenReturn(session);
            when(authService.validateUser(email, "ValidPass1!")).thenReturn(true);
            when(userService.getUserByEmail(email)).thenReturn(normalUser);

            servlet.doPost(request, response);

            verify(response).sendRedirect("userLottery");
            verify(session).setAttribute("displayName", "John Doe");
        }
    }

    @Test
    @DisplayName("#8 POST - Display name from firstName+lastName")
    void testDoPost_displayNameFromNames() throws Exception {
        String email = "user@test.com";
        when(request.getParameter("email")).thenReturn(email);
        when(request.getParameter("password")).thenReturn("ValidPass1!");

        User user = new User();
        user.setRole("user");
        user.setFirstName("First");
        user.setLastName("Last");

        try (MockedStatic<SessionManager> sm = mockStatic(SessionManager.class)) {
            sm.when(() -> SessionManager.createSecureSession(any(), any(), eq(email))).thenReturn(session);
            when(authService.validateUser(email, "ValidPass1!")).thenReturn(true);
            when(userService.getUserByEmail(email)).thenReturn(user);

            servlet.doPost(request, response);

            verify(session).setAttribute("displayName", "First Last");
        }
    }

    @Test
    @DisplayName("#9 POST - Display name fallback to email")
    void testDoPost_displayNameFallbackToEmail() throws Exception {
        String email = "user@test.com";
        when(request.getParameter("email")).thenReturn(email);
        when(request.getParameter("password")).thenReturn("ValidPass1!");

        User user = new User();
        user.setRole("user");
        user.setFirstName(null);
        user.setLastName(null);
        user.setEmail(email);

        try (MockedStatic<SessionManager> sm = mockStatic(SessionManager.class)) {
            sm.when(() -> SessionManager.createSecureSession(any(), any(), eq(email))).thenReturn(session);
            when(authService.validateUser(email, "ValidPass1!")).thenReturn(true);
            when(userService.getUserByEmail(email)).thenReturn(user);

            servlet.doPost(request, response);

            verify(session).setAttribute("displayName", email);
        }
    }

    @Test
    @DisplayName("#10 POST - Invalid credentials")
    void testDoPost_invalidCredentials() throws Exception {
        when(request.getParameter("email")).thenReturn("user@test.com");
        when(request.getParameter("password")).thenReturn("wrongpass");
        when(request.getRequestDispatcher("/login.jsp")).thenReturn(dispatcher);
        when(authService.validateUser(anyString(), anyString())).thenThrow(new InvalidCredentialsException("Bad creds"));

        servlet.doPost(request, response);

        verify(request).setAttribute("errorMessage", "Invalid email or password");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("#11 POST - User not found")
    void testDoPost_userNotFound() throws Exception {
        when(request.getParameter("email")).thenReturn("noone@test.com");
        when(request.getParameter("password")).thenReturn("somepass");
        when(request.getRequestDispatcher("/login.jsp")).thenReturn(dispatcher);
        when(authService.validateUser(anyString(), anyString())).thenThrow(new UserNotFoundException("Not found"));

        servlet.doPost(request, response);

        verify(request).setAttribute("errorMessage", "User not found");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("#12 POST - Service exception")
    void testDoPost_serviceException() throws Exception {
        when(request.getParameter("email")).thenReturn("user@test.com");
        when(request.getParameter("password")).thenReturn("somepass");
        when(request.getRequestDispatcher("/login.jsp")).thenReturn(dispatcher);
        when(authService.validateUser(anyString(), anyString())).thenThrow(new ServiceException("DB error"));

        servlet.doPost(request, response);

        verify(request).setAttribute("errorMessage", "Login failed: DB error");
        verify(dispatcher).forward(request, response);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
