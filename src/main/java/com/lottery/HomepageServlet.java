package com.lottery;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.lottery.config.ServiceFactory;
import com.lottery.db.UserSearchHistoryDAO;
import com.lottery.model.LotteryTicket;
import com.lottery.model.User;
import com.lottery.service.TicketService;
import com.lottery.service.TicketServiceImpl;
import com.lottery.service.UserService;
import com.lottery.service.exception.ServiceException;
import com.lottery.service.exception.UserNotFoundException;
import com.lottery.util.InputValidator;

public class HomepageServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final int TICKETS_PER_PAGE = 10;
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

        try {
            
            List<String> companies = ticketService.getAllPublishedCompanies();
            request.setAttribute("companies", companies);

            List<String> allCompanies = ticketService.getAllDistinctCompanies();
            request.setAttribute("allCompanies", allCompanies);
        } catch (ServiceException e) {
            
            request.setAttribute("companies", java.util.Collections.emptyList());
            request.setAttribute("allCompanies", java.util.Collections.emptyList());
        }

        String[] companyParams = request.getParameterValues("company");
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
        String fromHistory = request.getParameter("fromHistory");
        String searchIdStr = request.getParameter("searchId");
        String action = request.getParameter("action");

        HttpSession session = request.getSession(false);
        String email = (session != null) ? (String) session.getAttribute("email") : null;
        User currentUser = null;
        if (email != null) {
            try {
                currentUser = userService.getUserByEmail(email);
                request.setAttribute("user", currentUser);
            } catch (ServiceException e) {
                currentUser = null;
            }
        }

        if ("clearHistory".equals(action)) {
            if (currentUser != null) {
                UserSearchHistoryDAO.clearUserSearchHistory(currentUser.getUserID());
            } else if (session != null) {
                session.removeAttribute("guestSearchHistory");
            }

            if (session != null) {
                session.setAttribute("message", "Search history cleared successfully.");
            }
            response.sendRedirect("homepage");
            return;
        }

        try {
            
            com.lottery.service.SearchResult searchResult = ticketService.processUserLotterySearch(
                    companyParams, num1, num2, num3, num4, num5, num6,
                    startDateStr, endDateStr, specificDateStr, pageParam);

            List<LotteryTicket> tickets = searchResult.getTickets();
            int page = searchResult.getCurrentPage();
            int totalPages = searchResult.getTotalPages();
            int totalTickets = searchResult.getTotalTickets();
            String company = searchResult.getCompany();

            if (tickets != null && !tickets.isEmpty()) {
                java.util.List<String> ticketIds = new java.util.ArrayList<>();
                for (LotteryTicket ticket : tickets) {
                    if (ticket != null && ticket.getTicketID() != null) {
                        ticketIds.add(ticket.getTicketID());
                    }
                }
                if (!ticketIds.isEmpty()) {
                    try {
                        ticketService.incrementViewCountBatch(ticketIds);
                    } catch (ServiceException e) {
                        
                    }
                }
            }

            if (page == 1) {
                com.lottery.util.LotteryNumberParser.ParseResult parseResult = com.lottery.util.LotteryNumberParser
                        .parse(num1, num2, num3, num4, num5, num6);
                String numbers = parseResult.toCsvString();
                if (numbers.isEmpty()) {
                    numbers = null;
                }

                LocalDate startDate = null;
                LocalDate endDate = null;
                LocalDate specificDate = null;

                if (startDateStr != null && !startDateStr.isEmpty()) {
                    startDate = com.lottery.util.InputValidator.parseDate(startDateStr).orElse(null);
                }

                if (endDateStr != null && !endDateStr.isEmpty()) {
                    endDate = com.lottery.util.InputValidator.parseDate(endDateStr).orElse(null);
                }

                if (specificDateStr != null && !specificDateStr.isEmpty()) {
                    specificDate = com.lottery.util.InputValidator.parseDate(specificDateStr).orElse(null);
                }

                boolean hasDateRange = startDate != null && endDate != null;
                boolean hasSpecificDate = specificDate != null;

                StringBuilder searchPrompt = new StringBuilder();
                if (company != null && !company.isEmpty()) {
                    searchPrompt.append("Company: ").append(company);
                }
                if (numbers != null && !numbers.isEmpty()) {
                    if (searchPrompt.length() > 0)
                        searchPrompt.append(", ");
                    searchPrompt.append("Numbers: ").append(numbers);
                }
                if (hasDateRange) {
                    if (searchPrompt.length() > 0)
                        searchPrompt.append(", ");
                    searchPrompt.append("Date Range: ").append(startDateStr).append(" to ").append(endDateStr);
                } else if (hasSpecificDate) {
                    if (searchPrompt.length() > 0)
                        searchPrompt.append(", ");
                    searchPrompt.append("Date: ").append(specificDateStr);
                }

                if (searchPrompt.length() > 0) {
                    if (currentUser != null) {
                        if ("true".equals(fromHistory) && searchIdStr != null && !searchIdStr.isEmpty()) {
                            try {
                                int searchId = Integer.parseInt(searchIdStr);
                                UserSearchHistoryDAO.updateSearchHistoryDate(searchId);
                            } catch (NumberFormatException e) {
                                if (!UserSearchHistoryDAO.searchPromptExists(currentUser.getUserID(),
                                        searchPrompt.toString())) {
                                    UserSearchHistoryDAO.createSearchHistory(currentUser.getUserID(),
                                            searchPrompt.toString());
                                }
                            }
                        } else {
                            if (!UserSearchHistoryDAO.searchPromptExists(currentUser.getUserID(),
                                    searchPrompt.toString())) {
                                UserSearchHistoryDAO.createSearchHistory(currentUser.getUserID(),
                                        searchPrompt.toString());
                            }
                        }
                    } else if (session != null) {
                        
                        List<UserSearchHistoryDAO.SearchHistoryEntry> guestHistory = (List<UserSearchHistoryDAO.SearchHistoryEntry>) session
                                .getAttribute("guestSearchHistory");
                        if (guestHistory == null) {
                            guestHistory = new java.util.ArrayList<>();
                        }

                        String prompt = searchPrompt.toString();
                        
                        guestHistory.removeIf(e -> e.getSearchPrompt().equals(prompt));

                        UserSearchHistoryDAO.SearchHistoryEntry entry = new UserSearchHistoryDAO.SearchHistoryEntry();
                        entry.setSearchId(guestHistory.size() + 1); 
                        entry.setSearchPrompt(prompt);
                        entry.setSearchDate(java.time.LocalDateTime.now());

                        guestHistory.add(0, entry);
                        if (guestHistory.size() > 10) {
                            guestHistory.remove(guestHistory.size() - 1);
                        }
                        session.setAttribute("guestSearchHistory", guestHistory);
                    }
                }
            }

            request.setAttribute("tickets", tickets);
            request.setAttribute("currentPage", page);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("totalTickets", totalTickets);
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

            if (currentUser != null) {
                List<UserSearchHistoryDAO.SearchHistoryEntry> searchHistory = UserSearchHistoryDAO
                        .getUserSearchHistory(currentUser.getUserID(), 10);
                request.setAttribute("searchHistory", searchHistory);
            } else if (session != null) {
                List<UserSearchHistoryDAO.SearchHistoryEntry> guestHistory = (List<UserSearchHistoryDAO.SearchHistoryEntry>) session
                        .getAttribute("guestSearchHistory");
                request.setAttribute("searchHistory", guestHistory);
            }

        } catch (ServiceException e) {
            
            request.setAttribute("tickets", java.util.Collections.emptyList());
            request.setAttribute("currentPage", 1);
            request.setAttribute("totalPages", 0);
            request.setAttribute("totalTickets", 0);
        }

        request.getRequestDispatcher("/index.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        doGet(request, response);
    }
}
