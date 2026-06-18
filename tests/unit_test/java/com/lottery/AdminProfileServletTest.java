package com.lottery;

import com.lottery.model.User;
import com.lottery.service.AuthService;
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

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("9. AdminProfileServlet (/adminProfile)")
class AdminProfileServletTest {

    private AdminProfileServlet servlet;

    @Mock private UserService userService;
    @Mock private AuthService authService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new AdminProfileServlet();
        setField(servlet, "userService", userService);
        setField(servlet, "authService", authService);
    }

    @Test
    @DisplayName("#1 GET - Not logged in")
    void testDoGet_notLoggedIn() throws Exception {
        when(request.getSession(false)).thenReturn(null);
        servlet.doGet(request, response);
        verify(response).sendRedirect("login");
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
    @DisplayName("#3 GET - Admin user success")
    void testDoGet_adminSuccess() throws Exception {
        User admin = new User();
        admin.setRole("admin");

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("admin@test.com");
        when(userService.getUserByEmail("admin@test.com")).thenReturn(admin);
        when(request.getRequestDispatcher("/adminProfile.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute("user", admin);
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("#4 GET - Service exception")
    void testDoGet_serviceException() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("admin@test.com");
        when(userService.getUserByEmail("admin@test.com")).thenThrow(new ServiceException("DB error"));
        when(request.getRequestDispatcher("/adminProfile.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute("errorMessage", "Could not load profile.");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("#5 POST - Not logged in")
    void testDoPost_notLoggedIn() throws Exception {
        when(request.getSession(false)).thenReturn(null);
        servlet.doPost(request, response);
        verify(response).sendRedirect("login");
    }

    @Test
    @DisplayName("#6 POST - Non-admin user")
    void testDoPost_nonAdmin() throws Exception {
        User user = new User();
        user.setRole("user");

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("user@test.com");
        when(request.getParameter("action")).thenReturn("updateName");
        when(userService.getUserByEmail("user@test.com")).thenReturn(user);

        servlet.doPost(request, response);

        verify(response).sendRedirect("welcome.jsp");
    }

    @Test
    @DisplayName("#7 POST - updateName success")
    void testDoPost_updateName() throws Exception {
        User admin = new User();
        admin.setUserID(1);
        admin.setRole("admin");
        admin.setEmail("admin@test.com");

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("admin@test.com");
        when(request.getParameter("action")).thenReturn("updateName");
        when(request.getParameter("firstName")).thenReturn("Admin");
        when(request.getParameter("lastName")).thenReturn("New");
        when(request.getParameter("displayName")).thenReturn(null);
        when(userService.getUserByEmail("admin@test.com")).thenReturn(admin);

        servlet.doPost(request, response);

        verify(session).setAttribute("displayName", "Admin New");
        verify(session).setAttribute("successMessage", "Name updated successfully!");
        verify(response).sendRedirect("adminProfile");
    }

    @Test
    @DisplayName("#8 POST - updateName firstName too long")
    void testDoPost_updateName_tooLong() throws Exception {
        User admin = new User();
        admin.setUserID(1);
        admin.setRole("admin");

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("admin@test.com");
        when(request.getParameter("action")).thenReturn("updateName");
        when(request.getParameter("firstName")).thenReturn("A".repeat(51));
        when(request.getParameter("lastName")).thenReturn("B");
        when(request.getParameter("displayName")).thenReturn(null);
        when(userService.getUserByEmail("admin@test.com")).thenReturn(admin);

        servlet.doPost(request, response);

        verify(session).setAttribute("errorMessage", "First name is too long (max 50 characters).");
        verify(response).sendRedirect("adminProfile");
    }

    @Test
    @DisplayName("#9 POST - requestEmailChange empty")
    void testDoPost_requestEmailChange_empty() throws Exception {
        User admin = new User();
        admin.setRole("admin");

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("admin@test.com");
        when(request.getParameter("action")).thenReturn("requestEmailChange");
        when(request.getParameter("newEmail")).thenReturn(null);
        when(userService.getUserByEmail("admin@test.com")).thenReturn(admin);

        servlet.doPost(request, response);

        verify(session).setAttribute("errorMessage", "Email is required.");
        verify(response).sendRedirect("adminProfile");
    }

    @Test
    @DisplayName("#10 POST - requestEmailChange invalid format")
    void testDoPost_requestEmailChange_invalidFormat() throws Exception {
        User admin = new User();
        admin.setRole("admin");
        admin.setEmail("admin@test.com");

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("admin@test.com");
        when(request.getParameter("action")).thenReturn("requestEmailChange");
        when(request.getParameter("newEmail")).thenReturn("invalid");
        when(userService.getUserByEmail("admin@test.com")).thenReturn(admin);

        servlet.doPost(request, response);

        verify(session).setAttribute("errorMessage", "Invalid email format.");
        verify(response).sendRedirect("adminProfile");
    }

    @Test
    @DisplayName("#11 POST - requestEmailChange same email")
    void testDoPost_requestEmailChange_same() throws Exception {
        User admin = new User();
        admin.setRole("admin");
        admin.setEmail("admin@test.com");

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("admin@test.com");
        when(request.getParameter("action")).thenReturn("requestEmailChange");
        when(request.getParameter("newEmail")).thenReturn("admin@test.com");
        when(userService.getUserByEmail("admin@test.com")).thenReturn(admin);

        servlet.doPost(request, response);

        verify(session).setAttribute("errorMessage", "New email is the same as your current email.");
        verify(response).sendRedirect("adminProfile");
    }

    @Test
    @DisplayName("#12 POST - verifyEmailCode empty")
    void testDoPost_verifyEmailCode_empty() throws Exception {
        User admin = new User();
        admin.setRole("admin");

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("admin@test.com");
        when(request.getParameter("action")).thenReturn("verifyEmailCode");
        when(request.getParameter("verificationCode")).thenReturn(null);
        when(userService.getUserByEmail("admin@test.com")).thenReturn(admin);

        servlet.doPost(request, response);

        verify(session).setAttribute("errorMessage", "Verification code is required.");
        verify(response).sendRedirect("adminProfile");
    }

    @Test
    @DisplayName("#13 POST - verifyEmailCode success")
    void testDoPost_verifyEmailCode_success() throws Exception {
        User admin = new User();
        admin.setUserID(1);
        admin.setRole("admin");

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("admin@test.com");
        when(request.getParameter("action")).thenReturn("verifyEmailCode");
        when(request.getParameter("verificationCode")).thenReturn("123456");
        when(userService.getUserByEmail("admin@test.com")).thenReturn(admin);
        when(userService.verifyEmailCode(1, "123456")).thenReturn(true);
        when(session.getAttribute("pendingEmail")).thenReturn("new@test.com");

        servlet.doPost(request, response);

        verify(session).setAttribute("email", "new@test.com");
        verify(session).setAttribute("successMessage", "Email updated successfully!");
        verify(response).sendRedirect("adminProfile");
    }

    @Test
    @DisplayName("#14 POST - changePassword missing current")
    void testDoPost_changePassword_missingCurrent() throws Exception {
        User admin = new User();
        admin.setRole("admin");

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("admin@test.com");
        when(request.getParameter("action")).thenReturn("changePassword");
        when(request.getParameter("currentPassword")).thenReturn(null);
        when(userService.getUserByEmail("admin@test.com")).thenReturn(admin);

        servlet.doPost(request, response);

        verify(session).setAttribute("errorMessage", "Current password is required.");
        verify(response).sendRedirect("adminProfile");
    }

    @Test
    @DisplayName("#15 POST - changePassword don't match")
    void testDoPost_changePassword_dontMatch() throws Exception {
        User admin = new User();
        admin.setRole("admin");

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("admin@test.com");
        when(request.getParameter("action")).thenReturn("changePassword");
        when(request.getParameter("currentPassword")).thenReturn("OldPass1!");
        when(request.getParameter("newPassword")).thenReturn("NewPass1!");
        when(request.getParameter("confirmPassword")).thenReturn("DiffPass1!");
        when(userService.getUserByEmail("admin@test.com")).thenReturn(admin);

        servlet.doPost(request, response);

        verify(session).setAttribute("errorMessage", "Passwords do not match.");
        verify(response).sendRedirect("adminProfile");
    }

    @Test
    @DisplayName("#16 POST - changePassword success")
    void testDoPost_changePassword_success() throws Exception {
        User admin = new User();
        admin.setUserID(1);
        admin.setRole("admin");
        admin.setEmail("admin@test.com");

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("admin@test.com");
        when(request.getParameter("action")).thenReturn("changePassword");
        when(request.getParameter("currentPassword")).thenReturn("OldPass1!");
        when(request.getParameter("newPassword")).thenReturn("NewPass1!");
        when(request.getParameter("confirmPassword")).thenReturn("NewPass1!");
        when(userService.getUserByEmail("admin@test.com")).thenReturn(admin);
        when(authService.validateUser("admin@test.com", "OldPass1!")).thenReturn(true);

        servlet.doPost(request, response);

        verify(session).setAttribute("successMessage", "Password changed successfully!");
        verify(response).sendRedirect("adminProfile");
    }

    @Test
    @DisplayName("#17 POST - Unknown action")
    void testDoPost_unknownAction() throws Exception {
        User admin = new User();
        admin.setRole("admin");

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("admin@test.com");
        when(request.getParameter("action")).thenReturn("unknown");
        when(userService.getUserByEmail("admin@test.com")).thenReturn(admin);

        servlet.doPost(request, response);

        verify(response).sendRedirect("adminProfile");
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
