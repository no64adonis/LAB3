package com.lottery.db;

import com.lottery.model.PaymentMethod;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class PaymentMethodDAO extends BaseDAO {

    public static boolean addPaymentMethod(PaymentMethod paymentMethod) {
        String query = "INSERT INTO PaymentMethods (UserID, LastFourDigits, CardHolder, ExpiryDate) VALUES (?, ?, ?, ?)";

        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, paymentMethod.getUserId());
            statement.setString(2, paymentMethod.getLastFourDigits());
            statement.setString(3, paymentMethod.getCardHolder());
            statement.setString(4, paymentMethod.getExpiryDate());

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
    
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        paymentMethod.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error adding payment method for user ID: " + paymentMethod.getUserId(), e);
        }

        return false;
    }

    public static List<PaymentMethod> getPaymentMethodsByUserId(int userId) {
        List<PaymentMethod> paymentMethods = new ArrayList<>();
        String query = "SELECT PaymentMethodID, UserID, LastFourDigits, CardHolder, ExpiryDate, CreatedDate, IsActive FROM PaymentMethods WHERE UserID = ?";

        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, userId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                PaymentMethod paymentMethod = new PaymentMethod();
                paymentMethod.setId(resultSet.getInt("PaymentMethodID"));
                paymentMethod.setUserId(resultSet.getInt("UserID"));
                paymentMethod.setLastFourDigits(resultSet.getString("LastFourDigits"));
                paymentMethod.setCardHolder(resultSet.getString("CardHolder"));
                paymentMethod.setExpiryDate(resultSet.getString("ExpiryDate"));
                paymentMethods.add(paymentMethod);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting payment methods for user ID: " + userId, e);
        }

        return paymentMethods;
    }

    public static boolean deletePaymentMethod(int paymentMethodId, int userId) {
        String query = "DELETE FROM PaymentMethods WHERE PaymentMethodID = ? AND UserID = ?";

        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, paymentMethodId);
            statement.setInt(2, userId);

            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting payment method ID: " + paymentMethodId + " for user ID: " + userId,
                    e);
        }

        return false;
    }

    public static PaymentMethod getPaymentMethodById(int paymentMethodId) {
        String query = "SELECT PaymentMethodID, UserID, LastFourDigits, CardHolder, ExpiryDate, CreatedDate, IsActive FROM PaymentMethods WHERE PaymentMethodID = ?";

        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, paymentMethodId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                PaymentMethod paymentMethod = new PaymentMethod();
                paymentMethod.setId(resultSet.getInt("PaymentMethodID"));
                paymentMethod.setUserId(resultSet.getInt("UserID"));
                paymentMethod.setLastFourDigits(resultSet.getString("LastFourDigits"));
                paymentMethod.setCardHolder(resultSet.getString("CardHolder"));
                paymentMethod.setExpiryDate(resultSet.getString("ExpiryDate"));
                return paymentMethod;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting payment method by ID: " + paymentMethodId, e);
        }

        return null;
    }

    public static List<PaymentMethod> getAllPaymentMethods(int offset, int limit) {
        List<PaymentMethod> paymentMethods = new ArrayList<>();
        String query = "SELECT PaymentMethodID, UserID, LastFourDigits, CardHolder, ExpiryDate FROM PaymentMethods ORDER BY PaymentMethodID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, offset);
            statement.setInt(2, limit);

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                PaymentMethod paymentMethod = new PaymentMethod();
                paymentMethod.setId(resultSet.getInt("PaymentMethodID"));
                paymentMethod.setUserId(resultSet.getInt("UserID"));
                paymentMethod.setLastFourDigits(resultSet.getString("LastFourDigits"));
                paymentMethod.setCardHolder(resultSet.getString("CardHolder"));
                paymentMethod.setExpiryDate(resultSet.getString("ExpiryDate"));
                paymentMethods.add(paymentMethod);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting all payment methods", e);
        }

        return paymentMethods;
    }

    public static List<PaymentMethod> getAllPaymentMethodsWithUsers(int offset, int limit) {
        List<PaymentMethod> paymentMethods = new ArrayList<>();
        String query = "SELECT pm.PaymentMethodID, pm.UserID, pm.LastFourDigits, pm.CardHolder, pm.ExpiryDate, u.Email "
                +
                "FROM PaymentMethods pm " +
                "JOIN Users u ON pm.UserID = u.UserID " +
                "ORDER BY pm.PaymentMethodID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, offset);
            statement.setInt(2, limit);

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                PaymentMethod paymentMethod = new PaymentMethod();
                paymentMethod.setId(resultSet.getInt("PaymentMethodID"));
                paymentMethod.setUserId(resultSet.getInt("UserID"));
                paymentMethod.setLastFourDigits(resultSet.getString("LastFourDigits"));
                paymentMethod.setCardHolder(resultSet.getString("CardHolder"));
                paymentMethod.setExpiryDate(resultSet.getString("ExpiryDate"));

                String email = resultSet.getString("Email");
                paymentMethod.setCardHolder(paymentMethod.getCardHolder() + " (" + email + ")");

                paymentMethods.add(paymentMethod);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting all payment methods with users", e);
        }

        return paymentMethods;
    }

    public static int getTotalPaymentMethodCount() {
        String query = "SELECT COUNT(*) AS total FROM PaymentMethods";

        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(query);
                ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getInt("total");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting total payment method count", e);
        }

        return 0;
    }

    public static boolean updatePaymentMethod(PaymentMethod paymentMethod) {
        String query = "UPDATE PaymentMethods SET LastFourDigits = ?, CardHolder = ?, ExpiryDate = ? WHERE PaymentMethodID = ?";

        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, paymentMethod.getLastFourDigits());
            statement.setString(2, paymentMethod.getCardHolder());
            statement.setString(3, paymentMethod.getExpiryDate());
            statement.setInt(4, paymentMethod.getId());

            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating payment method ID: " + paymentMethod.getId(), e);
        }

        return false;
    }

    public static boolean deletePaymentMethods(List<Integer> paymentMethodIds) {
        if (paymentMethodIds == null || paymentMethodIds.isEmpty()) {
            return false;
        }

        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < paymentMethodIds.size(); i++) {
            placeholders.append("?");
            if (i < paymentMethodIds.size() - 1) {
                placeholders.append(",");
            }
        }

        String query = "DELETE FROM PaymentMethods WHERE PaymentMethodID IN (" + placeholders.toString() + ")";

        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            for (int i = 0; i < paymentMethodIds.size(); i++) {
                statement.setInt(i + 1, paymentMethodIds.get(i));
            }

            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting payment methods", e);
        }

        return false;
    }
}