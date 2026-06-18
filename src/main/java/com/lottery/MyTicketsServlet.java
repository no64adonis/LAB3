package com.lottery;

import com.lottery.model.LotteryTicket;
import com.lottery.model.Round;
import com.lottery.model.TicketHistorySummary;
import com.lottery.model.UserTicketHistory;
import com.lottery.model.User;
import com.lottery.config.ServiceFactory;
import com.lottery.db.RoundDAO;
import com.lottery.db.UserTicketHistoryDAO;
import com.lottery.service.SearchResult;
import com.lottery.service.TicketService;
import com.lottery.service.UserService;
import com.lottery.service.exception.ServiceException;
import com.lottery.service.exception.UserNotFoundException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
public class MyTicketsServlet extends HttpServlet {
    private static final int TICKETS_PER_PAGE = 10;
    private static final int HISTORY_PER_PAGE = 10;
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

            String pageParam = request.getParameter("page");

            SearchResult searchResult = ticketService.processUserTickets(user.getUserID(), pageParam);

            request.setAttribute("tickets", searchResult.getTickets());
            request.setAttribute("totalTickets", searchResult.getTotalTickets());
            request.setAttribute("currentPage", searchResult.getCurrentPage());
            request.setAttribute("totalPages", searchResult.getTotalPages());

            String historyPageParam = request.getParameter("historyPage");
            int historyPage = 1;
            if (historyPageParam != null && !historyPageParam.isEmpty()) {
                try {
                    historyPage = Integer.parseInt(historyPageParam);
                    if (historyPage < 1)
                        historyPage = 1;
                } catch (NumberFormatException e) {
                    historyPage = 1;
                }
            }

            int historyOffset = (historyPage - 1) * HISTORY_PER_PAGE;
            List<UserTicketHistory> purchaseHistory = UserTicketHistoryDAO.getAllHistoryForUser(
                    user.getUserID(), historyOffset, HISTORY_PER_PAGE);
            int totalHistoryCount = UserTicketHistoryDAO.countAllHistoryForUser(user.getUserID());
            int totalHistoryPages = (int) Math.ceil((double) totalHistoryCount / HISTORY_PER_PAGE);

            request.setAttribute("purchaseHistory", purchaseHistory);
            request.setAttribute("historyCurrentPage", historyPage);
            request.setAttribute("historyTotalPages", totalHistoryPages);
            request.setAttribute("totalHistoryCount", totalHistoryCount);

            String timePeriod = request.getParameter("timePeriod");
            if (timePeriod != null && !timePeriod.isEmpty()) {
                TicketHistorySummary timeSummary = UserTicketHistoryDAO.getFinancialSummaryByTimePeriod(
                        user.getUserID(), timePeriod);
                request.setAttribute("timeSummary", timeSummary);
                request.setAttribute("selectedTimePeriod", timePeriod);
            }

            List<Round> pastRounds = RoundDAO.getPastRoundsForUser(user.getUserID());
            request.setAttribute("pastRounds", pastRounds);

            String roundIdParam = request.getParameter("roundId");
            if (roundIdParam != null && !roundIdParam.trim().isEmpty()) {
                try {
                    int roundId = Integer.parseInt(roundIdParam);
                    List<UserTicketHistory> historyTickets = UserTicketHistoryDAO.getHistoryByRound(user.getUserID(),
                            roundId);
                    TicketHistorySummary summary = UserTicketHistoryDAO.getFinancialSummary(user.getUserID(), roundId);
                    request.setAttribute("historyTickets", historyTickets);
                    request.setAttribute("historySummary", summary);
                    request.setAttribute("selectedRoundId", roundId);

                    for (Round round : pastRounds) {
                        if (round.getRoundID() == roundId) {
                            request.setAttribute("selectedRound", round);
                            break;
                        }
                    }
                } catch (NumberFormatException e) {
                    request.setAttribute("historyTickets", new ArrayList<UserTicketHistory>());
                }
            }

            request.getRequestDispatcher("myTickets.jsp").forward(request, response);
        } catch (UserNotFoundException e) {
            session.setAttribute("error", "User not found.");
            response.sendRedirect("login.jsp");
        } catch (ServiceException e) {
            session.setAttribute("error", "Error retrieving tickets: " + e.getMessage());
            response.sendRedirect("welcome.jsp");
        }
    }
}
