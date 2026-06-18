package com.lottery.service.exception;

import java.math.BigDecimal;

public class InsufficientBalanceException extends UserException {
    private final BigDecimal currentBalance;
    private final BigDecimal requiredAmount;
    
    public InsufficientBalanceException(BigDecimal currentBalance, BigDecimal requiredAmount) {
        super("Insufficient balance. Current: " + currentBalance + ", Required: " + requiredAmount);
        this.currentBalance = currentBalance;
        this.requiredAmount = requiredAmount;
    }
    
    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }
    
    public BigDecimal getRequiredAmount() {
        return requiredAmount;
    }
    
    public double getTicketPrice() {
        return requiredAmount != null ? requiredAmount.doubleValue() : 0.0;
    }
}