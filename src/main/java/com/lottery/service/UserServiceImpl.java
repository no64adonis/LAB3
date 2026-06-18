package com.lottery.service;

import com.lottery.db.UserDAO;
import com.lottery.model.User;
import com.lottery.service.exception.ServiceException;
import com.lottery.service.exception.UserNotFoundException;
import com.lottery.service.exception.ValidationException;
import com.lottery.util.InputValidator;

import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserServiceImpl extends BaseService implements UserService {
    private static final Logger logger = Logger.getLogger(UserServiceImpl.class.getName());
    public User registerUser(String email, String firstName, String lastName, String phone,
            String password)
            throws ServiceException, ValidationException {

        logInfo("registerUser", "Attempting to register user: " + email);

        if (email != null) {
            email = InputValidator.sanitizeString(email);
        }
        if (firstName != null) {
            firstName = InputValidator.sanitizeString(firstName);
        }
        if (lastName != null) {
            lastName = InputValidator.sanitizeString(lastName);
        }
        if (phone != null) {
            phone = InputValidator.sanitizeString(phone);
        }
        if (password != null) {
            password = InputValidator.sanitizeString(password);
        }

        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("email", email, "Email is required");
        }

        if (!InputValidator.isValidEmail(email)) {
            throw new ValidationException("email", email, "Invalid email format.");
        }

        if (firstName == null || firstName.trim().isEmpty()) {
            throw new ValidationException("firstName", firstName, "First name is required");
        }

        if (lastName == null || lastName.trim().isEmpty()) {
            throw new ValidationException("lastName", lastName, "Last name is required");
        }

        if (password == null || password.trim().isEmpty()) {
            throw new ValidationException("password", password, "Password is required");
        }

        if (password.length() < 6 || password.length() > 100) {
            throw new ValidationException("password", password, "Password must be between 6 and 100 characters.");
        }

        try {
            User existingUser = UserDAO.getUserByEmail(email);
            if (existingUser != null) {
                throw new ValidationException("email", email,
                        "Email already registered. Please use a different email.");
            }
        } catch (RuntimeException e) {
            logWarning("registerUser", "Error checking email existence: " + e.getMessage());
            
        }

        User user = new User(hashPassword(password), email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPhone(phone);
        user.setActive(true);
        user.setRole("user");
        user.setBalance(BigDecimal.ZERO);

        try {
            boolean success = UserDAO.createUser(user);
            if (success) {
                logInfo("registerUser", "User registered successfully: " + email);
                return UserDAO.getUserByEmail(email);
            } else {
                throw new ServiceException("Failed to register user");
            }
        } catch (RuntimeException e) {
            logSevere("registerUser", "Error registering user: " + e.getMessage());
            throw new ServiceException("Error registering user: " + e.getMessage(), e);
        }
    }
    public User getUserById(int userId) throws UserNotFoundException, ServiceException {
        try {
            User user = UserDAO.getUserById(userId);
            if (user == null) {
                throw new UserNotFoundException(String.valueOf(userId));
            }
            return user;
        } catch (UserNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new ServiceException("Error retrieving user: " + e.getMessage(), e);
        }
    }
    public User getUserByEmail(String email) throws UserNotFoundException, ServiceException {
        try {
            User user = UserDAO.getUserByEmail(email);
            if (user == null) {
                throw new UserNotFoundException(email);
            }
            return user;
        } catch (UserNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new ServiceException("Error retrieving user: " + e.getMessage(), e);
        }
    }
    public List<User> getAllUsers(int offset, int limit) throws ServiceException {
        if (offset < 0 || limit <= 0 || limit > 100000) {
            throw new ServiceException("Invalid pagination parameters");
        }

        try {
            return UserDAO.getAllUsers(offset, limit);
        } catch (RuntimeException e) {
            throw new ServiceException("Error retrieving users: " + e.getMessage(), e);
        }
    }
    public List<User> searchUsers(String searchTerm, String lastLoginFrom, String lastLoginTo, String role, int offset,
            int limit) throws ServiceException {
        return searchUsers(searchTerm, null, lastLoginFrom, lastLoginTo, role, offset, limit);
    }
    public List<User> searchUsers(String searchTerm, List<String> searchFields, String lastLoginFrom,
            String lastLoginTo, String role, int offset, int limit) throws ServiceException {
        logInfo("searchUsers", "Searching users with term: " + searchTerm + ", fields: " + searchFields + ", offset: "
                + offset + ", limit: " + limit);

        if (searchTerm != null && !searchTerm.isEmpty() && !InputValidator.isValidSearchTerm(searchTerm, 100)) {
            logWarning("searchUsers", "Invalid search term provided: " + searchTerm);
            throw new ServiceException("Invalid search term");
        }

        if (offset < 0 || limit <= 0 || limit > 100000) {
            logWarning("searchUsers", "Invalid pagination parameters: offset=" + offset + ", limit=" + limit);
            throw new ServiceException("Invalid pagination parameters");
        }

        try {
            return UserDAO.searchUsers(searchTerm, searchFields, lastLoginFrom, lastLoginTo, role, offset, limit);
        } catch (RuntimeException e) {
            logSevere("searchUsers", "Error searching users: " + e.getMessage());
            throw new ServiceException("Error searching users: " + e.getMessage(), e);
        }
    }
    public int getUserCount() throws ServiceException {
        try {
            return UserDAO.getUserCount();
        } catch (RuntimeException e) {
            throw new ServiceException("Error getting user count: " + e.getMessage(), e);
        }
    }
    public int getSearchUserCount(String searchTerm, String lastLoginFrom, String lastLoginTo, String role)
            throws ServiceException {
        return getSearchUserCount(searchTerm, null, lastLoginFrom, lastLoginTo, role);
    }
    public int getSearchUserCount(String searchTerm, List<String> searchFields, String lastLoginFrom,
            String lastLoginTo, String role) throws ServiceException {
        try {
            return UserDAO.getSearchUserCount(searchTerm, searchFields, lastLoginFrom, lastLoginTo, role);
        } catch (RuntimeException e) {
            throw new ServiceException("Error getting search user count: " + e.getMessage(), e);
        }
    }
    public boolean updateUserRole(int userId, String role) throws ServiceException {
        logInfo("updateUserRole", "Updating user role for user ID: " + userId + " to role: " + role);

        if (userId <= 0) {
            logWarning("updateUserRole", "Invalid user ID provided: " + userId);
            throw new ServiceException("Invalid user ID");
        }

        if (role == null || role.isEmpty() || role.length() > 20) {
            logWarning("updateUserRole", "Invalid role provided: " + role);
            throw new ServiceException("Invalid role");
        }

        if (!InputValidator.isValidRole(role)) {
            logWarning("updateUserRole", "Invalid role value: " + role);
            throw new ServiceException("Invalid role value");
        }

        try {
            return UserDAO.updateUserRole(userId, role);
        } catch (RuntimeException e) {
            logSevere("updateUserRole", "Error updating user role: " + e.getMessage());
            throw new ServiceException("Error updating user role: " + e.getMessage(), e);
        }
    }
    public boolean createUser(User user) throws ServiceException {
        try {
            return UserDAO.createUser(user);
        } catch (RuntimeException e) {
            throw new ServiceException("Error creating user: " + e.getMessage(), e);
        }
    }
    public boolean setUserActiveStatus(int userId, boolean isActive) throws ServiceException {
        logInfo("setUserActiveStatus", "Setting user active status for user ID: " + userId + " to: " + isActive);

        if (userId <= 0) {
            logWarning("setUserActiveStatus", "Invalid user ID provided: " + userId);
            throw new ServiceException("Invalid user ID");
        }

        try {
            return UserDAO.setUserActiveStatus(userId, isActive);
        } catch (RuntimeException e) {
            logSevere("setUserActiveStatus", "Error setting user active status: " + e.getMessage());
            throw new ServiceException("Error setting user active status: " + e.getMessage(), e);
        }
    }
    public boolean updateUserDetails(int userId, String email, String firstName, String lastName, String phone)
            throws ServiceException {
        logInfo("updateUserDetails", "Updating user details for user ID: " + userId);

        if (userId <= 0) {
            logWarning("updateUserDetails", "Invalid user ID provided: " + userId);
            throw new ServiceException("Invalid user ID");
        }

        if (email != null && !email.isEmpty() && !InputValidator.isValidEmail(email)) {
            logWarning("updateUserDetails", "Invalid email provided: " + email);
            throw new ServiceException("Invalid email");
        }

        if (firstName != null && firstName.length() > 50) {
            logWarning("updateUserDetails", "First name too long: " + firstName);
            throw new ServiceException("First name too long");
        }

        if (lastName != null && lastName.length() > 50) {
            logWarning("updateUserDetails", "Last name too long: " + lastName);
            throw new ServiceException("Last name too long");
        }

        if (phone != null && phone.length() > 20) {
            logWarning("updateUserDetails", "Phone number too long: " + phone);
            throw new ServiceException("Phone number too long");
        }

        try {
            return UserDAO.updateUserDetails(userId, email, firstName, lastName, phone);
        } catch (RuntimeException e) {
            logSevere("updateUserDetails", "Error updating user details: " + e.getMessage());
            throw new ServiceException("Error updating user details: " + e.getMessage(), e);
        }
    }
    public boolean updateUserPassword(int userId, String newPassword) throws ServiceException {
        logInfo("updateUserPassword", "Updating password for user ID: " + userId);

        if (userId <= 0) {
            logWarning("updateUserPassword", "Invalid user ID provided: " + userId);
            throw new ServiceException("Invalid user ID");
        }

        if (newPassword == null || newPassword.isEmpty() || newPassword.length() > 100) {
            logWarning("updateUserPassword", "Invalid password provided for user ID: " + userId);
            throw new ServiceException("Invalid password");
        }

        if (!InputValidator.isValidPassword(newPassword)) {
            logWarning("updateUserPassword", "Invalid password strength for user ID: " + userId);
            throw new ServiceException("Invalid password strength");
        }

        try {
            return UserDAO.updateUserPassword(userId, newPassword);
        } catch (RuntimeException e) {
            logSevere("updateUserPassword", "Error updating user password: " + e.getMessage());
            throw new ServiceException("Error updating user password: " + e.getMessage(), e);
        }
    }
    public boolean updateUserBalance(int userId, BigDecimal amount) throws ServiceException {
        logInfo("updateUserBalance", "Updating balance for user ID: " + userId + " by amount: " + amount);

        if (userId <= 0) {
            logWarning("updateUserBalance", "Invalid user ID provided: " + userId);
            throw new ServiceException("Invalid user ID");
        }

        if (amount == null) {
            logWarning("updateUserBalance", "Null amount provided for user ID: " + userId);
            throw new ServiceException("Amount is required");
        }

        if (amount.abs().compareTo(new BigDecimal("1000000")) > 0) {
            logWarning("updateUserBalance", "Amount exceeds reasonable limits for user ID: " + userId + ", amount: " + amount);
            throw new ServiceException("Amount exceeds reasonable limits");
        }

        try {
            return UserDAO.updateUserBalance(userId, amount);
        } catch (RuntimeException e) {
            logSevere("updateUserBalance", "Error updating user balance: " + e.getMessage());
            throw new ServiceException("Error updating user balance: " + e.getMessage(), e);
        }
    }
    public List<User> getInactiveUsers(String period) throws ServiceException {
        logInfo("getInactiveUsers", "Getting inactive users for period: " + period);

        if (period == null || period.isEmpty()) {
            logWarning("getInactiveUsers", "Null or empty period provided");
            throw new ServiceException("Period is required");
        }

        String sanitizedPeriod = InputValidator.sanitizeString(period);
        if (sanitizedPeriod == null || sanitizedPeriod.isEmpty()) {
            logWarning("getInactiveUsers", "Invalid period after sanitization: " + period);
            throw new ServiceException("Invalid period");
        }

        try {
            return UserDAO.getInactiveUsers(sanitizedPeriod);
        } catch (RuntimeException e) {
            logSevere("getInactiveUsers", "Error getting inactive users: " + e.getMessage());
            throw new ServiceException("Error getting inactive users: " + e.getMessage(), e);
        }
    }

    private String hashPassword(String password) throws ServiceException {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            logSevere("hashPassword", "Error hashing password: " + e.getMessage());
            throw new ServiceException("Error processing password", e);
        }
    }
    public boolean updateLastLoginDate(String email) throws ServiceException {
        logInfo("updateLastLoginDate", "Updating last login date for email: " + email);

        if (email == null || email.trim().isEmpty()) {
            logWarning("updateLastLoginDate", "Invalid email provided: " + email);
            throw new ServiceException("Email is required");
        }

        if (!InputValidator.isValidEmail(email)) {
            logWarning("updateLastLoginDate", "Invalid email format: " + email);
            throw new ServiceException("Invalid email format");
        }

        try {
            boolean success = UserDAO.updateLastLoginDate(email);
            if (success) {
                logInfo("updateLastLoginDate", "Last login date updated successfully for email: " + email);
            } else {
                logWarning("updateLastLoginDate", "Failed to update last login date for email: " + email);
            }
            return success;
        } catch (RuntimeException e) {
            logSevere("updateLastLoginDate",
                    "Error updating last login date for email: " + email + ", error: " + e.getMessage());
            throw new ServiceException("Error updating last login date: " + e.getMessage(), e);
        }
    }
    public boolean processUserRegistration(String email, String firstName, String lastName,
            String phone, String password, String confirmPassword) throws ServiceException {
        logInfo("processUserRegistration", "Processing user registration for email: " + email);

        if (email == null || email.trim().isEmpty()) {
            logWarning("processUserRegistration", "Email is required");
            throw new ServiceException("Email is required");
        }

        if (firstName == null || firstName.trim().isEmpty()) {
            logWarning("processUserRegistration", "First name is required");
            throw new ServiceException("First name is required");
        }

        if (lastName == null || lastName.trim().isEmpty()) {
            logWarning("processUserRegistration", "Last name is required");
            throw new ServiceException("Last name is required");
        }

        if (password == null || password.isEmpty()) {
            logWarning("processUserRegistration", "Password is required");
            throw new ServiceException("Password is required");
        }

        if (confirmPassword == null || confirmPassword.isEmpty()) {
            logWarning("processUserRegistration", "Confirm password is required");
            throw new ServiceException("Confirm password is required");
        }

        if (!InputValidator.isValidEmail(email)) {
            logWarning("processUserRegistration", "Invalid email format: " + email);
            throw new ServiceException("Invalid email format.");
        }

        if (!InputValidator.isValidPassword(password)) {
            logWarning("processUserRegistration", "Invalid password strength");
            throw new ServiceException("Password must be at least 6 characters long.");
        }

        if (!password.equals(confirmPassword)) {
            logWarning("processUserRegistration", "Passwords do not match");
            throw new ServiceException("Passwords do not match.");
        }

        if (firstName.length() > 50) {
            logWarning("processUserRegistration", "First name too long: " + firstName);
            throw new ServiceException("First name too long");
        }

        if (lastName.length() > 50) {
            logWarning("processUserRegistration", "Last name too long: " + lastName);
            throw new ServiceException("Last name too long");
        }

        if (phone != null && !phone.isEmpty()) {
            if (phone.length() > 20) {
                logWarning("processUserRegistration", "Phone number too long: " + phone);
                throw new ServiceException("Phone number too long");
            }
            
        }

        try {
            
            User existingUser = UserDAO.getUserByEmail(email);
            if (existingUser != null) {
                logWarning("processUserRegistration", "Email already registered: " + email);
                throw new ServiceException("Email already registered. Please use a different email.");
            }

            User user = new User(hashPassword(password), email);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setPhone(phone);
            user.setActive(true);
            user.setRole("user");
            user.setBalance(BigDecimal.ZERO);

            boolean success = UserDAO.createUser(user);
            if (success) {
                logInfo("processUserRegistration", "User registered successfully: " + email);
            } else {
                logWarning("processUserRegistration", "Failed to register user: " + email);
                throw new ServiceException("Failed to register user");
            }

            return success;
        } catch (ServiceException e) {
            
            throw e;
        } catch (RuntimeException e) {
            logSevere("processUserRegistration", "Error processing user registration: " + e.getMessage());
            throw new ServiceException("Error processing user registration: " + e.getMessage(), e);
        }
    }
    public boolean updateUserEmail(int userId, String newEmail) throws ServiceException {
        logInfo("updateUserEmail", "Updating email for user ID: " + userId);

        if (userId <= 0) {
            throw new ServiceException("Invalid user ID");
        }
        if (newEmail == null || newEmail.trim().isEmpty()) {
            throw new ServiceException("Email is required");
        }
        if (!InputValidator.isValidEmail(newEmail)) {
            throw new ServiceException("Invalid email format");
        }

        try {
            User existingUser = UserDAO.getUserByEmail(newEmail);
            if (existingUser != null && existingUser.getUserID() != userId) {
                throw new ServiceException("Email is already in use by another account");
            }
        } catch (ServiceException e) {
            throw e;
        } catch (RuntimeException e) {
            
        }

        try {
            
            return UserDAO.updateUserEmail(userId, newEmail);
        } catch (RuntimeException e) {
            logSevere("updateUserEmail", "Error updating email: " + e.getMessage());
            throw new ServiceException("Failed to update email: " + e.getMessage(), e);
        }
    }
    public boolean sendEmailVerificationCode(int userId, String newEmail) throws ServiceException {
        logInfo("sendEmailVerificationCode", "Sending verification code for user ID: " + userId + " to email: " + newEmail);

        if (userId <= 0) {
            throw new ServiceException("Invalid user ID");
        }
        if (newEmail == null || newEmail.trim().isEmpty()) {
            throw new ServiceException("Email is required");
        }
        if (!InputValidator.isValidEmail(newEmail)) {
            throw new ServiceException("Invalid email format");
        }

        try {
            User existingUser = UserDAO.getUserByEmail(newEmail);
            if (existingUser != null && existingUser.getUserID() != userId) {
                throw new ServiceException("Email is already in use by another account");
            }
        } catch (ServiceException e) {
            throw e;
        } catch (RuntimeException e) {
            
        }

        String code = String.valueOf(100000 + new java.util.Random().nextInt(900000));

        try {
            
            boolean stored = UserDAO.storeEmailVerificationCode(userId, newEmail, code);
            if (!stored) {
                throw new ServiceException("Failed to generate verification code");
            }

            EmailService emailService = com.lottery.config.ServiceFactory.getInstance().getEmailService();
            emailService.sendCustomEmail(newEmail, "Email Verification Code",
                    "Your verification code is: " + code + "\n\nThis code expires in 10 minutes.");

            logInfo("sendEmailVerificationCode", "Verification code sent successfully to: " + newEmail);
            return true;
        } catch (RuntimeException e) {
            logSevere("sendEmailVerificationCode", "Error sending verification code: " + e.getMessage());
            throw new ServiceException("Failed to send verification code: " + e.getMessage(), e);
        }
    }
    public boolean verifyEmailCode(int userId, String code) throws ServiceException {
        logInfo("verifyEmailCode", "Verifying email code for user ID: " + userId);

        if (userId <= 0) {
            throw new ServiceException("Invalid user ID");
        }
        if (code == null || code.trim().isEmpty()) {
            throw new ServiceException("Verification code is required");
        }

        try {
            
            String newEmail = UserDAO.getNewEmailFromVerificationCode(userId, code);
            if (newEmail == null) {
                throw new ServiceException("Invalid or expired verification code");
            }

            UserDAO.markVerificationCodeAsUsed(userId, code);

            return updateUserEmail(userId, newEmail);
        } catch (ServiceException e) {
            throw e;
        } catch (RuntimeException e) {
            logSevere("verifyEmailCode", "Error verifying code: " + e.getMessage());
            throw new ServiceException("Failed to verify code: " + e.getMessage(), e);
        }
    }
}