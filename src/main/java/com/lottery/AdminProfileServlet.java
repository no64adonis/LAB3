package com.lottery;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.lottery.model.User;
import com.lottery.service.AuthService;
import com.lottery.service.AuthServiceImpl;
import com.lottery.service.UserService;
import com.lottery.service.UserServiceImpl;
import com.lottery.service.exception.ServiceException;
import com.lottery.util.InputValidator;
import com.lottery.util.PasswordValidator;
public class AdminProfileServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private UserService userService = new UserServiceImpl();
    private AuthService authService = new AuthServiceImpl();
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        String email = (session != null) ? (String) session.getAttribute("email") : null;
        if (email == null) {
            response.sendRedirect("login");
            return;
        }

        try {
            User user = userService.getUserByEmail(email);
            if (user == null || !"admin".equals(user.getRole())) {
                response.sendRedirect("welcome.jsp");
                return;
            }
            request.setAttribute("user", user);
        } catch (Exception e) {
            request.setAttribute("errorMessage", "Could not load profile.");
        }
        request.getRequestDispatcher("/adminProfile.jsp").forward(request, response);
    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        String email = (session != null) ? (String) session.getAttribute("email") : null;
        if (email == null) {
            response.sendRedirect("login");
            return;
        }

        String action = request.getParameter("action");
        try {
            User user = userService.getUserByEmail(email);
            if (user == null || !"admin".equals(user.getRole())) {
                response.sendRedirect("welcome.jsp");
                return;
            }

            switch (action != null ? action : "") {
                case "updateName":
                    handleNameChange(request, response, user, session);
                    break;
                case "requestEmailChange":
                    handleEmailChangeRequest(request, response, user, session);
                    break;
                case "verifyEmailCode":
                    handleEmailVerification(request, response, user, session);
                    break;
                case "changePassword":
                    handlePasswordChange(request, response, user, session);
                    break;
                default:
                    response.sendRedirect("adminProfile");
            }
        } catch (Exception e) {
            session.setAttribute("errorMessage", "Error: " + e.getMessage());
            response.sendRedirect("adminProfile");
        }
    }

    private void handleNameChange(HttpServletRequest request, HttpServletResponse response,
            User user, HttpSession session) throws IOException {
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String displayName = request.getParameter("displayName");

        if ((firstName == null || firstName.trim().isEmpty()) &&
                (lastName == null || lastName.trim().isEmpty()) &&
                displayName != null && !displayName.trim().isEmpty()) {
            String[] parts = displayName.trim().split("\\s+", 2);
            firstName = parts[0];
            lastName = parts.length > 1 ? parts[1] : "";
        }

        if (firstName == null)
            firstName = user.getFirstName() != null ? user.getFirstName() : "";
        if (lastName == null)
            lastName = user.getLastName() != null ? user.getLastName() : "";

        firstName = InputValidator.sanitizeString(firstName.trim());
        lastName = InputValidator.sanitizeString(lastName.trim());

        if (firstName.length() > 50) {
            session.setAttribute("errorMessage", "First name is too long (max 50 characters).");
            response.sendRedirect("adminProfile");
            return;
        }

        if (lastName.length() > 50) {
            session.setAttribute("errorMessage", "Last name is too long (max 50 characters).");
            response.sendRedirect("adminProfile");
            return;
        }

        try {
            userService.updateUserDetails(user.getUserID(), user.getEmail(), firstName, lastName, user.getPhone());
            String newDisplayName = firstName + " " + lastName;
            session.setAttribute("displayName", newDisplayName.trim());
            session.setAttribute("successMessage", "Name updated successfully!");
        } catch (ServiceException e) {
            session.setAttribute("errorMessage", "Failed to update name: " + e.getMessage());
        }

        response.sendRedirect("adminProfile");
    }

    private void handleEmailChangeRequest(HttpServletRequest request, HttpServletResponse response,
            User user, HttpSession session) throws IOException {
        String newEmail = request.getParameter("newEmail");

        if (newEmail == null || newEmail.trim().isEmpty()) {
            session.setAttribute("errorMessage", "Email is required.");
            response.sendRedirect("adminProfile");
            return;
        }

        newEmail = InputValidator.sanitizeString(newEmail.trim());

        if (!InputValidator.isValidEmail(newEmail)) {
            session.setAttribute("errorMessage", "Invalid email format.");
            response.sendRedirect("adminProfile");
            return;
        }

        if (newEmail.equalsIgnoreCase(user.getEmail())) {
            session.setAttribute("errorMessage", "New email is the same as your current email.");
            response.sendRedirect("adminProfile");
            return;
        }

        try {
            userService.sendEmailVerificationCode(user.getUserID(), newEmail);
            session.setAttribute("pendingEmail", newEmail);
            session.setAttribute("successMessage",
                    "Verification code sent to " + newEmail + ". Please check your email.");
        } catch (ServiceException e) {
            session.setAttribute("errorMessage", "Failed to send verification code: " + e.getMessage());
        }

        response.sendRedirect("adminProfile");
    }

    private void handleEmailVerification(HttpServletRequest request, HttpServletResponse response,
            User user, HttpSession session) throws IOException {
        String code = request.getParameter("verificationCode");

        if (code == null || code.trim().isEmpty()) {
            session.setAttribute("errorMessage", "Verification code is required.");
            response.sendRedirect("adminProfile");
            return;
        }

        try {
            boolean success = userService.verifyEmailCode(user.getUserID(), code.trim());
            if (success) {
                String pendingEmail = (String) session.getAttribute("pendingEmail");
                if (pendingEmail != null) {
                    session.setAttribute("email", pendingEmail);
                    session.removeAttribute("pendingEmail");
                }
                session.setAttribute("successMessage", "Email updated successfully!");
            } else {
                session.setAttribute("errorMessage", "Invalid or expired verification code.");
            }
        } catch (ServiceException e) {
            session.setAttribute("errorMessage", "Failed to verify code: " + e.getMessage());
        }

        response.sendRedirect("adminProfile");
    }

    private void handlePasswordChange(HttpServletRequest request, HttpServletResponse response,
            User user, HttpSession session) throws IOException {
        String currentPassword = request.getParameter("currentPassword");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");

        if (currentPassword == null || currentPassword.isEmpty()) {
            session.setAttribute("errorMessage", "Current password is required.");
            response.sendRedirect("adminProfile");
            return;
        }

        if (newPassword == null || newPassword.isEmpty()) {
            session.setAttribute("errorMessage", "New password is required.");
            response.sendRedirect("adminProfile");
            return;
        }

        String passwordError = PasswordValidator.validate(newPassword);
        if (passwordError != null) {
            session.setAttribute("errorMessage", passwordError);
            response.sendRedirect("adminProfile");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            session.setAttribute("errorMessage", "Passwords do not match.");
            response.sendRedirect("adminProfile");
            return;
        }

        try {
            if (!authService.validateUser(user.getEmail(), currentPassword)) {
                session.setAttribute("errorMessage", "Current password is incorrect.");
                response.sendRedirect("adminProfile");
                return;
            }

            userService.updateUserPassword(user.getUserID(), newPassword);
            session.setAttribute("successMessage", "Password changed successfully!");
        } catch (Exception e) {
            session.setAttribute("errorMessage", "Failed to change password: " + e.getMessage());
        }

        response.sendRedirect("adminProfile");
    }
}
