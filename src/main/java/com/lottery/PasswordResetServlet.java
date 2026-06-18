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
import com.lottery.service.EmailService;
import com.lottery.service.exception.ServiceException;
import com.lottery.service.exception.UserNotFoundException;
import com.lottery.util.InputValidator;
import com.lottery.util.PasswordValidator;
public class PasswordResetServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final com.lottery.config.ServiceFactory serviceFactory = com.lottery.config.ServiceFactory.getInstance();
    private AuthService authService = serviceFactory.getAuthService();
    private com.lottery.service.EmailService emailService = serviceFactory.getEmailService();

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String token = request.getParameter("token");

        if (token != null) {
            token = InputValidator.sanitizeString(token);
        }

        if (token != null && InputValidator.containsSQLInjectionPatterns(token)) {
            request.setAttribute("errorMessage", "Invalid request.");
            request.getRequestDispatcher("/forgotPassword.jsp").forward(request, response);
            return;
        }

        if (token != null && !token.isEmpty()) {
            
            try {
                if (authService.validatePasswordResetToken(token)) {
                    
                    request.setAttribute("resetToken", token);
                    request.getRequestDispatcher("/userResetPassword.jsp").forward(request, response);
                    return;
                } else {
                    
                    request.setAttribute("errorMessage",
                            "The password reset link has expired or is invalid. Please request a new one.");
                    request.getRequestDispatcher("/forgotPassword.jsp").forward(request, response);
                    return;
                }
            } catch (ServiceException e) {
                request.setAttribute("errorMessage", "Error validating password reset link: " + e.getMessage());
                request.getRequestDispatcher("/forgotPassword.jsp").forward(request, response);
                return;
            }
        }

        request.getRequestDispatcher("/forgotPassword.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = request.getParameter("email");
        String token = request.getParameter("token");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");

        if (email != null) {
            email = InputValidator.sanitizeString(email);
        }
        if (token != null) {
            token = InputValidator.sanitizeString(token);
        }
        if (newPassword != null) {
            newPassword = InputValidator.sanitizeString(newPassword);
        }
        if (confirmPassword != null) {
            confirmPassword = InputValidator.sanitizeString(confirmPassword);
        }

        if ((email != null && InputValidator.containsSQLInjectionPatterns(email)) ||
                (token != null && InputValidator.containsSQLInjectionPatterns(token)) ||
                (newPassword != null && InputValidator.containsSQLInjectionPatterns(newPassword)) ||
                (confirmPassword != null && InputValidator.containsSQLInjectionPatterns(confirmPassword))) {
            request.setAttribute("errorMessage", "Invalid input values.");
            request.getRequestDispatcher("/forgotPassword.jsp").forward(request, response);
            return;
        }

        if (email != null && !email.isEmpty()) {
            
            handleForgotPassword(request, response, email);
        } else if (token != null && !token.isEmpty() && newPassword != null && confirmPassword != null) {
            
            handlePasswordReset(request, response, token, newPassword, confirmPassword);
        } else {
            request.setAttribute("errorMessage", "Invalid request.");
            request.getRequestDispatcher("/forgotPassword.jsp").forward(request, response);
        }
    }

    private void handleForgotPassword(HttpServletRequest request, HttpServletResponse response, String email)
            throws IOException, ServletException {
        
        if (!InputValidator.isValidEmail(email)) {
            request.setAttribute("errorMessage", "Invalid email format.");
            request.getRequestDispatcher("/forgotPassword.jsp").forward(request, response);
            return;
        }

        try {
            
            if (authService.resetUserPassword(email)) {
                
                try {
                    
                    request.setAttribute("successMessage",
                            "If the email address exists in our system, a password reset link has been sent to your email address.");
                } catch (Exception e) {
                    
                    request.setAttribute("successMessage",
                            "If the email address exists in our system, a password reset link has been sent to your email address.");
                }
            } else {
                request.setAttribute("errorMessage", "Failed to process request. Please try again later.");
            }
        } catch (UserNotFoundException e) {
            
            request.setAttribute("successMessage",
                    "If the email address exists in our system, a password reset link has been sent to your email address.");
        } catch (ServiceException e) {
            request.setAttribute("errorMessage", "Error processing request: " + e.getMessage());
        }

        request.getRequestDispatcher("/forgotPassword.jsp").forward(request, response);
    }

    private void handlePasswordReset(HttpServletRequest request, HttpServletResponse response, String token,
            String newPassword, String confirmPassword)
            throws IOException, ServletException {
        
        if (newPassword == null || newPassword.isEmpty()) {
            request.setAttribute("errorMessage", "New password is required.");
            request.setAttribute("resetToken", token);
            request.getRequestDispatcher("/userResetPassword.jsp").forward(request, response);
            return;
        }

        if (confirmPassword == null || confirmPassword.isEmpty()) {
            request.setAttribute("errorMessage", "Please confirm your new password.");
            request.setAttribute("resetToken", token);
            request.getRequestDispatcher("/userResetPassword.jsp").forward(request, response);
            return;
        }

        String passwordError = PasswordValidator.validate(newPassword);
        if (passwordError != null) {
            request.setAttribute("errorMessage", passwordError);
            request.setAttribute("resetToken", token);
            request.getRequestDispatcher("/userResetPassword.jsp").forward(request, response);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            request.setAttribute("errorMessage", "Passwords do not match.");
            request.setAttribute("resetToken", token);
            request.getRequestDispatcher("/userResetPassword.jsp").forward(request, response);
            return;
        }

        try {
            
            if (authService.validatePasswordResetToken(token)) {
                
                User user = authService.getUserByPasswordResetToken(token);
                if (user != null && authService.setUserPassword(user.getUserID(), newPassword)) {
                    
                    HttpSession session = request.getSession();
                    session.setAttribute("email", user.getEmail());
                    
                    try {
                        authService.updateLastLoginDate(user.getEmail());
                    } catch (ServiceException e) {
                        
                    }

                    request.setAttribute("successMessage",
                            "Your password has been successfully reset and you have been logged in.");
                    request.getRequestDispatcher("/userResetPassword.jsp").forward(request, response);
                } else {
                    request.setAttribute("errorMessage", "Failed to reset password. Please try again.");
                    request.setAttribute("resetToken", token);
                    request.getRequestDispatcher("/userResetPassword.jsp").forward(request, response);
                }
            } else {
                
                request.setAttribute("errorMessage",
                        "The password reset link has expired or is invalid. Please request a new one.");
                request.getRequestDispatcher("/forgotPassword.jsp").forward(request, response);
            }
        } catch (UserNotFoundException e) {
            request.setAttribute("errorMessage", "Invalid password reset link.");
            request.getRequestDispatcher("/forgotPassword.jsp").forward(request, response);
        } catch (ServiceException e) {
            request.setAttribute("errorMessage", "Error resetting password: " + e.getMessage());
            request.setAttribute("resetToken", token);
            request.getRequestDispatcher("/userResetPassword.jsp").forward(request, response);
        }
    }
}
