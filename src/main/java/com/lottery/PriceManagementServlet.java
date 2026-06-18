package com.lottery;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.lottery.util.InputValidator;
public class PriceManagementServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final com.lottery.config.ServiceFactory serviceFactory = com.lottery.config.ServiceFactory.getInstance();
    private AdminService adminService = serviceFactory.getAdminService();
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

            List<String> companies = adminService.getAllCompanies();
            request.setAttribute("companies", companies);

            Map<String, Double> companyPrices = new HashMap<>();
            for (String company : companies) {
                double price = adminService.getPriceForCompany(company);
                companyPrices.put(company, price);
            }
            request.setAttribute("companyPrices", companyPrices);

            request.getRequestDispatcher("/priceManagement.jsp").forward(request, response);
        } catch (ServiceException e) {
            session.setAttribute("message", "Error retrieving data: " + e.getMessage());
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

            if (action != null && InputValidator.containsSQLInjectionPatterns(action)) {
                request.getSession().setAttribute("message", "Invalid request.");
                response.sendRedirect("priceManagement");
                return;
            }

            if ("updatePrices".equals(action)) {
                
                java.util.List<String> companies = adminService.getAllCompanies();

                java.util.Map<String, String> priceParams = new java.util.HashMap<>();
                for (String company : companies) {
                    String priceParam = request.getParameter("price_" + company);
                    priceParams.put(company, priceParam);
                }

                try {
                    
                    if (adminService.processPriceUpdates(priceParams)) {
                        request.getSession().setAttribute("message", "Prices updated successfully!");
                    } else {
                        request.getSession().setAttribute("message", "Failed to update prices.");
                    }
                } catch (ServiceException e) {
                    request.getSession().setAttribute("message", e.getMessage());
                }

                response.sendRedirect("priceManagement");
            } else {
                response.sendRedirect("priceManagement");
            }
        } catch (ServiceException e) {
            session.setAttribute("message", "Error processing request: " + e.getMessage());
            response.sendRedirect("priceManagement");
        }
    }
}