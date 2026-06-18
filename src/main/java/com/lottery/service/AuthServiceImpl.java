package com.lottery.service;

import com.lottery.db.PasswordResetTokenDAO;
import com.lottery.db.UserDAO;
import com.lottery.model.User;
import com.lottery.service.exception.InvalidCredentialsException;
import com.lottery.service.exception.ServiceException;
import com.lottery.service.exception.UserNotFoundException;
import com.lottery.util.InputValidator;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class AuthServiceImpl extends BaseService implements AuthService {
    private static final int TOKEN_LENGTH = 32;
    private static final int TOKEN_EXPIRY_HOURS = 24;
    public boolean validateUser(String email, String password) throws InvalidCredentialsException, ServiceException {
        if (email == null || email.trim().isEmpty()) {
            throw new InvalidCredentialsException("Email is required");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new InvalidCredentialsException("Password is required");
        }

        try {
            boolean isValid = UserDAO.validateUser(email, password);
            if (!isValid) {
                throw new InvalidCredentialsException();
            }
            return isValid;
        } catch (InvalidCredentialsException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Error validating user: " + e.getMessage(), e);
        }
    }
    public boolean updateLastLoginDate(String email) throws ServiceException {
        try {
            return UserDAO.updateLastLoginDate(email);
        } catch (Exception e) {
            throw new ServiceException("Error updating last login date: " + e.getMessage(), e);
        }
    }
    public boolean resetUserPassword(String email) throws UserNotFoundException, ServiceException {
        if (email == null || email.trim().isEmpty() || !InputValidator.isValidEmail(email)) {
            throw new ServiceException("Invalid email address");
        }

        try {
            User user = UserDAO.getUserByEmail(email);
            if (user == null) {
                throw new UserNotFoundException(email);
            }

            String token = generatePasswordResetToken(email);

            EmailService emailService = com.lottery.config.ServiceFactory.getInstance().getEmailService();
            boolean emailSent = emailService.sendPasswordResetEmail(email, token);

            if (!emailSent) {
                throw new ServiceException("Failed to send password reset email");
            }

            return true;
        } catch (UserNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logSevere("resetUserPassword", "Error resetting user password: " + e.getMessage());
            throw new ServiceException("Error resetting user password: " + e.getMessage(), e);
        }
    }
    public boolean changeUserPassword(int userId, String oldPassword, String newPassword)
            throws InvalidCredentialsException, ServiceException {

        if (oldPassword == null || oldPassword.trim().isEmpty()) {
            throw new InvalidCredentialsException("Current password is required");
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new InvalidCredentialsException("New password is required");
        }
        if (newPassword.length() < 6 || newPassword.length() > 100) {
            throw new InvalidCredentialsException("New password must be between 6 and 100 characters");
        }

        try {
            User user = UserDAO.getUserById(userId);
            if (user == null) {
                throw new InvalidCredentialsException("User not found");
            }

            String hashedOldPassword = hashPassword(oldPassword);
            if (!user.getPasswordHash().equals(hashedOldPassword)) {
                throw new InvalidCredentialsException("Current password is incorrect");
            }

            return UserDAO.updateUserPassword(userId, newPassword);
        } catch (InvalidCredentialsException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Error changing user password: " + e.getMessage(), e);
        }
    }
    public boolean setUserPassword(int userId, String newPassword) throws ServiceException {
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new ServiceException("New password is required");
        }
        if (newPassword.length() < 6 || newPassword.length() > 100) {
            throw new ServiceException("New password must be between 6 and 100 characters");
        }

        try {
            return UserDAO.updateUserPassword(userId, newPassword);
        } catch (Exception e) {
            throw new ServiceException("Error setting user password: " + e.getMessage(), e);
        }
    }
    public String generatePasswordResetToken(String email) throws UserNotFoundException, ServiceException {
        try {
            User user = UserDAO.getUserByEmail(email);
            if (user == null) {
                throw new UserNotFoundException(email);
            }

            String token = generateRandomToken();
            LocalDateTime expiryTime = LocalDateTime.now().plusHours(TOKEN_EXPIRY_HOURS);

            boolean success = PasswordResetTokenDAO.createToken(user.getUserID(), token, Timestamp.valueOf(expiryTime));
            if (!success) {
                throw new ServiceException("Failed to create password reset token");
            }

            return token;
        } catch (UserNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Error generating password reset token: " + e.getMessage(), e);
        }
    }
    public boolean validatePasswordResetToken(String token) throws ServiceException {
        try {
            return PasswordResetTokenDAO.isTokenValid(token);
        } catch (Exception e) {
            throw new ServiceException("Error validating password reset token: " + e.getMessage(), e);
        }
    }
    public User getUserByPasswordResetToken(String token) throws UserNotFoundException, ServiceException {
        try {
            User user = PasswordResetTokenDAO.getUserByToken(token);
            if (user == null) {
                throw new UserNotFoundException("Token: " + token);
            }
            return user;
        } catch (UserNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Error getting user by password reset token: " + e.getMessage(), e);
        }
    }

    private String generateRandomToken() throws ServiceException {
        try {
            SecureRandom random = new SecureRandom();
            byte[] tokenBytes = new byte[TOKEN_LENGTH];
            random.nextBytes(tokenBytes);

            StringBuilder sb = new StringBuilder();
            for (byte b : tokenBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new ServiceException("Error generating token", e);
        }
    }

    private String hashPassword(String password) throws ServiceException {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new ServiceException("Error processing password", e);
        }
    }
}