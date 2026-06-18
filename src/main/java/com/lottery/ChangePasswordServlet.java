package com.lottery;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.lottery.model.User;
import com.lottery.service.AuthService;
import com.lottery.service.UserService;
import com.lottery.service.exception.InvalidCredentialsException;
import com.lottery.service.exception.ServiceException;
import com.lottery.service.exception.UserNotFoundException;
import com.lottery.util.InputValidator;
import com.lottery.util.PasswordValidator;
public class ChangePasswordServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final com.lottery.config.ServiceFactory serviceFactory = com.lottery.config.ServiceFactory.getInstance();
    private AuthService authService = serviceFactory.getAuthService();
    private UserService userService = serviceFactory.getUserService();

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        String email = (session != null) ? (String) session.getAttribute("email") : null;

        if (email == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        String referer = request.getHeader("Referer");
        if (referer != null && !referer.contains("changePassword")) {
            session.setAttribute("previousPage", referer);
        }

        String action = request.getParameter("action");
        String userIdParam = request.getParameter("userId");

        if (userIdParam != null && InputValidator.containsSQLInjectionPatterns(userIdParam)) {
            request.getSession().setAttribute("errorMessage", "Invalid request.");
            response.sendRedirect("welcome.jsp");
            return;
        }

        if ("reset".equals(action) && userIdParam != null) {
            
            try {
                User currentUser = userService.getUserByEmail(email);
                if (currentUser == null || !"admin".equals(currentUser.getRole())) {
                    request.getSession().setAttribute("errorMessage", "Access denied. Admin privileges required.");
                    response.sendRedirect("welcome.jsp");
                    return;
                }

                request.setAttribute("resetUserId", userIdParam);
                request.getRequestDispatcher("/resetPassword.jsp").forward(request, response);
                return;
            } catch (UserNotFoundException e) {
                request.getSession().setAttribute("errorMessage", "User not found.");
                response.sendRedirect("welcome.jsp");
                return;
            } catch (ServiceException e) {
                request.getSession().setAttribute("errorMessage", "Error retrieving user: " + e.getMessage());
                response.sendRedirect("welcome.jsp");
                return;
            }
        }

        try {
            User currentUser = userService.getUserByEmail(email);
            if (currentUser == null || !"admin".equals(currentUser.getRole())) {
                response.sendRedirect("profile");
                return;
            }
        } catch (Exception e) {
            
        }

        request.getRequestDispatcher("/changePassword.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        String email = (session != null) ? (String) session.getAttribute("email") : null;

        if (email == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        String action = request.getParameter("action");

        if (action != null && InputValidator.containsSQLInjectionPatterns(action)) {
            request.getSession().setAttribute("errorMessage", "Invalid request.");
            response.sendRedirect("changePassword");
            return;
        }

        if ("changeOwnPassword".equals(action)) {
            changeOwnPassword(request, response, email);
        } else if ("resetUserPassword".equals(action)) {
            resetUserPassword(request, response, email);
        } else if ("cancel".equals(action)) {
            cancelChangePassword(request, response);
        } else {
            response.sendRedirect("changePassword");
        }
    }

    private void changeOwnPassword(HttpServletRequest request, HttpServletResponse response, String email)
            throws IOException {
        String currentPassword = request.getParameter("currentPassword");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");

        if (currentPassword != null) {
            currentPassword = InputValidator.sanitizeString(currentPassword);
        }
        if (newPassword != null) {
            newPassword = InputValidator.sanitizeString(newPassword);
        }
        if (confirmPassword != null) {
            confirmPassword = InputValidator.sanitizeString(confirmPassword);
        }

        if (InputValidator.containsSQLInjectionPatterns(currentPassword) ||
                InputValidator.containsSQLInjectionPatterns(newPassword) ||
                InputValidator.containsSQLInjectionPatterns(confirmPassword)) {
            request.getSession().setAttribute("errorMessage", "Invalid input values.");
            response.sendRedirect("changePassword");
            return;
        }

        if (currentPassword == null || currentPassword.isEmpty() ||
                newPassword == null || newPassword.isEmpty() ||
                confirmPassword == null || confirmPassword.isEmpty()) {
            request.getSession().setAttribute("errorMessage", "All fields are required.");
            response.sendRedirect("changePassword");
            return;
        }

        String passwordError = PasswordValidator.validate(newPassword);
        if (passwordError != null) {
            request.getSession().setAttribute("errorMessage", passwordError);
            response.sendRedirect("changePassword");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            request.getSession().setAttribute("errorMessage", "New password and confirm password do not match.");
            response.sendRedirect("changePassword");
            return;
        }

        if (currentPassword.equals(newPassword)) {
            request.getSession().setAttribute("errorMessage", "New password must be different from current password.");
            response.sendRedirect("changePassword");
            return;
        }

        try {
            
            User user = userService.getUserByEmail(email);
            if (user == null) {
                request.getSession().setAttribute("errorMessage", "User not found.");
                response.sendRedirect("changePassword");
                return;
            }

            if (authService.changeUserPassword(user.getUserID(), currentPassword, newPassword)) {
                request.getSession().setAttribute("successMessage", "Password changed successfully.");
                response.sendRedirect("changePassword");
            } else {
                request.getSession().setAttribute("errorMessage", "Failed to change password. Please try again.");
                response.sendRedirect("changePassword");
            }
        } catch (InvalidCredentialsException e) {
            request.getSession().setAttribute("errorMessage", e.getMessage());
            response.sendRedirect("changePassword");
        } catch (UserNotFoundException e) {
            request.getSession().setAttribute("errorMessage", "User not found.");
            response.sendRedirect("changePassword");
        } catch (ServiceException e) {
            request.getSession().setAttribute("errorMessage", "Error changing password: " + e.getMessage());
            response.sendRedirect("changePassword");
        }
    }

    private void resetUserPassword(HttpServletRequest request, HttpServletResponse response, String adminEmail)
            throws IOException {
        
        try {
            User adminUser = userService.getUserByEmail(adminEmail);
            if (adminUser == null || !"admin".equals(adminUser.getRole())) {
                request.getSession().setAttribute("errorMessage", "Access denied. Admin privileges required.");
                response.sendRedirect("welcome.jsp");
                return;
            }

            String userIdParam = request.getParameter("userId");
            String newPassword = request.getParameter("newPassword");
            String confirmPassword = request.getParameter("confirmPassword");

            if (userIdParam != null) {
                userIdParam = InputValidator.sanitizeString(userIdParam);
            }
            if (newPassword != null) {
                newPassword = InputValidator.sanitizeString(newPassword);
            }
            if (confirmPassword != null) {
                confirmPassword = InputValidator.sanitizeString(confirmPassword);
            }

            if (InputValidator.containsSQLInjectionPatterns(userIdParam) ||
                    InputValidator.containsSQLInjectionPatterns(newPassword) ||
                    InputValidator.containsSQLInjectionPatterns(confirmPassword)) {
                request.getSession().setAttribute("errorMessage", "Invalid input values.");
                response.sendRedirect("userManagement");
                return;
            }

            if (userIdParam == null || userIdParam.isEmpty() ||
                    newPassword == null || newPassword.isEmpty() ||
                    confirmPassword == null || confirmPassword.isEmpty()) {
                request.getSession().setAttribute("errorMessage", "All fields are required.");
                response.sendRedirect("userManagement");
                return;
            }

            String passwordError = PasswordValidator.validate(newPassword);
            if (passwordError != null) {
                request.getSession().setAttribute("errorMessage", passwordError);
                response.sendRedirect("userManagement");
                return;
            }

            int userId;
            try {
                userId = Integer.parseInt(userIdParam);
                
                if (userId <= 0) {
                    request.getSession().setAttribute("errorMessage", "Invalid user ID.");
                    response.sendRedirect("userManagement");
                    return;
                }
            } catch (NumberFormatException e) {
                request.getSession().setAttribute("errorMessage", "Invalid user ID.");
                response.sendRedirect("userManagement");
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                request.getSession().setAttribute("errorMessage", "New password and confirm password do not match.");
                response.sendRedirect("userManagement");
                return;
            }

            if (authService.setUserPassword(userId, newPassword)) {
                request.getSession().setAttribute("message", "User password reset successfully.");
                response.sendRedirect("userManagement");
            } else {
                request.getSession().setAttribute("error", "Failed to reset user password. Please try again.");
                response.sendRedirect("userManagement");
            }
        } catch (UserNotFoundException e) {
            request.getSession().setAttribute("errorMessage", "User not found.");
            response.sendRedirect("userManagement");
        } catch (ServiceException e) {
            request.getSession().setAttribute("errorMessage", "Error resetting password: " + e.getMessage());
            response.sendRedirect("userManagement");
        }
    }

    private void cancelChangePassword(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        String previousPage = (session != null) ? (String) session.getAttribute("previousPage") : null;

        if (previousPage != null && !previousPage.isEmpty()) {
            
            session.removeAttribute("previousPage");
            response.sendRedirect(previousPage);
        } else {
            
            response.sendRedirect("welcome.jsp");
        }
    }
}
