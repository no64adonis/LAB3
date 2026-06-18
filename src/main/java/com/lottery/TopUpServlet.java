package com.lottery;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import com.lottery.model.Transaction;
import com.lottery.service.TransactionService;
import com.lottery.config.ServiceFactory;

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
import com.lottery.service.exception.InsufficientBalanceException;
import com.lottery.service.exception.ServiceException;
import com.lottery.service.exception.UserNotFoundException;
import com.lottery.util.InputValidator;
public class TopUpServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final int TRANSACTIONS_PER_PAGE = 10;
    private final com.lottery.config.ServiceFactory serviceFactory = com.lottery.config.ServiceFactory.getInstance();
    private PaymentService paymentService = serviceFactory.getPaymentService();
    private UserService userService = serviceFactory.getUserService();
    private TransactionService transactionService = serviceFactory.getTransactionService();

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

            request.setAttribute("user", currentUser);

            List<PaymentMethod> paymentMethods = paymentService.getUserPaymentMethods(currentUser.getUserID());
            request.setAttribute("paymentMethods", paymentMethods);

            String pageParam = request.getParameter("page");
            Integer pageObj = InputValidator.validateInteger(pageParam);
            int page = (pageObj != null && pageObj > 0) ? pageObj : 1;
            int offset = (page - 1) * TRANSACTIONS_PER_PAGE;

            List<Transaction> transactions = transactionService.getUserTransactions(
                    currentUser.getUserID(), offset, TRANSACTIONS_PER_PAGE);
            int totalTransactions = transactionService.getUserTransactionCount(currentUser.getUserID());
            int totalPages = (int) Math.ceil((double) totalTransactions / TRANSACTIONS_PER_PAGE);

            request.setAttribute("transactions", transactions);
            request.setAttribute("currentPage", page);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("totalTransactions", totalTransactions);

            request.getRequestDispatcher("/topup.jsp").forward(request, response);
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

            if (action != null && InputValidator.containsSQLInjectionPatterns(action)) {
                session.setAttribute("message", "Invalid request.");
                session.setAttribute("messageType", "error");
                response.sendRedirect("topup");
                return;
            }

            if ("topup".equals(action)) {
                
                String amountStr = request.getParameter("amount");
                String paymentMethodIdStr = request.getParameter("paymentMethod");

                try {
                    
                    Double amountObj = InputValidator.validateDouble(amountStr);

                    if (amountObj != null) {
                        
                        if (paymentService.processTopUpRequest(currentUser, amountStr, paymentMethodIdStr)) {
                            BigDecimal amount = new BigDecimal(amountStr);
                            session.setAttribute("message", "Successfully topped up $" + amount + " to your account.");
                            session.setAttribute("messageType", "success");
                        } else {
                            session.setAttribute("message", "Failed to update balance. Please try again.");
                            session.setAttribute("messageType", "error");
                        }
                    } else {
                        session.setAttribute("message", "Invalid amount specified.");
                        session.setAttribute("messageType", "error");
                    }
                } catch (ServiceException e) {
                    session.setAttribute("message", e.getMessage());
                    session.setAttribute("messageType", "error");
                }
                response.sendRedirect("topup");
                return;
            } else if ("topupCustom".equals(action)) {
                
                String amountStr = request.getParameter("amount");
                String paymentMethodIdStr = request.getParameter("paymentMethod");

                try {
                    
                    Double amountObj = InputValidator.validateDouble(amountStr);

                    if (amountObj != null) {
                        
                        if (paymentService.processTopUpRequest(currentUser, amountStr, paymentMethodIdStr)) {
                            BigDecimal amount = new BigDecimal(amountStr);
                            session.setAttribute("message", "Successfully topped up $" + amount + " to your account.");
                            session.setAttribute("messageType", "success");
                        } else {
                            session.setAttribute("message", "Failed to update balance. Please try again.");
                            session.setAttribute("messageType", "error");
                        }
                    } else {
                        session.setAttribute("message", "Invalid amount specified.");
                        session.setAttribute("messageType", "error");
                    }
                } catch (ServiceException e) {
                    session.setAttribute("message", e.getMessage());
                    session.setAttribute("messageType", "error");
                }
                response.sendRedirect("topup");
                return;
            }
        } catch (UserNotFoundException e) {
            session.setAttribute("message", "User not found.");
            session.setAttribute("messageType", "error");
            response.sendRedirect("login.jsp");
        } catch (ServiceException e) {
            session.setAttribute("message", "Error processing request: " + e.getMessage());
            session.setAttribute("messageType", "error");
            response.sendRedirect("topup");
        }

        response.sendRedirect("topup");
    }
}