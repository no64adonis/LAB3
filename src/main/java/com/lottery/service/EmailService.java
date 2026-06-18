package com.lottery.service;

import com.lottery.service.exception.EmailException;
import com.lottery.service.exception.ServiceException;

public interface EmailService {
    
    boolean sendInvitationEmail(String toEmail, String firstName, String lastName) throws EmailException;
    
    boolean sendCustomInvitationEmail(String toEmail, String firstName, String lastName, String customMessage) throws EmailException;
    
    boolean sendPasswordResetEmail(String toEmail, String token) throws EmailException;
    
    boolean sendCustomEmail(String toEmail, String subject, String message) throws EmailException;
    
    boolean testEmailConfiguration() throws ServiceException;
}