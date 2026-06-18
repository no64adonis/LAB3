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
@DisplayName("8. ProfileServlet (/profile)")
class ProfileServletTest {

    private ProfileServlet servlet;

    @Mock private UserService userService;
    @Mock private AuthService authService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new ProfileServlet();
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
    @DisplayName("#2 GET - Successful load")
    void testDoGet_successfulLoad() throws Exception {
        User user = new User();
        user.setEmail("user@test.com");

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("user@test.com");
        when(userService.getUserByEmail("user@test.com")).thenReturn(user);
        when(request.getRequestDispatcher("/profile.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute("user", user);
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("#3 GET - Error loading user")
    void testDoGet_errorLoadingUser() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("user@test.com");
        when(userService.getUserByEmail("user@test.com")).thenThrow(new ServiceException("DB error"));
        when(request.getRequestDispatcher("/profile.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute("errorMessage", "Could not load profile.");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("#4 POST - Not logged in")
    void testDoPost_notLoggedIn() throws Exception {
        when(request.getSession(false)).thenReturn(null);
        servlet.doPost(request, response);
        verify(response).sendRedirect("login");
    }

    @Test
    @DisplayName("#5 POST - User not found")
    void testDoPost_userNotFound() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("user@test.com");
        when(request.getParameter("action")).thenReturn("updateName");
        when(userService.getUserByEmail("user@test.com")).thenReturn(null);

        servlet.doPost(request, response);

        verify(response).sendRedirect("login");
    }

    @Test
    @DisplayName("#6 POST - updateName with firstName/lastName")
    void testDoPost_updateName() throws Exception {
        User user = new User();
        user.setUserID(1);
        user.setEmail("user@test.com");

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("user@test.com");
        when(request.getParameter("action")).thenReturn("updateName");
        when(request.getParameter("firstName")).thenReturn("John");
        when(request.getParameter("lastName")).thenReturn("Doe");
        when(request.getParameter("displayName")).thenReturn(null);
        when(userService.getUserByEmail("user@test.com")).thenReturn(user);

        servlet.doPost(request, response);

        verify(session).setAttribute("displayName", "John Doe");
        verify(session).setAttribute("successMessage", "Name updated successfully!");
        verify(response).sendRedirect("profile");
    }

    @Test
    @DisplayName("#7 POST - updateName with displayName fallback")
    void testDoPost_updateNameDisplayNameFallback() throws Exception {
        User user = new User();
        user.setUserID(1);
        user.setEmail("user@test.com");

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("user@test.com");
        when(request.getParameter("action")).thenReturn("updateName");
        when(request.getParameter("firstName")).thenReturn(null);
        when(request.getParameter("lastName")).thenReturn(null);
        when(request.getParameter("displayName")).thenReturn("John Doe");
        when(userService.getUserByEmail("user@test.com")).thenReturn(user);

        servlet.doPost(request, response);

        verify(session).setAttribute("successMessage", "Name updated successfully!");
        verify(response).sendRedirect("profile");
    }

    @Test
    @DisplayName("#8 POST - updateName firstName too long")
    void testDoPost_updateName_firstNameTooLong() throws Exception {
        User user = new User();
        user.setUserID(1);
        user.setEmail("user@test.com");

        String longName = "A".repeat(51);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("user@test.com");
        when(request.getParameter("action")).thenReturn("updateName");
        when(request.getParameter("firstName")).thenReturn(longName);
        when(request.getParameter("lastName")).thenReturn("Doe");
        when(request.getParameter("displayName")).thenReturn(null);
        when(userService.getUserByEmail("user@test.com")).thenReturn(user);

        servlet.doPost(request, response);

        verify(session).setAttribute("errorMessage", "First name is too long (max 50 characters).");
        verify(response).sendRedirect("profile");
    }

    @Test
    @DisplayName("#9 POST - updateName lastName too long")
    void testDoPost_updateName_lastNameTooLong() throws Exception {
        User user = new User();
        user.setUserID(1);
        user.setEmail("user@test.com");

        String longName = "B".repeat(51);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("user@test.com");
        when(request.getParameter("action")).thenReturn("updateName");
        when(request.getParameter("firstName")).thenReturn("John");
        when(request.getParameter("lastName")).thenReturn(longName);
        when(request.getParameter("displayName")).thenReturn(null);
        when(userService.getUserByEmail("user@test.com")).thenReturn(user);

        servlet.doPost(request, response);

        verify(session).setAttribute("errorMessage", "Last name is too long (max 50 characters).");
        verify(response).sendRedirect("profile");
    }

    @Test
    @DisplayName("#11 POST - requestEmailChange empty email")
    void testDoPost_requestEmailChange_empty() throws Exception {
        User user = new User();
        user.setUserID(1);
        user.setEmail("user@test.com");

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("user@test.com");
        when(request.getParameter("action")).thenReturn("requestEmailChange");
        when(request.getParameter("newEmail")).thenReturn(null);
        when(userService.getUserByEmail("user@test.com")).thenReturn(user);

        servlet.doPost(request, response);

        verify(session).setAttribute("errorMessage", "Email is required.");
        verify(response).sendRedirect("profile");
    }

    @Test
    @DisplayName("#12 POST - requestEmailChange invalid format")
    void testDoPost_requestEmailChange_invalidFormat() throws Exception {
        User user = new User();
        user.setUserID(1);
        user.setEmail("user@test.com");

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("user@test.com");
        when(request.getParameter("action")).thenReturn("requestEmailChange");
        when(request.getParameter("newEmail")).thenReturn("bad");
        when(userService.getUserByEmail("user@test.com")).thenReturn(user);

        servlet.doPost(request, response);

        verify(session).setAttribute("errorMessage", "Invalid email format.");
        verify(response).sendRedirect("profile");
    }

    @Test
    @DisplayName("#13 POST - requestEmailChange same email")
    void testDoPost_requestEmailChange_sameEmail() throws Exception {
        User user = new User();
        user.setUserID(1);
        user.setEmail("user@test.com");

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("user@test.com");
        when(request.getParameter("action")).thenReturn("requestEmailChange");
        when(request.getParameter("newEmail")).thenReturn("user@test.com");
        when(userService.getUserByEmail("user@test.com")).thenReturn(user);

        servlet.doPost(request, response);

        verify(session).setAttribute("errorMessage", "New email is the same as your current email.");
        verify(response).sendRedirect("profile");
    }

    @Test
    @DisplayName("#14 POST - requestEmailChange success")
    void testDoPost_requestEmailChange_success() throws Exception {
        User user = new User();
        user.setUserID(1);
        user.setEmail("user@test.com");

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("user@test.com");
        when(request.getParameter("action")).thenReturn("requestEmailChange");
        when(request.getParameter("newEmail")).thenReturn("new@test.com");
        when(userService.getUserByEmail("user@test.com")).thenReturn(user);

        servlet.doPost(request, response);

        verify(session).setAttribute(eq("successMessage"), contains("Verification code sent"));
        verify(response).sendRedirect("profile");
    }

    @Test
    @DisplayName("#15 POST - verifyEmailCode empty code")
    void testDoPost_verifyEmailCode_empty() throws Exception {
        User user = new User();
        user.setUserID(1);
        user.setEmail("user@test.com");

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("user@test.com");
        when(request.getParameter("action")).thenReturn("verifyEmailCode");
        when(request.getParameter("verificationCode")).thenReturn(null);
        when(userService.getUserByEmail("user@test.com")).thenReturn(user);

        servlet.doPost(request, response);

        verify(session).setAttribute("errorMessage", "Verification code is required.");
        verify(response).sendRedirect("profile");
    }

    @Test
    @DisplayName("#16 POST - verifyEmailCode success")
    void testDoPost_verifyEmailCode_success() throws Exception {
        User user = new User();
        user.setUserID(1);
        user.setEmail("user@test.com");

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("user@test.com");
        when(request.getParameter("action")).thenReturn("verifyEmailCode");
        when(request.getParameter("verificationCode")).thenReturn("123456");
        when(userService.getUserByEmail("user@test.com")).thenReturn(user);
        when(userService.verifyEmailCode(1, "123456")).thenReturn(true);
        when(session.getAttribute("pendingEmail")).thenReturn("new@test.com");

        servlet.doPost(request, response);

        verify(session).setAttribute("email", "new@test.com");
        verify(session).setAttribute("successMessage", "Email updated successfully!");
        verify(response).sendRedirect("profile");
    }

    @Test
    @DisplayName("#17 POST - verifyEmailCode invalid code")
    void testDoPost_verifyEmailCode_invalidCode() throws Exception {
        User user = new User();
        user.setUserID(1);
        user.setEmail("user@test.com");

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("user@test.com");
        when(request.getParameter("action")).thenReturn("verifyEmailCode");
        when(request.getParameter("verificationCode")).thenReturn("000000");
        when(userService.getUserByEmail("user@test.com")).thenReturn(user);
        when(userService.verifyEmailCode(1, "000000")).thenReturn(false);

        servlet.doPost(request, response);

        verify(session).setAttribute("errorMessage", "Invalid or expired verification code.");
        verify(response).sendRedirect("profile");
    }

    @Test
    @DisplayName("#18 POST - changePassword missing current password")
    void testDoPost_changePassword_missingCurrent() throws Exception {
        User user = new User();
        user.setUserID(1);
        user.setEmail("user@test.com");

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("user@test.com");
        when(request.getParameter("action")).thenReturn("changePassword");
        when(request.getParameter("currentPassword")).thenReturn(null);
        when(userService.getUserByEmail("user@test.com")).thenReturn(user);

        servlet.doPost(request, response);

        verify(session).setAttribute("errorMessage", "Current password is required.");
        verify(response).sendRedirect("profile");
    }

    @Test
    @DisplayName("#19 POST - changePassword missing new password")
    void testDoPost_changePassword_missingNew() throws Exception {
        User user = new User();
        user.setUserID(1);
        user.setEmail("user@test.com");

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("user@test.com");
        when(request.getParameter("action")).thenReturn("changePassword");
        when(request.getParameter("currentPassword")).thenReturn("OldPass1!");
        when(request.getParameter("newPassword")).thenReturn(null);
        when(userService.getUserByEmail("user@test.com")).thenReturn(user);

        servlet.doPost(request, response);

        verify(session).setAttribute("errorMessage", "New password is required.");
        verify(response).sendRedirect("profile");
    }

    @Test
    @DisplayName("#21 POST - changePassword passwords don't match")
    void testDoPost_changePassword_dontMatch() throws Exception {
        User user = new User();
        user.setUserID(1);
        user.setEmail("user@test.com");

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("user@test.com");
        when(request.getParameter("action")).thenReturn("changePassword");
        when(request.getParameter("currentPassword")).thenReturn("OldPass1!");
        when(request.getParameter("newPassword")).thenReturn("NewPass1!");
        when(request.getParameter("confirmPassword")).thenReturn("DiffPass1!");
        when(userService.getUserByEmail("user@test.com")).thenReturn(user);

        servlet.doPost(request, response);

        verify(session).setAttribute("errorMessage", "Passwords do not match.");
        verify(response).sendRedirect("profile");
    }

    @Test
    @DisplayName("#23 POST - changePassword success")
    void testDoPost_changePassword_success() throws Exception {
        User user = new User();
        user.setUserID(1);
        user.setEmail("user@test.com");

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("user@test.com");
        when(request.getParameter("action")).thenReturn("changePassword");
        when(request.getParameter("currentPassword")).thenReturn("OldPass1!");
        when(request.getParameter("newPassword")).thenReturn("NewPass1!");
        when(request.getParameter("confirmPassword")).thenReturn("NewPass1!");
        when(userService.getUserByEmail("user@test.com")).thenReturn(user);
        when(authService.validateUser("user@test.com", "OldPass1!")).thenReturn(true);

        servlet.doPost(request, response);

        verify(session).setAttribute("successMessage", "Password changed successfully!");
        verify(response).sendRedirect("profile");
    }

    @Test
    @DisplayName("#24 POST - Unknown action")
    void testDoPost_unknownAction() throws Exception {
        User user = new User();
        user.setUserID(1);
        user.setEmail("user@test.com");

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("user@test.com");
        when(request.getParameter("action")).thenReturn("invalid");
        when(userService.getUserByEmail("user@test.com")).thenReturn(user);

        servlet.doPost(request, response);

        verify(response).sendRedirect("profile");
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
