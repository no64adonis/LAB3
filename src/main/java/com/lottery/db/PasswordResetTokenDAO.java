package com.lottery.db;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.logging.Level;

public class PasswordResetTokenDAO extends BaseDAO {

    public static boolean saveToken(int userId, String token, Timestamp expiryTime) {
        String query = "INSERT INTO PasswordResetTokens (UserID, Token, ExpiryTime) VALUES (?, ?, ?)";

        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, userId);
            statement.setString(2, token);
            statement.setTimestamp(3, expiryTime);

            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error saving token for user ID: " + userId, e);
        }

        return false;
    }

    public static int getUserIdByToken(String token) {
        String query = "SELECT UserID FROM PasswordResetTokens WHERE Token = ? AND ExpiryTime > GETDATE()";

        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, token);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("UserID");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting user ID by token: " + token, e);
        }

        return -1;
    }

    public static boolean isTokenExpired(String token) {
        String query = "SELECT ExpiryTime FROM PasswordResetTokens WHERE Token = ?";

        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, token);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                Timestamp expiryTime = resultSet.getTimestamp("ExpiryTime");
                return expiryTime.before(new Timestamp(System.currentTimeMillis()));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error checking if token is expired: " + token, e);
        }

        return true; 
    }

    public static boolean deleteToken(String token) {
        String query = "DELETE FROM PasswordResetTokens WHERE Token = ?";

        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, token);

            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting token: " + token, e);
        }

        return false;
    }

    public static boolean deleteExpiredTokens() {
        String query = "DELETE FROM PasswordResetTokens WHERE ExpiryTime < GETDATE()";

        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            int rowsAffected = statement.executeUpdate();
            return rowsAffected >= 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting expired tokens", e);
        }

        return false;
    }

    public static com.lottery.model.User getUserByToken(String token) {
        String query = "SELECT u.* FROM Users u " +
                "INNER JOIN PasswordResetTokens prt ON u.UserID = prt.UserID " +
                "WHERE prt.Token = ? AND prt.ExpiryTime > GETDATE()";

        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, token);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                com.lottery.model.User user = new com.lottery.model.User();
                user.setUserID(resultSet.getInt("UserID"));
                
                user.setPasswordHash(resultSet.getString("PasswordHash"));
                user.setEmail(resultSet.getString("Email"));
                user.setFirstName(resultSet.getString("FirstName"));
                user.setLastName(resultSet.getString("LastName"));
                user.setPhone(resultSet.getString("Phone"));
                user.setRole(resultSet.getString("Role"));
                Date lastLogin = resultSet.getDate("LastLoginDate");
                if (lastLogin != null) {
                    user.setLastLoginDate(lastLogin.toLocalDate());
                }
                Date createdDate = resultSet.getDate("CreationDate");
                if (createdDate != null) {
                    user.setCreatedDate(createdDate.toLocalDate());
                }
                user.setActive(resultSet.getBoolean("IsActive"));
                user.setBalance(resultSet.getBigDecimal("Balance"));
                return user;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting user by token: " + token, e);
        }

        return null;
    }

    public static boolean isTokenValid(String token) {
        String query = "SELECT COUNT(*) FROM PasswordResetTokens WHERE Token = ? AND ExpiryTime > GETDATE()";

        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, token);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error checking if token is valid: " + token, e);
        }

        return false;
    }

    public static boolean createToken(int userId, String token, Timestamp expiryTime) {
        return saveToken(userId, token, expiryTime);
    }
}
