package com.lottery.service;

import com.lottery.db.TransactionDAO;
import com.lottery.model.Transaction;
import com.lottery.service.exception.ServiceException;

import java.math.BigDecimal;
import java.util.List;

public class TransactionServiceImpl extends BaseService implements TransactionService {
    public boolean recordTransaction(int userId, BigDecimal amount, int paymentMethodId) throws ServiceException {
        if (userId <= 0) {
            throw new ServiceException("Invalid user ID");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ServiceException("Invalid transaction amount");
        }

        try {
            return TransactionDAO.createTransaction(userId, amount, paymentMethodId);
        } catch (RuntimeException e) {
            logSevere("recordTransaction", "Error recording transaction: " + e.getMessage());
            throw new ServiceException("Error recording transaction: " + e.getMessage(), e);
        }
    }
    public List<Transaction> getUserTransactions(int userId, int offset, int limit) throws ServiceException {
        if (userId <= 0) {
            throw new ServiceException("Invalid user ID");
        }
        if (offset < 0 || limit <= 0 || limit > 1000) {
            throw new ServiceException("Invalid pagination parameters");
        }

        try {
            return TransactionDAO.getTransactionsByUserId(userId, offset, limit);
        } catch (RuntimeException e) {
            throw new ServiceException("Error getting user transactions: " + e.getMessage(), e);
        }
    }
    public int getUserTransactionCount(int userId) throws ServiceException {
        if (userId <= 0) {
            throw new ServiceException("Invalid user ID");
        }

        try {
            return TransactionDAO.getTransactionCountByUserId(userId);
        } catch (RuntimeException e) {
            throw new ServiceException("Error getting user transaction count: " + e.getMessage(), e);
        }
    }
    public List<Transaction> getAllTransactions(int offset, int limit) throws ServiceException {
        if (offset < 0 || limit <= 0 || limit > 1000) {
            throw new ServiceException("Invalid pagination parameters");
        }

        try {
            return TransactionDAO.getAllTransactions(offset, limit);
        } catch (RuntimeException e) {
            throw new ServiceException("Error getting all transactions: " + e.getMessage(), e);
        }
    }
    public int getAllTransactionCount() throws ServiceException {
        try {
            return TransactionDAO.getAllTransactionCount();
        } catch (RuntimeException e) {
            throw new ServiceException("Error getting total transaction count: " + e.getMessage(), e);
        }
    }
    public List<Transaction> searchTransactions(String email, String startDate, String endDate,
            int offset, int limit) throws ServiceException {
        if (offset < 0 || limit <= 0 || limit > 1000) {
            throw new ServiceException("Invalid pagination parameters");
        }

        try {
            return TransactionDAO.searchTransactions(email, startDate, endDate, offset, limit);
        } catch (RuntimeException e) {
            throw new ServiceException("Error searching transactions: " + e.getMessage(), e);
        }
    }
    public int searchTransactionCount(String email, String startDate, String endDate) throws ServiceException {
        try {
            return TransactionDAO.searchTransactionCount(email, startDate, endDate);
        } catch (RuntimeException e) {
            throw new ServiceException("Error getting search transaction count: " + e.getMessage(), e);
        }
    }
}
