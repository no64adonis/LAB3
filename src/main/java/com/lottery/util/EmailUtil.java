package com.lottery.util;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.mail.*;
import jakarta.mail.internet.*;

public class EmailUtil {
    private static final Logger logger = Logger.getLogger(EmailUtil.class.getName());
    
    public static boolean sendInvitationEmail(String toEmail, String firstName, String lastName) {
        
        String defaultMessage = "Dear " + firstName + " " + lastName + ",\n\n" +
                              "You have been invited to join the Lottery Application.\n\n" +
                              "Please visit our website to register and set up your account.\n\n" +
                              "Best regards,\n" +
                              "Lottery Application Team";
        
        return sendEmail(toEmail, "Lottery Application Invitation", defaultMessage);
    }
    
    public static boolean sendCustomInvitationEmail(String toEmail, String firstName, String lastName, String customMessage) {
        return sendEmail(toEmail, "Lottery Application Invitation", customMessage);
    }
    
    public static boolean sendCustomEmail(String toEmail, String subject, String message) {
        return sendEmail(toEmail, subject, message);
    }
    
    private static boolean sendEmail(String toEmail, String subject, String message) {
        
        if (!EmailConfig.isValidConfig()) {
            logger.severe("Email configuration is invalid. Please check your SMTP settings.");
            return false;
        }
        
        if (!isValidEmail(toEmail)) {
            logger.warning("Invalid email address: " + toEmail);
            return false;
        }
        
        try {
            logger.info("Attempting to send email to: " + toEmail);
            logger.info("SMTP Host: " + EmailConfig.SMTP_HOST);
            logger.info("SMTP Port: " + EmailConfig.SMTP_PORT);
            logger.info("SMTP Username: " + EmailConfig.SMTP_USERNAME);
            logger.info("From Email: " + EmailConfig.FROM_EMAIL);
            
            Properties props = new Properties();
            props.put("mail.smtp.host", EmailConfig.SMTP_HOST);
            props.put("mail.smtp.port", EmailConfig.SMTP_PORT);
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");
            props.put("mail.smtp.timeout", String.valueOf(EmailConfig.TIMEOUT_MS));
            props.put("mail.smtp.connectiontimeout", String.valueOf(EmailConfig.CONNECTION_TIMEOUT_MS));
            
            logger.fine("SMTP properties configured: " + props);
            
            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    logger.info("Authenticating with username: " + EmailConfig.SMTP_USERNAME);
                    return new PasswordAuthentication(EmailConfig.SMTP_USERNAME, EmailConfig.SMTP_PASSWORD);
                }
            });
            
            boolean isDebugMode = Boolean.getBoolean("email.debug.mode");
            if (isDebugMode) {
                session.setDebug(true);
                logger.info("Email debug mode enabled");
            }
            
            Message emailMessage = new MimeMessage(session);
            emailMessage.setFrom(new InternetAddress(EmailConfig.FROM_EMAIL));
            emailMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            emailMessage.setSubject(subject);
            emailMessage.setText(message);
            
            logger.info("Email message created. Subject: " + subject);
            
            Transport.send(emailMessage);
            
            logger.info("Email sent successfully to: " + toEmail);
            return true;
            
        } catch (MessagingException e) {
            logger.log(Level.SEVERE, "Failed to send email to: " + toEmail, e);
            logger.severe("MessagingException details: " + e.getMessage());
            
            if (e instanceof AuthenticationFailedException) {
                logger.severe("Authentication failed. Please check your email credentials and app password.");
            } else if (e instanceof SendFailedException) {
                logger.severe("Send failed. Please check the recipient email address and SMTP server settings.");
            } else if (e instanceof MessagingException) {
                logger.severe("Messaging error. Please check your network connection and SMTP server availability.");
            }
            
            return false;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error while sending email to: " + toEmail, e);
            logger.severe("Exception details: " + e.getMessage());
            return false;
        }
    }
    
    private static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@" +
                           "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        
        return email.matches(emailRegex);
    }
    
    public static boolean testEmailConfiguration() {
        logger.info("Testing email configuration...");
        
        if (!EmailConfig.isValidConfig()) {
            logger.severe("Email configuration is invalid");
            return false;
        }
        
        try {
            
            Properties props = new Properties();
            props.put("mail.smtp.host", EmailConfig.SMTP_HOST);
            props.put("mail.smtp.port", EmailConfig.SMTP_PORT);
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            
            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(EmailConfig.SMTP_USERNAME, EmailConfig.SMTP_PASSWORD);
                }
            });
            
            Transport transport = session.getTransport("smtp");
            transport.connect(EmailConfig.SMTP_HOST, Integer.parseInt(EmailConfig.SMTP_PORT), 
                            EmailConfig.SMTP_USERNAME, EmailConfig.SMTP_PASSWORD);
            transport.close();
            
            logger.info("Email configuration test successful");
            return true;
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Email configuration test failed", e);
            return false;
        }
    }
}
