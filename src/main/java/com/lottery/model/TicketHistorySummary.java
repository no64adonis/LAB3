package com.lottery.model;

import java.math.BigDecimal;

public class TicketHistorySummary {
    private final BigDecimal totalSpent;
    private final BigDecimal totalWinnings;
    private final BigDecimal netResult;

    public TicketHistorySummary(BigDecimal totalSpent, BigDecimal totalWinnings) {
        this.totalSpent = totalSpent;
        this.totalWinnings = totalWinnings;
        this.netResult = totalWinnings.subtract(totalSpent);
    }

    public BigDecimal getTotalSpent() {
        return totalSpent;
    }

    public BigDecimal getTotalWinnings() {
        return totalWinnings;
    }

    public BigDecimal getNetResult() {
        return netResult;
    }
}