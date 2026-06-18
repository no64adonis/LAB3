package com.lottery.service;

import com.lottery.db.LotteryTicketDAO;
import com.lottery.db.LotteryTicketSearchCriteria;
import com.lottery.db.PaymentMethodDAO;
import com.lottery.db.UserDAO;
import com.lottery.model.LotteryTicket;
import com.lottery.model.PaymentMethod;
import com.lottery.model.User;
import com.lottery.service.exception.ServiceException;
import com.lottery.service.exception.TicketNotFoundException;
import com.lottery.util.InputValidator;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class AdminServiceImpl extends BaseService implements AdminService {
    private static final Logger logger = Logger.getLogger(AdminServiceImpl.class.getName());
    private EmailService emailService = new EmailServiceImpl();
    public List<LotteryTicket> searchAllTickets(LotteryTicketSearchCriteria criteria) throws ServiceException {
        validateSearchCriteria(criteria, true);

        try {
            return LotteryTicketDAO.searchTickets(criteria);
        } catch (Exception e) {
            logSevere("searchAllTickets", "Error searching tickets: " + e.getMessage());
            throw new ServiceException("Error searching all tickets: " + e.getMessage(), e);
        }
    }

    private void validateSearchCriteria(LotteryTicketSearchCriteria criteria, boolean includePagination)
            throws ServiceException {
        if (criteria == null) {
            throw new ServiceException("Search criteria is required");
        }

        if (includePagination &&
                (criteria.getOffset() < 0 || criteria.getLimit() <= 0
                        || (criteria.getLimit() > 1000 && criteria.getLimit() != Integer.MAX_VALUE))) {
            throw new ServiceException("Invalid pagination parameters");
        }

        if (criteria.getNumbers() != null && !criteria.getNumbers().isEmpty()
                && !isValidSearchNumbers(criteria.getNumbers())) {
            throw new ServiceException("Invalid numbers format");
        }

        if (criteria.getCompany() != null && !criteria.getCompany().isEmpty()) {
            for (String comp : criteria.getCompany().split(",")) {
                String trimmed = comp.trim();
                if (!trimmed.isEmpty() && !InputValidator.isValidCompany(trimmed)) {
                    throw new ServiceException("Invalid company format");
                }
            }
        }

        if (criteria.getOwnerId() != null && !criteria.getOwnerId().isEmpty()
                && !InputValidator.isValidIdentifier(criteria.getOwnerId())
                && !criteria.getOwnerId().matches("^\\d+$")) {
            throw new ServiceException("Invalid owner format");
        }

        if (criteria.shouldApplyDateFilter()) {
            if (criteria.isUseDateRange() != null && criteria.isUseDateRange()) {
                if (criteria.getStartDate() == null || criteria.getEndDate() == null) {
                    throw new ServiceException("Date range search requires both start and end dates");
                }
                if (criteria.getStartDate().isAfter(criteria.getEndDate())) {
                    throw new ServiceException("Start date cannot be after end date");
                }
            } else if (criteria.getSpecificDate() != null
                    && criteria.getSpecificDate().isAfter(LocalDate.now())) {
                throw new ServiceException("Specific date cannot be in the future");
            }
        }
    }

    private static boolean isValidSearchNumbers(String numbers) {
        if (numbers == null || numbers.isEmpty()) {
            return false;
        }
        
        String[] parts = numbers.split(",");
        if (parts.length < 1 || parts.length > 6) {
            return false;
        }
        for (String part : parts) {
            try {
                int num = Integer.parseInt(part.trim());
                if (num < 1 || num > 99) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }
    public int getTicketCount(LotteryTicketSearchCriteria criteria) throws ServiceException {
        validateSearchCriteria(criteria, false);

        try {
            return LotteryTicketDAO.getSearchTicketCount(criteria);
        } catch (Exception e) {
            logSevere("getTicketCount", "Error getting ticket count: " + e.getMessage());
            throw new ServiceException("Error getting ticket count: " + e.getMessage(), e);
        }
    }
    public boolean updateTicketStatus(String ticketId, boolean published)
            throws TicketNotFoundException, ServiceException {
        logInfo("updateTicketStatus", "Updating ticket status for ticket ID: " + ticketId + " to: " + published);

        if (!InputValidator.isValidTicketId(ticketId)) {
            logWarning("updateTicketStatus", "Invalid ticket ID provided: " + ticketId);
            throw new ServiceException("Invalid ticket ID");
        }

        try {
            
            LotteryTicket ticket = LotteryTicketDAO.getTicketById(ticketId);
            if (ticket == null) {
                throw new TicketNotFoundException(ticketId);
            }

            boolean success = LotteryTicketDAO.updateTicketStatus(ticketId, published);
            if (success) {
                logInfo("updateTicketStatus", "Ticket status updated successfully for ticket ID: " + ticketId);
            } else {
                logWarning("updateTicketStatus", "Failed to update ticket status for ticket ID: " + ticketId);
            }
            return success;
        } catch (TicketNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logSevere("updateTicketStatus",
                    "Error updating ticket status for ticket ID: " + ticketId + ", error: " + e.getMessage());
            throw new ServiceException("Error updating ticket status: " + e.getMessage(), e);
        }
    }
    public boolean updateTicketStatus(List<String> ticketIds, boolean published) throws ServiceException {
        logInfo("updateTicketStatus", "Bulk updating ticket status for " + ticketIds.size() + " tickets");

        if (ticketIds == null || ticketIds.isEmpty()) {
            logWarning("updateTicketStatus", "Null or empty ticket IDs list provided");
            throw new ServiceException("Ticket IDs list is required");
        }

        for (String ticketId : ticketIds) {
            if (!InputValidator.isValidTicketId(ticketId)) {
                logWarning("updateTicketStatus", "Invalid ticket ID in list: " + ticketId);
                throw new ServiceException("Invalid ticket ID in list: " + ticketId);
            }
        }

        try {
            boolean success = LotteryTicketDAO.updateTicketStatus(ticketIds, published);
            if (success) {
                logInfo("updateTicketStatus", "Bulk ticket status update successful for " + ticketIds.size() + " tickets");
            } else {
                logWarning("updateTicketStatus", "Failed to bulk update ticket status for " + ticketIds.size() + " tickets");
            }
            return success;
        } catch (Exception e) {
            logSevere("updateTicketStatus", "Error bulk updating ticket status: " + e.getMessage());
            throw new ServiceException("Error bulk updating ticket status: " + e.getMessage(), e);
        }
    }
    public boolean updatePriceForCompany(String company, double price) throws ServiceException {
        logInfo("updatePriceForCompany", "Updating price for company: " + company + " to: " + price);

        if (company == null || company.trim().isEmpty() || !InputValidator.isValidCompany(company)) {
            logWarning("updatePriceForCompany", "Invalid company name: " + company);
            throw new ServiceException("Invalid company name: " + company);
        }

        if (price < 0 || price > 1000000) {
            logWarning("updatePriceForCompany", "Invalid price: " + price);
            throw new ServiceException("Invalid price: " + price);
        }

        try {
            boolean success = LotteryTicketDAO.updatePriceForCompany(company, price);
            if (success) {
                logInfo("updatePriceForCompany", "Price updated successfully for company: " + company);
            } else {
                logWarning("updatePriceForCompany", "Failed to update price for company: " + company);
            }
            return success;
        } catch (Exception e) {
            logSevere("updatePriceForCompany", "Error updating price for company: " + company + ", error: " + e.getMessage());
            throw new ServiceException("Error updating price for company: " + e.getMessage(), e);
        }
    }
    public boolean bulkDeactivateUsers(List<Integer> userIds) throws ServiceException {
        logInfo("bulkDeactivateUsers", "Bulk deactivating " + userIds.size() + " users");

        if (userIds == null || userIds.isEmpty()) {
            logWarning("bulkDeactivateUsers", "Null or empty user IDs list provided");
            throw new ServiceException("User IDs list is required");
        }

        try {
            int successCount = 0;
            for (Integer userId : userIds) {
                if (userId != null && userId > 0) {
                    if (UserDAO.setUserActiveStatus(userId, false)) {
                        successCount++;
                    }
                }
            }

            logInfo("bulkDeactivateUsers", "Bulk user deactivation completed. Success: " + successCount + "/" + userIds.size());
            return successCount > 0;
        } catch (Exception e) {
            logSevere("bulkDeactivateUsers", "Error bulk deactivating users: " + e.getMessage());
            throw new ServiceException("Error bulk deactivating users: " + e.getMessage(), e);
        }
    }
    public List<String> getAllCompanies() throws ServiceException {
        try {
            return LotteryTicketDAO.getAllCompanies();
        } catch (Exception e) {
            throw new ServiceException("Error getting all companies: " + e.getMessage(), e);
        }
    }
    public double getPriceForCompany(String company) throws ServiceException {
        if (company == null || company.trim().isEmpty() || !InputValidator.isValidCompany(company)) {
            throw new ServiceException("Invalid company name: " + company);
        }

        try {
            return LotteryTicketDAO.getPriceForCompany(company);
        } catch (Exception e) {
            throw new ServiceException("Error getting price for company: " + e.getMessage(), e);
        }
    }
    public List<LotteryTicket> getAllTickets(int offset, int limit) throws ServiceException {
        if (offset < 0 || limit <= 0 || (limit > 1000 && limit != Integer.MAX_VALUE)) {
            throw new ServiceException("Invalid pagination parameters");
        }

        try {
            return LotteryTicketDAO.getAllTickets(offset, limit);
        } catch (Exception e) {
            throw new ServiceException("Error getting all tickets: " + e.getMessage(), e);
        }
    }
    public int getTotalTicketCount() throws ServiceException {
        try {
            return LotteryTicketDAO.getTotalTicketCount();
        } catch (Exception e) {
            throw new ServiceException("Error getting total ticket count: " + e.getMessage(), e);
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
        } catch (Exception e) {
            logSevere("getTicketById", "Error getting ticket by ID: " + e.getMessage());
            throw new ServiceException("Error getting ticket by ID: " + e.getMessage(), e);
        }
    }
    public List<User> getInactiveUsers(String period) throws ServiceException {
        logInfo("getInactiveUsers", "Getting inactive users for period: " + period);

        try {
            if (period == null || period.trim().isEmpty()) {
                logWarning("getInactiveUsers", "Invalid period provided: " + period);
                throw new ServiceException("Period is required");
            }

            String sanitizedPeriod = InputValidator.sanitizeString(period);
            if (sanitizedPeriod == null || sanitizedPeriod.isEmpty()) {
                logWarning("getInactiveUsers", "Invalid period after sanitization: " + period);
                throw new ServiceException("Invalid period");
            }

            return UserDAO.getInactiveUsers(sanitizedPeriod);
        } catch (Exception e) {
            logSevere("getInactiveUsers", "Error getting inactive users for period: " + period + ", error: " + e.getMessage());
            throw new ServiceException("Error getting inactive users: " + e.getMessage(), e);
        }
    }
    public boolean createLotteryTicket(LotteryTicket ticket) throws ServiceException {
        logInfo("createLotteryTicket", "Creating lottery ticket with ID: " + ticket.getTicketID());

        if (ticket == null) {
            logWarning("createLotteryTicket", "Null ticket provided");
            throw new ServiceException("Ticket is required");
        }

        if (ticket.getTicketID() == null || ticket.getTicketID().trim().isEmpty()) {
            logWarning("createLotteryTicket", "Invalid ticket ID provided: " + ticket.getTicketID());
            throw new ServiceException("Ticket ID is required");
        }

        if (!InputValidator.isValidTicketId(ticket.getTicketID())) {
            logWarning("createLotteryTicket", "Invalid ticket ID format: " + ticket.getTicketID());
            throw new ServiceException("Invalid ticket ID format");
        }

        if (ticket.getNumbers() == null || ticket.getNumbers().trim().isEmpty()) {
            logWarning("createLotteryTicket", "Invalid numbers provided: " + ticket.getNumbers());
            throw new ServiceException("Numbers are required");
        }

        if (!InputValidator.isValidNumbers(ticket.getNumbers())) {
            logWarning("createLotteryTicket", "Invalid numbers format: " + ticket.getNumbers());
            throw new ServiceException("Invalid numbers format");
        }

        if (ticket.getCompany() == null || ticket.getCompany().trim().isEmpty()) {
            logWarning("createLotteryTicket", "Invalid company provided: " + ticket.getCompany());
            throw new ServiceException("Company is required");
        }

        if (!InputValidator.isValidCompany(ticket.getCompany())) {
            logWarning("createLotteryTicket", "Invalid company format: " + ticket.getCompany());
            throw new ServiceException("Invalid company format");
        }

        if (ticket.getPrice() < 0 || ticket.getPrice() > 1000000) {
            logWarning("createLotteryTicket", "Invalid ticket price: " + ticket.getPrice());
            throw new ServiceException("Invalid ticket price");
        }

        if (LotteryTicketDAO.existsTicketId(ticket.getTicketID())) {
            logWarning("createLotteryTicket", "Ticket ID already exists: " + ticket.getTicketID());
            throw new ServiceException("Ticket ID already exists: " + ticket.getTicketID());
        }

        if (LotteryTicketDAO.existsNumbers(ticket.getNumbers(), ticket.getCompany(), ticket.getCreationDate())) {
            logWarning("createLotteryTicket",
                    "Ticket with these numbers already exists for this company today: " + ticket.getNumbers());
            throw new ServiceException("Ticket with these numbers already exists for this company today.");
        }

        try {
            boolean success = LotteryTicketDAO.createLotteryTicket(ticket);
            if (success) {
                logInfo("createLotteryTicket", "Lottery ticket created successfully with ID: " + ticket.getTicketID());
            } else {
                logWarning("createLotteryTicket", "Failed to create lottery ticket with ID: " + ticket.getTicketID());
            }
            return success;
        } catch (Exception e) {
            logSevere("createLotteryTicket",
                    "Error creating lottery ticket with ID: " + ticket.getTicketID() + ", error: " + e.getMessage());
            throw new ServiceException("Error creating lottery ticket: " + e.getMessage(), e);
        }
    }
    public boolean updateUserRole(int userId, String role) throws ServiceException {
        logInfo("updateUserRole", "Updating user role for user ID: " + userId + " to role: " + role);

        if (userId <= 0) {
            logWarning("updateUserRole", "Invalid user ID: " + userId);
            throw new ServiceException("Invalid user ID: " + userId);
        }

        if (role == null || role.trim().isEmpty() || role.length() > 20) {
            logWarning("updateUserRole", "Invalid role: " + role);
            throw new ServiceException("Invalid role: " + role);
        }

        if (!InputValidator.isValidRole(role)) {
            logWarning("updateUserRole", "Invalid role value: " + role);
            throw new ServiceException("Invalid role value: " + role);
        }

        String sanitizedRole = InputValidator.sanitizeString(role);

        try {
            boolean success = UserDAO.updateUserRole(userId, sanitizedRole);
            if (success) {
                logInfo("updateUserRole", "User role updated successfully for user ID: " + userId);
            } else {
                logWarning("updateUserRole", "Failed to update user role for user ID: " + userId);
            }
            return success;
        } catch (Exception e) {
            logSevere("updateUserRole", "Error updating user role for user ID: " + userId + ", error: " + e.getMessage());
            throw new ServiceException("Error updating user role: " + e.getMessage(), e);
        }
    }
    public boolean setUserActiveStatus(int userId, boolean isActive) throws ServiceException {
        logInfo("setUserActiveStatus", "Setting user active status for user ID: " + userId + " to: " + isActive);

        if (userId <= 0) {
            logWarning("setUserActiveStatus", "Invalid user ID: " + userId);
            throw new ServiceException("Invalid user ID: " + userId);
        }

        try {
            boolean success = UserDAO.setUserActiveStatus(userId, isActive);
            if (success) {
                logInfo("setUserActiveStatus", "User active status updated successfully for user ID: " + userId);
            } else {
                logWarning("setUserActiveStatus", "Failed to update user active status for user ID: " + userId);
            }
            return success;
        } catch (Exception e) {
            logSevere("setUserActiveStatus",
                    "Error setting user active status for user ID: " + userId + ", error: " + e.getMessage());
            throw new ServiceException("Error setting user active status: " + e.getMessage(), e);
        }
    }
    public boolean updateUserDetails(int userId, String email, String firstName, String lastName, String phone)
            throws ServiceException {
        logInfo("updateUserDetails", "Updating user details for user ID: " + userId);

        if (userId <= 0) {
            logWarning("updateUserDetails", "Invalid user ID: " + userId);
            throw new ServiceException("Invalid user ID: " + userId);
        }

        if (email != null && !email.isEmpty() && !InputValidator.isValidEmail(email)) {
            logWarning("updateUserDetails", "Invalid email: " + email);
            throw new ServiceException("Invalid email: " + email);
        }

        if (firstName != null && firstName.length() > 50) {
            logWarning("updateUserDetails", "First name too long: " + firstName);
            throw new ServiceException("First name too long: " + firstName);
        }

        if (lastName != null && lastName.length() > 50) {
            logWarning("updateUserDetails", "Last name too long: " + lastName);
            throw new ServiceException("Last name too long: " + lastName);
        }

        if (phone != null && phone.length() > 20) {
            logWarning("updateUserDetails", "Phone number too long: " + phone);
            throw new ServiceException("Phone number too long: " + phone);
        }

        String sanitizedEmail = InputValidator.sanitizeString(email);
        String sanitizedFirstName = InputValidator.sanitizeString(firstName);
        String sanitizedLastName = InputValidator.sanitizeString(lastName);
        String sanitizedPhone = InputValidator.sanitizeString(phone);

        try {
            boolean success = UserDAO.updateUserDetails(userId, sanitizedEmail, sanitizedFirstName, sanitizedLastName,
                    sanitizedPhone);
            if (success) {
                logInfo("updateUserDetails", "User details updated successfully for user ID: " + userId);
            } else {
                logWarning("updateUserDetails", "Failed to update user details for user ID: " + userId);
            }
            return success;
        } catch (Exception e) {
            logSevere("updateUserDetails", "Error updating user details for user ID: " + userId + ", error: " + e.getMessage());
            throw new ServiceException("Error updating user details: " + e.getMessage(), e);
        }
    }
    public boolean sendInvitation(String email) throws ServiceException {
        logInfo("sendInvitation", "Sending invitation to email: " + email);

        if (email == null || email.trim().isEmpty() || !InputValidator.isValidEmail(email)) {
            logWarning("sendInvitation", "Invalid email: " + email);
            throw new ServiceException("Invalid email: " + email);
        }

        try {
            
            return emailService.sendInvitationEmail(email, "User", "");
        } catch (Exception e) {
            logSevere("sendInvitation", "Error sending invitation to email: " + email + ", error: " + e.getMessage());
            throw new ServiceException("Error sending invitation: " + e.getMessage(), e);
        }
    }
    public boolean processPriceUpdates(Map<String, String> priceParams)
            throws ServiceException {

        logInfo("processPriceUpdates", "Processing price updates for " + priceParams.size() + " companies");

        try {
            
            for (Map.Entry<String, String> entry : priceParams.entrySet()) {
                String company = entry.getKey();
                String priceParam = entry.getValue();

                if (priceParam != null) {
                    priceParam = InputValidator.sanitizeString(priceParam);
                }

                if (priceParam != null && InputValidator.containsSQLInjectionPatterns(priceParam)) {
                    logWarning("processPriceUpdates",
                            "SQL injection patterns detected in price parameter for company: " + company);
                    throw new ServiceException("Invalid input values.");
                }

                if (priceParam != null && !priceParam.isEmpty()) {
                    Double priceObj = InputValidator.validateDouble(priceParam);

                    if (priceObj != null) {
                        double price = priceObj;

                        if (price < 0.01 || price > 1000) {
                            logWarning("processPriceUpdates", "Invalid price for company " + company + ": " + price);
                            continue;
                        }

                        if (company != null && !company.isEmpty() && InputValidator.isValidCompany(company)) {
                            if (!updatePriceForCompany(company, price)) {
                                logWarning("processPriceUpdates", "Failed to update price for company: " + company);
                                throw new ServiceException("Failed to update price for " + company);
                            }
                        }
                    }
                }
            }

            logInfo("processPriceUpdates", "Prices updated successfully");
            return true;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            logSevere("processPriceUpdates", "Error updating prices: " + e.getMessage());
            throw new ServiceException("Error updating prices: " + e.getMessage(), e);
        }
    }
    public LotteryTicket processTicketCreation(
            String ticketID, String num1, String num2, String num3, String num4, String num5, String num6,
            String company, String publishedStr)
            throws ServiceException {

        logInfo("processTicketCreation", "Processing ticket creation for ticket ID: " + ticketID);

        if (ticketID != null && !ticketID.isEmpty() && !InputValidator.isValidTicketId(ticketID)) {
            logWarning("processTicketCreation", "Invalid ticket ID format: " + ticketID);
            throw new ServiceException("Invalid ticket ID format.");
        }

        if (company != null && !company.isEmpty() && !InputValidator.isValidCompany(company)) {
            logWarning("processTicketCreation", "Invalid company name: " + company);
            throw new ServiceException("Invalid company name.");
        }

        com.lottery.util.LotteryNumberParser.ParseResult parseResult =
                com.lottery.util.LotteryNumberParser.parse(num1, num2, num3, num4, num5, num6);

        if (!parseResult.isValid()) {
            logWarning("processTicketCreation", "Number validation failed: " + parseResult.getErrorMessage());
            throw new ServiceException(parseResult.getErrorMessage());
        }

        String numbers = parseResult.toCsvString();
        boolean allProvided = (numbers != null && numbers.split(",").length == 6);

        if (ticketID != null && !ticketID.isEmpty() &&
                allProvided &&
                company != null && !company.isEmpty()) {

            LotteryTicket ticket = new LotteryTicket(numbers, company);
            ticket.setTicketID(ticketID);

            double price = LotteryTicketDAO.getPriceForCompany(company);
            ticket.setPrice(price);

            if (publishedStr != null && "on".equals(publishedStr)) {
                ticket.setPublished(true);
            }

            if (createLotteryTicket(ticket)) {
                logInfo("processTicketCreation", "Lottery ticket created successfully with ID: " + ticketID);
                return ticket;
            } else {
                logWarning("processTicketCreation", "Failed to create lottery ticket with ID: " + ticketID);
                throw new ServiceException("Failed to create lottery ticket.");
            }
        } else {
            logWarning("processTicketCreation", "Missing required parameters for ticket creation");
            throw new ServiceException("Please provide ticket ID, all 6 numbers, and company.");
        }
    }
    public boolean processTicketStatusUpdate(String ticketId, String publishedStr)
            throws ServiceException, TicketNotFoundException {

        logInfo("processTicketStatusUpdate", "Processing ticket status update for ticket ID: " + ticketId);

        if (ticketId != null && !ticketId.isEmpty() && !InputValidator.isValidTicketId(ticketId)) {
            logWarning("processTicketStatusUpdate", "Invalid ticket ID: " + ticketId);
            throw new ServiceException("Invalid ticket ID.");
        }

        boolean published = "true".equals(publishedStr);

        if (updateTicketStatus(ticketId, published)) {
            logInfo("processTicketStatusUpdate", "Ticket status updated successfully for ticket ID: " + ticketId);
            return true;
        } else {
            logWarning("processTicketStatusUpdate", "Failed to update ticket status for ticket ID: " + ticketId);
            throw new ServiceException("Failed to update ticket status.");
        }
    }
    public boolean processBulkTicketStatusUpdate(String selectedTicketsStr, String publishedStr)
            throws ServiceException {

        logInfo("processBulkTicketStatusUpdate", "Processing bulk ticket status update");

        if (selectedTicketsStr != null && !selectedTicketsStr.isEmpty()) {

            String[] ticketIds = selectedTicketsStr.split(",");
            java.util.List<String> ticketIdList = new java.util.ArrayList<>();

            for (String ticketIdStr : ticketIds) {
                String trimmedId = ticketIdStr.trim();
                if (InputValidator.isValidTicketId(trimmedId)) {
                    ticketIdList.add(trimmedId);
                }
            }

            boolean published = "true".equals(publishedStr);

            if (!ticketIdList.isEmpty()) {
                if (updateTicketStatus(ticketIdList, published)) {
                    logInfo("processBulkTicketStatusUpdate", ticketIdList.size() + " ticket(s) status updated successfully.");
                    return true;
                } else {
                    logWarning("processBulkTicketStatusUpdate", "Failed to update ticket status for " + ticketIdList.size() + " tickets.");
                    throw new ServiceException("Failed to update ticket status.");
                }
            } else {
                logWarning("processBulkTicketStatusUpdate", "No valid tickets selected for bulk update.");
                throw new ServiceException("No valid tickets selected.");
            }
        } else {
            logWarning("processBulkTicketStatusUpdate", "No tickets selected for bulk update.");
            throw new ServiceException("No tickets selected.");
        }
    }
    public boolean processBulkUserDeactivation(String[] userIdsStr)
            throws ServiceException {

        logInfo("processBulkUserDeactivation", "Processing bulk user deactivation");

        try {
            if (userIdsStr != null && userIdsStr.length > 0) {
                java.util.List<Integer> userIdList = new java.util.ArrayList<>();
                for (String userIdStr : userIdsStr) {
                    try {
                        int userId = Integer.parseInt(userIdStr.trim());
                        
                        if (userId > 0) {
                            userIdList.add(userId);
                        }
                    } catch (NumberFormatException e) {
                        
                        logWarning("processBulkUserDeactivation", "Invalid user ID format: " + userIdStr);
                    }
                }

                if (!userIdList.isEmpty()) {
                    if (bulkDeactivateUsers(userIdList)) {
                        logInfo("processBulkUserDeactivation", userIdList.size() + " users deactivated successfully.");
                        return true;
                    } else {
                        logWarning("processBulkUserDeactivation", "Failed to deactivate users.");
                        throw new ServiceException("Failed to deactivate users.");
                    }
                } else {
                    logWarning("processBulkUserDeactivation", "No valid users selected for deactivation.");
                    throw new ServiceException("No valid users selected.");
                }
            } else {
                logWarning("processBulkUserDeactivation", "No users selected for deactivation.");
                throw new ServiceException("No users selected.");
            }
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            logSevere("processBulkUserDeactivation", "Error deactivating users: " + e.getMessage());
            throw new ServiceException("Error deactivating users: " + e.getMessage(), e);
        }
    }
    public boolean processUserRoleUpdate(String userIdStr, String role)
            throws ServiceException {

        logInfo("processUserRoleUpdate", "Processing user role update for user ID: " + userIdStr);

        try {
            int userId = Integer.parseInt(userIdStr);

            if (userId > 0 && role != null && !role.isEmpty()) {
                
                if (InputValidator.containsSQLInjectionPatterns(role)) {
                    logWarning("processUserRoleUpdate", "SQL injection patterns detected in role: " + role);
                    throw new ServiceException("Invalid role value.");
                }

                if (updateUserRole(userId, role)) {
                    logInfo("processUserRoleUpdate", "User role updated successfully for user ID: " + userId);
                    return true;
                } else {
                    logWarning("processUserRoleUpdate", "Failed to update user role for user ID: " + userId);
                    throw new ServiceException("Failed to update user role.");
                }
            } else {
                logWarning("processUserRoleUpdate", "Invalid user ID or role: " + userId + ", " + role);
                throw new ServiceException("Invalid user ID or role.");
            }
        } catch (NumberFormatException e) {
            logWarning("processUserRoleUpdate", "Invalid user ID format: " + userIdStr);
            throw new ServiceException("Invalid user ID.");
        }
    }
    public boolean processUserActiveStatusUpdate(String userIdStr, String isActiveStr)
            throws ServiceException {

        logInfo("processUserActiveStatusUpdate", "Processing user active status update for user ID: " + userIdStr);

        try {
            int userId = Integer.parseInt(userIdStr);
            boolean isActive = Boolean.parseBoolean(isActiveStr);

            if (userId > 0) {
                if (setUserActiveStatus(userId, isActive)) {
                    logInfo("processUserActiveStatusUpdate", "User status updated successfully for user ID: " + userId);
                    return true;
                } else {
                    logWarning("processUserActiveStatusUpdate", "Failed to update user status for user ID: " + userId);
                    throw new ServiceException("Failed to update user status.");
                }
            } else {
                logWarning("processUserActiveStatusUpdate", "Invalid user ID: " + userId);
                throw new ServiceException("Invalid user ID.");
            }
        } catch (NumberFormatException e) {
            logWarning("processUserActiveStatusUpdate", "Invalid user ID format: " + userIdStr);
            throw new ServiceException("Invalid user ID.");
        }
    }
    public boolean processUserDetailsUpdate(String userIdStr, String email, String firstName, String lastName,
            String phone)
            throws ServiceException {

        logInfo("processUserDetailsUpdate", "Processing user details update for user ID: " + userIdStr);

        try {
            int userId = Integer.parseInt(userIdStr);

            if (userId > 0 && email != null && !email.isEmpty() && InputValidator.isValidEmail(email)) {
                firstName = firstName != null ? InputValidator.sanitizeString(firstName) : "";
                lastName = lastName != null ? InputValidator.sanitizeString(lastName) : "";
                phone = phone != null ? InputValidator.sanitizeString(phone) : "";

                if (InputValidator.containsSQLInjectionPatterns(email) ||
                        InputValidator.containsSQLInjectionPatterns(firstName) ||
                        InputValidator.containsSQLInjectionPatterns(lastName) ||
                        InputValidator.containsSQLInjectionPatterns(phone)) {
                    logWarning("processUserDetailsUpdate", "SQL injection patterns detected in user details");
                    throw new ServiceException("Invalid input values.");
                }

                if (updateUserDetails(userId, email, firstName, lastName, phone)) {
                    logInfo("processUserDetailsUpdate", "User details updated successfully for user ID: " + userId);
                    return true;
                } else {
                    logWarning("processUserDetailsUpdate", "Failed to update user details for user ID: " + userId);
                    throw new ServiceException("Failed to update user details.");
                }
            } else {
                logWarning("processUserDetailsUpdate", "Invalid user ID or email: " + userId + ", " + email);
                throw new ServiceException("Invalid user ID or email.");
            }
        } catch (NumberFormatException e) {
            logWarning("processUserDetailsUpdate", "Invalid user ID format: " + userIdStr);
            throw new ServiceException("Invalid user ID.");
        }
    }
    public boolean processUserCreation(String email, String password, String firstName, String lastName, String role,
            String balanceStr, boolean isActive)
            throws ServiceException {

        logInfo("processUserCreation", "Processing user creation for email: " + email);

        if (email == null || email.trim().isEmpty() || !InputValidator.isValidEmail(email)) {
            logWarning("processUserCreation", "Invalid email: " + email);
            throw new ServiceException("Please provide a valid email address.");
        }

        if (password == null || password.trim().isEmpty()) {
            logWarning("processUserCreation", "Empty password");
            throw new ServiceException("Password cannot be empty.");
        }
        String passwordError = com.lottery.util.PasswordValidator.validate(password);
        if (passwordError != null) {
            logWarning("processUserCreation", "Invalid password: " + passwordError);
            throw new ServiceException(passwordError);
        }

        if (firstName == null || firstName.trim().isEmpty() || lastName == null || lastName.trim().isEmpty()) {
            throw new ServiceException("First name and last name are required.");
        }

        if (!"user".equals(role) && !"admin".equals(role) && !"none".equals(role)) {
            throw new ServiceException("Invalid role selected.");
        }

        java.math.BigDecimal initialBalance = java.math.BigDecimal.ZERO;
        if (balanceStr != null && !balanceStr.trim().isEmpty()) {
            try {
                initialBalance = new java.math.BigDecimal(balanceStr.trim());
                if (initialBalance.compareTo(java.math.BigDecimal.ZERO) < 0) {
                    throw new ServiceException("Balance cannot be negative.");
                }
            } catch (NumberFormatException e) {
                throw new ServiceException("Invalid balance format.");
            }
        }

        if (UserDAO.getUserByEmail(email) != null) {
            logWarning("processUserCreation", "Email already exists in the system: " + email);
            throw new ServiceException("An account with this email already exists.");
        }

        User newUser = new User();
        newUser.setEmail(email.trim());
        newUser.setPasswordHash(com.lottery.util.PasswordUtil.hash(password));
        newUser.setFirstName(InputValidator.sanitizeString(firstName.trim()));
        newUser.setLastName(InputValidator.sanitizeString(lastName.trim()));
        newUser.setRole(role);
        newUser.setActive(isActive);
        newUser.setBalance(initialBalance);

        if (!UserDAO.createUser(newUser)) {
            logWarning("processUserCreation", "Failed to create account for email: " + email);
            throw new ServiceException("Failed to create user account.");
        }

        logInfo("processUserCreation", "Successfully created user account for: " + email);
        return true;
    }
    public List<PaymentMethod> getAllPaymentMethodsWithUsers(int offset, int limit)
            throws ServiceException {
        logInfo("getAllPaymentMethodsWithUsers", "Getting all payment methods with users, offset: " + offset + ", limit: " + limit);

        if (offset < 0 || limit <= 0 || (limit > 1000 && limit != Integer.MAX_VALUE)) {
            logWarning("getAllPaymentMethodsWithUsers", "Invalid pagination parameters: offset=" + offset + ", limit=" + limit);
            throw new ServiceException("Invalid pagination parameters");
        }

        try {
            return PaymentMethodDAO.getAllPaymentMethodsWithUsers(offset, limit);
        } catch (Exception e) {
            logSevere("getAllPaymentMethodsWithUsers", "Error getting all payment methods with users: " + e.getMessage());
            throw new ServiceException("Error getting all payment methods with users: " + e.getMessage(), e);
        }
    }
    public int getTotalPaymentMethodCount() throws ServiceException {
        try {
            return PaymentMethodDAO.getTotalPaymentMethodCount();
        } catch (Exception e) {
            throw new ServiceException("Error getting total payment method count: " + e.getMessage(), e);
        }
    }
    public boolean updatePaymentMethod(PaymentMethod paymentMethod)
            throws ServiceException {
        logInfo("updatePaymentMethod", "Updating payment method ID: " + paymentMethod.getId());

        if (paymentMethod == null) {
            logWarning("updatePaymentMethod", "Null payment method provided");
            throw new ServiceException("Payment method is required");
        }

        if (paymentMethod.getId() <= 0) {
            logWarning("updatePaymentMethod", "Invalid payment method ID: " + paymentMethod.getId());
            throw new ServiceException("Invalid payment method ID");
        }

        if (paymentMethod.getLastFourDigits() == null || paymentMethod.getLastFourDigits().trim().isEmpty()) {
            logWarning("updatePaymentMethod", "Invalid last four digits: " + paymentMethod.getLastFourDigits());
            throw new ServiceException("Last four digits are required");
        }

        if (paymentMethod.getCardHolder() == null || paymentMethod.getCardHolder().trim().isEmpty()) {
            logWarning("updatePaymentMethod", "Invalid card holder: " + paymentMethod.getCardHolder());
            throw new ServiceException("Card holder is required");
        }

        if (paymentMethod.getExpiryDate() == null || paymentMethod.getExpiryDate().trim().isEmpty()) {
            logWarning("updatePaymentMethod", "Invalid expiry date: " + paymentMethod.getExpiryDate());
            throw new ServiceException("Expiry date is required");
        }

        try {
            boolean success = PaymentMethodDAO.updatePaymentMethod(paymentMethod);
            if (success) {
                logInfo("updatePaymentMethod", "Payment method updated successfully with ID: " + paymentMethod.getId());
            } else {
                logWarning("updatePaymentMethod", "Failed to update payment method with ID: " + paymentMethod.getId());
            }
            return success;
        } catch (Exception e) {
            logSevere("updatePaymentMethod",
                    "Error updating payment method with ID: " + paymentMethod.getId() + ", error: " + e.getMessage());
            throw new ServiceException("Error updating payment method: " + e.getMessage(), e);
        }
    }
    public boolean deletePaymentMethod(int paymentMethodId)
            throws ServiceException {
        logInfo("deletePaymentMethod", "Deleting payment method ID: " + paymentMethodId);

        if (paymentMethodId <= 0) {
            logWarning("deletePaymentMethod", "Invalid payment method ID: " + paymentMethodId);
            throw new ServiceException("Invalid payment method ID: " + paymentMethodId);
        }

        try {
            
            List<Integer> paymentMethodIds = new ArrayList<>();
            paymentMethodIds.add(paymentMethodId);

            boolean success = PaymentMethodDAO.deletePaymentMethods(paymentMethodIds);
            if (success) {
                logInfo("deletePaymentMethod", "Payment method deleted successfully with ID: " + paymentMethodId);
            } else {
                logWarning("deletePaymentMethod", "Failed to delete payment method with ID: " + paymentMethodId);
            }
            return success;
        } catch (Exception e) {
            logSevere("deletePaymentMethod",
                    "Error deleting payment method with ID: " + paymentMethodId + ", error: " + e.getMessage());
            throw new ServiceException("Error deleting payment method: " + e.getMessage(), e);
        }
    }
    public boolean bulkDeletePaymentMethods(String[] paymentMethodIdsStr)
            throws ServiceException {
        logInfo("bulkDeletePaymentMethods", "Bulk deleting payment methods");

        try {
            if (paymentMethodIdsStr != null && paymentMethodIdsStr.length > 0) {
                List<Integer> paymentMethodIdList = new ArrayList<>();
                for (String paymentMethodIdStr : paymentMethodIdsStr) {
                    try {
                        int paymentMethodId = Integer.parseInt(paymentMethodIdStr.trim());
                        if (paymentMethodId > 0) {
                            paymentMethodIdList.add(paymentMethodId);
                        }
                    } catch (NumberFormatException e) {
                        
                        logWarning("bulkDeletePaymentMethods", "Invalid payment method ID format: " + paymentMethodIdStr);
                    }
                }

                if (!paymentMethodIdList.isEmpty()) {
                    if (PaymentMethodDAO.deletePaymentMethods(paymentMethodIdList)) {
                        logInfo("bulkDeletePaymentMethods", paymentMethodIdList.size() + " payment methods deleted successfully.");
                        return true;
                    } else {
                        logWarning("bulkDeletePaymentMethods", "Failed to delete payment methods.");
                        throw new ServiceException("Failed to delete payment methods.");
                    }
                } else {
                    logWarning("bulkDeletePaymentMethods", "No valid payment methods selected for deletion.");
                    throw new ServiceException("No valid payment methods selected.");
                }
            } else {
                logWarning("bulkDeletePaymentMethods", "No payment methods selected for deletion.");
                throw new ServiceException("No payment methods selected.");
            }
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            logSevere("bulkDeletePaymentMethods", "Error deleting payment methods: " + e.getMessage());
            throw new ServiceException("Error deleting payment methods: " + e.getMessage(), e);
        }
    }
    public java.util.Map<String, Object> processBulkTicketInsertion(String csvData) throws ServiceException {
        logInfo("processBulkTicketInsertion", "Processing bulk ticket insertion");

        java.util.Map<String, Object> results = new java.util.HashMap<>();
        List<LotteryTicket> ticketsToInsert = new ArrayList<>();
        List<String> duplicates = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int successCount = 0;

        if (csvData == null || csvData.trim().isEmpty()) {
            throw new ServiceException("CSV data is empty");
        }

        String[] lines = csvData.split("\\r?\\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty())
                continue;

            String[] parts = line.split(",");
            if (parts.length < 8) {
                errors.add("Line " + (i + 1) + ": Invalid format (expected 8+ columns)");
                continue;
            }

            try {
                String ticketID = parts[0].trim();
                String numbers = String.join(",", parts[1].trim(), parts[2].trim(), parts[3].trim(),
                        parts[4].trim(), parts[5].trim(), parts[6].trim());
                String company = parts[7].trim();

                if (!InputValidator.isValidTicketId(ticketID) ||
                        !InputValidator.isValidNumbers(numbers) ||
                        !InputValidator.isValidCompany(company)) {
                    errors.add("Line " + (i + 1) + ": Invalid data format for ticket " + ticketID);
                    continue;
                }

                boolean batchDuplicate = ticketsToInsert.stream().anyMatch(t -> t.getTicketID().equals(ticketID));
                if (batchDuplicate || LotteryTicketDAO.existsTicketId(ticketID)) {
                    duplicates.add(ticketID);
                    continue;
                }

                LotteryTicket ticket = new LotteryTicket(numbers, company);
                ticket.setTicketID(ticketID);

                double price = LotteryTicketDAO.getPriceForCompany(company);
                ticket.setPrice(price);

                if (parts.length > 8)
                    ticket.setPublished(
                            "true".equalsIgnoreCase(parts[8].trim()) || "on".equalsIgnoreCase(parts[8].trim()));

                ticketsToInsert.add(ticket);
            } catch (Exception e) {
                errors.add("Line " + (i + 1) + ": Error parsing data - " + e.getMessage());
            }
        }

        if (!ticketsToInsert.isEmpty()) {
            successCount = LotteryTicketDAO.createLotteryTickets(ticketsToInsert);
        }

        results.put("successCount", successCount);
        results.put("duplicates", duplicates);
        results.put("errors", errors);
        results.put("totalProcessed", lines.length);

        logInfo("processBulkTicketInsertion", "Bulk insertion completed. Success: " + successCount + ", Duplicates: "
                + duplicates.size() + ", Errors: " + errors.size());
        return results;
    }
    public boolean createPaymentMethod(PaymentMethod paymentMethod) throws ServiceException {
        logInfo("createPaymentMethod", "Creating payment method for user ID: " + paymentMethod.getUserId());

        if (paymentMethod.getUserId() <= 0) {
            throw new ServiceException("Invalid User ID");
        }
        if (paymentMethod.getCardHolder() == null || paymentMethod.getCardHolder().trim().isEmpty()) {
            throw new ServiceException("Card Holder name is required");
        }
        if (paymentMethod.getLastFourDigits() == null || paymentMethod.getLastFourDigits().length() != 4) {
            throw new ServiceException("Invalid card number. Last four digits are required.");
        }
        if (paymentMethod.getExpiryDate() == null
                || !paymentMethod.getExpiryDate().matches("(0[1-9]|1[0-2])/[0-9]{2}")) {
            throw new ServiceException("Invalid expiry date format. Use MM/YY");
        }

        try {
            
            if (UserDAO.getUserById(paymentMethod.getUserId()) == null) {
                throw new ServiceException("User with ID " + paymentMethod.getUserId() + " does not exist.");
            }

            boolean success = PaymentMethodDAO.addPaymentMethod(paymentMethod);
            if (success) {
                logInfo("createPaymentMethod", "Payment method created successfully for user ID: " + paymentMethod.getUserId());
            } else {
                logWarning("createPaymentMethod", "Failed to create payment method for user ID: " + paymentMethod.getUserId());
            }
            return success;
        } catch (Exception e) {
            logSevere("createPaymentMethod", "Error creating payment method: " + e.getMessage());
            throw new ServiceException("Error creating payment method: " + e.getMessage(), e);
        }
    }
}
