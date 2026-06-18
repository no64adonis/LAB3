package com.lottery;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.lottery.model.User;
import com.lottery.service.AdminService;
import com.lottery.service.UserService;
import com.lottery.service.exception.ServiceException;
import com.lottery.service.exception.UserNotFoundException;
public class ExportUsersServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final com.lottery.config.ServiceFactory serviceFactory = com.lottery.config.ServiceFactory.getInstance();
    private UserService userService = serviceFactory.getUserService();

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
            String lastLoginFrom = request.getParameter("lastLoginFrom");
            String lastLoginTo = request.getParameter("lastLoginTo");
            String role = request.getParameter("role");
            String[] searchFieldsArr = request.getParameterValues("searchFields");
            List<String> searchFields = (searchFieldsArr != null) ? java.util.Arrays.asList(searchFieldsArr) : null;

            if (searchTerm != null)
                searchTerm = searchTerm.trim();
            if (lastLoginFrom != null)
                lastLoginFrom = lastLoginFrom.trim();
            if (lastLoginTo != null)
                lastLoginTo = lastLoginTo.trim();
            if (role != null)
                role = role.trim();

            List<User> users;
            
            int exportLimit = 50000;

            boolean isSearching = (searchTerm != null && !searchTerm.isEmpty()) ||
                    (lastLoginFrom != null && !lastLoginFrom.isEmpty()) ||
                    (lastLoginTo != null && !lastLoginTo.isEmpty()) ||
                    (role != null && !role.isEmpty());

            if ("inactive".equals(action) && !isSearching) {
                String period = request.getParameter("period");
                if (period == null || period.isEmpty()) {
                    period = "30 days";
                }
                AdminService adminService = serviceFactory.getAdminService();
                users = adminService.getInactiveUsers(period);
            } else if (isSearching) {
                users = userService.searchUsers(searchTerm, searchFields, lastLoginFrom, lastLoginTo, role, 0,
                        exportLimit);
            } else {
                users = userService.getAllUsers(0, exportLimit);
            }

            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment; filename=users.xls");

            PrintWriter out = response.getWriter();

            out.println("ID,Email,First Name,Last Name,Phone,Role,Status,Last Login,Created Date");

            for (User user : users) {
                out.println(user.getUserID() + "," +
                        user.getEmail() + "," +
                        user.getFirstName() + "," +
                        user.getLastName() + "," +
                        user.getPhone() + "," +
                        user.getRole() + "," +
                        (user.isActive() ? "Active" : "Inactive") + "," +
                        user.getLastLoginDate() + "," +
                        user.getCreatedDate());
            }

            out.close();
        } catch (UserNotFoundException e) {
            response.sendRedirect("login.jsp");
        } catch (ServiceException e) {
            session.setAttribute("error", "Error exporting users: " + e.getMessage());
            response.sendRedirect("userManagement");
        }
    }
}