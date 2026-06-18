package com.lottery;

import com.lottery.config.ServiceFactory;
import com.lottery.model.LotteryTicket;
import com.lottery.model.User;
import com.lottery.service.SearchResult;
import com.lottery.service.TicketService;
import com.lottery.service.UserService;
import com.lottery.service.exception.InsufficientBalanceException;
import com.lottery.service.exception.ServiceException;
import com.lottery.service.exception.TicketNotFoundException;
import com.lottery.service.exception.UserNotFoundException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
public class TicketPurchaseServlet extends HttpServlet {
    private final ServiceFactory serviceFactory = ServiceFactory.getInstance();
    private TicketService ticketService;
    private UserService userService;
    public void init() throws ServletException {
        super.init();
        ticketService = serviceFactory.getTicketService();
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
            User user = userService.getUserByEmail(email);
            if (user == null) {
                response.sendRedirect("login.jsp");
                return;
            }
            
            request.setAttribute("user", user);

            String company = request.getParameter("company");
            String num1 = request.getParameter("num1");
            String num2 = request.getParameter("num2");
            String num3 = request.getParameter("num3");
            String num4 = request.getParameter("num4");
            String num5 = request.getParameter("num5");
            String num6 = request.getParameter("num6");
            String startDateStr = request.getParameter("startDate");
            String endDateStr = request.getParameter("endDate");
            String specificDateStr = request.getParameter("specificDate");
            String pageParam = request.getParameter("page");

            boolean isSearchRequest = company != null || num1 != null || num2 != null || num3 != null || num4 != null
                    || num5 != null || num6 != null || startDateStr != null || endDateStr != null
                    || specificDateStr != null || pageParam != null;

            if (isSearchRequest) {
                SearchResult result = ticketService.searchTicketsForPurchase(
                        company, num1, num2, num3, num4, num5, num6, startDateStr, endDateStr, specificDateStr,
                        pageParam);
                request.setAttribute("tickets", result.getTickets());
                request.setAttribute("totalPages", result.getTotalPages());
                request.setAttribute("currentPage", result.getCurrentPage());
                request.setAttribute("searchPerformed", true);
            }

            List<String> companies = ticketService.getAllPublishedCompanies();
            request.setAttribute("companies", companies);

            request.getRequestDispatcher("ticketPurchase.jsp").forward(request, response);
        } catch (UserNotFoundException e) {
            session.setAttribute("error", "User not found.");
            response.sendRedirect("login.jsp");
        } catch (ServiceException e) {
            session.setAttribute("error", "Error retrieving data: " + e.getMessage());
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
            User user = userService.getUserByEmail(email);
            if (user == null) {
                response.sendRedirect("login.jsp");
                return;
            }
            
            request.setAttribute("user", user);

            String ticketId = request.getParameter("ticketId");
            String ticketIds = request.getParameter("ticketIds");
            String action = request.getParameter("action");
            String company = request.getParameter("company");
            String num1 = request.getParameter("num1");
            String num2 = request.getParameter("num2");
            String num3 = request.getParameter("num3");
            String num4 = request.getParameter("num4");
            String num5 = request.getParameter("num5");
            String num6 = request.getParameter("num6");
            String startDateStr = request.getParameter("startDate");
            String endDateStr = request.getParameter("endDate");
            String specificDateStr = request.getParameter("specificDate");
            String pageParam = request.getParameter("page");

            String errorMessage = null;
            String successMessage = null;
            LotteryTicket purchasedTicket = null;
            List<String> purchasedTicketIds = null;

            try {
                if ("bulkPurchase".equals(action) && ticketIds != null && !ticketIds.isEmpty()) {
                    
                    java.util.List<String> ticketIdList = java.util.Arrays.asList(ticketIds.split(","));
                    purchasedTicketIds = ticketService.purchaseMultipleTickets(ticketIdList, user);

                    if (purchasedTicketIds != null && !purchasedTicketIds.isEmpty()) {
                        successMessage = "Successfully purchased " + purchasedTicketIds.size() + " ticket(s)! ";

                        double totalPrice = 0;
                        for (String id : purchasedTicketIds) {
                            try {
                                LotteryTicket ticket = ticketService.getTicketById(id);
                                totalPrice += ticket.getPrice();
                            } catch (TicketNotFoundException e) {
                                
                            }
                        }
                        user.setBalance(user.getBalance().subtract(BigDecimal.valueOf(totalPrice)));
                        session.setAttribute("user", user);
                    } else {
                        errorMessage = "No tickets were purchased. They may no longer be available.";
                    }
                } else {
                    if (ticketId == null || ticketId.isEmpty()) {
                        throw new ServiceException("Ticket ID is required for purchase.");
                    }

                    LotteryTicket ticket = ticketService.getTicketById(ticketId);

                    if (!ticket.isPublished()) {
                        throw new ServiceException("Only published tickets can be purchased.");
                    }

                    if (ticket.getOwnerId() != null && !ticket.getOwnerId().isEmpty()) {
                        throw new TicketNotFoundException("Ticket is no longer available.");
                    }

                    if (ticketService.purchaseTicket(ticket, user)) {
                        purchasedTicket = ticket;
                    } else {
                        throw new ServiceException("Failed to purchase ticket. Please try again.");
                    }

                    successMessage = "Ticket purchased successfully for $" + purchasedTicket.getPrice() + "!";

                    user.setBalance(user.getBalance().subtract(BigDecimal.valueOf(purchasedTicket.getPrice())));
                    session.setAttribute("user", user);
                }

            } catch (TicketNotFoundException e) {
                errorMessage = "Ticket is no longer available.";
            } catch (InsufficientBalanceException e) {
                errorMessage = "Insufficient balance to purchase this ticket. Price: $" + e.getTicketPrice();
            } catch (ServiceException e) {
                errorMessage = e.getMessage();
            }

            if (errorMessage != null) {
                request.setAttribute("errorMessage", errorMessage);
            }
            if (successMessage != null) {
                request.setAttribute("successMessage", successMessage);
                if (purchasedTicket != null) {
                    request.setAttribute("purchasedTicket", purchasedTicket);
                }
            }

            boolean isSearchRequest = company != null || num1 != null || num2 != null || num3 != null || num4 != null
                    || num5 != null || num6 != null || startDateStr != null || endDateStr != null
                    || specificDateStr != null || pageParam != null;

            if (isSearchRequest) {
                SearchResult result = ticketService.searchTicketsForPurchase(
                        company, num1, num2, num3, num4, num5, num6, startDateStr, endDateStr, specificDateStr,
                        pageParam);
                request.setAttribute("tickets", result.getTickets());
                request.setAttribute("totalPages", result.getTotalPages());
                request.setAttribute("currentPage", result.getCurrentPage());
                request.setAttribute("searchPerformed", true);
            }

            List<String> companies = ticketService.getAllPublishedCompanies();
            request.setAttribute("companies", companies);

            request.setAttribute("company", company);
            request.setAttribute("num1", num1);
            request.setAttribute("num2", num2);
            request.setAttribute("num3", num3);
            request.setAttribute("num4", num4);
            request.setAttribute("num5", num5);
            request.setAttribute("num6", num6);
            request.setAttribute("startDate", startDateStr);
            request.setAttribute("endDate", endDateStr);
            request.setAttribute("specificDate", specificDateStr);

            request.getRequestDispatcher("ticketPurchase.jsp").forward(request, response);
        } catch (UserNotFoundException e) {
            session.setAttribute("error", "User not found.");
            response.sendRedirect("login.jsp");
        } catch (ServiceException e) {
            session.setAttribute("error", "Error processing request: " + e.getMessage());
            response.sendRedirect("ticketPurchase");
        }
    }
}
