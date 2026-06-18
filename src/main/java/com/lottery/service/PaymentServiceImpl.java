package com.lottery.service;

import com.lottery.db.PaymentMethodDAO;
import com.lottery.db.UserDAO;
import com.lottery.model.PaymentMethod;
import com.lottery.model.User;
import com.lottery.service.exception.InsufficientBalanceException;
import com.lottery.service.exception.ServiceException;
import com.lottery.service.exception.UserNotFoundException;
import com.lottery.util.InputValidator;

import java.math.BigDecimal;
import java.util.List;

public class PaymentServiceImpl extends BaseService implements PaymentService {
    public boolean processTopUp(User user, double amount, String paymentMethod)
            throws InsufficientBalanceException, ServiceException {

        if (user == null) {
            throw new ServiceException("User is required");
        }
        if (amount <= 0) {
            throw new ServiceException("Invalid top-up amount: " + amount);
        }
        if (amount > 10000) {
            throw new ServiceException("Top-up amount exceeds limit: " + amount);
        }

        try {
            boolean success = UserDAO.updateUserBalance(user.getUserID(), BigDecimal.valueOf(amount));

            if (success) {
                int paymentMethodId = 0;
                if (paymentMethod != null && paymentMethod.startsWith("paymentMethod:")) {
                    try {
                        paymentMethodId = Integer.parseInt(paymentMethod.substring("paymentMethod:".length()));
                    } catch (NumberFormatException ex) {
                        
                    }
                }
                com.lottery.db.TransactionDAO.createTransaction(user.getUserID(), BigDecimal.valueOf(amount),
                        paymentMethodId);
                return true;
            } else {
                throw new ServiceException("Failed to process top-up");
            }
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Error processing top-up: " + e.getMessage(), e);
        }
    }
    public List<PaymentMethod> getUserPaymentMethods(int userId) throws UserNotFoundException, ServiceException {
        if (userId <= 0) {
            throw new ServiceException("Invalid user ID");
        }

        try {
            User user = UserDAO.getUserById(userId);
            if (user == null) {
                throw new UserNotFoundException(String.valueOf(userId));
            }
            return PaymentMethodDAO.getPaymentMethodsByUserId(userId);
        } catch (UserNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Error getting user payment methods: " + e.getMessage(), e);
        }
    }
    public boolean addPaymentMethod(User user, String cardNumber, String expiryDate, String cvv)
            throws ServiceException {

        if (user == null) {
            throw new ServiceException("User is required");
        }
        if (cardNumber == null || cardNumber.trim().isEmpty()) {
            throw new ServiceException("Card number is required");
        }
        if (cardNumber.trim().length() < 13 || cardNumber.trim().length() > 19) {
            throw new ServiceException("Invalid card number length");
        }
        if (expiryDate == null || expiryDate.trim().isEmpty()) {
            throw new ServiceException("Expiry date is required");
        }
        if (!expiryDate.trim().matches("\\d{2}/\\d{2}")) {
            throw new ServiceException("Invalid expiry date format. Use MM/YY format.");
        }
        if (cvv == null || cvv.trim().isEmpty()) {
            throw new ServiceException("CVV is required");
        }
        if (cvv.trim().length() < 3 || cvv.trim().length() > 4 || !cvv.trim().matches("\\d{3,4}")) {
            throw new ServiceException("Invalid CVV format. Must be 3 or 4 digits.");
        }

        try {
            
            com.lottery.service.CardValidationService validationService = com.lottery.config.ServiceFactory.getInstance().getCardValidationService();
            if (validationService != null && !validationService.isValidCard(cardNumber)) {
                throw new ServiceException("The provided credit card details could not be verified or are invalid.");
            }

            PaymentMethod paymentMethod = new PaymentMethod();
            paymentMethod.setUserId(user.getUserID());
            paymentMethod.setCardHolder(user.getEmail());
            paymentMethod.setExpiryDate(expiryDate);
            if (cardNumber.length() >= 4) {
                paymentMethod.setLastFourDigits(cardNumber.substring(cardNumber.length() - 4));
            }
            return PaymentMethodDAO.addPaymentMethod(paymentMethod);
        } catch (Exception e) {
            throw new ServiceException("Error adding payment method: " + e.getMessage(), e);
        }
    }
    public boolean addPaymentMethodWithCardHolder(User user, String cardNumber, String expiryDate, String cvv,
            String cardHolder) throws ServiceException {

        if (user == null) {
            throw new ServiceException("User is required");
        }
        if (cardNumber == null || cardNumber.trim().isEmpty()) {
            throw new ServiceException("Card number is required");
        }
        if (cardNumber.trim().length() < 13 || cardNumber.trim().length() > 19) {
            throw new ServiceException("Invalid card number length");
        }
        if (expiryDate == null || expiryDate.trim().isEmpty()) {
            throw new ServiceException("Expiry date is required");
        }
        if (!expiryDate.trim().matches("\\d{2}/\\d{2}")) {
            throw new ServiceException("Invalid expiry date format. Use MM/YY format.");
        }
        if (cvv == null || cvv.trim().isEmpty()) {
            throw new ServiceException("CVV is required");
        }
        if (cvv.trim().length() < 3 || cvv.trim().length() > 4 || !cvv.trim().matches("\\d{3,4}")) {
            throw new ServiceException("Invalid CVV format. Must be 3 or 4 digits.");
        }
        if (cardHolder == null || cardHolder.trim().isEmpty()) {
            throw new ServiceException("Card holder name is required");
        }
        if (cardHolder.trim().length() > 100) {
            throw new ServiceException("Card holder name too long");
        }

        try {
            
            com.lottery.service.CardValidationService validationService = com.lottery.config.ServiceFactory.getInstance().getCardValidationService();
            if (validationService != null && !validationService.isValidCard(cardNumber)) {
                throw new ServiceException("The provided credit card details could not be verified or are invalid.");
            }

            PaymentMethod paymentMethod = new PaymentMethod();
            paymentMethod.setUserId(user.getUserID());
            paymentMethod.setCardHolder(cardHolder);
            paymentMethod.setExpiryDate(expiryDate);
            if (cardNumber.length() >= 4) {
                paymentMethod.setLastFourDigits(cardNumber.substring(cardNumber.length() - 4));
            }
            return PaymentMethodDAO.addPaymentMethod(paymentMethod);
        } catch (Exception e) {
            throw new ServiceException("Error adding payment method: " + e.getMessage(), e);
        }
    }
    public boolean removePaymentMethod(int paymentMethodId) throws ServiceException {
        if (paymentMethodId <= 0) {
            throw new ServiceException("Invalid payment method ID");
        }

        try {
            PaymentMethod paymentMethod = PaymentMethodDAO.getPaymentMethodById(paymentMethodId);
            if (paymentMethod == null) {
                throw new ServiceException("Payment method not found: " + paymentMethodId);
            }
            return PaymentMethodDAO.deletePaymentMethod(paymentMethodId, paymentMethod.getUserId());
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Error removing payment method: " + e.getMessage(), e);
        }
    }
    public List<String> getUserPaymentHistory(int userId, int offset, int limit)
            throws UserNotFoundException, ServiceException {
        if (userId <= 0) {
            throw new ServiceException("Invalid user ID");
        }
        if (offset < 0 || limit <= 0 || limit > 1000) {
            throw new ServiceException("Invalid pagination parameters");
        }

        try {
            User user = UserDAO.getUserById(userId);
            if (user == null) {
                throw new UserNotFoundException(String.valueOf(userId));
            }
            
            return List.of();
        } catch (UserNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Error getting user payment history: " + e.getMessage(), e);
        }
    }
    public boolean processAddPaymentMethod(User currentUser, String cardNumber, String expiryMonth,
            String expiryYear, String cvv, String cardHolder)
            throws ServiceException, UserNotFoundException {

        if (cardHolder == null || cardHolder.trim().isEmpty()) {
            cardHolder = currentUser.getEmail();
        }

        if (cardNumber == null || cardNumber.trim().isEmpty() ||
                cardHolder == null || cardHolder.trim().isEmpty() ||
                expiryMonth == null || expiryMonth.trim().isEmpty() ||
                expiryYear == null || expiryYear.trim().isEmpty() ||
                cvv == null || cvv.trim().isEmpty()) {
            throw new ServiceException("All fields are required.");
        }

        cardNumber = InputValidator.sanitizeString(cardNumber);
        cardHolder = InputValidator.sanitizeString(cardHolder);
        expiryMonth = InputValidator.sanitizeString(expiryMonth);
        expiryYear = InputValidator.sanitizeString(expiryYear);
        cvv = InputValidator.sanitizeString(cvv);

        if (InputValidator.containsSQLInjectionPatterns(cardNumber) ||
                InputValidator.containsSQLInjectionPatterns(cardHolder) ||
                InputValidator.containsSQLInjectionPatterns(expiryMonth) ||
                InputValidator.containsSQLInjectionPatterns(expiryYear) ||
                InputValidator.containsSQLInjectionPatterns(cvv)) {
            throw new ServiceException("Invalid input values.");
        }

        cardNumber = cardNumber.trim();
        if (cardNumber.length() < 13 || cardNumber.length() > 19) {
            throw new ServiceException("Invalid card number length.");
        }

        cardHolder = cardHolder.trim();
        if (cardHolder.length() > 100) {
            throw new ServiceException("Card holder name too long.");
        }

        expiryMonth = expiryMonth.trim();
        if (!expiryMonth.matches("\\d{2}") || Integer.parseInt(expiryMonth) < 1 || Integer.parseInt(expiryMonth) > 12) {
            throw new ServiceException("Invalid expiry month. Use MM format (01-12).");
        }

        expiryYear = expiryYear.trim();
        if (!expiryYear.matches("\\d{2}")) {
            throw new ServiceException("Invalid expiry year. Use YY format.");
        }

        String expiryDate = expiryMonth + "/" + expiryYear;

        cvv = cvv.trim();
        if (cvv.length() < 3 || cvv.length() > 4 || !cvv.matches("\\d{3,4}")) {
            throw new ServiceException("Invalid CVV. Must be 3 or 4 digits.");
        }

        if (addPaymentMethodWithCardHolder(currentUser, cardNumber, expiryDate, cvv, cardHolder)) {
            return true;
        } else {
            throw new ServiceException("Failed to add payment method. Please try again.");
        }
    }
    public boolean processDeletePaymentMethod(String paymentIdStr)
            throws ServiceException {

        try {
            int paymentId = Integer.parseInt(paymentIdStr);
            if (paymentId <= 0) {
                throw new ServiceException("Invalid payment method ID.");
            }

            if (removePaymentMethod(paymentId)) {
                return true;
            } else {
                throw new ServiceException("Failed to delete payment method.");
            }
        } catch (NumberFormatException e) {
            throw new ServiceException("Invalid payment method ID.");
        }
    }
    public boolean processTopUpRequest(User currentUser, String amountStr, String paymentMethodIdStr)
            throws ServiceException, UserNotFoundException, InsufficientBalanceException {

        if (amountStr != null) {
            amountStr = InputValidator.sanitizeString(amountStr);
        }
        if (paymentMethodIdStr != null) {
            paymentMethodIdStr = InputValidator.sanitizeString(paymentMethodIdStr);
        }

        if ((amountStr != null && InputValidator.containsSQLInjectionPatterns(amountStr)) ||
                (paymentMethodIdStr != null && InputValidator.containsSQLInjectionPatterns(paymentMethodIdStr))) {
            throw new ServiceException("Invalid input values.");
        }

        try {
            BigDecimal amount = new BigDecimal(amountStr);
            int paymentMethodId = Integer.parseInt(paymentMethodIdStr);

            if (amount.compareTo(BigDecimal.ZERO) <= 0 || amount.compareTo(new BigDecimal("10000")) > 0) {
                throw new ServiceException("Invalid top-up amount. Amount must be between $0.01 and $10,000.");
            }
            if (paymentMethodId <= 0) {
                throw new ServiceException("Invalid payment method.");
            }

            if (processTopUp(currentUser, amount.doubleValue(), "paymentMethod:" + paymentMethodId)) {
                return true;
            } else {
                throw new ServiceException("Failed to update balance. Please try again.");
            }
        } catch (NumberFormatException e) {
            throw new ServiceException("Invalid top-up amount or payment method.");
        }
    }
}