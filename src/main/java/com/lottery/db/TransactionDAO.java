package com.lottery.db;

import com.lottery.model.Transaction;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TransactionDAO {
    private static final Logger logger = Logger.getLogger(TransactionDAO.class.getName());

    public static boolean createTransaction(int userId, BigDecimal amount, int paymentMethodId) {
        String sql = "INSERT INTO Transactions (UserID, Amount, PaymentMethodID) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setBigDecimal(2, amount);
            stmt.setInt(3, paymentMethodId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error creating transaction", e);
            return false;
        }
    }

    public static List<Transaction> getTransactionsByUserId(int userId, int offset, int limit) {
        String sql = "SELECT t.TransactionID, t.UserID, t.Amount, t.PaymentMethodID, t.TransactionDate, " +
                "pm.LastFourDigits, pm.CardHolder " +
                "FROM Transactions t " +
                "LEFT JOIN PaymentMethods pm ON t.PaymentMethodID = pm.PaymentMethodID " +
                "WHERE t.UserID = ? " +
                "ORDER BY t.TransactionDate DESC " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        List<Transaction> transactions = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, offset);
            stmt.setInt(3, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapResultSet(rs, false));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting transactions for user " + userId, e);
        }
        return transactions;
    }

    public static int getTransactionCountByUserId(int userId) {
        String sql = "SELECT COUNT(*) FROM Transactions WHERE UserID = ?";
        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next())
                    return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting transaction count for user " + userId, e);
        }
        return 0;
    }

    public static List<Transaction> getAllTransactions(int offset, int limit) {
        String sql = "SELECT t.TransactionID, t.UserID, t.Amount, t.PaymentMethodID, t.TransactionDate, " +
                "pm.LastFourDigits, pm.CardHolder, u.Email " +
                "FROM Transactions t " +
                "LEFT JOIN PaymentMethods pm ON t.PaymentMethodID = pm.PaymentMethodID " +
                "LEFT JOIN Users u ON t.UserID = u.UserID " +
                "ORDER BY t.TransactionDate DESC " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        List<Transaction> transactions = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, offset);
            stmt.setInt(2, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapResultSet(rs, true));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting all transactions", e);
        }
        return transactions;
    }

    public static int getAllTransactionCount() {
        String sql = "SELECT COUNT(*) FROM Transactions";
        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next())
                    return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting total transaction count", e);
        }
        return 0;
    }

    public static List<Transaction> searchTransactions(String email, String startDate, String endDate,
            int offset, int limit) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT t.TransactionID, t.UserID, t.Amount, t.PaymentMethodID, t.TransactionDate, ");
        sql.append("pm.LastFourDigits, pm.CardHolder, u.Email ");
        sql.append("FROM Transactions t ");
        sql.append("LEFT JOIN PaymentMethods pm ON t.PaymentMethodID = pm.PaymentMethodID ");
        sql.append("LEFT JOIN Users u ON t.UserID = u.UserID ");
        sql.append("WHERE 1=1 ");

        List<Object> params = new ArrayList<>();
        appendSearchConditions(sql, params, email, startDate, endDate);

        sql.append("ORDER BY t.TransactionDate DESC ");
        sql.append("OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        params.add(offset);
        params.add(limit);

        List<Transaction> transactions = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            setParameters(stmt, params);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapResultSet(rs, true));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error searching transactions", e);
        }
        return transactions;
    }

    public static int searchTransactionCount(String email, String startDate, String endDate) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) FROM Transactions t ");
        sql.append("LEFT JOIN Users u ON t.UserID = u.UserID ");
        sql.append("WHERE 1=1 ");

        List<Object> params = new ArrayList<>();
        appendSearchConditions(sql, params, email, startDate, endDate);

        try (Connection conn = DatabaseConfig.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            setParameters(stmt, params);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next())
                    return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting search transaction count", e);
        }
        return 0;
    }

    private static void appendSearchConditions(StringBuilder sql, List<Object> params,
            String email, String startDate, String endDate) {
        if (email != null && !email.trim().isEmpty()) {
            sql.append("AND u.Email LIKE ? ");
            params.add("%" + email.trim() + "%");
        }
        if (startDate != null && !startDate.trim().isEmpty()) {
            sql.append("AND t.TransactionDate >= ? ");
            params.add(startDate.trim() + " 00:00:00");
        }
        if (endDate != null && !endDate.trim().isEmpty()) {
            sql.append("AND t.TransactionDate <= ? ");
            params.add(endDate.trim() + " 23:59:59");
        }
    }

    private static void setParameters(PreparedStatement stmt, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            Object param = params.get(i);
            if (param instanceof Integer) {
                stmt.setInt(i + 1, (Integer) param);
            } else if (param instanceof String) {
                stmt.setString(i + 1, (String) param);
            } else if (param instanceof BigDecimal) {
                stmt.setBigDecimal(i + 1, (BigDecimal) param);
            }
        }
    }

    private static Transaction mapResultSet(ResultSet rs, boolean includeEmail) throws SQLException {
        Transaction t = new Transaction();
        t.setTransactionId(rs.getInt("TransactionID"));
        t.setUserId(rs.getInt("UserID"));
        t.setAmount(rs.getBigDecimal("Amount"));
        t.setPaymentMethodId(rs.getInt("PaymentMethodID"));
        t.setTransactionDate(rs.getTimestamp("TransactionDate"));
        t.setLastFourDigits(rs.getString("LastFourDigits"));
        t.setCardHolder(rs.getString("CardHolder"));
        if (includeEmail) {
            t.setUserEmail(rs.getString("Email"));
        }
        return t;
    }
}
