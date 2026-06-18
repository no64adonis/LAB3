package com.lottery.db;

import com.lottery.model.User;
import com.lottery.util.InputValidator;
import java.sql.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import java.util.logging.Level;
import org.mindrot.jbcrypt.BCrypt;
import com.lottery.config.AppConfig;

import com.lottery.util.PasswordUtil;

public class UserDAO extends BaseDAO {

    public static boolean validateUser(String email, String password) {
        
        if (!InputValidator.isValidEmail(email) || password == null || password.isEmpty()) {
            logger.log(Level.WARNING, "Invalid email or password provided for validation");
            return false;
        }

        String query = "SELECT UserID, PasswordHash, IsActive FROM Users WHERE Email = ?";

        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, email);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                boolean isActive = resultSet.getBoolean("IsActive");
                if (!isActive) {
                    logger.log(Level.INFO, "User account is deactivated: " + maskSensitive(email));
                    return false; 
                }

                String storedHash = resultSet.getString("PasswordHash");
                return PasswordUtil.verify(password, storedHash);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error validating user: " + maskSensitive(email), e);
        }

        return false;
    }

    private static User mapResultSetToUser(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setUserID(resultSet.getInt("UserID"));
        
        user.setPasswordHash(resultSet.getString("PasswordHash"));
        user.setEmail(resultSet.getString("Email"));
        user.setFirstName(resultSet.getString("FirstName"));
        user.setLastName(resultSet.getString("LastName"));
        user.setPhone(resultSet.getString("Phone"));
        user.setRole(resultSet.getString("Role"));

        Date lastLogin = resultSet.getDate("LastLoginDate");
        if (lastLogin != null) {
            user.setLastLoginDate(lastLogin);
        }

        Timestamp createdDate = resultSet.getTimestamp("CreationDate");
        if (createdDate != null) {
            user.setCreationDate(createdDate.toLocalDateTime());
        }

        user.setBalance(resultSet.getBigDecimal("Balance"));
        user.setActive(resultSet.getBoolean("IsActive"));

        try {
            user.setPasswordSet(resultSet.getBoolean("password_set"));
        } catch (SQLException e) {
            
        }

        return user;
    }

    public static User getUserById(int userId) {
        
        if (userId <= 0) {
            logger.log(Level.WARNING, "Invalid user ID provided: " + userId);
            return null;
        }

        String query = "SELECT * FROM Users WHERE UserID = ?";

        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, userId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return mapResultSetToUser(resultSet);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting user by ID: " + userId, e);
        }

        return null;
    }

    public static User getUserByEmail(String email) {
        
        if (!InputValidator.isValidEmail(email)) {
            logger.log(Level.WARNING, "Invalid email provided: " + maskSensitive(email));
            return null;
        }

        String query = "SELECT * FROM Users WHERE Email = ?";

        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, email);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return mapResultSetToUser(resultSet);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting user by email: " + email, e);
        }

        return null;
    }

    public static List<User> getAllUsers(int offset, int limit) {
        List<User> users = new ArrayList<>();

        if (offset < 0 || limit <= 0 || limit > 1000) {
            logger.log(Level.WARNING, "Invalid pagination parameters: offset=" + offset + ", limit=" + limit);
            return users;
        }

        String query = "SELECT * FROM Users ORDER BY UserID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, offset);
            statement.setInt(2, limit);

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                users.add(mapResultSetToUser(resultSet));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting all users", e);
        }

        return users;
    }

    public static List<User> searchUsers(String searchTerm, String lastLoginFrom, String lastLoginTo, String role,
            int offset, int limit) {
        return searchUsers(searchTerm, null, lastLoginFrom, lastLoginTo, role, offset, limit);
    }

    public static List<User> searchUsers(String searchTerm, List<String> searchFields, String lastLoginFrom,
            String lastLoginTo, String role,
            int offset, int limit) {
        List<User> users = new ArrayList<>();

        if (searchTerm != null && !searchTerm.isEmpty() && !InputValidator.isValidSearchTerm(searchTerm, 100)) {
            logger.log(Level.WARNING, "Invalid search term provided: " + searchTerm);
            return users;
        }

        String sanitizedSearchTerm = InputValidator.sanitizeString(searchTerm);
        if (sanitizedSearchTerm == null || sanitizedSearchTerm.isEmpty()) {
            sanitizedSearchTerm = "";
        }

        StringBuilder queryBuilder = new StringBuilder("SELECT * FROM Users");
        buildSearchWhereClause(queryBuilder, sanitizedSearchTerm, searchFields, role, lastLoginFrom, lastLoginTo);
        queryBuilder.append(" ORDER BY UserID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");

        String query = queryBuilder.toString();
        logger.log(Level.INFO, "Executing Search Query: " + query);

        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            int paramIndex = bindSearchParameters(statement, 1, sanitizedSearchTerm, searchFields,
                    role, lastLoginFrom, lastLoginTo);

            statement.setInt(paramIndex++, offset);
            statement.setInt(paramIndex++, limit);

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                users.add(mapResultSetToUser(resultSet));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error searching users with term: " + maskSensitive(searchTerm), e);
        }

        return users;
    }

    private static String validateFieldName(String field) {
        if (field == null)
            return null;
        switch (field) {
            case "Username":
                return null; 
            case "Email":
                return "Email";
            case "FirstName":
                return "FirstName";
            case "LastName":
                return "LastName";
            case "Phone":
                return "Phone";
            case "UserID":
                return "UserID";
            default:
                return null;
        }
    }

    public static int getUserCount() {
        String query = "SELECT COUNT(*) AS total FROM Users";

        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(query);
                ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                int count = resultSet.getInt("total");
                if (count < 0) {
                    logger.log(Level.WARNING, "Negative user count returned: " + count);
                    return 0;
                }
                return count;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting user count", e);
        }

        return 0;
    }

    public static int getSearchUserCount(String searchTerm, String lastLoginFrom, String lastLoginTo, String role) {
        return getSearchUserCount(searchTerm, null, lastLoginFrom, lastLoginTo, role);
    }

    public static int getSearchUserCount(String searchTerm, List<String> searchFields, String lastLoginFrom,
            String lastLoginTo, String role) {

        if (searchTerm != null && !searchTerm.isEmpty() && !InputValidator.isValidSearchTerm(searchTerm, 100)) {
            logger.log(Level.WARNING, "Invalid search term provided: " + maskSensitive(searchTerm));
            return 0;
        }

        String sanitizedSearchTerm = InputValidator.sanitizeString(searchTerm);
        if (sanitizedSearchTerm == null || sanitizedSearchTerm.isEmpty()) {
            sanitizedSearchTerm = "";
        }

        StringBuilder queryBuilder = new StringBuilder("SELECT COUNT(*) AS total FROM Users");
        buildSearchWhereClause(queryBuilder, sanitizedSearchTerm, searchFields, role, lastLoginFrom, lastLoginTo);

        String query = queryBuilder.toString();
        logger.log(Level.INFO, "Executing Search Count Query: " + query);

        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            bindSearchParameters(statement, 1, sanitizedSearchTerm, searchFields,
                    role, lastLoginFrom, lastLoginTo);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("total");
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting search user count with term: " + searchTerm, e);
        }

        return 0;
    }

    private static void buildSearchWhereClause(StringBuilder queryBuilder, String sanitizedSearchTerm,
            List<String> searchFields, String role, String lastLoginFrom, String lastLoginTo) {
        queryBuilder.append(" WHERE 1=1");

        boolean hasSearchTerm = sanitizedSearchTerm != null && !sanitizedSearchTerm.isEmpty();
        if (hasSearchTerm) {
            if (searchFields != null && !searchFields.isEmpty()) {
                queryBuilder.append(" AND (");
                for (int i = 0; i < searchFields.size(); i++) {
                    String field = searchFields.get(i);
                    if (i > 0)
                        queryBuilder.append(" OR ");
                    if ("UserID".equalsIgnoreCase(field)) {
                        queryBuilder.append("CAST(UserID AS VARCHAR) LIKE ?");
                    } else {
                        String validatedField = validateFieldName(field);
                        if (validatedField != null) {
                            queryBuilder.append(validatedField).append(" LIKE ?");
                        }
                    }
                }
                queryBuilder.append(")");
            } else {
                queryBuilder.append(
                        " AND (Email LIKE ? OR Phone LIKE ? OR FirstName LIKE ? OR LastName LIKE ? OR CAST(UserID AS VARCHAR) LIKE ?)");
            }
        }

        if (role != null && !role.isEmpty()) {
            queryBuilder.append(" AND Role = ?");
        }

        if (lastLoginFrom != null && !lastLoginFrom.isEmpty() && InputValidator.isValidDate(lastLoginFrom)) {
            queryBuilder.append(" AND CAST(LastLoginDate AS DATE) >= ?");
        }
        if (lastLoginTo != null && !lastLoginTo.isEmpty() && InputValidator.isValidDate(lastLoginTo)) {
            queryBuilder.append(" AND CAST(LastLoginDate AS DATE) <= ?");
        }
    }

    private static int bindSearchParameters(PreparedStatement statement, int paramIndex,
            String sanitizedSearchTerm, List<String> searchFields, String role,
            String lastLoginFrom, String lastLoginTo) throws SQLException {
        int DEFAULT_SEARCH_FIELD_COUNT = 5;

        if (sanitizedSearchTerm != null && !sanitizedSearchTerm.isEmpty()) {
            int fieldCount = (searchFields != null && !searchFields.isEmpty())
                    ? searchFields.size() : DEFAULT_SEARCH_FIELD_COUNT;
            for (int i = 0; i < fieldCount; i++) {
                statement.setString(paramIndex++, "%" + sanitizedSearchTerm + "%");
            }
        }

        if (role != null && !role.isEmpty()) {
            statement.setString(paramIndex++, role);
        }

        if (lastLoginFrom != null && !lastLoginFrom.isEmpty() && InputValidator.isValidDate(lastLoginFrom)) {
            statement.setDate(paramIndex++, Date.valueOf(lastLoginFrom));
        }
        if (lastLoginTo != null && !lastLoginTo.isEmpty() && InputValidator.isValidDate(lastLoginTo)) {
            statement.setDate(paramIndex++, Date.valueOf(lastLoginTo));
        }

        return paramIndex;
    }

    public static boolean updateUserRole(int userId, String role) {
        
        if (userId <= 0 || role == null || role.isEmpty() || role.length() > 20) {
            logger.log(Level.WARNING, "Invalid user ID or role provided: userId=" + userId + ", role=" + role);
            return false;
        }

        String sanitizedRole = InputValidator.sanitizeString(role);

        String query = "UPDATE Users SET Role = ? WHERE UserID = ?";

        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, sanitizedRole);
            statement.setInt(2, userId);

            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating user role for user ID: " + userId, e);
        }

        return false;
    }

    public static boolean createUser(User user) {
        
        if (user == null) {
            logger.log(Level.WARNING, "Null user object provided for creation");
            return false;
        }

        if (user.getPasswordHash() == null || user.getPasswordHash().isEmpty() ||
                !InputValidator.isValidEmail(user.getEmail())) {
            logger.log(Level.WARNING,
                    "Invalid user data provided for creation: email=" + maskSensitive(user.getEmail()));
            return false;
        }

        if (user.getFirstName() != null && user.getFirstName().length() > 50) {
            logger.log(Level.WARNING, "First name too long: " + user.getFirstName());
            return false;
        }

        if (user.getLastName() != null && user.getLastName().length() > 50) {
            logger.log(Level.WARNING, "Last name too long: " + user.getLastName());
            return false;
        }

        if (user.getPhone() != null && user.getPhone().length() > 20) {
            logger.log(Level.WARNING, "Phone number too long: " + user.getPhone());
            return false;
        }

        if (user.getRole() != null && user.getRole().length() > 20) {
            logger.log(Level.WARNING, "Role too long: " + user.getRole());
            return false;
        }

        String sanitizedFirstName = InputValidator.sanitizeString(user.getFirstName());
        String sanitizedLastName = InputValidator.sanitizeString(user.getLastName());
        String sanitizedPhone = InputValidator.sanitizeString(user.getPhone());
        String sanitizedRole = InputValidator.sanitizeString(user.getRole());

        String query = "INSERT INTO Users (PasswordHash, Email, FirstName, LastName, Phone, Role, CreationDate, IsActive, password_set) VALUES (?, ?, ?, ?, ?, ?, GETDATE(), ?, ?)";

        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, user.getPasswordHash());
            statement.setString(2, user.getEmail());
            statement.setString(3, sanitizedFirstName);
            statement.setString(4, sanitizedLastName);
            statement.setString(5, sanitizedPhone);
            statement.setString(6, sanitizedRole != null ? sanitizedRole : "user");
            statement.setBoolean(7, user.isActive());
            statement.setBoolean(8, user.isPasswordSet());

            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error creating user: " + maskSensitive(user.getEmail()), e);
        }

        return false;
    }

    public static boolean setUserActiveStatus(int userId, boolean isActive) {
        
        if (userId <= 0) {
            logger.log(Level.WARNING, "Invalid user ID provided: " + userId);
            return false;
        }

        String query = "UPDATE Users SET IsActive = ? WHERE UserID = ?";

        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setBoolean(1, isActive);
            statement.setInt(2, userId);

            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error setting user active status for user ID: " + userId, e);
        }

        return false;
    }

    public static boolean updateUserDetails(int userId, String email, String firstName, String lastName, String phone) {
        
        if (userId <= 0) {
            logger.log(Level.WARNING, "Invalid user ID provided: " + userId);
            return false;
        }

        if (email != null && !email.isEmpty() && !InputValidator.isValidEmail(email)) {
            logger.log(Level.WARNING, "Invalid email provided: " + maskSensitive(email));
            return false;
        }

        if (firstName != null && firstName.length() > 50) {
            logger.log(Level.WARNING, "First name too long: " + firstName);
            return false;
        }

        if (lastName != null && lastName.length() > 50) {
            logger.log(Level.WARNING, "Last name too long: " + lastName);
            return false;
        }

        if (phone != null && phone.length() > 20) {
            logger.log(Level.WARNING, "Phone number too long: " + phone);
            return false;
        }

        String sanitizedEmail = InputValidator.sanitizeString(email);
        String sanitizedFirstName = InputValidator.sanitizeString(firstName);
        String sanitizedLastName = InputValidator.sanitizeString(lastName);
        String sanitizedPhone = InputValidator.sanitizeString(phone);

        String query = "UPDATE Users SET Email = ?, FirstName = ?, LastName = ?, Phone = ? WHERE UserID = ?";

        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, sanitizedEmail);
            statement.setString(2, sanitizedFirstName);
            statement.setString(3, sanitizedLastName);
            statement.setString(4, sanitizedPhone);
            statement.setInt(5, userId);

            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating user details for user ID: " + userId, e);
        }

        return false;
    }

    public static boolean updateUserPassword(int userId, String newPassword) {
        
        if (userId <= 0 || newPassword == null || newPassword.isEmpty() || newPassword.length() > 100) {
            logger.log(Level.WARNING, "Invalid user ID or password provided: userId=" + userId);
            return false;
        }

        String query = "UPDATE Users SET PasswordHash = ? WHERE UserID = ?";
        String hashedPassword = PasswordUtil.hash(newPassword);

        if (hashedPassword == null) {
            logger.log(Level.SEVERE, "Failed to hash password for user ID: " + userId);
            return false;
        }

        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, hashedPassword);
            statement.setInt(2, userId);

            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating user password for user ID: " + userId, e);
        }

        return false;
    }

    public static boolean updateLastLoginDate(String email) {
        
        if (!InputValidator.isValidEmail(email)) {
            logger.log(Level.WARNING, "Invalid email provided: " + email);
            return false;
        }

        String query = "UPDATE Users SET LastLoginDate = GETDATE() WHERE Email = ?";

        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, email);

            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating last login date for email: " + maskSensitive(email), e);
        }

        return false;
    }

    public static List<User> getInactiveUsers(String period) {
        List<User> users = new ArrayList<>();

        if (period == null || period.isEmpty()) {
            logger.log(Level.WARNING, "Null or empty period provided");
            return users;
        }

        String sanitizedPeriod = InputValidator.sanitizeString(period);
        if (sanitizedPeriod == null || sanitizedPeriod.isEmpty()) {
            logger.log(Level.WARNING, "Invalid period after sanitization: " + period);
            return users;
        }

        String[] parts = sanitizedPeriod.split(" ");
        if (parts.length != 2) {
            logger.log(Level.WARNING, "Invalid period format: " + sanitizedPeriod);
            return users; 
        }

        int value;
        String unit;
        try {
            value = Integer.parseInt(parts[0]);
            if (value <= 0) {
                logger.log(Level.WARNING, "Invalid period value: " + value);
                return users; 
            }
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, "Invalid period number format: " + parts[0]);
            return users; 
        }

        unit = parts[1].toLowerCase();
        String sqlUnit;
        switch (unit) {
            case "day":
            case "days":
                sqlUnit = "day";
                break;
            case "week":
            case "weeks":
                sqlUnit = "week";
                break;
            case "month":
            case "months":
                sqlUnit = "month";
                break;
            case "quarter":
            case "quarters":
                sqlUnit = "quarter";
                break;
            case "year":
            case "years":
                sqlUnit = "year";
                break;
            default:
                logger.log(Level.WARNING, "Unsupported time unit: " + unit);
                return users; 
        }

        String query = "SELECT * FROM Users WHERE IsActive = 1 AND (LastLoginDate < DATEADD(" + sqlUnit
                + ", ?, GETDATE()) OR LastLoginDate IS NULL)";

        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, -value); 

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                users.add(mapResultSetToUser(resultSet));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting inactive users with period: " + period, e);
        }

        return users;
    }

    public static boolean updateUserBalance(int userId, BigDecimal amount) {
        
        if (userId <= 0 || amount == null) {
            logger.log(Level.WARNING, "Invalid user ID or amount provided: userId=" + userId + ", amount=" + amount);
            return false;
        }

        if (amount.abs().compareTo(new BigDecimal("1000000")) > 0) {
            logger.log(Level.WARNING, "Amount exceeds reasonable limits: " + amount);
            return false;
        }

        String query = "UPDATE Users SET Balance = Balance + ? WHERE UserID = ?";

        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setBigDecimal(1, amount);
            statement.setInt(2, userId);

            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating user balance for user ID: " + userId, e);
        }

        return false;
    }

    public static boolean updateUserBalance(Connection connection, int userId, BigDecimal amount) throws SQLException {
        
        if (userId <= 0 || amount == null) {
            logger.log(Level.WARNING, "Invalid user ID or amount provided: userId=" + userId + ", amount=" + amount);
            return false;
        }

        if (amount.abs().compareTo(new BigDecimal("1000000")) > 0) {
            logger.log(Level.WARNING, "Amount exceeds reasonable limits: " + amount);
            return false;
        }

        String query = "UPDATE Users SET Balance = Balance + ? WHERE UserID = ? AND (? >= 0 OR Balance >= ABS(?))";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setBigDecimal(1, amount);
            statement.setInt(2, userId);
            statement.setBigDecimal(3, amount);
            statement.setBigDecimal(4, amount);

            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        }
    }

    private static String maskSensitive(String value) {
        if (value == null || value.length() < 4)
            return "***";
        return value.substring(0, 2) + "***" + value.substring(value.length() - 2);
    }

    public static boolean updateUserEmail(int userId, String newEmail) {
        if (userId <= 0 || newEmail == null || newEmail.isEmpty()) {
            logger.log(Level.WARNING, "Invalid user ID or email provided");
            return false;
        }

        String query = "UPDATE Users SET Email = ? WHERE UserID = ?";

        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, newEmail);
            statement.setInt(2, userId);

            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating email for user ID: " + userId, e);
        }

        return false;
    }

    public static boolean storeEmailVerificationCode(int userId, String newEmail, String code) {
        if (userId <= 0 || newEmail == null || code == null) {
            return false;
        }

        String query = "INSERT INTO EmailVerificationCodes (UserID, NewEmail, VerificationCode, ExpiresAt) " +
                "VALUES (?, ?, ?, DATEADD(MINUTE, 10, GETDATE()))";

        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, userId);
            statement.setString(2, newEmail);
            statement.setString(3, code);

            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error storing verification code for user ID: " + userId, e);
        }

        return false;
    }

    public static String getNewEmailFromVerificationCode(int userId, String code) {
        if (userId <= 0 || code == null) {
            return null;
        }

        String query = "SELECT TOP 1 NewEmail FROM EmailVerificationCodes " +
                "WHERE UserID = ? AND VerificationCode = ? AND IsUsed = 0 " +
                "AND ExpiresAt > GETDATE() ORDER BY CreatedAt DESC";

        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, userId);
            statement.setString(2, code);

            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return rs.getString("NewEmail");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting verification code for user ID: " + userId, e);
        }

        return null;
    }

    public static boolean markVerificationCodeAsUsed(int userId, String code) {
        if (userId <= 0 || code == null) {
            return false;
        }

        String query = "UPDATE EmailVerificationCodes SET IsUsed = 1 " +
                "WHERE UserID = ? AND VerificationCode = ?";

        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, userId);
            statement.setString(2, code);

            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error marking verification code as used for user ID: " + userId, e);
        }

        return false;
    }

    public static boolean updatePasswordSet(String email, boolean value) {
        String sql = "UPDATE Users SET password_set = ? WHERE Email = ?";
        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBoolean(1, value);
            statement.setString(2, email);
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating password set status for email: " + maskSensitive(email), e);
        }
        return false;
    }
}
