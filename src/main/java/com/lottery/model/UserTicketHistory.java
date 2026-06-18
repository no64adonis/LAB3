package com.lottery.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class UserTicketHistory {
    private int historyID;
    private int userID;
    private String ticketID;
    private String numbers;
    private String company;
    private int roundID;
    private BigDecimal purchasePrice;
    private BigDecimal winnings;
    private int matchCount;
    private LocalDateTime claimDate;

    public int getHistoryID() {
        return historyID;
    }

    public void setHistoryID(int historyID) {
        this.historyID = historyID;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getTicketID() {
        return ticketID;
    }

    public void setTicketID(String ticketID) {
        this.ticketID = ticketID;
    }

    public String getNumbers() {
        return numbers;
    }

    public void setNumbers(String numbers) {
        this.numbers = numbers;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public int getRoundID() {
        return roundID;
    }

    public void setRoundID(int roundID) {
        this.roundID = roundID;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public BigDecimal getWinnings() {
        return winnings;
    }

    public void setWinnings(BigDecimal winnings) {
        this.winnings = winnings;
    }

    public int getMatchCount() {
        return matchCount;
    }

    public void setMatchCount(int matchCount) {
        this.matchCount = matchCount;
    }

    public LocalDateTime getClaimDate() {
        return claimDate;
    }

    public void setClaimDate(LocalDateTime claimDate) {
        this.claimDate = claimDate;
    }
}