package com.lottery.service;

import com.lottery.model.User;
import com.lottery.service.exception.ServiceException;
import com.lottery.service.exception.UserNotFoundException;
import com.lottery.service.exception.ValidationException;

import java.math.BigDecimal;
import java.util.List;

public interface UserService {

        User registerUser(String email, String firstName, String lastName, String phone,
                        String password)
                        throws ServiceException, ValidationException;

        User getUserById(int userId) throws UserNotFoundException, ServiceException;

        User getUserByEmail(String email) throws UserNotFoundException, ServiceException;

        List<User> getAllUsers(int offset, int limit) throws ServiceException;

        List<User> searchUsers(String searchTerm, String lastLoginFrom, String lastLoginTo, String role, int offset,
                        int limit) throws ServiceException;

        List<User> searchUsers(String searchTerm, List<String> searchFields, String lastLoginFrom, String lastLoginTo,
                        String role, int offset, int limit) throws ServiceException;

        int getUserCount() throws ServiceException;

        int getSearchUserCount(String searchTerm, String lastLoginFrom, String lastLoginTo, String role)
                        throws ServiceException;

        int getSearchUserCount(String searchTerm, List<String> searchFields, String lastLoginFrom, String lastLoginTo,
                        String role) throws ServiceException;

        boolean updateUserRole(int userId, String role) throws ServiceException;

        boolean createUser(User user) throws ServiceException;

        boolean setUserActiveStatus(int userId, boolean isActive) throws ServiceException;

        boolean updateUserDetails(int userId, String email, String firstName, String lastName, String phone)
                        throws ServiceException;

        boolean updateUserPassword(int userId, String newPassword) throws ServiceException;

        boolean updateUserBalance(int userId, BigDecimal amount) throws ServiceException;

        List<User> getInactiveUsers(String period) throws ServiceException;

        boolean updateLastLoginDate(String email) throws ServiceException;

        boolean processUserRegistration(String email, String firstName, String lastName,
                        String phone, String password, String confirmPassword)
                        throws ServiceException;

        boolean updateUserEmail(int userId, String newEmail) throws ServiceException;

        boolean sendEmailVerificationCode(int userId, String newEmail) throws ServiceException;

        boolean verifyEmailCode(int userId, String code) throws ServiceException;
}