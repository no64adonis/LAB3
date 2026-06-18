package com.lottery.service;

import com.lottery.model.PaymentMethod;
import com.lottery.model.User;
import com.lottery.service.exception.InsufficientBalanceException;
import com.lottery.service.exception.ServiceException;
import com.lottery.service.exception.UserNotFoundException;

import java.util.List;

public interface PaymentService {
    
    boolean processTopUp(User user, double amount, String paymentMethod) throws InsufficientBalanceException, ServiceException;
    
    List<PaymentMethod> getUserPaymentMethods(int userId) throws UserNotFoundException, ServiceException;
    
    boolean addPaymentMethod(User user, String cardNumber, String expiryDate, String cvv) throws ServiceException;
    
    boolean addPaymentMethodWithCardHolder(User user, String cardNumber, String expiryDate, String cvv, String cardHolder) throws ServiceException;
    
    boolean removePaymentMethod(int paymentMethodId) throws ServiceException;
    
     List<String> getUserPaymentHistory(int userId, int offset, int limit) throws UserNotFoundException, ServiceException;
     
     boolean processAddPaymentMethod(User currentUser, String cardNumber, String expiryMonth,
                                   String expiryYear, String cvv, String cardHolder)
         throws ServiceException, UserNotFoundException;
     
      boolean processDeletePaymentMethod(String paymentIdStr)
          throws ServiceException;
      
      boolean processTopUpRequest(User currentUser, String amountStr, String paymentMethodIdStr)
          throws ServiceException, UserNotFoundException, InsufficientBalanceException;
  }