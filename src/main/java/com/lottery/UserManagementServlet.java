package com.lottery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.lottery.model.User;
import com.lottery.service.AdminService;
import com.lottery.service.EmailService;
import com.lottery.service.UserService;
import com.lottery.service.exception.EmailException;
import com.lottery.service.exception.ServiceException;
import com.lottery.service.exception.UserNotFoundException;
import com.lottery.util.InputValidator;
public class UserManagementServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final int USERS_PER_PAGE = 20;
    private final com.lottery.config.ServiceFactory serviceFactory = com.lottery.config.ServiceFactory.getInstance();
    private UserService userService = serviceFactory.getUserService();
    private AdminService adminService = serviceFactory.getAdminService();
    private EmailService emailService = serviceFactory.getEmailService();

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        String email = (session != null) ? (String) session.getAttribute("email") : null;

        if (email == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        try {
            
            User currentUser = userService.getUserByEmail(email);
            if (currentUser == null || !"admin".equals(currentUser.getRole())) {
                response.sendRedirect("welcome.jsp");
                return;
            }

            String action = request.getParameter("action");
            String searchTerm = request.getParameter("search");
            String pageParam = request.getParameter("page");
            String lastLoginFrom = request.getParameter("lastLoginFrom");
            String lastLoginTo = request.getParameter("lastLoginTo");
            String role = request.getParameter("role");

            if (lastLoginFrom != null)
                lastLoginFrom = lastLoginFrom.trim();
            if (lastLoginTo != null)
                lastLoginTo = lastLoginTo.trim();
            if (role != null)
                role = role.trim();
            if (searchTerm != null)
                searchTerm = searchTerm.trim();

            String[] searchFieldsArr = request.getParameterValues("searchFields");
            List<String> searchFields = (searchFieldsArr != null) ? java.util.Arrays.asList(searchFieldsArr) : null;

            if (searchTerm != null && !searchTerm.isEmpty()) {
                if (!InputValidator.isValidSearchTerm(searchTerm, 100)) {
                    searchTerm = "";
                } else {
                    
                    searchTerm = InputValidator.sanitizeString(searchTerm);
                }
            }

            Integer pageObj = InputValidator.validateInteger(pageParam);
            int page = (pageObj != null && pageObj > 0) ? pageObj : 1;

            int offset = (page - 1) * USERS_PER_PAGE;

            List<User> users = new ArrayList<>();
            int totalUsers = 0;

            boolean isSearching = (searchTerm != null && !searchTerm.isEmpty()) ||
                    (lastLoginFrom != null && !lastLoginFrom.isEmpty() && InputValidator.isValidDate(lastLoginFrom)) ||
                    (lastLoginTo != null && !lastLoginTo.isEmpty() && InputValidator.isValidDate(lastLoginTo)) ||
                    (role != null && !role.isEmpty());

            if ("inactive".equals(action) && !isSearching) {
                
                String period = request.getParameter("period");
                if (period == null || period.isEmpty()) {
                    period = "30 days"; 
                }
                users = adminService.getInactiveUsers(period);
                totalUsers = users.size();
            } else if (isSearching) {
                users = userService.searchUsers(searchTerm, searchFields, lastLoginFrom, lastLoginTo, role, offset,
                        USERS_PER_PAGE);
                totalUsers = userService.getSearchUserCount(searchTerm, searchFields, lastLoginFrom, lastLoginTo, role);
            } else {
                users = userService.getAllUsers(offset, USERS_PER_PAGE);
                totalUsers = userService.getUserCount();
            }

            int totalPages = (int) Math.ceil((double) totalUsers / USERS_PER_PAGE);

            java.time.LocalDate today = java.time.LocalDate.now();
            request.setAttribute("todayMinus1", today.minusDays(1).toString());
            request.setAttribute("todayMinus7", today.minusDays(7).toString());
            request.setAttribute("todayMinus30", today.minusMonths(1).toString());
            request.setAttribute("todayMinus90", today.minusDays(90).toString());
            request.setAttribute("todayMinus365", today.minusYears(1).toString());

            request.setAttribute("users", users);
            request.setAttribute("currentPage", page);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("totalUsers", totalUsers);
            request.setAttribute("searchTerm", searchTerm);
            request.setAttribute("searchFields", searchFields);
            request.setAttribute("lastLoginFrom", lastLoginFrom);
            request.setAttribute("lastLoginTo", lastLoginTo);
            request.setAttribute("role", role);
            request.setAttribute("action", action);

            request.getRequestDispatcher("/userManagement.jsp").forward(request, response);
        } catch (ServiceException e) {
            session.setAttribute("error", "Error retrieving users: " + e.getMessage());
            response.sendRedirect("welcome.jsp");
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        String email = (session != null) ? (String) session.getAttribute("email") : null;

        if (email == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        try {
            
            User currentUser = userService.getUserByEmail(email);
            if (currentUser == null || !"admin".equals(currentUser.getRole())) {
                response.sendRedirect("welcome.jsp");
                return;
            }

            String action = request.getParameter("action");

            if ("updateRole".equals(action)) {
                updateRole(request, response);
            } else if ("setActiveStatus".equals(action)) {
                setActiveStatus(request, response);
            } else if ("updateUser".equals(action)) {
                updateUser(request, response);
            } else if ("updateUserAccount".equals(action)) {
                updateUserAccount(request, response);
            } else if ("createUser".equals(action)) {
                createUser(request, response);
            } else if ("bulkDeactivate".equals(action)) {
                bulkDeactivate(request, response);
            } else {
                response.sendRedirect("userManagement");
            }
        } catch (ServiceException e) {
            session.setAttribute("error", "Error processing request: " + e.getMessage());
            response.sendRedirect("userManagement");
        }
    }

    private void bulkDeactivate(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            String[] userIds = request.getParameterValues("selectedUsers");

            if (adminService.processBulkUserDeactivation(userIds)) {
                
            } else {
                request.getSession().setAttribute("error", "Failed to deactivate users.");
            }
        } catch (ServiceException e) {
            request.getSession().setAttribute("error", e.getMessage());
        }

        response.sendRedirect("userManagement");
    }

    private void updateRole(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            String userId = request.getParameter("userId");
            String role = request.getParameter("role");

            if (adminService.processUserRoleUpdate(userId, role)) {
                request.getSession().setAttribute("message", "User role updated successfully.");
            } else {
                request.getSession().setAttribute("error", "Failed to update user role.");
            }
        } catch (ServiceException e) {
            request.getSession().setAttribute("error", e.getMessage());
        }

        response.sendRedirect("userManagement");
    }

    private void setActiveStatus(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            String userId = request.getParameter("userId");
            String isActive = request.getParameter("isActive");

            if (adminService.processUserActiveStatusUpdate(userId, isActive)) {
                request.getSession().setAttribute("message", "User status updated successfully.");
            } else {
                request.getSession().setAttribute("error", "Failed to update user status.");
            }
        } catch (ServiceException e) {
            request.getSession().setAttribute("error", e.getMessage());
        }

        response.sendRedirect("userManagement");
    }

    private void updateUser(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            String userId = request.getParameter("userId");
            String email = request.getParameter("email");
            String firstName = request.getParameter("firstName");
            String lastName = request.getParameter("lastName");
            String phone = request.getParameter("phone");

            if (adminService.processUserDetailsUpdate(userId, email, firstName, lastName, phone)) {
                request.getSession().setAttribute("message", "User details updated successfully.");
            } else {
                request.getSession().setAttribute("error", "Failed to update user details.");
            }
        } catch (ServiceException e) {
            request.getSession().setAttribute("error", e.getMessage());
        }

        response.sendRedirect("userManagement");
    }

    private void updateUserAccount(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String userIdStr = request.getParameter("userId");
            String email = request.getParameter("email");
            String firstName = request.getParameter("firstName");
            String lastName = request.getParameter("lastName");
            String password = request.getParameter("password");

            int userId = Integer.parseInt(userIdStr);
            User existingUser = userService.getUserById(userId);
            String phone = (existingUser != null) ? existingUser.getPhone() : null;

            boolean success = adminService.processUserDetailsUpdate(userIdStr, email, firstName, lastName, phone);

            if (success) {
                if (password != null && !password.trim().isEmpty()) {
                    userService.updateUserPassword(userId, password);
                }
                request.getSession().setAttribute("message", "User account updated successfully.");
            } else {
                request.getSession().setAttribute("error", "Failed to update user account.");
            }
        } catch (Exception e) {
            request.getSession().setAttribute("error", e.getMessage());
        }

        response.sendRedirect("userManagement");
    }

    private void createUser(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            String email = request.getParameter("email");
            String password = request.getParameter("password");
            String firstName = request.getParameter("firstName");
            String lastName = request.getParameter("lastName");
            String role = request.getParameter("role");
            String balanceStr = request.getParameter("balance");
            boolean isActive = "true".equals(request.getParameter("isActive"));

            if (adminService.processUserCreation(email, password, firstName, lastName, role, balanceStr, isActive)) {
                request.getSession().setAttribute("message", "User account created successfully for " + email);
            } else {
                request.getSession().setAttribute("error", "Failed to create user account for " + email);
            }
        } catch (ServiceException e) {
            request.getSession().setAttribute("error", e.getMessage());
        }

        response.sendRedirect("userManagement");
    }

}