package com.lottery;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.lottery.model.PaymentMethod;
import com.lottery.model.User;
import com.lottery.service.PaymentService;
import com.lottery.service.UserService;
import com.lottery.service.exception.ServiceException;
import com.lottery.service.exception.UserNotFoundException;
import com.lottery.util.InputValidator;
public class PaymentsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final com.lottery.config.ServiceFactory serviceFactory = com.lottery.config.ServiceFactory.getInstance();
    private PaymentService paymentService = serviceFactory.getPaymentService();
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
            if (currentUser == null) {
                response.sendRedirect("login.jsp");
                return;
            }

            List<PaymentMethod> paymentMethods = paymentService.getUserPaymentMethods(currentUser.getUserID());
            request.setAttribute("paymentMethods", paymentMethods);

            request.getRequestDispatcher("/payments.jsp").forward(request, response);
        } catch (UserNotFoundException e) {
            session.setAttribute("message", "User not found.");
            session.setAttribute("messageType", "error");
            response.sendRedirect("login.jsp");
        } catch (ServiceException e) {
            session.setAttribute("message", "Error retrieving payment methods: " + e.getMessage());
            session.setAttribute("messageType", "error");
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
            if (currentUser == null) {
                response.sendRedirect("login.jsp");
                return;
            }

            String action = request.getParameter("action");

            if ("addPayment".equals(action)) {
                
                String cardNumber = request.getParameter("cardNumber");
                String cardHolder = request.getParameter("cardHolder");
                String expiryMonth = request.getParameter("expiryMonth");
                String expiryYear = request.getParameter("expiryYear");
                String cvv = request.getParameter("cvv");

                try {
                    
                    if (paymentService.processAddPaymentMethod(currentUser, cardNumber, expiryMonth, expiryYear, cvv,
                            cardHolder)) {
                        session.setAttribute("message", "Payment method added successfully.");
                        session.setAttribute("messageType", "success");
                    } else {
                        session.setAttribute("message", "Failed to add payment method. Please try again.");
                        session.setAttribute("messageType", "error");
                    }
                } catch (ServiceException e) {
                    session.setAttribute("message", e.getMessage());
                    session.setAttribute("messageType", "error");
                }
                response.sendRedirect("payments");
                return;
            } else if ("deletePayment".equals(action)) {
                
                String paymentIdStr = request.getParameter("paymentId");

                try {
                    
                    if (paymentService.processDeletePaymentMethod(paymentIdStr)) {
                        session.setAttribute("message", "Payment method deleted successfully.");
                        session.setAttribute("messageType", "success");
                    } else {
                        session.setAttribute("message", "Failed to delete payment method.");
                        session.setAttribute("messageType", "error");
                    }
                } catch (ServiceException e) {
                    session.setAttribute("message", e.getMessage());
                    session.setAttribute("messageType", "error");
                }

                response.sendRedirect("payments");
                return;
            }
        } catch (UserNotFoundException e) {
            session.setAttribute("message", "User not found.");
            session.setAttribute("messageType", "error");
            response.sendRedirect("login.jsp");
        } catch (ServiceException e) {
            session.setAttribute("message", "Error processing request: " + e.getMessage());
            session.setAttribute("messageType", "error");
            response.sendRedirect("payments");
        }

        response.sendRedirect("payments");
    }
}