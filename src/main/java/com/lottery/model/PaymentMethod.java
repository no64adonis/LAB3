package com.lottery.model;

public class PaymentMethod {
    private int id;
    private int userId;
    private String cardNumber;
    private String lastFourDigits;
    private String cardHolder;
    private String expiryDate;
    private String cvv;
    
    public PaymentMethod() {}
    
    public PaymentMethod(int userId, String cardNumber, String cardHolder, String expiryDate, String cvv) {
        this.userId = userId;
        this.cardNumber = cardNumber;
        this.cardHolder = cardHolder;
        this.expiryDate = expiryDate;
        this.cvv = cvv;
        
        if (cardNumber != null && cardNumber.length() >= 4) {
            this.lastFourDigits = cardNumber.substring(cardNumber.length() - 4);
        }
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public String getCardNumber() {
        return cardNumber;
    }
    
    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
        
        if (cardNumber != null && cardNumber.length() >= 4) {
            this.lastFourDigits = cardNumber.substring(cardNumber.length() - 4);
        }
    }
    
    public String getLastFourDigits() {
        return lastFourDigits;
    }
    
    public void setLastFourDigits(String lastFourDigits) {
        this.lastFourDigits = lastFourDigits;
    }
    
    public String getCardHolder() {
        return cardHolder;
    }
    
    public void setCardHolder(String cardHolder) {
        this.cardHolder = cardHolder;
    }
    
    public String getExpiryDate() {
        return expiryDate;
    }
    
    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }
    
    public String getCvv() {
        return cvv;
    }
    
    public void setCvv(String cvv) {
        this.cvv = cvv;
    }
}