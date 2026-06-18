package com.lottery.db;

import com.lottery.model.LotteryTicket;
import com.lottery.util.InputValidator;
import java.sql.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

public class LotteryTicketDAO extends BaseDAO {

    private static final String[] VALID_FIELD_NAMES = {
            "Published", "Price", "OwnerID", "ViewCount"
    };

    private static boolean isValidFieldName(String fieldName) {
        if (fieldName == null || fieldName.isEmpty()) {
            return false;
        }

        for (String validField : VALID_FIELD_NAMES) {
            if (validField.equals(fieldName)) {
                return true;
            }
        }

        return false;
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

    private static boolean isValidFieldValue(String fieldName, Object fieldValue) {
        if (fieldValue == null) {
            return true; 
        }

        switch (fieldName) {
            case "Published":
                return fieldValue instanceof Boolean;

            case "Price":
                if (fieldValue instanceof Double) {
                    double price = (Double) fieldValue;
                    return price >= 0 && price <= 1000000;
                } else if (fieldValue instanceof Integer) {
                    int price = (Integer) fieldValue;
                    return price >= 0 && price <= 1000000;
                }
                return false;

            case "OwnerID":
                if (fieldValue instanceof String) {
                    String ownerIdValue = (String) fieldValue;
                    
                    return ownerIdValue.isEmpty() || ownerIdValue.matches("^\\d+$");
                } else if (fieldValue instanceof Integer) {
                    int ownerIdInt = (Integer) fieldValue;
                    return ownerIdInt > 0;
                }
                return false;

            case "ViewCount":
                if (fieldValue instanceof Integer) {
                    int count = (Integer) fieldValue;
                    return count >= 0;
                }
                return false;

            default:
                return false;
        }
    }

    public static boolean createLotteryTicket(LotteryTicket ticket) {
        
        if (ticket == null) {
            logger.log(Level.WARNING, "Null ticket object provided for creation");
            return false;
        }

        if (!InputValidator.isValidTicketId(ticket.getTicketID()) ||
                !InputValidator.isValidCompany(ticket.getCompany()) ||
                !InputValidator.isValidNumbers(ticket.getNumbers())) {
            logger.log(Level.WARNING, "Invalid ticket data provided for creation: ticketID=" + ticket.getTicketID() +
                    ", company=" + ticket.getCompany() + ", numbers=" + ticket.getNumbers());
            return false;
        }

        if (ticket.getPrice() < 0 || ticket.getPrice() > 1000000) {
            logger.log(Level.WARNING, "Invalid ticket price: " + ticket.getPrice());
            return false;
        }

        String sanitizedCompany = InputValidator.sanitizeString(ticket.getCompany());
        String sanitizedOwnerId = InputValidator.sanitizeString(ticket.getOwnerId());

        String query = "INSERT INTO LotteryTickets (TicketID, Company, [Number 1], [Number 2], [Number 3], [Number 4], [Number 5], [Number 6], CreationDate, Published, ViewCount, Price, OwnerID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, ticket.getTicketID());
            statement.setString(2, sanitizedCompany);

            String[] nums = ticket.getNumbers().split(",");
            for (int i = 0; i < 6; i++) {
                statement.setInt(3 + i, Integer.parseInt(nums[i].trim()));
            }

            statement.setDate(9, Date.valueOf(ticket.getCreationDate()));
            statement.setBoolean(10, ticket.isPublished());
            statement.setInt(11, ticket.getViewCount());
            statement.setDouble(12, ticket.getPrice());
            
            if (sanitizedOwnerId != null && !sanitizedOwnerId.isEmpty()) {
                try {
                    int ownerID = Integer.parseInt(sanitizedOwnerId);
                    statement.setInt(13, ownerID);
                } catch (NumberFormatException e) {
                    
                    statement.setNull(13, java.sql.Types.INTEGER);
                }
            } else {
                statement.setNull(13, java.sql.Types.INTEGER);
            }

            int rowsAffected = statement.executeUpdate();
            logger.info("Created lottery ticket with ID: " + ticket.getTicketID() + ", rows affected: " + rowsAffected);
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error creating lottery ticket: " + ticket.getTicketID(), e);
            return false;
        }
    }

