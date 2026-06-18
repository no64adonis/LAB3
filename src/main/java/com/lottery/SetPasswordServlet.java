package com.lottery;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.lottery.db.UserDAO;
import com.lottery.model.User;
import com.lottery.config.ServiceFactory;
import com.lottery.service.AuthService;
import com.lottery.service.UserService;
import com.lottery.service.exception.ServiceException;
import com.lottery.service.exception.UserNotFoundException;
import com.lottery.util.InputValidator;
import com.lottery.util.PasswordValidator;
public class SetPasswordServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final ServiceFactory serviceFactory = ServiceFactory.getInstance();
    private AuthService authService;
    private UserService userService;
    public void init() throws ServletException {
        super.init();
        authService = serviceFactory.getAuthService();
        userService = serviceFactory.getUserService();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        String email = (session != null) ? (String) session.getAttribute("email") : null;

        if (email != null) {
            email = InputValidator.sanitizeString(email);
        }

        if (email == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        try {
            
            User user = userService.getUserByEmail(email);
            if (user == null) {
                response.sendRedirect("login.jsp");
                return;
            }

            Boolean needsPasswordSetup = (session != null) ? (Boolean) session.getAttribute("needsPasswordSetup")
                    : null;

            if (needsPasswordSetup == null || !needsPasswordSetup) {
                
                if ("admin".equals(user.getRole())) {
                    response.sendRedirect("userManagement");
                } else {
                    response.sendRedirect("userLottery");
                }
                return;
            }

            request.getRequestDispatcher("/setPassword.jsp").forward(request, response);
        } catch (UserNotFoundException e) {
            response.sendRedirect("login.jsp");
        } catch (ServiceException e) {
            request.getSession().setAttribute("errorMessage", "Error retrieving user: " + e.getMessage());
            response.sendRedirect("login.jsp");
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        String email = (session != null) ? (String) session.getAttribute("email") : null;

        if (email != null) {
            email = InputValidator.sanitizeString(email);
        }

        if (email == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        try {
            
            Boolean needsPasswordSetup = (session != null) ? (Boolean) session.getAttribute("needsPasswordSetup")
                    : null;

            if (needsPasswordSetup == null || !needsPasswordSetup) {
                
                User user = userService.getUserByEmail(email);
                if (user != null && "admin".equals(user.getRole())) {
                    response.sendRedirect("userManagement");
                } else {
                    response.sendRedirect("userLottery");
                }
                return;
            }

            String newPassword = request.getParameter("newPassword");
            String confirmPassword = request.getParameter("confirmPassword");

            if (newPassword != null) {
                newPassword = InputValidator.sanitizeString(newPassword);
            }
            if (confirmPassword != null) {
                confirmPassword = InputValidator.sanitizeString(confirmPassword);
            }

            if ((newPassword != null && InputValidator.containsSQLInjectionPatterns(newPassword)) ||
                    (confirmPassword != null && InputValidator.containsSQLInjectionPatterns(confirmPassword))) {
                request.getSession().setAttribute("errorMessage", "Invalid input values.");
                response.sendRedirect("setPassword");
                return;
            }

            if (newPassword == null || newPassword.isEmpty() ||
                    confirmPassword == null || confirmPassword.isEmpty()) {
                request.getSession().setAttribute("errorMessage", "All fields are required.");
                response.sendRedirect("setPassword");
                return;
            }

            String passwordError = PasswordValidator.validate(newPassword);
            if (passwordError != null) {
                request.getSession().setAttribute("errorMessage", passwordError);
                response.sendRedirect("setPassword");
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                request.getSession().setAttribute("errorMessage", "New password and confirm password do not match.");
                response.sendRedirect("setPassword");
                return;
            }

            User user = userService.getUserByEmail(email);
            if (user == null) {
                request.getSession().setAttribute("errorMessage", "User not found.");
                response.sendRedirect("setPassword");
                return;
            }

            if (authService.setUserPassword(user.getUserID(), newPassword)) {
                
                session.removeAttribute("needsPasswordSetup");
                session.removeAttribute("oauthProvider");

                UserDAO.updatePasswordSet(email, true);

                request.getSession().setAttribute("successMessage",
                        "Password set successfully. Welcome to Fortuna Lotto!");

                if ("admin".equals(user.getRole())) {
                    response.sendRedirect("userManagement");
                } else {
                    response.sendRedirect("userLottery");
                }
            } else {
                request.getSession().setAttribute("errorMessage", "Failed to set password. Please try again.");
                response.sendRedirect("setPassword");
            }
        } catch (UserNotFoundException e) {
            request.getSession().setAttribute("errorMessage", "User not found.");
            response.sendRedirect("setPassword");
        } catch (ServiceException e) {
            request.getSession().setAttribute("errorMessage", "Error setting password: " + e.getMessage());
            response.sendRedirect("setPassword");
        }
    }
}