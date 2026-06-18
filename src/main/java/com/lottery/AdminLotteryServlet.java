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

import com.lottery.db.LotteryTicketSearchCriteria;
import com.lottery.db.UserSearchHistoryDAO;
import com.lottery.model.LotteryTicket;
import com.lottery.model.User;
import com.lottery.service.AdminService;
import com.lottery.service.TicketService;
import com.lottery.service.UserService;
import com.lottery.service.exception.ServiceException;
import com.lottery.service.exception.TicketNotFoundException;
import com.lottery.service.exception.UserNotFoundException;
import com.lottery.util.InputValidator;
public class AdminLotteryServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final int TICKETS_PER_PAGE = 10;
    private final com.lottery.config.ServiceFactory serviceFactory = com.lottery.config.ServiceFactory.getInstance();
    private AdminService adminService = serviceFactory.getAdminService();
    private TicketService ticketService = serviceFactory.getTicketService();
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

            request.setAttribute("user", currentUser);

            String action = request.getParameter("action");
            String[] companies = request.getParameterValues("company");
            String num1 = request.getParameter("num1");
            String num2 = request.getParameter("num2");
            String num3 = request.getParameter("num3");
            String num4 = request.getParameter("num4");
            String num5 = request.getParameter("num5");
            String num6 = request.getParameter("num6");
            String numbers = null;
            String startDateStr = request.getParameter("startDate");
            String endDateStr = request.getParameter("endDate");
            String specificDateStr = request.getParameter("specificDate");
            String pageParam = request.getParameter("page");
            String fromHistory = request.getParameter("fromHistory");
            String searchIdStr = request.getParameter("searchId");

            String company = (companies != null) ? String.join(",", companies) : "";

            Integer pageObj = InputValidator.validateInteger(pageParam);
            int page = (pageObj != null && pageObj > 0) ? pageObj : 1;

            int offset = (page - 1) * TICKETS_PER_PAGE;

            List<LotteryTicket> tickets = new ArrayList<>();
            int totalTickets = 0;

            if (company != null && !company.isEmpty()) {
                
                String[] companyArray = company.split(",");
                StringBuilder validCompanies = new StringBuilder();
                for (String comp : companyArray) {
                    String trimmedComp = comp.trim();
                    if (InputValidator.isValidCompany(trimmedComp)) {
                        if (validCompanies.length() > 0) {
                            validCompanies.append(",");
                        }
                        validCompanies.append(trimmedComp);
                    }
                }
                company = validCompanies.toString();
            }

            String[] numParams = { num1, num2, num3, num4, num5, num6 };
            Integer[] parsedNums = new Integer[6];
            boolean numbersValid = true;

            for (int i = 0; i < numParams.length; i++) {
                if (numParams[i] != null && !numParams[i].isEmpty()) {
                    try {
                        parsedNums[i] = Integer.parseInt(numParams[i].trim());
                        if (!InputValidator.isValidLotteryNumber(parsedNums[i])) {
                            numbersValid = false;
                        }
                    } catch (NumberFormatException e) {
                        numbersValid = false;
                    }
                }
            }

            if (numbersValid && java.util.Arrays.stream(parsedNums).anyMatch(java.util.Objects::nonNull)) {
                StringBuilder numbersBuilder = new StringBuilder();
                for (Integer n : parsedNums) {
                    if (n != null) {
                        if (numbersBuilder.length() > 0) numbersBuilder.append(",");
                        numbersBuilder.append(n);
                    }
                }
                numbers = numbersBuilder.toString();
            }

            LocalDate startDate = null;
            LocalDate endDate = null;
            LocalDate specificDate = null;

            if (startDateStr != null && !startDateStr.isEmpty()) {
                if (InputValidator.isValidDate(startDateStr)) {
                    try {
                        startDate = LocalDate.parse(startDateStr);
                    } catch (Exception e) {
                        java.util.logging.Logger.getLogger(getClass().getName()).warning("Failed to parse startDate: " + startDateStr);
                    }
                }
            }

            if (endDateStr != null && !endDateStr.isEmpty()) {
                if (InputValidator.isValidDate(endDateStr)) {
                    try {
                        endDate = LocalDate.parse(endDateStr);
                    } catch (Exception e) {
                        java.util.logging.Logger.getLogger(getClass().getName()).warning("Failed to parse endDate: " + endDateStr);
                    }
                }
            }

            if (specificDateStr != null && !specificDateStr.isEmpty()) {
                if (InputValidator.isValidDate(specificDateStr)) {
                    try {
                        specificDate = LocalDate.parse(specificDateStr);
                    } catch (Exception e) {
                        java.util.logging.Logger.getLogger(getClass().getName()).warning("Failed to parse specificDate: " + specificDateStr);
                    }
                }
            }

            if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
                
                LocalDate temp = startDate;
                startDate = endDate;
                endDate = temp;
            }

            boolean isSearchFormSubmitted = request.getParameter("company") != null ||
                    request.getParameter("num1") != null ||
                    request.getParameter("startDate") != null ||
                    request.getParameter("endDate") != null ||
                    request.getParameter("specificDate") != null;

            boolean hasSearchParameters = (company != null && !company.isEmpty()) ||
                    (numbers != null && !numbers.isEmpty()) ||
                    (specificDate != null) ||
                    (startDate != null && endDate != null);

            if (isSearchFormSubmitted && !hasSearchParameters) {
                
                tickets = adminService.getAllTickets(offset, TICKETS_PER_PAGE);
                totalTickets = adminService.getTotalTicketCount();
            } else if (hasSearchParameters) {
                
                boolean hasDateRange = startDate != null && endDate != null;

                boolean hasSpecificDate = specificDate != null;

                LotteryTicketSearchCriteria criteria = LotteryTicketSearchCriteria.create()
                        .numbers(numbers)
                        .company(company)
                        .pagination(offset, TICKETS_PER_PAGE);

                if (hasDateRange) {
                    criteria.dateRange(startDate, endDate);
                } else if (hasSpecificDate) {
                    criteria.specificDate(specificDate);
                } else {
                    
                    criteria.clearDates();
                }

                tickets = adminService.searchAllTickets(criteria);
                totalTickets = adminService.getTicketCount(criteria);

                if (page == 1) {
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
                                UserSearchHistoryDAO.createSearchHistory(currentUser.getUserID(),
                                        searchPrompt.toString());
                            }
                        }
                    }
                }
            } else {
                
                tickets = adminService.getAllTickets(offset, TICKETS_PER_PAGE);
                totalTickets = adminService.getTotalTicketCount();
            }

            int totalPages = (int) Math.ceil((double) totalTickets / TICKETS_PER_PAGE);

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
            request.setAttribute("action", action);

            if ("clearHistory".equals(action)) {
                UserSearchHistoryDAO.clearUserSearchHistory(currentUser.getUserID());
                session.setAttribute("message", "Search history cleared successfully.");
                response.sendRedirect("adminLottery");
                return;
            }

            List<UserSearchHistoryDAO.SearchHistoryEntry> searchHistory = UserSearchHistoryDAO
                    .getUserSearchHistory(currentUser.getUserID(), 10);
            request.setAttribute("searchHistory", searchHistory);

            List<String> companyList = adminService.getAllCompanies();
            request.setAttribute("companies", companyList);

            request.getRequestDispatcher("/adminLottery.jsp").forward(request, response);
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
            
            User currentUser = userService.getUserByEmail(email);
            if (currentUser == null || !"admin".equals(currentUser.getRole())) {
                response.sendRedirect("welcome.jsp");
                return;
            }

            String action = request.getParameter("action");
            if (action == null) {
                response.sendRedirect("adminLottery");
                return;
            }

            switch (action) {
                
                case "create":
                    createTicket(request, response);
                    break;

                case "updateStatus":
                    updateStatus(request, response);
                    break;

                case "bulkUpdate":
                    handleBulkUpdate(request, response);
                    break;

                case "bulkInsert":
                    bulkInsert(request, response);
                    break;

                case "updatePrice":
                    handlePriceUpdate(request, response);
                    break;

                default:
                    response.sendRedirect("adminLottery");
                    break;
            }
        } catch (UserNotFoundException e) {
            session.setAttribute("error", "User not found.");
            response.sendRedirect("login.jsp");
        } catch (ServiceException e) {
            session.setAttribute("error", "Error processing request: " + e.getMessage());
            response.sendRedirect("adminLottery");
        }
    }

    private void createTicket(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            String ticketID = request.getParameter("ticketID");
            String num1 = request.getParameter("num1");
            String num2 = request.getParameter("num2");
            String num3 = request.getParameter("num3");
            String num4 = request.getParameter("num4");
            String num5 = request.getParameter("num5");
            String num6 = request.getParameter("num6");
            String company = request.getParameter("selectedCompany");
            String publishedStr = request.getParameter("published");

            LotteryTicket ticket = adminService.processTicketCreation(
                    ticketID, num1, num2, num3, num4, num5, num6,
                    company, publishedStr);

            request.getSession().setAttribute("message", "Lottery ticket created successfully.");

        } catch (ServiceException e) {
            request.getSession().setAttribute("error", e.getMessage());
        }

        response.sendRedirect("adminLottery" + getSearchParameters(request));
    }

    private void updateStatus(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            String ticketId = request.getParameter("ticketId");
            String publishedStr = request.getParameter("published");

            if (adminService.processTicketStatusUpdate(ticketId, publishedStr)) {
                request.getSession().setAttribute("message", "Ticket status updated successfully.");
            } else {
                request.getSession().setAttribute("error", "Failed to update ticket status.");
            }
        } catch (TicketNotFoundException e) {
            request.getSession().setAttribute("error", "Ticket not found.");
        } catch (ServiceException e) {
            request.getSession().setAttribute("error", e.getMessage());
        }

        response.sendRedirect("adminLottery" + getSearchParameters(request));
    }

    private void handleBulkUpdate(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            String selectedTicketsStr = request.getParameter("selectedTickets");
            String type = request.getParameter("type");
            String value = request.getParameter("value");

            if ("ALL".equals(selectedTicketsStr)) {
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

                String[] bulkNums = { num1, num2, num3, num4, num5, num6 };
                StringBuilder nb = new StringBuilder();
                for (String n : bulkNums) {
                    if (n != null && !n.trim().isEmpty()) {
                        if (nb.length() > 0) nb.append(",");
                        nb.append(n.trim());
                    }
                }
                String numbers = nb.length() > 0 ? nb.toString() : null;

                com.lottery.db.LotteryTicketSearchCriteria criteria = com.lottery.db.LotteryTicketSearchCriteria
                        .create()
                        .numbers(numbers)
                        .pagination(0, Integer.MAX_VALUE);

                if (company != null && !company.trim().isEmpty() && !company.trim().equalsIgnoreCase("All Companies")
                        && !company.trim().equalsIgnoreCase("All")) {
                    criteria.company(company.trim());
                }

                if (startDateStr != null && !startDateStr.trim().isEmpty() && endDateStr != null
                        && !endDateStr.trim().isEmpty()) {
                    try {
                        criteria.dateRange(java.time.LocalDate.parse(startDateStr.trim()),
                                java.time.LocalDate.parse(endDateStr.trim()));
                    } catch (Exception e) {
                    }
                } else if (specificDateStr != null && !specificDateStr.trim().isEmpty()) {
                    try {
                        criteria.specificDate(java.time.LocalDate.parse(specificDateStr.trim()));
                    } catch (Exception e) {
                    }
                } else {
                    criteria.clearDates();
                }

                java.util.List<com.lottery.model.LotteryTicket> allTickets = adminService.searchAllTickets(criteria);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < allTickets.size(); i++) {
                    sb.append(allTickets.get(i).getTicketID());
                    if (i < allTickets.size() - 1)
                        sb.append(",");
                }
                selectedTicketsStr = sb.toString();
            }

            if (selectedTicketsStr == null || selectedTicketsStr.trim().isEmpty()) {
                request.getSession().setAttribute("error", "No tickets selected.");
            } else if (type == null || value == null) {
                request.getSession().setAttribute("error", "Invalid bulk action parameters.");
            } else {
                boolean success = false;
                String actionLabel = "";

                if ("publish".equals(type)) {
                    success = adminService.processBulkTicketStatusUpdate(selectedTicketsStr, value);
                    actionLabel = "publication status";
                }

                if (success) {
                    request.getSession().setAttribute("message", "Bulk update of " + actionLabel + " successful!");
                } else {
                    request.getSession().setAttribute("error", "Failed to perform bulk update of " + actionLabel + ".");
                }
            }
        } catch (ServiceException e) {
            request.getSession().setAttribute("error", e.getMessage());
        }

        response.sendRedirect("adminLottery" + getSearchParameters(request));
    }

    private void bulkInsert(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String csvData = request.getParameter("csvData");
            java.util.Map<String, Object> results = adminService.processBulkTicketInsertion(csvData);

            int successCount = (int) results.get("successCount");
            java.util.List<String> duplicates = (java.util.List<String>) results.get("duplicates");
            java.util.List<String> errors = (java.util.List<String>) results.get("errors");

            StringBuilder message = new StringBuilder();
            message.append("Bulk insertion completed. Successfully inserted: ").append(successCount)
                    .append(" tickets.");

            if (!duplicates.isEmpty()) {
                message.append(" Found ").append(duplicates.size()).append(" duplicate IDs: ")
                        .append(String.join(", ", duplicates)).append(".");
            }

            if (!errors.isEmpty()) {
                message.append(" Errors encountered: ").append(String.join(" | ", errors)).append(".");
            }

            request.getSession().setAttribute("message", message.toString());

        } catch (ServiceException e) {
            request.getSession().setAttribute("error", e.getMessage());
        }

        response.sendRedirect("adminLottery" + getSearchParameters(request));
    }

    private void handlePriceUpdate(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String selectedCompaniesStr = request.getParameter("selectedCompaniesPrice");
            String newPriceStr = request.getParameter("newPrice");

            if (selectedCompaniesStr == null || selectedCompaniesStr.isEmpty()) {
                request.getSession().setAttribute("error", "Please select at least one company.");
            } else if (newPriceStr == null || newPriceStr.isEmpty()) {
                request.getSession().setAttribute("error", "Please enter a valid price.");
            } else {
                
                String[] companies = selectedCompaniesStr.split(",");
                java.util.Map<String, String> priceParams = new java.util.HashMap<>();

                for (String company : companies) {
                    if (InputValidator.isValidCompany(company.trim())) {
                        priceParams.put(company.trim(), newPriceStr);
                    }
                }

                if (priceParams.isEmpty()) {
                    request.getSession().setAttribute("error", "No valid companies selected.");
                } else {
                    if (adminService.processPriceUpdates(priceParams)) {
                        request.getSession().setAttribute("message",
                                "Prices updated successfully for " + priceParams.size() + " companies.");
                    } else {
                        request.getSession().setAttribute("error", "Failed to update prices.");
                    }
                }
            }
        } catch (ServiceException e) {
            request.getSession().setAttribute("error", e.getMessage());
        }

        response.sendRedirect("adminLottery" + getSearchParameters(request));
    }

    private String getSearchParameters(HttpServletRequest request) {
        StringBuilder params = new StringBuilder();
        String company = request.getParameter("company");
        String num1 = request.getParameter("num1");
        String num2 = request.getParameter("num2");
        String num3 = request.getParameter("num3");
        String num4 = request.getParameter("num4");
        String num5 = request.getParameter("num5");
        String num6 = request.getParameter("num6");
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");
        String specificDate = request.getParameter("specificDate");
        String page = request.getParameter("page");

        if (company != null && !company.isEmpty())
            params.append("&company=")
                    .append(java.net.URLEncoder.encode(company, java.nio.charset.StandardCharsets.UTF_8));
        if (num1 != null && !num1.isEmpty())
            params.append("&num1=").append(num1);
        if (num2 != null && !num2.isEmpty())
            params.append("&num2=").append(num2);
        if (num3 != null && !num3.isEmpty())
            params.append("&num3=").append(num3);
        if (num4 != null && !num4.isEmpty())
            params.append("&num4=").append(num4);
        if (num5 != null && !num5.isEmpty())
            params.append("&num5=").append(num5);
        if (num6 != null && !num6.isEmpty())
            params.append("&num6=").append(num6);
        if (startDate != null && !startDate.isEmpty())
            params.append("&startDate=").append(startDate);
        if (endDate != null && !endDate.isEmpty())
            params.append("&endDate=").append(endDate);
        if (specificDate != null && !specificDate.isEmpty())
            params.append("&specificDate=").append(specificDate);

        if (page != null && !page.isEmpty())
            params.append("&page=").append(page);

        String result = params.toString();
        if (result.isEmpty()) {
            return "";
        }
        return "?" + (result.startsWith("&") ? result.substring(1) : result) + "&action=search";
    }
}
