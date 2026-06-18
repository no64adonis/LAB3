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

import com.lottery.model.PaymentMethod;
import com.lottery.model.User;
import com.lottery.service.AdminService;
import com.lottery.service.UserService;
import com.lottery.service.exception.ServiceException;
import com.lottery.util.InputValidator;
public class PaymentMethodManagementServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final int PAYMENT_METHODS_PER_PAGE = 20;
    private final com.lottery.config.ServiceFactory serviceFactory = com.lottery.config.ServiceFactory.getInstance();
    private UserService userService = serviceFactory.getUserService();
    private AdminService adminService = serviceFactory.getAdminService();

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
            request.setAttribute("user", currentUser);

            String pageParam = request.getParameter("page");

            Integer pageObj = InputValidator.validateInteger(pageParam);
            int page = (pageObj != null && pageObj > 0) ? pageObj : 1;

            int offset = (page - 1) * PAYMENT_METHODS_PER_PAGE;

            List<PaymentMethod> paymentMethods = new ArrayList<>();
            int totalPaymentMethods = 0;

            paymentMethods = adminService.getAllPaymentMethodsWithUsers(offset, PAYMENT_METHODS_PER_PAGE);
            totalPaymentMethods = adminService.getTotalPaymentMethodCount();

            int totalPages = (int) Math.ceil((double) totalPaymentMethods / PAYMENT_METHODS_PER_PAGE);

            request.setAttribute("paymentMethods", paymentMethods);
            request.setAttribute("currentPage", page);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("totalPaymentMethods", totalPaymentMethods);

            request.getRequestDispatcher("/paymentMethodManagement.jsp").forward(request, response);
        } catch (ServiceException e) {
            session.setAttribute("error", "Error retrieving payment methods: " + e.getMessage());
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

            if ("updatePaymentMethod".equals(action)) {
                updatePaymentMethod(request, response);
            } else if ("deletePaymentMethod".equals(action)) {
                deletePaymentMethod(request, response);
            } else if ("bulkDelete".equals(action)) {
                bulkDelete(request, response);
            } else {
                response.sendRedirect("paymentMethodManagement");
            }
        } catch (ServiceException e) {
            session.setAttribute("error", "Error processing request: " + e.getMessage());
            response.sendRedirect("paymentMethodManagement");
        }
    }

    private void bulkDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            String[] paymentMethodIds = request.getParameterValues("selectedPaymentMethods");

            if (adminService.bulkDeletePaymentMethods(paymentMethodIds)) {
                request.getSession().setAttribute("message", "Payment methods deleted successfully.");
            } else {
                request.getSession().setAttribute("error", "Failed to delete payment methods.");
            }
        } catch (ServiceException e) {
            request.getSession().setAttribute("error", e.getMessage());
        }

        response.sendRedirect("paymentMethodManagement");
    }

    private void updatePaymentMethod(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            String paymentMethodId = request.getParameter("paymentMethodId");
            String lastFourDigits = request.getParameter("lastFourDigits");
            String cardHolder = request.getParameter("cardHolder");
            String expiryDate = request.getParameter("expiryDate");

            if (paymentMethodId != null && !paymentMethodId.isEmpty()) {
                Integer idObj = InputValidator.validateInteger(paymentMethodId);
                if (idObj != null && idObj > 0) {
                    PaymentMethod paymentMethod = new PaymentMethod();
                    paymentMethod.setId(idObj);
                    paymentMethod.setLastFourDigits(lastFourDigits);
                    paymentMethod.setCardHolder(cardHolder);
                    paymentMethod.setExpiryDate(expiryDate);

                    if (adminService.updatePaymentMethod(paymentMethod)) {
                        request.getSession().setAttribute("message", "Payment method updated successfully.");
                    } else {
                        request.getSession().setAttribute("error", "Failed to update payment method.");
                    }
                } else {
                    request.getSession().setAttribute("error", "Invalid payment method ID.");
                }
            } else {
                request.getSession().setAttribute("error", "Payment method ID is required.");
            }
        } catch (ServiceException e) {
            request.getSession().setAttribute("error", e.getMessage());
        }

        response.sendRedirect("paymentMethodManagement");
    }

    private void deletePaymentMethod(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            String paymentMethodId = request.getParameter("paymentMethodId");

            if (paymentMethodId != null && !paymentMethodId.isEmpty()) {
                Integer idObj = InputValidator.validateInteger(paymentMethodId);
                if (idObj != null && idObj > 0) {
                    
                    if (adminService.deletePaymentMethod(idObj)) {
                        request.getSession().setAttribute("message", "Payment method deleted successfully.");
                    } else {
                        request.getSession().setAttribute("error", "Failed to delete payment method.");
                    }
                } else {
                    request.getSession().setAttribute("error", "Invalid payment method ID.");
                }
            } else {
                request.getSession().setAttribute("error", "Payment method ID is required.");
            }
        } catch (ServiceException e) {
            request.getSession().setAttribute("error", e.getMessage());
        }

        response.sendRedirect("paymentMethodManagement");
    }

}