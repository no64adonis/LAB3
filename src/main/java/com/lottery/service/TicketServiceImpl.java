package com.lottery.service;

import com.lottery.db.LotteryTicketDAO;
import com.lottery.db.LotteryTicketSearchCriteria;
import com.lottery.model.LotteryTicket;
import com.lottery.model.User;
import com.lottery.service.exception.InsufficientBalanceException;
import com.lottery.service.exception.ServiceException;
import com.lottery.service.exception.TicketNotFoundException;
import com.lottery.service.exception.UserNotFoundException;
import com.lottery.util.InputValidator;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.security.SecureRandom;
import java.sql.Connection;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.Collections;
import com.lottery.config.AppConfig;

public class TicketServiceImpl extends BaseService implements TicketService {
    private static final Logger logger = Logger.getLogger(TicketServiceImpl.class.getName());
    public List<LotteryTicket> searchAvailableTickets(String company, LocalDate specificDate, LocalDate startDate,
            LocalDate endDate, int offset, int limit) throws ServiceException {
        logInfo("searchAvailableTickets", "Searching available tickets");

        if (offset < 0 || limit <= 0 || limit > AppConfig.MAX_PAGE_SIZE) {
            logWarning("searchAvailableTickets", "Invalid pagination parameters: offset=" + offset + ", limit=" + limit);
            throw new ServiceException("Invalid pagination parameters");
        }

        if (company != null && !company.isEmpty()) {
            String[] companies = company.split(",");
            for (String comp : companies) {
                String trimmedComp = comp.trim();
                if (!trimmedComp.isEmpty() && !InputValidator.isValidCompany(trimmedComp)) {
                    logWarning("searchAvailableTickets", "Invalid company format: " + trimmedComp);
                    throw new ServiceException("Invalid company format");
                }
            }
        }

        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            logWarning("searchAvailableTickets", "Start date cannot be after end date");
            throw new ServiceException("Start date cannot be after end date");
        }

