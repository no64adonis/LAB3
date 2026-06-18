package com.lottery.service;

import com.lottery.model.Transaction;
import com.lottery.service.exception.ServiceException;

import java.math.BigDecimal;
import java.util.List;

public interface TransactionService {

    boolean recordTransaction(int userId, BigDecimal amount, int paymentMethodId) throws ServiceException;

    List<Transaction> getUserTransactions(int userId, int offset, int limit) throws ServiceException;

    int getUserTransactionCount(int userId) throws ServiceException;

    List<Transaction> getAllTransactions(int offset, int limit) throws ServiceException;

    int getAllTransactionCount() throws ServiceException;

    List<Transaction> searchTransactions(String email, String startDate, String endDate,
            int offset, int limit) throws ServiceException;

    int searchTransactionCount(String email, String startDate, String endDate) throws ServiceException;
}
