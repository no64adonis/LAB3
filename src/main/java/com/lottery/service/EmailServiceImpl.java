package com.lottery.service;

import com.lottery.service.exception.EmailException;
import com.lottery.service.exception.ServiceException;
import com.lottery.util.EmailUtil;

public class EmailServiceImpl extends BaseService implements EmailService {
    public boolean sendInvitationEmail(String toEmail, String firstName, String lastName) throws EmailException {
        try {
            return EmailUtil.sendInvitationEmail(toEmail, firstName, lastName);
        } catch (Exception e) {
            throw new EmailException("Error sending invitation email: " + e.getMessage(), e);
        }
    }
    public boolean sendCustomInvitationEmail(String toEmail, String firstName, String lastName, String customMessage)
            throws EmailException {
        try {
            return EmailUtil.sendCustomInvitationEmail(toEmail, firstName, lastName, customMessage);
        } catch (Exception e) {
            throw new EmailException("Error sending custom invitation email: " + e.getMessage(), e);
        }
    }
    public boolean sendPasswordResetEmail(String toEmail, String token) throws EmailException {
        try {
            String message = "You have requested to reset your password. Please use the following link to reset your password:\n\n" +
                    "http://localhost:8080/Lottery/passwordReset?token=" + token + "\n\n" +
                    "This link will expire in 24 hours.\n\n" +
                    "If you did not request a password reset, please ignore this email.";

            return EmailUtil.sendCustomEmail(toEmail, "Lottery Application - Password Reset", message);
        } catch (Exception e) {
            throw new EmailException("Error sending password reset email: " + e.getMessage(), e);
        }
    }
    public boolean sendCustomEmail(String toEmail, String subject, String message) throws EmailException {
        try {
            return EmailUtil.sendCustomEmail(toEmail, subject, message);
        } catch (Exception e) {
            throw new EmailException("Error sending custom email: " + e.getMessage(), e);
        }
    }
    public boolean testEmailConfiguration() throws ServiceException {
        try {
            return EmailUtil.testEmailConfiguration();
        } catch (Exception e) {
            throw new ServiceException("Error testing email configuration: " + e.getMessage(), e);
        }
    }
}