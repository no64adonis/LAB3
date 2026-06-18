package com.lottery;

import com.lottery.model.User;
import com.lottery.service.AuthService;
import com.lottery.service.EmailService;
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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Field;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("6. PasswordResetServlet (/passwordReset)")
class PasswordResetServletTest {

    private PasswordResetServlet servlet;

    @Mock private AuthService authService;
    @Mock private EmailService emailService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new PasswordResetServlet();
        setField(servlet, "authService", authService);
        setField(servlet, "emailService", emailService);
    }

    @Test
    @DisplayName("#1 GET - SQL injection in token")
    void testDoGet_sqlInjectionToken() throws Exception {
        when(request.getParameter("token")).thenReturn("'; DROP TABLE--");
        when(request.getRequestDispatcher("/forgotPassword.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute("errorMessage", "Invalid request.");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("#2 GET - Valid token")
    void testDoGet_validToken() throws Exception {
        when(request.getParameter("token")).thenReturn("validtoken123");
        when(authService.validatePasswordResetToken("validtoken123")).thenReturn(true);
        when(request.getRequestDispatcher("/userResetPassword.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute("resetToken", "validtoken123");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("#3 GET - Invalid/expired token")
    void testDoGet_invalidToken() throws Exception {
        when(request.getParameter("token")).thenReturn("expiredtoken");
        when(authService.validatePasswordResetToken("expiredtoken")).thenReturn(false);
        when(request.getRequestDispatcher("/forgotPassword.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute("errorMessage",
                "The password reset link has expired or is invalid. Please request a new one.");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("#4 GET - Service exception validating token")
    void testDoGet_serviceException() throws Exception {
        when(request.getParameter("token")).thenReturn("sometoken");
        when(authService.validatePasswordResetToken("sometoken")).thenThrow(new ServiceException("DB error"));
        when(request.getRequestDispatcher("/forgotPassword.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute("errorMessage", "Error validating password reset link: DB error");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("#5 GET - No token (show forgot password form)")
    void testDoGet_noToken() throws Exception {
        when(request.getParameter("token")).thenReturn(null);
        when(request.getRequestDispatcher("/forgotPassword.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("#6 POST - SQL injection in input")
    void testDoPost_sqlInjectionInput() throws Exception {
        when(request.getParameter("email")).thenReturn("'; DROP TABLE--");
        when(request.getParameter("token")).thenReturn(null);
        when(request.getParameter("newPassword")).thenReturn(null);
        when(request.getParameter("confirmPassword")).thenReturn(null);
        when(request.getRequestDispatcher("/forgotPassword.jsp")).thenReturn(dispatcher);

        servlet.doPost(request, response);

        verify(request).setAttribute("errorMessage", "Invalid input values.");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("#7 POST - Forgot password - invalid email format")
    void testDoPost_forgotPassword_invalidEmail() throws Exception {
        when(request.getParameter("email")).thenReturn("bad");
        when(request.getParameter("token")).thenReturn(null);
        when(request.getParameter("newPassword")).thenReturn(null);
        when(request.getParameter("confirmPassword")).thenReturn(null);
        when(request.getRequestDispatcher("/forgotPassword.jsp")).thenReturn(dispatcher);

        servlet.doPost(request, response);

        verify(request).setAttribute("errorMessage", "Invalid email format.");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("#8 POST - Forgot password - success")
    void testDoPost_forgotPassword_success() throws Exception {
        when(request.getParameter("email")).thenReturn("user@test.com");
        when(request.getParameter("token")).thenReturn(null);
        when(request.getParameter("newPassword")).thenReturn(null);
        when(request.getParameter("confirmPassword")).thenReturn(null);
        when(authService.resetUserPassword("user@test.com")).thenReturn(true);
        when(request.getRequestDispatcher("/forgotPassword.jsp")).thenReturn(dispatcher);

        servlet.doPost(request, response);

        verify(request).setAttribute(eq("successMessage"), contains("If the email address exists"));
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("#9 POST - Forgot password - user not found (same message for security)")
    void testDoPost_forgotPassword_userNotFound() throws Exception {
        when(request.getParameter("email")).thenReturn("nobody@test.com");
        when(request.getParameter("token")).thenReturn(null);
        when(request.getParameter("newPassword")).thenReturn(null);
        when(request.getParameter("confirmPassword")).thenReturn(null);
        when(authService.resetUserPassword("nobody@test.com")).thenThrow(new UserNotFoundException("Not found"));
        when(request.getRequestDispatcher("/forgotPassword.jsp")).thenReturn(dispatcher);

        servlet.doPost(request, response);

        verify(request).setAttribute(eq("successMessage"), contains("If the email address exists"));
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("#10 POST - Forgot password - service failure")
    void testDoPost_forgotPassword_serviceFails() throws Exception {
        when(request.getParameter("email")).thenReturn("user@test.com");
        when(request.getParameter("token")).thenReturn(null);
        when(request.getParameter("newPassword")).thenReturn(null);
        when(request.getParameter("confirmPassword")).thenReturn(null);
        when(authService.resetUserPassword("user@test.com")).thenReturn(false);
        when(request.getRequestDispatcher("/forgotPassword.jsp")).thenReturn(dispatcher);

        servlet.doPost(request, response);

        verify(request).setAttribute("errorMessage", "Failed to process request. Please try again later.");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("#11 POST - Reset password - new password empty")
    void testDoPost_resetPassword_emptyNewPassword() throws Exception {
        when(request.getParameter("email")).thenReturn(null);
        when(request.getParameter("token")).thenReturn("validtoken");
        when(request.getParameter("newPassword")).thenReturn("");
        when(request.getParameter("confirmPassword")).thenReturn("SomePass1!");
        when(request.getRequestDispatcher("/userResetPassword.jsp")).thenReturn(dispatcher);

        servlet.doPost(request, response);

        verify(request).setAttribute("errorMessage", "New password is required.");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("#12 POST - Reset password - confirm empty")
    void testDoPost_resetPassword_emptyConfirm() throws Exception {
        when(request.getParameter("email")).thenReturn(null);
        when(request.getParameter("token")).thenReturn("validtoken");
        when(request.getParameter("newPassword")).thenReturn("NewPass1!");
        when(request.getParameter("confirmPassword")).thenReturn("");
        when(request.getRequestDispatcher("/userResetPassword.jsp")).thenReturn(dispatcher);

        servlet.doPost(request, response);

        verify(request).setAttribute("errorMessage", "Please confirm your new password.");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("#13 POST - Reset password - weak password")
    void testDoPost_resetPassword_weakPassword() throws Exception {
        when(request.getParameter("email")).thenReturn(null);
        when(request.getParameter("token")).thenReturn("validtoken");
        when(request.getParameter("newPassword")).thenReturn("123");
        when(request.getParameter("confirmPassword")).thenReturn("123");
        when(request.getRequestDispatcher("/userResetPassword.jsp")).thenReturn(dispatcher);

        servlet.doPost(request, response);

        verify(request).setAttribute(eq("errorMessage"), anyString());
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("#14 POST - Reset password - passwords don't match")
    void testDoPost_resetPassword_dontMatch() throws Exception {
        when(request.getParameter("email")).thenReturn(null);
        when(request.getParameter("token")).thenReturn("validtoken");
        when(request.getParameter("newPassword")).thenReturn("NewPass1!");
        when(request.getParameter("confirmPassword")).thenReturn("DiffPass1!");
        when(request.getRequestDispatcher("/userResetPassword.jsp")).thenReturn(dispatcher);

        servlet.doPost(request, response);

        verify(request).setAttribute("errorMessage", "Passwords do not match.");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("#15 POST - Reset password - valid token, success")
    void testDoPost_resetPassword_success() throws Exception {
        User user = new User();
        user.setUserID(1);
        user.setEmail("user@test.com");

        when(request.getParameter("email")).thenReturn(null);
        when(request.getParameter("token")).thenReturn("validtoken");
        when(request.getParameter("newPassword")).thenReturn("NewPass1!");
        when(request.getParameter("confirmPassword")).thenReturn("NewPass1!");
        when(authService.validatePasswordResetToken("validtoken")).thenReturn(true);
        when(authService.getUserByPasswordResetToken("validtoken")).thenReturn(user);
        when(authService.setUserPassword(1, "NewPass1!")).thenReturn(true);
        when(request.getSession()).thenReturn(session);
        when(request.getRequestDispatcher("/userResetPassword.jsp")).thenReturn(dispatcher);

        servlet.doPost(request, response);

        verify(session).setAttribute("email", "user@test.com");
        verify(request).setAttribute(eq("successMessage"), contains("successfully reset"));
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("#16 POST - Reset password - setUserPassword fails")
    void testDoPost_resetPassword_setPasswordFails() throws Exception {
        User user = new User();
        user.setUserID(1);
        user.setEmail("user@test.com");

        when(request.getParameter("email")).thenReturn(null);
        when(request.getParameter("token")).thenReturn("validtoken");
        when(request.getParameter("newPassword")).thenReturn("NewPass1!");
        when(request.getParameter("confirmPassword")).thenReturn("NewPass1!");
        when(authService.validatePasswordResetToken("validtoken")).thenReturn(true);
        when(authService.getUserByPasswordResetToken("validtoken")).thenReturn(user);
        when(authService.setUserPassword(1, "NewPass1!")).thenReturn(false);
        when(request.getRequestDispatcher("/userResetPassword.jsp")).thenReturn(dispatcher);

        servlet.doPost(request, response);

        verify(request).setAttribute("errorMessage", "Failed to reset password. Please try again.");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("#17 POST - Reset password - token expired during reset")
    void testDoPost_resetPassword_tokenExpiredDuringReset() throws Exception {
        when(request.getParameter("email")).thenReturn(null);
        when(request.getParameter("token")).thenReturn("expiredtoken");
        when(request.getParameter("newPassword")).thenReturn("NewPass1!");
        when(request.getParameter("confirmPassword")).thenReturn("NewPass1!");
        when(authService.validatePasswordResetToken("expiredtoken")).thenReturn(false);
        when(request.getRequestDispatcher("/forgotPassword.jsp")).thenReturn(dispatcher);

        servlet.doPost(request, response);

        verify(request).setAttribute("errorMessage",
                "The password reset link has expired or is invalid. Please request a new one.");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("#18 POST - No email and no token")
    void testDoPost_noEmailNoToken() throws Exception {
        when(request.getParameter("email")).thenReturn(null);
        when(request.getParameter("token")).thenReturn(null);
        when(request.getParameter("newPassword")).thenReturn(null);
        when(request.getParameter("confirmPassword")).thenReturn(null);
        when(request.getRequestDispatcher("/forgotPassword.jsp")).thenReturn(dispatcher);

        servlet.doPost(request, response);

        verify(request).setAttribute("errorMessage", "Invalid request.");
        verify(dispatcher).forward(request, response);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
