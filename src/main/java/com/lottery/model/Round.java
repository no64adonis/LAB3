package com.lottery.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Round {
    private int roundID;
    private LocalDate startDate;
    private LocalDate endDate;
    private String winningNumbers;
    private String status;
    private LocalDateTime createdAt;

    public int getRoundID() {
        return roundID;
    }

    public void setRoundID(int roundID) {
        this.roundID = roundID;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getWinningNumbers() {
        return winningNumbers;
    }

    public void setWinningNumbers(String winningNumbers) {
        this.winningNumbers = winningNumbers;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}