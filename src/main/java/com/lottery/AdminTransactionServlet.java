package com.lottery;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.lottery.config.ServiceFactory;
import com.lottery.model.Transaction;
import com.lottery.model.User;
import com.lottery.service.TransactionService;
import com.lottery.service.UserService;
import com.lottery.service.exception.ServiceException;
import com.lottery.service.exception.UserNotFoundException;
import com.lottery.util.InputValidator;
public class AdminTransactionServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final int TRANSACTIONS_PER_PAGE = 10;
    private final ServiceFactory serviceFactory = ServiceFactory.getInstance();
    private TransactionService transactionService;
    private UserService userService;
    public void init() throws ServletException {
        super.init();
        transactionService = serviceFactory.getTransactionService();
        userService = serviceFactory.getUserService();
    }

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

            String searchEmail = request.getParameter("email");
            String startDateStr = request.getParameter("startDate");
            String endDateStr = request.getParameter("endDate");
            String pageParam = request.getParameter("page");

            if (searchEmail != null) {
                searchEmail = InputValidator.sanitizeString(searchEmail);
                if (InputValidator.containsSQLInjectionPatterns(searchEmail)) {
                    searchEmail = "";
                }
            }
            if (startDateStr != null && !startDateStr.isEmpty() && !InputValidator.isValidDate(startDateStr)) {
                startDateStr = null;
            }
            if (endDateStr != null && !endDateStr.isEmpty() && !InputValidator.isValidDate(endDateStr)) {
                endDateStr = null;
            }

            Integer pageObj = InputValidator.validateInteger(pageParam);
            int page = (pageObj != null && pageObj > 0) ? pageObj : 1;
            int offset = (page - 1) * TRANSACTIONS_PER_PAGE;

            List<Transaction> transactions;
            int totalTransactions;

            boolean hasSearchParams = (searchEmail != null && !searchEmail.trim().isEmpty()) ||
                    (startDateStr != null && !startDateStr.trim().isEmpty()) ||
                    (endDateStr != null && !endDateStr.trim().isEmpty());

            if (hasSearchParams) {
                transactions = transactionService.searchTransactions(
                        searchEmail, startDateStr, endDateStr, offset, TRANSACTIONS_PER_PAGE);
                totalTransactions = transactionService.searchTransactionCount(
                        searchEmail, startDateStr, endDateStr);
            } else {
                transactions = transactionService.getAllTransactions(offset, TRANSACTIONS_PER_PAGE);
                totalTransactions = transactionService.getAllTransactionCount();
            }

            int totalPages = (int) Math.ceil((double) totalTransactions / TRANSACTIONS_PER_PAGE);

            request.setAttribute("transactions", transactions);
            request.setAttribute("currentPage", page);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("totalTransactions", totalTransactions);
            request.setAttribute("searchEmail", searchEmail);
            request.setAttribute("startDate", startDateStr);
            request.setAttribute("endDate", endDateStr);

            request.getRequestDispatcher("adminTransactions.jsp").forward(request, response);

        } catch (UserNotFoundException e) {
            session.setAttribute("error", "User not found.");
            response.sendRedirect("login.jsp");
        } catch (ServiceException e) {
            session.setAttribute("error", "Error retrieving transactions: " + e.getMessage());
            response.sendRedirect("welcome.jsp");
        }
    }
}
