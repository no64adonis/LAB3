package com.lottery.db;

import com.lottery.model.TicketHistorySummary;
import com.lottery.model.UserTicketHistory;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class UserTicketHistoryDAO extends BaseDAO {

    public static boolean archiveTicket(int userId, String ticketId, String numbers, String company, int roundId,
            BigDecimal purchasePrice, BigDecimal winnings, int matchCount) {
        if (userId <= 0 || roundId <= 0 || ticketId == null || ticketId.isEmpty() || numbers == null
                || numbers.isEmpty()) {
            logger.log(Level.WARNING, "Invalid history archive parameters.");
            return false;
        }

        String query = "INSERT INTO UserTicketHistory (UserID, TicketID, Numbers, Company, RoundID, PurchasePrice, Winnings, MatchCount) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, userId);
            statement.setString(2, ticketId);
            statement.setString(3, numbers);
            statement.setString(4, company);
            statement.setInt(5, roundId);
            statement.setBigDecimal(6, purchasePrice);
            statement.setBigDecimal(7, winnings);
            statement.setInt(8, matchCount);

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error archiving ticket history", e);
            return false;
        }
    }

    public static List<UserTicketHistory> getHistoryByRound(int userId, int roundId) {
        List<UserTicketHistory> history = new ArrayList<>();
        if (userId <= 0 || roundId <= 0) {
            return history;
        }

        String query = "SELECT * FROM UserTicketHistory WHERE UserID = ? AND RoundID = ? ORDER BY ClaimDate DESC";

        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, userId);
            statement.setInt(2, roundId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    history.add(mapHistory(resultSet));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving ticket history", e);
        }

        return history;
    }

    public static TicketHistorySummary getFinancialSummary(int userId, int roundId) {
        if (userId <= 0 || roundId <= 0) {
            return new TicketHistorySummary(BigDecimal.ZERO, BigDecimal.ZERO);
        }

        String query = "SELECT COALESCE(SUM(PurchasePrice), 0) AS TotalSpent, "
                + "COALESCE(SUM(Winnings), 0) AS TotalWinnings FROM UserTicketHistory "
                + "WHERE UserID = ? AND RoundID = ?";

        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, userId);
            statement.setInt(2, roundId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    BigDecimal totalSpent = resultSet.getBigDecimal("TotalSpent");
                    BigDecimal totalWinnings = resultSet.getBigDecimal("TotalWinnings");
                    if (totalSpent == null) {
                        totalSpent = BigDecimal.ZERO;
                    }
                    if (totalWinnings == null) {
                        totalWinnings = BigDecimal.ZERO;
                    }
                    return new TicketHistorySummary(totalSpent, totalWinnings);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving financial summary", e);
        }

        return new TicketHistorySummary(BigDecimal.ZERO, BigDecimal.ZERO);
    }

    public static List<UserTicketHistory> getAllHistoryForUser(int userId, int offset, int limit) {
        List<UserTicketHistory> history = new ArrayList<>();
        if (userId <= 0) {
            return history;
        }

        String query = "SELECT * FROM UserTicketHistory WHERE UserID = ? ORDER BY ClaimDate DESC "
                + "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, userId);
            statement.setInt(2, offset);
            statement.setInt(3, limit);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    history.add(mapHistory(resultSet));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving all ticket history for user", e);
        }

        return history;
    }

    public static int countAllHistoryForUser(int userId) {
        if (userId <= 0) {
            return 0;
        }

        String query = "SELECT COUNT(*) AS total FROM UserTicketHistory WHERE UserID = ?";

        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("total");
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error counting ticket history for user", e);
        }

        return 0;
    }

    public static TicketHistorySummary getFinancialSummaryByTimePeriod(int userId, String period) {
        if (userId <= 0 || period == null || period.isEmpty()) {
            return new TicketHistorySummary(BigDecimal.ZERO, BigDecimal.ZERO);
        }

        String sqlUnit;
        switch (period.toLowerCase()) {
            case "week":
                sqlUnit = "week";
                break;
            case "month":
                sqlUnit = "month";
                break;
            case "quarter":
                sqlUnit = "quarter";
                break;
            case "year":
                sqlUnit = "year";
                break;
            default:
                return new TicketHistorySummary(BigDecimal.ZERO, BigDecimal.ZERO);
        }

        String query = "SELECT COALESCE(SUM(PurchasePrice), 0) AS TotalSpent, "
                + "COALESCE(SUM(Winnings), 0) AS TotalWinnings FROM UserTicketHistory "
                + "WHERE UserID = ? AND ClaimDate >= DATEADD(" + sqlUnit + ", -1, GETDATE())";

        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    BigDecimal totalSpent = resultSet.getBigDecimal("TotalSpent");
                    BigDecimal totalWinnings = resultSet.getBigDecimal("TotalWinnings");
                    if (totalSpent == null) {
                        totalSpent = BigDecimal.ZERO;
                    }
                    if (totalWinnings == null) {
                        totalWinnings = BigDecimal.ZERO;
                    }
                    return new TicketHistorySummary(totalSpent, totalWinnings);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving financial summary by time period", e);
        }

        return new TicketHistorySummary(BigDecimal.ZERO, BigDecimal.ZERO);
    }

    private static UserTicketHistory mapHistory(ResultSet resultSet) throws SQLException {
        UserTicketHistory history = new UserTicketHistory();
        history.setHistoryID(resultSet.getInt("HistoryID"));
        history.setUserID(resultSet.getInt("UserID"));
        history.setTicketID(resultSet.getString("TicketID"));
        history.setNumbers(resultSet.getString("Numbers"));
        history.setCompany(resultSet.getString("Company"));
        history.setRoundID(resultSet.getInt("RoundID"));
        history.setPurchasePrice(resultSet.getBigDecimal("PurchasePrice"));
        history.setWinnings(resultSet.getBigDecimal("Winnings"));
        history.setMatchCount(resultSet.getInt("MatchCount"));
        Timestamp claimDate = resultSet.getTimestamp("ClaimDate");
        if (claimDate != null) {
            history.setClaimDate(claimDate.toLocalDateTime());
        }
        return history;
    }
}