package com.lottery;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.lottery.config.ServiceFactory;
import com.lottery.db.RoundDAO;
import com.lottery.db.UserSearchHistoryDAO;
import com.lottery.model.LotteryTicket;
import com.lottery.model.Round;
import com.lottery.model.User;
import com.lottery.service.SearchResult;
import com.lottery.service.TicketService;
import com.lottery.service.UserService;
import com.lottery.service.exception.ServiceException;
import com.lottery.service.exception.UserNotFoundException;
import com.lottery.util.InputValidator;
public class UserLotteryServlet extends HttpServlet {
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

            SearchResult searchResult = ticketService.processUserLotterySearch(
                    companyParams, num1, num2, num3, num4, num5, num6,
                    startDateStr, endDateStr, specificDateStr, pageParam);

            List<LotteryTicket> tickets = searchResult.getTickets();
            int page = searchResult.getCurrentPage();
            int totalPages = searchResult.getTotalPages();
            int totalTickets = searchResult.getTotalTickets();
            String company = searchResult.getCompany();

            if (tickets != null && !tickets.isEmpty()) {
                List<String> ticketIds = new ArrayList<>();
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
                            UserSearchHistoryDAO.createSearchHistory(currentUser.getUserID(), searchPrompt.toString());
                        }
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

            if ("clearHistory".equals(action)) {
                UserSearchHistoryDAO.clearUserSearchHistory(currentUser.getUserID());
                session.setAttribute("message", "Search history cleared successfully.");
                response.sendRedirect("userLottery");
                return;
            }

            List<UserSearchHistoryDAO.SearchHistoryEntry> searchHistory = UserSearchHistoryDAO
                    .getUserSearchHistory(currentUser.getUserID(), 10);
            request.setAttribute("searchHistory", searchHistory);

            List<String> companies = ticketService.getAllPublishedCompanies();
            request.setAttribute("companies", companies);

            List<Round> allPastRounds = RoundDAO.getAllPastRounds();
            int roundsPerPage = 10;
            String roundsPageParam = request.getParameter("roundsPage");
            int roundsPage = 1;
            if (roundsPageParam != null && !roundsPageParam.isEmpty()) {
                try {
                    roundsPage = Integer.parseInt(roundsPageParam);
                    if (roundsPage < 1)
                        roundsPage = 1;
                } catch (NumberFormatException e) {
                    roundsPage = 1;
                }
            }
            int totalRounds = allPastRounds.size();
            int roundsTotalPages = (int) Math.ceil((double) totalRounds / roundsPerPage);
            if (roundsPage > roundsTotalPages && roundsTotalPages > 0)
                roundsPage = roundsTotalPages;
            int roundsOffset = (roundsPage - 1) * roundsPerPage;
            int roundsEnd = Math.min(roundsOffset + roundsPerPage, totalRounds);
            List<Round> paginatedRounds = (roundsOffset < totalRounds)
                    ? allPastRounds.subList(roundsOffset, roundsEnd)
                    : new java.util.ArrayList<>();

            request.setAttribute("pastRounds", paginatedRounds);
            request.setAttribute("roundsCurrentPage", roundsPage);
            request.setAttribute("roundsTotalPages", roundsTotalPages);

            request.getRequestDispatcher("/userLottery.jsp").forward(request, response);
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
        
        doGet(request, response);
    }
}