    public static boolean existsTicketId(String ticketId) {
        if (ticketId == null || ticketId.isEmpty()) {
            return false;
        }

        String query = "SELECT COUNT(*) FROM LotteryTickets WHERE TicketID = ?";

        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, ticketId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error checking ticket ID existence: " + ticketId, e);
        }

        return false;
    }

    public static boolean existsNumbers(String numbers, String company, LocalDate date) {
        if (numbers == null || company == null || date == null) {
            return false;
        }

        String query = "SELECT COUNT(*) FROM LotteryTickets WHERE [Number 1] = ? AND [Number 2] = ? AND [Number 3] = ? AND [Number 4] = ? AND [Number 5] = ? AND [Number 6] = ? AND Company = ? AND CreationDate = ?";

        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            String[] nums = numbers.split(",");
            for (int i = 0; i < 6; i++) {
                statement.setInt(1 + i, Integer.parseInt(nums[i].trim()));
            }

            statement.setString(7, company);
            statement.setDate(8, Date.valueOf(date));

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error checking numbers existence: " + numbers, e);
        }

        return false;
    }

    public static int createLotteryTickets(List<LotteryTicket> tickets) {
        if (tickets == null || tickets.isEmpty()) {
            return 0;
        }

        String query = "INSERT INTO LotteryTickets (TicketID, Company, [Number 1], [Number 2], [Number 3], [Number 4], [Number 5], [Number 6], CreationDate, Published, ViewCount, Price, OwnerID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            connection.setAutoCommit(false);

            for (LotteryTicket ticket : tickets) {
                statement.setString(1, ticket.getTicketID());
                statement.setString(2, InputValidator.sanitizeString(ticket.getCompany()));

                String[] nums = ticket.getNumbers().split(",");
                for (int i = 0; i < 6; i++) {
                    statement.setInt(3 + i, Integer.parseInt(nums[i].trim()));
                }

                statement.setDate(9, Date.valueOf(ticket.getCreationDate()));
                statement.setBoolean(10, ticket.isPublished());
                statement.setInt(11, ticket.getViewCount());
                statement.setDouble(12, ticket.getPrice());

                String sanitizedOwnerId = InputValidator.sanitizeString(ticket.getOwnerId());
                if (sanitizedOwnerId != null && !sanitizedOwnerId.isEmpty()) {
                    try {
                        int ownerID = Integer.parseInt(sanitizedOwnerId);
                        statement.setInt(13, ownerID);
                    } catch (NumberFormatException e) {
                        statement.setNull(13, java.sql.Types.INTEGER);
                    }
                } else {
                    statement.setNull(13, java.sql.Types.INTEGER);
                }

                statement.addBatch();
            }

            int[] results = statement.executeBatch();
            connection.commit();

            int count = 0;
            for (int res : results) {
                if (res >= 0 || res == Statement.SUCCESS_NO_INFO) {
                    count++;
                }
            }

            logger.info("Bulk created " + count + " lottery tickets");
            return count;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error bulk creating lottery tickets", e);
            return 0;
        }
    }

    public static LotteryTicket getTicketById(String ticketId) {
        
        if (ticketId == null || ticketId.isEmpty()) {
            logger.log(Level.WARNING, "Invalid ticket ID provided: " + ticketId);
            return null;
        }

        String query = "SELECT * FROM LotteryTickets WHERE TicketID = ?";

        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, ticketId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return LotteryTicketResultSetMapper.mapResultSetToLotteryTicket(resultSet);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving lottery ticket by ID: " + ticketId, e);
        }

        return null;
    }

    public static List<LotteryTicket> searchTickets(LotteryTicketSearchCriteria criteria) {
        List<LotteryTicket> tickets = new ArrayList<>();

        if (criteria == null) {
            logger.log(Level.WARNING, "Null search criteria provided");
            return tickets;
        }

        if (criteria.getOffset() < 0 || criteria.getLimit() <= 0
                || (criteria.getLimit() > 1000 && criteria.getLimit() != Integer.MAX_VALUE)) {
            logger.log(Level.WARNING, "Invalid pagination parameters: offset=" + criteria.getOffset() +
                    ", limit=" + criteria.getLimit());
            return tickets;
        }

        if (criteria.getNumbers() != null && !criteria.getNumbers().isEmpty() &&
                !isValidSearchNumbers(criteria.getNumbers())) {
            logger.log(Level.WARNING, "Invalid numbers format in search criteria: " + criteria.getNumbers());
            return tickets;
        }

        if (criteria.getCompany() != null && !criteria.getCompany().isEmpty()) {
            
            if (criteria.getCompany().contains(",")) {
                String[] companies = criteria.getCompany().split(",");
                for (String company : companies) {
                    if (!InputValidator.isValidCompany(company.trim())) {
                        logger.log(Level.WARNING, "Invalid company format in search criteria: " + company.trim());
                        return tickets;
                    }
                }
            } else {
                
                if (!InputValidator.isValidCompany(criteria.getCompany())) {
                    logger.log(Level.WARNING, "Invalid company format in search criteria: " + criteria.getCompany());
                    return tickets;
                }
            }
        }

        if (criteria.getOwnerId() != null && !criteria.getOwnerId().isEmpty() &&
                !InputValidator.isValidIdentifier(criteria.getOwnerId()) &&
                !criteria.getOwnerId().matches("^\\d+$")) {
            logger.log(Level.WARNING, "Invalid owner format in search criteria: " + criteria.getOwnerId());
            return tickets;
        }

        if (criteria.shouldApplyDateFilter()) {
            if (criteria.isUseDateRange()) {
                if (criteria.getStartDate() == null || criteria.getEndDate() == null) {
                    logger.log(Level.WARNING, "Date range search requires both start and end dates");
                    return tickets;
                }

                if (criteria.getStartDate().isAfter(criteria.getEndDate())) {
                    logger.log(Level.WARNING, "Start date cannot be after end date");
                    return tickets;
                }
            } else if (criteria.getSpecificDate() != null) {
                
                if (criteria.getSpecificDate().isAfter(LocalDate.now())) {
                    logger.log(Level.WARNING, "Specific date cannot be in the future");
                    return tickets;
                }
            }
        }

        LotteryTicketQueryBuilder queryBuilder = new LotteryTicketQueryBuilder(
                "SELECT * FROM LotteryTickets");

        boolean hasPositionNumbers = criteria.getNum1() != null || criteria.getNum2() != null ||
                criteria.getNum3() != null || criteria.getNum4() != null ||
                criteria.getNum5() != null || criteria.getNum6() != null;

        if (hasPositionNumbers) {
            queryBuilder.addPositionBasedNumbersSearchCondition(
                    criteria.getNum1(), criteria.getNum2(), criteria.getNum3(),
                    criteria.getNum4(), criteria.getNum5(), criteria.getNum6());
        }

        queryBuilder.addExactMatchCondition("Company", criteria.getCompany())
                .addCondition("Published", criteria.getPublished())
                .addCondition("OwnerID", criteria.getOwnerId());

        if (Boolean.TRUE.equals(criteria.getOnlyUnpurchased())) {
            queryBuilder.addNullCondition("OwnerID");
        }

        if (criteria.shouldApplyDateFilter()) {
            if (criteria.isUseDateRange()) {
                queryBuilder.addDateRangeCondition("CreationDate", criteria.getStartDate(), criteria.getEndDate());
            } else {
                queryBuilder.addDateCondition("CreationDate", criteria.getSpecificDate());
            }
        }

        queryBuilder
                .addOrderBy("CreationDate", true) 
                .addPagination(criteria.getOffset(), criteria.getLimit());

        String query = queryBuilder.getQuery();
        Object[] parameters = queryBuilder.getParameters();

        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            for (int i = 0; i < parameters.length; i++) {
                statement.setObject(i + 1, parameters[i]);
            }

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                tickets.add(LotteryTicketResultSetMapper.mapResultSetToLotteryTicket(resultSet));
            }

            logger.info("Found " + tickets.size() + " tickets matching search criteria");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error searching for lottery tickets", e);
        }

        return tickets;
    }

    public static List<LotteryTicket> searchPublishedTickets(LotteryTicketSearchCriteria criteria) {
        
        LotteryTicketSearchCriteria publishedCriteria = LotteryTicketSearchCriteria.create()
                .number1(criteria.getNum1())
                .number2(criteria.getNum2())
                .number3(criteria.getNum3())
                .number4(criteria.getNum4())
                .number5(criteria.getNum5())
                .number6(criteria.getNum6())
                .company(criteria.getCompany())
                .onlyUnpurchased(criteria.getOnlyUnpurchased())
                .published(true) 
                .pagination(criteria.getOffset(), criteria.getLimit());

        Boolean useDateRange = criteria.isUseDateRange();
        if (useDateRange != null && useDateRange) {
            publishedCriteria.dateRange(criteria.getStartDate(), criteria.getEndDate());
        } else if (useDateRange != null && !useDateRange) {
            publishedCriteria.specificDate(criteria.getSpecificDate());
        }
        
        return searchTickets(publishedCriteria);
    }

    public static int getSearchTicketCount(LotteryTicketSearchCriteria criteria) {
        
        if (criteria == null) {
            logger.log(Level.WARNING, "Null search criteria provided for count");
            return 0;
        }

        if (criteria.getNumbers() != null && !criteria.getNumbers().isEmpty() &&
                !isValidSearchNumbers(criteria.getNumbers())) {
            logger.log(Level.WARNING, "Invalid numbers format in search criteria for count: " + criteria.getNumbers());
            return 0;
        }

        if (criteria.getCompany() != null && !criteria.getCompany().isEmpty()) {
    
            if (criteria.getCompany().contains(",")) {
                String[] companies = criteria.getCompany().split(",");
                for (String company : companies) {
                    if (!InputValidator.isValidCompany(company.trim())) {
                        logger.log(Level.WARNING,
                                "Invalid company format in search criteria for count: " + company.trim());
                        return 0;
                    }
                }
            } else {
                
                if (!InputValidator.isValidCompany(criteria.getCompany())) {
                    logger.log(Level.WARNING,
                            "Invalid company format in search criteria for count: " + criteria.getCompany());
                    return 0;
                }
            }
        }

        if (criteria.getOwnerId() != null && !criteria.getOwnerId().isEmpty() &&
                !InputValidator.isValidIdentifier(criteria.getOwnerId()) &&
                !criteria.getOwnerId().matches("^\\d+$")) {
            logger.log(Level.WARNING, "Invalid owner format in search criteria for count: " + criteria.getOwnerId());
            return 0;
        }

        if (criteria.shouldApplyDateFilter()) {
            if (criteria.isUseDateRange()) {
                if (criteria.getStartDate() == null || criteria.getEndDate() == null) {
                    logger.log(Level.WARNING, "Date range search requires both start and end dates for count");
                    return 0;
                }

                if (criteria.getStartDate().isAfter(criteria.getEndDate())) {
                    logger.log(Level.WARNING, "Start date cannot be after end date for count");
                    return 0;
                }
            } else if (criteria.getSpecificDate() != null) {
                
                if (criteria.getSpecificDate().isAfter(LocalDate.now())) {
                    logger.log(Level.WARNING, "Specific date cannot be in the future for count");
                    return 0;
                }
            }
        }

        LotteryTicketQueryBuilder queryBuilder = new LotteryTicketQueryBuilder(
                "SELECT COUNT(*) AS total FROM LotteryTickets");

        boolean hasPositionNumbers = criteria.getNum1() != null || criteria.getNum2() != null ||
                criteria.getNum3() != null || criteria.getNum4() != null ||
                criteria.getNum5() != null || criteria.getNum6() != null;

        if (hasPositionNumbers) {
            queryBuilder.addPositionBasedNumbersSearchCondition(
                    criteria.getNum1(), criteria.getNum2(), criteria.getNum3(),
                    criteria.getNum4(), criteria.getNum5(), criteria.getNum6());
        }

        queryBuilder.addExactMatchCondition("Company", criteria.getCompany())
                .addCondition("Published", criteria.getPublished())
                .addCondition("OwnerID", criteria.getOwnerId());

        if (Boolean.TRUE.equals(criteria.getOnlyUnpurchased())) {
            queryBuilder.addNullCondition("OwnerID");
        }

        if (criteria.shouldApplyDateFilter()) {
            if (criteria.isUseDateRange()) {
                queryBuilder.addDateRangeCondition("CreationDate", criteria.getStartDate(), criteria.getEndDate());
            } else {
                queryBuilder.addDateCondition("CreationDate", criteria.getSpecificDate());
            }
        }

        String query = queryBuilder.getQuery();
        Object[] parameters = queryBuilder.getParameters();

        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            for (int i = 0; i < parameters.length; i++) {
                statement.setObject(i + 1, parameters[i]);
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt("total");
                    if (count < 0) {
                        logger.log(Level.WARNING, "Negative ticket count returned: " + count);
                        return 0;
                    }
                    logger.info("Counted " + count + " tickets matching search criteria");
                    return count;
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error counting lottery tickets", e);
        }

        return 0;
    }

    public static int getPublishedTicketCount(LotteryTicketSearchCriteria criteria) {
        
        LotteryTicketSearchCriteria publishedCriteria = LotteryTicketSearchCriteria.create()
                .number1(criteria.getNum1())
                .number2(criteria.getNum2())
                .number3(criteria.getNum3())
                .number4(criteria.getNum4())
                .number5(criteria.getNum5())
                .number6(criteria.getNum6())
                .company(criteria.getCompany())
                .onlyUnpurchased(criteria.getOnlyUnpurchased())
                .published(true); 

        Boolean useDateRange = criteria.isUseDateRange();
        if (useDateRange != null && useDateRange) {
            publishedCriteria.dateRange(criteria.getStartDate(), criteria.getEndDate());
        } else if (useDateRange != null && !useDateRange) {
            publishedCriteria.specificDate(criteria.getSpecificDate());
        }
        
        return getSearchTicketCount(publishedCriteria);
    }

    public static boolean updateTicketField(String ticketId, String fieldName, Object fieldValue) {
        
        if (!InputValidator.isValidTicketId(ticketId) || fieldName == null || fieldName.isEmpty()) {
            logger.log(Level.WARNING, "Invalid ticket ID or field name provided: ticketId=" + ticketId +
                    ", fieldName=" + fieldName);
            return false;
        }

        if (!isValidFieldName(fieldName)) {
            logger.log(Level.WARNING, "Invalid field name provided: " + fieldName);
            return false;
        }

        if (!isValidFieldValue(fieldName, fieldValue)) {
            logger.log(Level.WARNING, "Invalid field value for field '" + fieldName + "': " + fieldValue);
            return false;
        }

        String query = "UPDATE LotteryTickets SET " + fieldName + " = ? WHERE TicketID = ?";

        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            if (fieldValue instanceof Boolean) {
                statement.setBoolean(1, (Boolean) fieldValue);
            } else if (fieldValue instanceof String) {
                statement.setString(1, (String) fieldValue);
            } else if (fieldValue instanceof Double) {
                statement.setDouble(1, (Double) fieldValue);
            } else {
                statement.setObject(1, fieldValue);
            }

            statement.setString(2, ticketId);

            int rowsAffected = statement.executeUpdate();
            logger.info("Updated field '" + fieldName + "' for ticket ID: " + ticketId + ", rows affected: "
                    + rowsAffected);
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating field '" + fieldName + "' for ticket ID: " + ticketId, e);
            return false;
        }
    }

    public static boolean updateTicketField(Connection connection, String ticketId, String fieldName, Object fieldValue)
            throws SQLException {
        
        if (!InputValidator.isValidTicketId(ticketId) || fieldName == null || fieldName.isEmpty()) {
            logger.log(Level.WARNING, "Invalid ticket ID or field name provided: ticketId=" + ticketId +
                    ", fieldName=" + fieldName);
            return false;
        }

        if (!isValidFieldName(fieldName)) {
            logger.log(Level.WARNING, "Invalid field name provided: " + fieldName);
            return false;
        }

        if (!isValidFieldValue(fieldName, fieldValue)) {
            logger.log(Level.WARNING, "Invalid field value for field '" + fieldName + "': " + fieldValue);
            return false;
        }

        String query = "UPDATE LotteryTickets SET " + fieldName + " = ? WHERE TicketID = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {

            if (fieldValue instanceof Boolean) {
                statement.setBoolean(1, (Boolean) fieldValue);
            } else if (fieldValue instanceof String) {
                statement.setString(1, (String) fieldValue);
            } else if (fieldValue instanceof Double) {
                statement.setDouble(1, (Double) fieldValue);
            } else {
                statement.setObject(1, fieldValue);
            }

            statement.setString(2, ticketId);

            int rowsAffected = statement.executeUpdate();
            logger.info("Updated field '" + fieldName + "' for ticket ID: " + ticketId + ", rows affected: "
                    + rowsAffected);
            return rowsAffected > 0;
        }
    }

    public static boolean updateTicketStatus(String ticketId, boolean published) {
        return updateTicketField(ticketId, "Published", published);
    }

    public static boolean updateTicketStatus(List<String> ticketIds, boolean published) {
        
        if (ticketIds == null || ticketIds.isEmpty()) {
            logger.log(Level.WARNING, "Null or empty ticket IDs list provided for bulk update");
            return false;
        }

        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < ticketIds.size(); i++) {
            placeholders.append("?");
            if (i < ticketIds.size() - 1) {
                placeholders.append(",");
            }
        }

        String query = "UPDATE LotteryTickets SET Published = ? WHERE TicketID IN (" + placeholders.toString() + ")";
        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setBoolean(1, published);

            for (int i = 0; i < ticketIds.size(); i++) {
                statement.setString(i + 2, ticketIds.get(i));
            }

            int rowsAffected = statement.executeUpdate();
            boolean result = rowsAffected > 0;
            logger.info("Bulk updated ticket status for " + ticketIds.size() + " tickets, result: " + result);
            return result;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error bulk updating ticket status", e);
            return false;
        }
    }

    public static boolean updatePriceForCompany(String company, double price) {
        
        if (company == null || company.trim().isEmpty()) {
            logger.log(Level.WARNING, "Null or empty company name provided for price update");
            return false;
        }

        if (price < 0 || price > 1000000) {
            logger.log(Level.WARNING, "Invalid price provided for company '" + company + "': " + price);
            return false;
        }

        String query = "UPDATE LotteryTickets SET Price = ? WHERE Company = ?";
        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setDouble(1, price);
            statement.setString(2, company);

            int rowsAffected = statement.executeUpdate();
            boolean result = rowsAffected > 0;
            logger.info("Updated price for all tickets of company '" + company + "', result: " + result);
            return result;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating price for company: " + company, e);
            return false;
        }
    }

    public static boolean updateTicketOwner(String ticketId, String owner) {
        return updateTicketField(ticketId, "OwnerID", owner);
    }

    public static boolean updateTicketOwner(Connection connection, String ticketId, String owner) throws SQLException {
        return updateTicketField(connection, ticketId, "OwnerID", owner);
    }

    public static boolean updateTicketOwnersBatch(Connection connection, List<String> ticketIds, String owner) throws SQLException {
        if (ticketIds == null || ticketIds.isEmpty()) return false;
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < ticketIds.size(); i++) {
            placeholders.append("?");
            if (i < ticketIds.size() - 1) placeholders.append(",");
        }
        String query = "UPDATE LotteryTickets SET OwnerID = ? WHERE TicketID IN (" + placeholders.toString() + ")";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, owner);
            for (int i = 0; i < ticketIds.size(); i++) {
                statement.setString(i + 2, ticketIds.get(i));
            }
            int rowsAffected = statement.executeUpdate();
            logger.info("Batch updated owner for " + rowsAffected + " tickets");
            return rowsAffected > 0;
        }
    }

    public static boolean incrementViewCount(String ticketId) {
        
        if (!InputValidator.isValidTicketId(ticketId)) {
            logger.log(Level.WARNING, "Invalid ticket ID provided for view count increment: " + ticketId);
            return false;
        }

        String query = "UPDATE LotteryTickets SET ViewCount = ViewCount + 1 WHERE TicketID = ?";

        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, ticketId);

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Incremented view count for ticket ID: " + ticketId);
            }
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error incrementing view count for ticket ID: " + ticketId, e);
            return false;
        }
    }

    public static boolean incrementViewCountBatch(List<String> ticketIds) {
        if (ticketIds == null || ticketIds.isEmpty()) {
            return false;
        }

        String placeholders = String.join(",", Collections.nCopies(ticketIds.size(), "?"));
        String query = "UPDATE LotteryTickets SET ViewCount = ViewCount + 1 WHERE TicketID IN (" + placeholders + ")";

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            for (int i = 0; i < ticketIds.size(); i++) {
                stmt.setString(i + 1, ticketIds.get(i));
            }

            int rowsAffected = stmt.executeUpdate();
            logger.info("Batch incremented view count for " + rowsAffected + " tickets");
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Batch view count update failed", e);
            return false;
        }
    }

    public static double getPriceForCompany(String company) {
        
        if (!InputValidator.isValidCompany(company)) {
            logger.log(Level.WARNING, "Invalid company provided for price retrieval: " + company);
            return 10.0; 
        }

        String sanitizedCompany = InputValidator.sanitizeString(company);

        String query = "SELECT TOP 1 Price FROM LotteryTickets WHERE Company = ?";

        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, sanitizedCompany);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    double price = resultSet.getDouble("Price");
                    if (price < 0 || price > 1000000) {
                        logger.log(Level.WARNING, "Invalid price retrieved for company '" + company + "': " + price);
                        return 10.0; 
                    }
                    logger.info("Retrieved price for company '" + company + "': " + price);
                    return price;
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving price for company: " + company, e);
        }

        logger.info("Using default price for company '" + company + "': 10.0");
        return 10.0;
    }

    public static List<LotteryTicket> getAllTickets(int offset, int limit) {
        return searchTickets(LotteryTicketSearchCriteria.create().pagination(offset, limit));
    }

    public static int getTotalTicketCount() {
        return getSearchTicketCount(LotteryTicketSearchCriteria.create());
    }

    public static List<LotteryTicket> getTicketsByOwnerId(String ownerId, int offset, int limit) {
        return searchTickets(LotteryTicketSearchCriteria.create()
                .ownerId(ownerId)
                .pagination(offset, limit));
    }

    public static int getTicketCountByOwnerId(String ownerId) {
        return getSearchTicketCount(LotteryTicketSearchCriteria.create().ownerId(ownerId));
    }

    public static List<LotteryTicket> getPurchasedTickets() {
        List<LotteryTicket> tickets = new ArrayList<>();
        String query = "SELECT * FROM LotteryTickets WHERE OwnerID IS NOT NULL";

        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(query);
                ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                tickets.add(LotteryTicketResultSetMapper.mapResultSetToLotteryTicket(resultSet));
            }

            logger.info("Retrieved " + tickets.size() + " purchased tickets");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving purchased tickets", e);
        }

        return tickets;
    }

    public static boolean resetTicketsForNewRound() {
        String query = "UPDATE LotteryTickets SET OwnerID = NULL";

        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            int rowsAffected = statement.executeUpdate();
            logger.info("Reset tickets for new round, rows affected: " + rowsAffected);
            return rowsAffected >= 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error resetting tickets for new round", e);
            return false;
        }
    }

    public static List<LotteryTicket> getAllPublishedTickets(int offset, int limit) {
        return searchPublishedTickets(LotteryTicketSearchCriteria.create()
                .published(true)
                .pagination(offset, limit));
    }

    public static int getTotalPublishedTicketCount() {
        return getPublishedTicketCount(LotteryTicketSearchCriteria.create().published(true));
    }

    public static List<String> getAllPublishedCompanies() {
        List<String> companies = new ArrayList<>();
        String query = "SELECT DISTINCT Company FROM LotteryTickets WHERE Published = 1 ORDER BY Company";

        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(query);
                ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String company = resultSet.getString("Company");
                
                if (company != null && InputValidator.isValidCompany(company)) {
                    companies.add(company);
                }
            }

            logger.info("Retrieved " + companies.size() + " published companies");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving published companies", e);
        }

        return companies;
    }

    public static List<String> getAllDistinctCompanies() {
        List<String> companies = new ArrayList<>();
        String query = "SELECT DISTINCT Company FROM LotteryTickets ORDER BY Company";

        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(query);
                ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String company = resultSet.getString("Company");
                
                if (company != null && InputValidator.isValidCompany(company)) {
                    companies.add(company);
                }
            }

            logger.info("Retrieved " + companies.size() + " distinct companies");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving distinct companies", e);
        }

        return companies;
    }

    public static List<String> getAllCompanies() {
        List<String> companies = new ArrayList<>();
        String query = "SELECT DISTINCT Company FROM LotteryTickets ORDER BY Company";

        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(query);
                ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String company = resultSet.getString("Company");
                
                if (company != null && InputValidator.isValidCompany(company)) {
                    companies.add(company);
                }
            }

            logger.info("Retrieved " + companies.size() + " companies");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving companies", e);
        }

        return companies;
    }
}