        try {
            LotteryTicketSearchCriteria criteria = LotteryTicketSearchCriteria.create()
                    .company(company)
                    .published(true)
                    .pagination(offset, limit);

            if (specificDate != null) {
                criteria.specificDate(specificDate);
            } else if (startDate != null || endDate != null) {
                LocalDate start = startDate != null ? startDate : LocalDate.now().minusDays(30);
                LocalDate end = endDate != null ? endDate : LocalDate.now();
                criteria.dateRange(start, end);
            }

            List<LotteryTicket> availableTickets = LotteryTicketDAO.searchPublishedTickets(criteria);

            if (availableTickets != null) {
                availableTickets.removeIf(ticket -> ticket.getOwnerId() != null);
            }

            return availableTickets;
        } catch (RuntimeException e) {
            logSevere("searchAvailableTickets", "Error searching available tickets: " + e.getMessage());
            throw new ServiceException("Error searching available tickets: " + e.getMessage(), e);
        }
    }
    public LotteryTicket selectTicket(List<LotteryTicket> availableTickets, String numbers)
            throws TicketNotFoundException, ServiceException {
        logInfo("selectTicket", "Selecting ticket from available tickets");

        if (numbers != null && !numbers.isEmpty() && !InputValidator.isValidNumbers(numbers)) {
            logWarning("selectTicket", "Invalid numbers format: " + numbers);
            throw new ServiceException("Invalid numbers format");
        }

        try {
            if (availableTickets == null || availableTickets.isEmpty()) {
                logWarning("selectTicket", "No available tickets");
                throw new TicketNotFoundException("No available tickets");
            }

            LotteryTicket selectedTicket = null;

            if (numbers != null && !numbers.isEmpty()) {
                for (LotteryTicket ticket : availableTickets) {
                    if (ticket.getNumbers().equals(numbers)) {
                        selectedTicket = ticket;
                        break;
                    }
                }
            }

            if (selectedTicket == null) {
                selectedTicket = availableTickets.get(0);
            }

            if (selectedTicket.getOwnerId() != null) {
                logWarning("selectTicket", "Selected ticket has already been purchased: " + selectedTicket.getTicketID());
                throw new TicketNotFoundException("Selected ticket has already been purchased");
            }

            return selectedTicket;
        } catch (TicketNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            logSevere("selectTicket", "Error selecting ticket: " + e.getMessage());
            throw new ServiceException("Error selecting ticket: " + e.getMessage(), e);
        }
    }
    public boolean purchaseTicket(LotteryTicket ticket, User user)
            throws InsufficientBalanceException, ServiceException {
        logInfo("purchaseTicket", "Purchasing ticket ID: " + ticket.getTicketID() + " for user ID: " + user.getUserID());

        if (ticket == null) {
            logWarning("purchaseTicket", "Null ticket provided");
            throw new ServiceException("Ticket is required");
        }

        if (user == null) {
            logWarning("purchaseTicket", "Null user provided");
            throw new ServiceException("User is required");
        }

        if (ticket.getTicketID() == null || ticket.getTicketID().trim().isEmpty()) {
            logWarning("purchaseTicket", "Invalid ticket ID: " + ticket.getTicketID());
            throw new ServiceException("Ticket ID is required");
        }

        if (!InputValidator.isValidTicketId(ticket.getTicketID())) {
            logWarning("purchaseTicket", "Invalid ticket ID format: " + ticket.getTicketID());
            throw new ServiceException("Invalid ticket ID format");
        }

        Connection conn = null;
        try {
            conn = com.lottery.db.DatabaseConfig.getConnection();
            conn.setAutoCommit(false);

            double ticketPrice = ticket.getPrice();
            boolean balanceUpdated = com.lottery.db.UserDAO.updateUserBalance(conn, user.getUserID(),
                    BigDecimal.valueOf(-ticketPrice));

            if (!balanceUpdated) {
                conn.rollback();
                logWarning("purchaseTicket", "Insufficient balance for user ID: " + user.getUserID() +
                        ", ticket price: " + ticketPrice);
                throw new InsufficientBalanceException(user.getBalance(), BigDecimal.valueOf(ticketPrice));
            }

            boolean ticketUpdated = LotteryTicketDAO.updateTicketOwner(conn,
                    ticket.getTicketID(), String.valueOf(user.getUserID()));

            if (!ticketUpdated) {
                conn.rollback();
                logWarning("purchaseTicket", "Failed to update ticket owner");
                throw new ServiceException("Failed to purchase ticket");
            }

            conn.commit();
            logInfo("purchaseTicket", "Ticket purchased successfully for $" + ticketPrice);
            return true;

        } catch (InsufficientBalanceException e) {
            if (conn != null)
                try {
                    conn.rollback();
                } catch (Exception ignored) {
                }
            throw e;
        } catch (java.sql.SQLException | RuntimeException e) {
            if (conn != null)
                try {
                    conn.rollback();
                } catch (Exception ignored) {
                }
            logSevere("purchaseTicket", "Error purchasing ticket: " + e.getMessage());
            throw new ServiceException("Error purchasing ticket: " + e.getMessage(), e);
        } finally {
            if (conn != null)
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (Exception ignored) {
                }
        }
    }
    public List<String> purchaseMultipleTickets(List<String> ticketIds, User user)
            throws InsufficientBalanceException, ServiceException {
        logInfo("purchaseMultipleTickets", "Purchasing " + ticketIds.size() + " tickets for user ID: " + user.getUserID());

        if (ticketIds == null || ticketIds.isEmpty()) {
            logWarning("purchaseMultipleTickets", "Empty ticket IDs list provided");
            throw new ServiceException("At least one ticket ID is required");
        }

        if (user == null) {
            logWarning("purchaseMultipleTickets", "Null user provided");
            throw new ServiceException("User is required");
        }

        Connection conn = null;
        List<String> successfullyPurchased = new ArrayList<>();

        try {
            conn = com.lottery.db.DatabaseConfig.getConnection();
            conn.setAutoCommit(false);

            double totalPrice = 0;
            List<LotteryTicket> ticketsToPurchase = new ArrayList<>();

            for (String ticketId : ticketIds) {
                if (ticketId == null || ticketId.trim().isEmpty()) {
                    logWarning("purchaseMultipleTickets", "Invalid ticket ID in list");
                    continue;
                }

                if (!InputValidator.isValidTicketId(ticketId)) {
                    logWarning("purchaseMultipleTickets", "Invalid ticket ID format: " + ticketId);
                    continue;
                }

                try {
                    LotteryTicket ticket = getTicketById(ticketId);

                    if (!ticket.isPublished()) {
                        logWarning("purchaseMultipleTickets", "Ticket " + ticketId + " is not published");
                        continue;
                    }

                    if (ticket.getOwnerId() != null && !ticket.getOwnerId().isEmpty()) {
                        logWarning("purchaseMultipleTickets", "Ticket " + ticketId + " is already owned");
                        continue;
                    }

                    ticketsToPurchase.add(ticket);
                    totalPrice += ticket.getPrice();

                } catch (TicketNotFoundException e) {
                    logWarning("purchaseMultipleTickets", "Ticket " + ticketId + " not found");
                    continue;
                }
            }

            if (ticketsToPurchase.isEmpty()) {
                logWarning("purchaseMultipleTickets", "No valid tickets to purchase");
                throw new ServiceException("No valid tickets available for purchase");
            }

            boolean balanceUpdated = com.lottery.db.UserDAO.updateUserBalance(conn, user.getUserID(),
                    BigDecimal.valueOf(-totalPrice));

            if (!balanceUpdated) {
                conn.rollback();
                logWarning("purchaseMultipleTickets", "Insufficient balance for user ID: " + user.getUserID() +
                        ", total ticket price: " + totalPrice);
                throw new InsufficientBalanceException(user.getBalance(), BigDecimal.valueOf(totalPrice));
            }

            List<String> validTicketIds = new ArrayList<>();
            for (LotteryTicket ticket : ticketsToPurchase) {
                validTicketIds.add(ticket.getTicketID());
            }

            boolean ticketsUpdated = LotteryTicketDAO.updateTicketOwnersBatch(conn, validTicketIds, String.valueOf(user.getUserID()));

            if (ticketsUpdated) {
                successfullyPurchased.addAll(validTicketIds);
                logInfo("purchaseMultipleTickets", "Batch successfully purchased " + validTicketIds.size() + " tickets");
            } else {
                conn.rollback();
                logWarning("purchaseMultipleTickets", "Failed to update owners for tickets");
                throw new ServiceException("Failed to update ticket owners during bulk purchase.");
            }

            conn.commit();
            logInfo("purchaseMultipleTickets", "Bulk purchase completed. Successfully purchased " + successfullyPurchased.size() +
                    " out of " + ticketIds.size() + " tickets for $" + totalPrice);
            return successfullyPurchased;

        } catch (InsufficientBalanceException e) {
            if (conn != null)
                try {
                    conn.rollback();
                } catch (Exception ignored) {
                }
            throw e;
        } catch (java.sql.SQLException | RuntimeException e) {
            if (conn != null)
                try {
                    conn.rollback();
                } catch (Exception ignored) {
                }
            logSevere("purchaseMultipleTickets", "Error purchasing multiple tickets: " + e.getMessage());
            throw new ServiceException("Error purchasing tickets: " + e.getMessage(), e);
        } finally {
            if (conn != null)
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (Exception ignored) {
                }
        }
    }
    public List<LotteryTicket> getUserTickets(int userId, int offset, int limit)
            throws UserNotFoundException, ServiceException {
        logInfo("getUserTickets", "Getting tickets for user ID: " + userId);

        if (userId <= 0) {
            logWarning("getUserTickets", "Invalid user ID: " + userId);
            throw new ServiceException("Invalid user ID");
        }

        if (offset < 0 || limit <= 0 || limit > AppConfig.MAX_PAGINATION_LIMIT) {
            logWarning("getUserTickets", "Invalid pagination parameters: offset=" + offset + ", limit=" + limit);
            throw new ServiceException("Invalid pagination parameters");
        }

        try {
            
            com.lottery.model.User user = com.lottery.db.UserDAO.getUserById(userId);
            if (user == null) {
                logWarning("getUserTickets", "User not found with ID: " + userId);
                throw new UserNotFoundException(String.valueOf(userId));
            }

            return LotteryTicketDAO.getTicketsByOwnerId(String.valueOf(userId), offset, limit);
        } catch (UserNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            logSevere("getUserTickets", "Error getting user tickets: " + e.getMessage());
            throw new ServiceException("Error getting user tickets: " + e.getMessage(), e);
        }
    }
    public int getUserTicketCount(int userId) throws UserNotFoundException, ServiceException {
        logInfo("getUserTicketCount", "Getting ticket count for user ID: " + userId);

        if (userId <= 0) {
            logWarning("getUserTicketCount", "Invalid user ID: " + userId);
            throw new ServiceException("Invalid user ID");
        }

        try {
            
            com.lottery.model.User user = com.lottery.db.UserDAO.getUserById(userId);
            if (user == null) {
                logWarning("getUserTicketCount", "User not found with ID: " + userId);
                throw new UserNotFoundException(String.valueOf(userId));
            }

            return LotteryTicketDAO.getTicketCountByOwnerId(String.valueOf(userId));
        } catch (UserNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            logSevere("getUserTicketCount", "Error getting user ticket count: " + e.getMessage());
            throw new ServiceException("Error getting user ticket count: " + e.getMessage(), e);
        }
    }
    public List<LotteryTicket> searchPublishedTickets(String company, LocalDate specificDate, LocalDate startDate,
            LocalDate endDate, int offset, int limit) throws ServiceException {
        logInfo("searchPublishedTickets", "Searching published tickets");

        if (offset < 0 || limit <= 0 || limit > AppConfig.MAX_PAGINATION_LIMIT) {
            logWarning("searchPublishedTickets", "Invalid pagination parameters: offset=" + offset + ", limit=" + limit);
            throw new ServiceException("Invalid pagination parameters");
        }

        if (company != null && !company.isEmpty()) {
            
            if (company.contains(",")) {
                String[] companies = company.split(",");
                for (String comp : companies) {
                    if (!InputValidator.isValidCompany(comp.trim())) {
                        logWarning("searchPublishedTickets", "Invalid company format: " + comp.trim());
                        throw new ServiceException("Invalid company format: " + comp.trim());
                    }
                }
            } else {
                
                if (!InputValidator.isValidCompany(company)) {
                    logWarning("searchPublishedTickets", "Invalid company format: " + company);
                    throw new ServiceException("Invalid company format");
                }
            }
        }

        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            logWarning("searchPublishedTickets", "Start date cannot be after end date");
            throw new ServiceException("Start date cannot be after end date");
        }

        try {
            LotteryTicketSearchCriteria criteria = LotteryTicketSearchCriteria.create()
                    .company(company)
                    .published(true)
                    .pagination(offset, limit);

            if (specificDate != null) {
                criteria.specificDate(specificDate);
            } else if (startDate != null || endDate != null) {
                LocalDate start = startDate != null ? startDate : LocalDate.now().minusDays(30);
                LocalDate end = endDate != null ? endDate : LocalDate.now();
                criteria.dateRange(start, end);
            }

            List<LotteryTicket> publishedTickets = LotteryTicketDAO.searchPublishedTickets(criteria);

            return publishedTickets;
        } catch (RuntimeException e) {
            logSevere("searchPublishedTickets", "Error searching published tickets: " + e.getMessage());
            throw new ServiceException("Error searching published tickets: " + e.getMessage(), e);
        }
    }
    public List<String> getAllPublishedCompanies() throws ServiceException {
        logInfo("getAllPublishedCompanies", "Getting all published companies");

        try {
            return LotteryTicketDAO.getAllPublishedCompanies();
        } catch (RuntimeException e) {
            logSevere("getAllPublishedCompanies", "Error getting published companies: " + e.getMessage());
            throw new ServiceException("Error getting published companies: " + e.getMessage(), e);
        }
    }
    public int getPublishedTicketCount(String company, LocalDate specificDate, LocalDate startDate, LocalDate endDate)
            throws ServiceException {
        logInfo("getPublishedTicketCount", "Getting published ticket count");

        if (company != null && !company.isEmpty()) {
            
            if (company.contains(",")) {
                String[] companies = company.split(",");
                for (String comp : companies) {
                    if (!InputValidator.isValidCompany(comp.trim())) {
                        logWarning("getPublishedTicketCount", "Invalid company format: " + comp.trim());
                        throw new ServiceException("Invalid company format: " + comp.trim());
                    }
                }
            } else {
                
                if (!InputValidator.isValidCompany(company)) {
                    logWarning("getPublishedTicketCount", "Invalid company format: " + company);
                    throw new ServiceException("Invalid company format");
                }
            }
        }

        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            logWarning("getPublishedTicketCount", "Start date cannot be after end date");
            throw new ServiceException("Start date cannot be after end date");
        }

        try {
            LotteryTicketSearchCriteria criteria = LotteryTicketSearchCriteria.create()
                    .company(company)
                    .published(true);

            if (specificDate != null) {
                criteria.specificDate(specificDate);
            } else if (startDate != null || endDate != null) {
                LocalDate start = startDate != null ? startDate : LocalDate.now().minusDays(30);
                LocalDate end = endDate != null ? endDate : LocalDate.now();
                criteria.dateRange(start, end);
            }

            return LotteryTicketDAO.getPublishedTicketCount(criteria);
        } catch (RuntimeException e) {
            logSevere("getPublishedTicketCount", "Error getting published ticket count: " + e.getMessage());
            throw new ServiceException("Error getting published ticket count: " + e.getMessage(), e);
        }
    }
    public boolean incrementViewCount(String ticketId) throws ServiceException {
        logInfo("incrementViewCount", "Incrementing view count for ticket ID: " + ticketId);

        try {
            return LotteryTicketDAO.incrementViewCount(ticketId);
        } catch (RuntimeException e) {
            logSevere("incrementViewCount", "Error incrementing view count: " + e.getMessage());
            throw new ServiceException("Error incrementing view count: " + e.getMessage(), e);
        }
    }
    public boolean incrementViewCountBatch(List<String> ticketIds) throws ServiceException {

        if (ticketIds == null || ticketIds.isEmpty()) {
            return false;
        }

        try {
            return LotteryTicketDAO.incrementViewCountBatch(ticketIds);
        } catch (RuntimeException e) {
            logSevere("incrementViewCountBatch", "Error incrementing view count for batch: " + e.getMessage());
            throw new ServiceException("Error incrementing view count for batch", e);
        }
    }
    public LotteryTicket getTicketById(String ticketId) throws TicketNotFoundException, ServiceException {
        logInfo("getTicketById", "Getting ticket by ID: " + ticketId);

        if (ticketId == null || ticketId.trim().isEmpty()) {
            logWarning("getTicketById", "Invalid ticket ID provided: " + ticketId);
            throw new ServiceException("Ticket ID is required");
        }

        if (!InputValidator.isValidTicketId(ticketId)) {
            logWarning("getTicketById", "Invalid ticket ID format: " + ticketId);
            throw new ServiceException("Invalid ticket ID format");
        }

        try {
            
            LotteryTicket ticket = LotteryTicketDAO.getTicketById(ticketId);

            if (ticket == null) {
                logInfo("getTicketById", "Ticket not found with ID: " + ticketId);
                throw new TicketNotFoundException(ticketId);
            }

            logInfo("getTicketById", "Successfully retrieved ticket with ID: " + ticketId);
            return ticket;
        } catch (TicketNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            logSevere("getTicketById", "Error getting ticket by ID: " + e.getMessage());
            throw new ServiceException("Error getting ticket by ID: " + e.getMessage(), e);
        }
    }
    public List<LotteryTicket> searchPublishedTicketsByNumbersAndDate(String numbers, LocalDate startDate,
            LocalDate endDate, int offset, int limit) throws ServiceException {
        logInfo("searchPublishedTicketsByNumbersAndDate", "Searching published tickets by numbers and date range");

        try {
            LotteryTicketSearchCriteria criteria = LotteryTicketSearchCriteria.create()
                    .numbers(numbers)
                    .dateRange(startDate, endDate)
                    .published(true)
                    .pagination(offset, limit);
            return LotteryTicketDAO.searchPublishedTickets(criteria);
        } catch (RuntimeException e) {
            logSevere("searchPublishedTicketsByNumbersAndDate", "Error searching published tickets by numbers and date range: " + e.getMessage());
            throw new ServiceException("Error searching published tickets by numbers and date range: " + e.getMessage(),
                    e);
        }
    }
    public List<LotteryTicket> searchPublishedTicketsByNumbersAndDate(String numbers, LocalDate specificDate,
            int offset, int limit) throws ServiceException {
        logInfo("searchPublishedTicketsByNumbersAndDate", "Searching published tickets by numbers and specific date");

        try {
            LotteryTicketSearchCriteria criteria = LotteryTicketSearchCriteria.create()
                    .numbers(numbers)
                    .specificDate(specificDate)
                    .published(true)
                    .pagination(offset, limit);
            return LotteryTicketDAO.searchPublishedTickets(criteria);
        } catch (RuntimeException e) {
            logSevere("searchPublishedTicketsByNumbersAndDate", "Error searching published tickets by numbers and specific date: " + e.getMessage());
            throw new ServiceException(
                    "Error searching published tickets by numbers and specific date: " + e.getMessage(), e);
        }
    }
    public List<LotteryTicket> searchPublishedTicketsByNumbersAndCompany(String numbers, String company, int offset,
            int limit) throws ServiceException {
        logInfo("searchPublishedTicketsByNumbersAndCompany", "Searching published tickets by numbers and company");

        try {
            LotteryTicketSearchCriteria criteria = LotteryTicketSearchCriteria.create()
                    .numbers(numbers)
                    .company(company)
                    .published(true)
                    .pagination(offset, limit);
            return LotteryTicketDAO.searchPublishedTickets(criteria);
        } catch (RuntimeException e) {
            logSevere("searchPublishedTicketsByNumbersAndCompany", "Error searching published tickets by numbers and company: " + e.getMessage());
            throw new ServiceException("Error searching published tickets by numbers and company: " + e.getMessage(),
                    e);
        }
    }
    public List<LotteryTicket> searchPublishedTicketsByNumbers(String numbers, int offset, int limit)
            throws ServiceException {
        logInfo("searchPublishedTicketsByNumbers", "Searching published tickets by numbers");

        try {
            LotteryTicketSearchCriteria criteria = LotteryTicketSearchCriteria.create()
                    .numbers(numbers)
                    .published(true)
                    .pagination(offset, limit);
            return LotteryTicketDAO.searchPublishedTickets(criteria);
        } catch (RuntimeException e) {
            logSevere("searchPublishedTicketsByNumbers", "Error searching published tickets by numbers: " + e.getMessage());
            throw new ServiceException("Error searching published tickets by numbers: " + e.getMessage(), e);
        }
    }
    public int getPublishedTicketCountByNumbersAndDate(String numbers, LocalDate startDate, LocalDate endDate)
            throws ServiceException {
        logInfo("getPublishedTicketCountByNumbersAndDate", "Getting published ticket count by numbers and date range");

        try {
            LotteryTicketSearchCriteria criteria = LotteryTicketSearchCriteria.create()
                    .numbers(numbers)
                    .dateRange(startDate, endDate)
                    .published(true);
            return LotteryTicketDAO.getPublishedTicketCount(criteria);
        } catch (RuntimeException e) {
            logSevere("getPublishedTicketCountByNumbersAndDate", "Error getting published ticket count by numbers and date range: " + e.getMessage());
            throw new ServiceException(
                    "Error getting published ticket count by numbers and date range: " + e.getMessage(), e);
        }
    }
    public int getPublishedTicketCountByNumbersAndDate(String numbers, LocalDate specificDate) throws ServiceException {
        logInfo("getPublishedTicketCountByNumbersAndDate", "Getting published ticket count by numbers and specific date");

        try {
            LotteryTicketSearchCriteria criteria = LotteryTicketSearchCriteria.create()
                    .numbers(numbers)
                    .specificDate(specificDate)
                    .published(true);
            return LotteryTicketDAO.getPublishedTicketCount(criteria);
        } catch (RuntimeException e) {
            logSevere("getPublishedTicketCountByNumbersAndDate",
                    "Error getting published ticket count by numbers and specific date: " + e.getMessage());
            throw new ServiceException(
                    "Error getting published ticket count by numbers and specific date: " + e.getMessage(), e);
        }
    }
    public int getPublishedTicketCountByNumbersAndCompany(String numbers, String company) throws ServiceException {
        logInfo("getPublishedTicketCountByNumbersAndCompany", "Getting published ticket count by numbers and company");

        try {
            LotteryTicketSearchCriteria criteria = LotteryTicketSearchCriteria.create()
                    .numbers(numbers)
                    .company(company)
                    .published(true);
            return LotteryTicketDAO.getPublishedTicketCount(criteria);
        } catch (RuntimeException e) {
            logSevere("getPublishedTicketCountByNumbersAndCompany", "Error getting published ticket count by numbers and company: " + e.getMessage());
            throw new ServiceException("Error getting published ticket count by numbers and company: " + e.getMessage(),
                    e);
        }
    }
    public LotteryTicket processTicketPurchase(
            String company, String num1, String num2, String num3, String num4, String num5, String num6,
            String startDateStr, String endDateStr, String specificDateStr, User user)
            throws ServiceException, InsufficientBalanceException, TicketNotFoundException {

        logInfo("processTicketPurchase", "Processing ticket purchase for user ID: " + user.getUserID());

        String errorMessage = null;

        if (company != null && !company.isEmpty()) {
            if (!InputValidator.isValidCompany(company)) {
                logWarning("processTicketPurchase", "Invalid company name: " + company);
                throw new ServiceException("Invalid company name.");
            }
        }

        if (company == null || company.isEmpty()) {
            List<String> companies = getAllPublishedCompanies();
            if (companies.isEmpty()) {
                logWarning("processTicketPurchase", "No companies available for ticket purchase.");
                throw new ServiceException("No companies available for ticket purchase.");
            }
        }

        com.lottery.util.LotteryNumberParser.ParseResult parseResult = com.lottery.util.LotteryNumberParser.parse(num1,
                num2, num3, num4, num5, num6);

        if (!parseResult.isValid()) {
            logWarning("processTicketPurchase", "Number validation failed: " + parseResult.getErrorMessage());
            throw new ServiceException(parseResult.getErrorMessage());
        }

        Integer[] nums = parseResult.getNumbers();
        SecureRandom random = new SecureRandom();
        nums = com.lottery.util.LotteryNumberParser.generateRandom(nums, random);

        Integer n1 = nums[0];
        Integer n2 = nums[1];
        Integer n3 = nums[2];
        Integer n4 = nums[3];
        Integer n5 = nums[4];
        Integer n6 = nums[5];

        LocalDate startDate = null;
        LocalDate endDate = null;
        LocalDate specificDate = null;

        if (startDateStr != null && !startDateStr.isEmpty()) {
            if (InputValidator.isValidDate(startDateStr)) {
                try {
                    startDate = LocalDate.parse(startDateStr);
                } catch (RuntimeException e) {
                    errorMessage = "Invalid start date format.";
                }
            } else {
                errorMessage = "Invalid start date.";
            }
        }

        if (endDateStr != null && !endDateStr.isEmpty()) {
            if (InputValidator.isValidDate(endDateStr)) {
                try {
                    endDate = LocalDate.parse(endDateStr);
                } catch (RuntimeException e) {
                    errorMessage = "Invalid end date format.";
                }
            } else {
                errorMessage = "Invalid end date.";
            }
        }

        if (specificDateStr != null && !specificDateStr.isEmpty()) {
            if (InputValidator.isValidDate(specificDateStr)) {
                try {
                    specificDate = LocalDate.parse(specificDateStr);
                } catch (RuntimeException e) {
                    errorMessage = "Invalid specific date format.";
                }
            } else {
                errorMessage = "Invalid specific date.";
            }
        }

        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            errorMessage = "Start date cannot be after end date.";
        }

        if (errorMessage != null) {
            logWarning("processTicketPurchase", "Validation error: " + errorMessage);
            throw new ServiceException(errorMessage);
        }

        try {
            
            List<LotteryTicket> availableTickets = searchAvailableTickets(
                    company, specificDate, startDate, endDate, 0, 100);

            if (availableTickets == null || availableTickets.isEmpty()) {
                logInfo("processTicketPurchase", "No available tickets found matching criteria.");
                throw new TicketNotFoundException("No available tickets found matching your criteria.");
            }

            String numbers = n1 + "," + n2 + "," + n3 + "," + n4 + "," + n5 + "," + n6;
            LotteryTicket selectedTicket = selectTicket(availableTickets, numbers);

            if (purchaseTicket(selectedTicket, user)) {
                logInfo("processTicketPurchase", "Ticket purchased successfully for $" + selectedTicket.getPrice());
                return selectedTicket;
            } else {
                logWarning("processTicketPurchase", "Failed to purchase ticket.");
                throw new ServiceException("Failed to purchase ticket. Please try again.");
            }
        } catch (TicketNotFoundException e) {
            logInfo("processTicketPurchase", "No available tickets found matching criteria: " + e.getMessage());
            throw e;
        } catch (InsufficientBalanceException e) {
            logInfo("processTicketPurchase", "Insufficient balance for user ID: " + user.getUserID());
            throw e;
        } catch (RuntimeException e) {
            logSevere("processTicketPurchase", "Error processing ticket purchase: " + e.getMessage());
            throw new ServiceException("Error processing ticket purchase: " + e.getMessage(), e);
        }
    }
    public SearchResult processUserLotterySearch(
            String[] companyParams, String num1, String num2, String num3, String num4, String num5, String num6,
            String startDateStr, String endDateStr, String specificDateStr, String pageParam)
            throws ServiceException {

        logInfo("processUserLotterySearch", "Processing user lottery search");

        final int TICKETS_PER_PAGE = 10;

        int page = 1;
        if (pageParam != null && !pageParam.isEmpty()) {
            try {
                page = Integer.parseInt(pageParam);
                if (page < 1) {
                    page = 1;
                }
            } catch (NumberFormatException e) {
                page = 1;
            }
        }

        int offset = (page - 1) * TICKETS_PER_PAGE;

        String company = (companyParams != null) ? String.join(",", companyParams) : "";

        if (company != null && !company.isEmpty()) {
            
            String[] companies = company.split(",");
            StringBuilder validCompanies = new StringBuilder();
            for (String comp : companies) {
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

        com.lottery.util.LotteryNumberParser.ParseResult parseResult = com.lottery.util.LotteryNumberParser.parse(num1,
                num2, num3, num4, num5, num6);

        String numbers = null;
        if (parseResult.isValid()) {
            numbers = parseResult.toCsvString();
            if (numbers.isEmpty()) {
                numbers = null;
            }
        }

        java.time.LocalDate startDate = com.lottery.util.InputValidator.parseDate(startDateStr).orElse(null);
        java.time.LocalDate endDate = com.lottery.util.InputValidator.parseDate(endDateStr).orElse(null);
        java.time.LocalDate specificDate = com.lottery.util.InputValidator.parseDate(specificDateStr).orElse(null);

        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            
            java.time.LocalDate temp = startDate;
            startDate = endDate;
            endDate = temp;
        }

        boolean isSearchFormSubmitted = companyParams != null ||
                num1 != null ||
                num2 != null ||
                num3 != null ||
                num4 != null ||
                num5 != null ||
                num6 != null ||
                startDateStr != null ||
                endDateStr != null ||
                specificDateStr != null;

        boolean hasSearchParameters = (company != null && !company.isEmpty()) ||
                (numbers != null && !numbers.isEmpty()) ||
                (specificDate != null) ||
                (startDate != null && endDate != null);

        java.util.List<LotteryTicket> tickets = new java.util.ArrayList<>();
        int totalTickets = 0;

        if (isSearchFormSubmitted) {
            LotteryTicketSearchCriteria criteria = LotteryTicketSearchCriteria.create()
                    .company(company)
                    .number1(parseResult.getNumbers()[0])
                    .number2(parseResult.getNumbers()[1])
                    .number3(parseResult.getNumbers()[2])
                    .number4(parseResult.getNumbers()[3])
                    .number5(parseResult.getNumbers()[4])
                    .number6(parseResult.getNumbers()[5])
                    .pagination(offset, TICKETS_PER_PAGE);

            if (specificDate != null) {
                criteria.specificDate(specificDate);
            } else if (startDate != null && endDate != null) {
                criteria.dateRange(startDate, endDate);
            } else {
                criteria.clearDates();
            }

            tickets = LotteryTicketDAO.searchPublishedTickets(criteria);
            totalTickets = LotteryTicketDAO.getPublishedTicketCount(criteria);
        } else if (!hasSearchParameters) {
            
            LotteryTicketSearchCriteria criteria = LotteryTicketSearchCriteria.create()
                    .pagination(offset, TICKETS_PER_PAGE);
            tickets = LotteryTicketDAO.searchPublishedTickets(criteria);
            totalTickets = LotteryTicketDAO.getPublishedTicketCount(criteria);
        }

        SearchResult result = new SearchResult();
        result.setTickets(tickets);
        result.setCurrentPage(page);
        result.setTotalPages((int) Math.ceil((double) totalTickets / TICKETS_PER_PAGE));
        result.setTotalTickets(totalTickets);
        result.setCompany(company);
        result.setNum1(num1);
        result.setNum2(num2);
        result.setNum3(num3);
        result.setNum4(num4);
        result.setNum5(num5);
        result.setNum6(num6);
        result.setStartDate(startDateStr);
        result.setEndDate(endDateStr);
        result.setSpecificDate(specificDateStr);

        logInfo("processUserLotterySearch", "User lottery search processed successfully. Found " + tickets.size() + " tickets.");
        return result;
    }
    public SearchResult searchTicketsForPurchase(
            String company, String num1, String num2, String num3, String num4, String num5, String num6,
            String startDateStr, String endDateStr, String specificDateStr, String pageParam)
            throws ServiceException {

        logInfo("searchTicketsForPurchase", "Searching tickets eligible for purchase");

        final int TICKETS_PER_PAGE = 10;

        int page = 1;
        if (pageParam != null && !pageParam.isEmpty()) {
            try {
                page = Integer.parseInt(pageParam);
                if (page < 1) {
                    page = 1;
                }
            } catch (NumberFormatException e) {
                page = 1;
            }
        }

        int offset = (page - 1) * TICKETS_PER_PAGE;

        if (company != null && !company.isEmpty()) {
            String[] companies = company.split(",");
            for (String comp : companies) {
                String trimmedComp = comp.trim();
                if (!InputValidator.isValidCompany(trimmedComp)) {
                    logWarning("searchTicketsForPurchase", "Invalid company name: " + trimmedComp);
                    throw new ServiceException("Invalid company name: " + trimmedComp);
                }
            }
        }

        com.lottery.util.LotteryNumberParser.ParseResult parseResult = com.lottery.util.LotteryNumberParser.parse(num1,
                num2, num3, num4, num5, num6);

        String numbers = null;
        if (parseResult.isValid()) {
            numbers = parseResult.toCsvString();
            if (numbers.isEmpty()) {
                numbers = null;
            }
        }

        LocalDate startDate = InputValidator.parseDate(startDateStr).orElse(null);
        LocalDate endDate = InputValidator.parseDate(endDateStr).orElse(null);
        LocalDate specificDate = InputValidator.parseDate(specificDateStr).orElse(null);

        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            LocalDate temp = startDate;
            startDate = endDate;
            endDate = temp;
        }

        try {
            LotteryTicketSearchCriteria criteria = LotteryTicketSearchCriteria.create()
                    .company(company)
                    .number1(parseResult.getNumbers()[0])
                    .number2(parseResult.getNumbers()[1])
                    .number3(parseResult.getNumbers()[2])
                    .number4(parseResult.getNumbers()[3])
                    .number5(parseResult.getNumbers()[4])
                    .number6(parseResult.getNumbers()[5])
                    .onlyUnpurchased(true)
                    .published(true)
                    .pagination(offset, TICKETS_PER_PAGE);

            if (specificDate != null) {
                criteria.specificDate(specificDate);
            } else if (startDate != null && endDate != null) {
                criteria.dateRange(startDate, endDate);
            } else {
                criteria.clearDates();
            }

            List<LotteryTicket> availableTickets = LotteryTicketDAO.searchPublishedTickets(criteria);
            int totalTickets = LotteryTicketDAO.getPublishedTicketCount(criteria);

            SearchResult result = new SearchResult();
            result.setTickets(availableTickets);
            result.setCurrentPage(page);
            result.setTotalPages((int) Math.ceil((double) totalTickets / TICKETS_PER_PAGE));
            result.setTotalTickets(totalTickets);
            result.setCompany(company);
            result.setNum1(num1);
            result.setNum2(num2);
            result.setNum3(num3);
            result.setNum4(num4);
            result.setNum5(num5);
            result.setNum6(num6);
            result.setStartDate(startDateStr);
            result.setEndDate(endDateStr);
            result.setSpecificDate(specificDateStr);

            return result;
        } catch (RuntimeException e) {
            logSevere("searchTicketsForPurchase", "Error searching tickets for purchase: " + e.getMessage());
            throw new ServiceException("Error searching tickets for purchase: " + e.getMessage(), e);
        }
    }
    public SearchResult processUserTickets(int userId, String pageParam)
            throws ServiceException, UserNotFoundException {

        logInfo("processUserTickets", "Processing user tickets retrieval for user ID: " + userId);

        final int TICKETS_PER_PAGE = 10;

        int page = 1;
        if (pageParam != null && !pageParam.isEmpty()) {
            try {
                page = Integer.parseInt(pageParam);
                if (page < 1)
                    page = 1;
            } catch (NumberFormatException e) {
                page = 1;
            }
        }

        int offset = (page - 1) * TICKETS_PER_PAGE;

        List<LotteryTicket> tickets = getUserTickets(userId, offset, TICKETS_PER_PAGE);

        int totalTickets = getUserTicketCount(userId);
        int totalPages = (int) Math.ceil((double) totalTickets / TICKETS_PER_PAGE);

        SearchResult result = new SearchResult();
        result.setTickets(tickets);
        result.setCurrentPage(page);
        result.setTotalPages(totalPages);
        result.setTotalTickets(totalTickets);

        logInfo("processUserTickets", "User tickets retrieval processed successfully for user ID: " + userId +
                ". Found " + tickets.size() + " tickets.");
        return result;
    }
    public int getPublishedTicketCountByNumbers(String numbers) throws ServiceException {
        logInfo("getPublishedTicketCountByNumbers", "Getting published ticket count by numbers");

        try {
            LotteryTicketSearchCriteria criteria = LotteryTicketSearchCriteria.create()
                    .numbers(numbers)
                    .published(true);
            return LotteryTicketDAO.getPublishedTicketCount(criteria);
        } catch (RuntimeException e) {
            logSevere("getPublishedTicketCountByNumbers", "Error getting published ticket count by numbers: " + e.getMessage());
            throw new ServiceException("Error getting published ticket count by numbers: " + e.getMessage(), e);
        }
    }
    public List<String> getAllDistinctCompanies() throws ServiceException {
        logInfo("getAllDistinctCompanies", "Getting all distinct companies");

        try {
            return LotteryTicketDAO.getAllDistinctCompanies();
        } catch (RuntimeException e) {
            logSevere("getAllDistinctCompanies", "Error getting distinct companies: " + e.getMessage());
            throw new ServiceException("Error getting distinct companies: " + e.getMessage(), e);
        }
    }

}
