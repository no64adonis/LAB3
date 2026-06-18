package com.lottery;

import com.lottery.model.User;
import com.lottery.service.AuthService;
import com.lottery.service.UserService;
import com.lottery.service.exception.InvalidCredentialsException;
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
@DisplayName("5. ChangePasswordServlet (/changePassword)")
class ChangePasswordServletTest {

    private ChangePasswordServlet servlet;

    @Mock private AuthService authService;
    @Mock private UserService userService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new ChangePasswordServlet();
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
    @DisplayName("#2 GET - SQL injection in userId")
    void testDoGet_sqlInjectionUserId() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("admin@test.com");
        when(request.getHeader("Referer")).thenReturn(null);
        when(request.getParameter("action")).thenReturn("reset");
        when(request.getParameter("userId")).thenReturn("'; DROP TABLE--");
        when(request.getSession()).thenReturn(session);

        servlet.doGet(request, response);

        verify(session).setAttribute("errorMessage", "Invalid request.");
        verify(response).sendRedirect("welcome.jsp");
    }

    @Test
    @DisplayName("#3 GET - Admin reset action - non-admin user")
    void testDoGet_adminResetNotAdmin() throws Exception {
        User normalUser = new User();
        normalUser.setRole("user");

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("user@test.com");
        when(request.getHeader("Referer")).thenReturn(null);
        when(request.getParameter("action")).thenReturn("reset");
        when(request.getParameter("userId")).thenReturn("5");
        when(userService.getUserByEmail("user@test.com")).thenReturn(normalUser);
        when(request.getSession()).thenReturn(session);

        servlet.doGet(request, response);

        verify(session).setAttribute("errorMessage", "Access denied. Admin privileges required.");
        verify(response).sendRedirect("welcome.jsp");
    }

    @Test
    @DisplayName("#4 GET - Admin reset action - valid admin")
    void testDoGet_adminResetValid() throws Exception {
        User adminUser = new User();
        adminUser.setRole("admin");

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("admin@test.com");
        when(request.getHeader("Referer")).thenReturn(null);
        when(request.getParameter("action")).thenReturn("reset");
        when(request.getParameter("userId")).thenReturn("5");
        when(userService.getUserByEmail("admin@test.com")).thenReturn(adminUser);
        when(request.getRequestDispatcher("/resetPassword.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute("resetUserId", "5");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("#5 GET - Admin reset - user not found")
    void testDoGet_adminResetUserNotFound() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("admin@test.com");
        when(request.getHeader("Referer")).thenReturn(null);
        when(request.getParameter("action")).thenReturn("reset");
        when(request.getParameter("userId")).thenReturn("5");
        when(userService.getUserByEmail("admin@test.com")).thenThrow(new UserNotFoundException("Not found"));
        when(request.getSession()).thenReturn(session);

        servlet.doGet(request, response);

        verify(session).setAttribute("errorMessage", "User not found.");
        verify(response).sendRedirect("welcome.jsp");
    }

    @Test
    @DisplayName("#6 GET - Regular user - redirect to profile")
    void testDoGet_regularUserRedirectProfile() throws Exception {
        User normalUser = new User();
        normalUser.setRole("user");

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("user@test.com");
        when(request.getHeader("Referer")).thenReturn(null);
        when(request.getParameter("action")).thenReturn(null);
        when(request.getParameter("userId")).thenReturn(null);
        when(userService.getUserByEmail("user@test.com")).thenReturn(normalUser);

        servlet.doGet(request, response);

        verify(response).sendRedirect("profile");
    }

    @Test
    @DisplayName("#7 GET - Admin - show change password page")
    void testDoGet_adminShowChangePassword() throws Exception {
        User adminUser = new User();
        adminUser.setRole("admin");

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("admin@test.com");
        when(request.getHeader("Referer")).thenReturn(null);
        when(request.getParameter("action")).thenReturn(null);
        when(request.getParameter("userId")).thenReturn(null);
        when(userService.getUserByEmail("admin@test.com")).thenReturn(adminUser);
        when(request.getRequestDispatcher("/changePassword.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("#8 POST - Not logged in")
    void testDoPost_notLoggedIn() throws Exception {
        when(request.getSession(false)).thenReturn(null);
        servlet.doPost(request, response);
        verify(response).sendRedirect("login.jsp");
    }

    @Test
    @DisplayName("#9 POST - SQL injection in action")
    void testDoPost_sqlInjectionAction() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("user@test.com");
        when(request.getParameter("action")).thenReturn("'; DROP TABLE--");
        when(request.getSession()).thenReturn(session);

        servlet.doPost(request, response);

        verify(session).setAttribute("errorMessage", "Invalid request.");
        verify(response).sendRedirect("changePassword");
    }

    @Test
    @DisplayName("#11 POST - changeOwnPassword - empty fields")
    void testDoPost_changeOwnPassword_emptyFields() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("user@test.com");
        when(request.getParameter("action")).thenReturn("changeOwnPassword");
        when(request.getParameter("currentPassword")).thenReturn(null);
        when(request.getParameter("newPassword")).thenReturn(null);
        when(request.getParameter("confirmPassword")).thenReturn(null);
        when(request.getSession()).thenReturn(session);

        servlet.doPost(request, response);

        verify(session).setAttribute("errorMessage", "All fields are required.");
        verify(response).sendRedirect("changePassword");
    }

    @Test
    @DisplayName("#12 POST - changeOwnPassword - weak password")
    void testDoPost_changeOwnPassword_weakPassword() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("user@test.com");
        when(request.getParameter("action")).thenReturn("changeOwnPassword");
        when(request.getParameter("currentPassword")).thenReturn("OldPass1!");
        when(request.getParameter("newPassword")).thenReturn("123");
        when(request.getParameter("confirmPassword")).thenReturn("123");
        when(request.getSession()).thenReturn(session);

        servlet.doPost(request, response);

        verify(session).setAttribute(eq("errorMessage"), anyString());
        verify(response).sendRedirect("changePassword");
    }

    @Test
    @DisplayName("#13 POST - changeOwnPassword - passwords don't match")
    void testDoPost_changeOwnPassword_passwordsDontMatch() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("user@test.com");
        when(request.getParameter("action")).thenReturn("changeOwnPassword");
        when(request.getParameter("currentPassword")).thenReturn("OldPass1!");
        when(request.getParameter("newPassword")).thenReturn("NewPass1!");
        when(request.getParameter("confirmPassword")).thenReturn("DiffPass1!");
        when(request.getSession()).thenReturn(session);

        servlet.doPost(request, response);

        verify(session).setAttribute("errorMessage", "New password and confirm password do not match.");
        verify(response).sendRedirect("changePassword");
    }

    @Test
    @DisplayName("#14 POST - changeOwnPassword - same as current")
    void testDoPost_changeOwnPassword_sameAsCurrent() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("user@test.com");
        when(request.getParameter("action")).thenReturn("changeOwnPassword");
        when(request.getParameter("currentPassword")).thenReturn("SamePass1!");
        when(request.getParameter("newPassword")).thenReturn("SamePass1!");
        when(request.getParameter("confirmPassword")).thenReturn("SamePass1!");
        when(request.getSession()).thenReturn(session);

        servlet.doPost(request, response);

        verify(session).setAttribute("errorMessage", "New password must be different from current password.");
        verify(response).sendRedirect("changePassword");
    }

    @Test
    @DisplayName("#15 POST - changeOwnPassword - user not found")
    void testDoPost_changeOwnPassword_userNotFound() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("user@test.com");
        when(request.getParameter("action")).thenReturn("changeOwnPassword");
        when(request.getParameter("currentPassword")).thenReturn("OldPass1!");
        when(request.getParameter("newPassword")).thenReturn("NewPass1!");
        when(request.getParameter("confirmPassword")).thenReturn("NewPass1!");
        when(userService.getUserByEmail("user@test.com")).thenReturn(null);
        when(request.getSession()).thenReturn(session);

        servlet.doPost(request, response);

        verify(session).setAttribute("errorMessage", "User not found.");
        verify(response).sendRedirect("changePassword");
    }

    @Test
    @DisplayName("#16 POST - changeOwnPassword - success")
    void testDoPost_changeOwnPassword_success() throws Exception {
        User user = new User();
        user.setUserID(1);

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("user@test.com");
        when(request.getParameter("action")).thenReturn("changeOwnPassword");
        when(request.getParameter("currentPassword")).thenReturn("OldPass1!");
        when(request.getParameter("newPassword")).thenReturn("NewPass1!");
        when(request.getParameter("confirmPassword")).thenReturn("NewPass1!");
        when(userService.getUserByEmail("user@test.com")).thenReturn(user);
        when(authService.changeUserPassword(1, "OldPass1!", "NewPass1!")).thenReturn(true);
        when(request.getSession()).thenReturn(session);

        servlet.doPost(request, response);

        verify(session).setAttribute("successMessage", "Password changed successfully.");
        verify(response).sendRedirect("changePassword");
    }

    @Test
    @DisplayName("#17 POST - changeOwnPassword - service returns false")
    void testDoPost_changeOwnPassword_serviceFails() throws Exception {
        User user = new User();
        user.setUserID(1);

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("user@test.com");
        when(request.getParameter("action")).thenReturn("changeOwnPassword");
        when(request.getParameter("currentPassword")).thenReturn("OldPass1!");
        when(request.getParameter("newPassword")).thenReturn("NewPass1!");
        when(request.getParameter("confirmPassword")).thenReturn("NewPass1!");
        when(userService.getUserByEmail("user@test.com")).thenReturn(user);
        when(authService.changeUserPassword(1, "OldPass1!", "NewPass1!")).thenReturn(false);
        when(request.getSession()).thenReturn(session);

        servlet.doPost(request, response);

        verify(session).setAttribute("errorMessage", "Failed to change password. Please try again.");
        verify(response).sendRedirect("changePassword");
    }

    @Test
    @DisplayName("#18 POST - changeOwnPassword - InvalidCredentials")
    void testDoPost_changeOwnPassword_invalidCredentials() throws Exception {
        User user = new User();
        user.setUserID(1);

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("user@test.com");
        when(request.getParameter("action")).thenReturn("changeOwnPassword");
        when(request.getParameter("currentPassword")).thenReturn("WrongPass1!");
        when(request.getParameter("newPassword")).thenReturn("NewPass1!");
        when(request.getParameter("confirmPassword")).thenReturn("NewPass1!");
        when(userService.getUserByEmail("user@test.com")).thenReturn(user);
        when(authService.changeUserPassword(1, "WrongPass1!", "NewPass1!"))
                .thenThrow(new InvalidCredentialsException("Current password is incorrect."));
        when(request.getSession()).thenReturn(session);

        servlet.doPost(request, response);

        verify(session).setAttribute("errorMessage", "Current password is incorrect.");
        verify(response).sendRedirect("changePassword");
    }

    @Test
    @DisplayName("#19 POST - resetUserPassword - not admin")
    void testDoPost_resetUserPassword_notAdmin() throws Exception {
        User normalUser = new User();
        normalUser.setRole("user");

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("user@test.com");
        when(request.getParameter("action")).thenReturn("resetUserPassword");
        when(userService.getUserByEmail("user@test.com")).thenReturn(normalUser);
        when(request.getSession()).thenReturn(session);

        servlet.doPost(request, response);

        verify(session).setAttribute("errorMessage", "Access denied. Admin privileges required.");
        verify(response).sendRedirect("welcome.jsp");
    }

    @Test
    @DisplayName("#20 POST - resetUserPassword - empty fields")
    void testDoPost_resetUserPassword_emptyFields() throws Exception {
        User adminUser = new User();
        adminUser.setRole("admin");

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("admin@test.com");
        when(request.getParameter("action")).thenReturn("resetUserPassword");
        when(userService.getUserByEmail("admin@test.com")).thenReturn(adminUser);
        when(request.getParameter("userId")).thenReturn(null);
        when(request.getParameter("newPassword")).thenReturn(null);
        when(request.getParameter("confirmPassword")).thenReturn(null);
        when(request.getSession()).thenReturn(session);

        servlet.doPost(request, response);

        verify(session).setAttribute("errorMessage", "All fields are required.");
        verify(response).sendRedirect("userManagement");
    }

    @Test
    @DisplayName("#21 POST - resetUserPassword - invalid userId")
    void testDoPost_resetUserPassword_invalidUserId() throws Exception {
        User adminUser = new User();
        adminUser.setRole("admin");

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("admin@test.com");
        when(request.getParameter("action")).thenReturn("resetUserPassword");
        when(userService.getUserByEmail("admin@test.com")).thenReturn(adminUser);
        when(request.getParameter("userId")).thenReturn("abc");
        when(request.getParameter("newPassword")).thenReturn("NewPass1!");
        when(request.getParameter("confirmPassword")).thenReturn("NewPass1!");
        when(request.getSession()).thenReturn(session);

        servlet.doPost(request, response);

        verify(session).setAttribute("errorMessage", "Invalid user ID.");
        verify(response).sendRedirect("userManagement");
    }

    @Test
    @DisplayName("#22 POST - resetUserPassword - negative userId")
    void testDoPost_resetUserPassword_negativeUserId() throws Exception {
        User adminUser = new User();
        adminUser.setRole("admin");

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("admin@test.com");
        when(request.getParameter("action")).thenReturn("resetUserPassword");
        when(userService.getUserByEmail("admin@test.com")).thenReturn(adminUser);
        when(request.getParameter("userId")).thenReturn("-1");
        when(request.getParameter("newPassword")).thenReturn("NewPass1!");
        when(request.getParameter("confirmPassword")).thenReturn("NewPass1!");
        when(request.getSession()).thenReturn(session);

        servlet.doPost(request, response);

        verify(session).setAttribute("errorMessage", "Invalid user ID.");
        verify(response).sendRedirect("userManagement");
    }

    @Test
    @DisplayName("#23 POST - resetUserPassword - passwords don't match")
    void testDoPost_resetUserPassword_passwordsDontMatch() throws Exception {
        User adminUser = new User();
        adminUser.setRole("admin");

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("admin@test.com");
        when(request.getParameter("action")).thenReturn("resetUserPassword");
        when(userService.getUserByEmail("admin@test.com")).thenReturn(adminUser);
        when(request.getParameter("userId")).thenReturn("5");
        when(request.getParameter("newPassword")).thenReturn("NewPass1!");
        when(request.getParameter("confirmPassword")).thenReturn("DiffPass1!");
        when(request.getSession()).thenReturn(session);

        servlet.doPost(request, response);

        verify(session).setAttribute("errorMessage", "New password and confirm password do not match.");
        verify(response).sendRedirect("userManagement");
    }

    @Test
    @DisplayName("#24 POST - resetUserPassword - success")
    void testDoPost_resetUserPassword_success() throws Exception {
        User adminUser = new User();
        adminUser.setRole("admin");

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("admin@test.com");
        when(request.getParameter("action")).thenReturn("resetUserPassword");
        when(userService.getUserByEmail("admin@test.com")).thenReturn(adminUser);
        when(request.getParameter("userId")).thenReturn("5");
        when(request.getParameter("newPassword")).thenReturn("NewPass1!");
        when(request.getParameter("confirmPassword")).thenReturn("NewPass1!");
        when(authService.setUserPassword(5, "NewPass1!")).thenReturn(true);
        when(request.getSession()).thenReturn(session);

        servlet.doPost(request, response);

        verify(session).setAttribute("message", "User password reset successfully.");
        verify(response).sendRedirect("userManagement");
    }

    @Test
    @DisplayName("#25 POST - Cancel with previous page")
    void testDoPost_cancelWithPreviousPage() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("user@test.com");
        when(request.getParameter("action")).thenReturn("cancel");
        when(session.getAttribute("previousPage")).thenReturn("http://localhost/profile");

        servlet.doPost(request, response);

        verify(session).removeAttribute("previousPage");
        verify(response).sendRedirect("http://localhost/profile");
    }

    @Test
    @DisplayName("#26 POST - Cancel without previous page")
    void testDoPost_cancelWithoutPreviousPage() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("user@test.com");
        when(request.getParameter("action")).thenReturn("cancel");
        when(session.getAttribute("previousPage")).thenReturn(null);

        servlet.doPost(request, response);

        verify(response).sendRedirect("welcome.jsp");
    }

    @Test
    @DisplayName("#27 POST - Unknown action")
    void testDoPost_unknownAction() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("user@test.com");
        when(request.getParameter("action")).thenReturn("unknown");

        servlet.doPost(request, response);

        verify(response).sendRedirect("changePassword");
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
