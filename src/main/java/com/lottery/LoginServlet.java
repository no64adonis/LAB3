package com.lottery;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.lottery.util.SessionManager;

import com.lottery.model.User;
import com.lottery.service.AuthService;
import com.lottery.service.UserService;
import com.lottery.service.exception.InvalidCredentialsException;
import com.lottery.service.exception.ServiceException;
import com.lottery.service.exception.UserNotFoundException;
import com.lottery.util.InputValidator;
import com.lottery.config.ServiceFactory;
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final ServiceFactory serviceFactory = ServiceFactory.getInstance();
    private AuthService authService = serviceFactory.getAuthService();
    private UserService userService = serviceFactory.getUserService();

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String registrationSuccess = request.getParameter("registrationSuccess");
        if ("true".equals(registrationSuccess)) {
            request.setAttribute("successMessage", "Registration successful! You can now login.");
        }
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        if (email == null || email.trim().isEmpty() || password == null || password.isEmpty()) {
            request.setAttribute("errorMessage", "Email and password are required");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        }

        String sanitizedEmail = InputValidator.sanitizeString(email.trim());

        try {
            if (authService.validateUser(sanitizedEmail, password)) {
                HttpSession session = SessionManager.createSecureSession(request, response, sanitizedEmail);
                userService.updateLastLoginDate(sanitizedEmail);

                User user = userService.getUserByEmail(sanitizedEmail);
                if (user != null) {
                    String displayName = (user.getFirstName() != null ? user.getFirstName() : "") +
                            " " + (user.getLastName() != null ? user.getLastName() : "");
                    displayName = displayName.trim();
                    if (displayName.isEmpty()) {
                        displayName = sanitizedEmail;
                    }
                    session.setAttribute("displayName", displayName);

                    if ("admin".equals(user.getRole())) {
                        response.sendRedirect("userManagement");
                    } else {
                        response.sendRedirect("userLottery");
                    }
                }
            }
        } catch (InvalidCredentialsException e) {
            request.setAttribute("errorMessage", "Invalid email or password");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        } catch (UserNotFoundException e) {
            request.setAttribute("errorMessage", "User not found");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        } catch (ServiceException e) {
            request.setAttribute("errorMessage", "Login failed: " + e.getMessage());
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        }
    }
}
