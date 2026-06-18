package com.lottery.service;

import com.lottery.model.User;
import com.lottery.service.exception.InvalidCredentialsException;
import com.lottery.service.exception.ServiceException;
import com.lottery.service.exception.UserNotFoundException;

public interface AuthService {

    boolean validateUser(String email, String password) throws InvalidCredentialsException, ServiceException;

    boolean updateLastLoginDate(String email) throws ServiceException;

    boolean resetUserPassword(String email) throws UserNotFoundException, ServiceException;

    boolean changeUserPassword(int userId, String oldPassword, String newPassword)
            throws InvalidCredentialsException, ServiceException;

    boolean setUserPassword(int userId, String newPassword) throws ServiceException;

    String generatePasswordResetToken(String email) throws UserNotFoundException, ServiceException;

    boolean validatePasswordResetToken(String token) throws ServiceException;

    User getUserByPasswordResetToken(String token) throws UserNotFoundException, ServiceException;
}