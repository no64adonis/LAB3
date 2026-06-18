package com.lottery.db;

import java.time.LocalDate;

public class LotteryTicketSearchCriteria {
    private String numbers;
    private String company;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate specificDate;

    private Boolean published; 
    private String ownerId;
    private Boolean onlyUnpurchased; 
    private Boolean useDateRange = null; 

    private Integer num1;
    private Integer num2;
    private Integer num3;
    private Integer num4;
    private Integer num5;
    private Integer num6;

    private int offset = 0;
    private int limit = 10;

    public static LotteryTicketSearchCriteria create() {
        return new LotteryTicketSearchCriteria();
    }

    public LotteryTicketSearchCriteria numbers(String numbers) {
        this.numbers = numbers;
        return this;
    }

    public LotteryTicketSearchCriteria number1(Integer num1) {
        this.num1 = num1;
        return this;
    }

    public LotteryTicketSearchCriteria number2(Integer num2) {
        this.num2 = num2;
        return this;
    }

    public LotteryTicketSearchCriteria number3(Integer num3) {
        this.num3 = num3;
        return this;
    }

    public LotteryTicketSearchCriteria number4(Integer num4) {
        this.num4 = num4;
        return this;
    }

    public LotteryTicketSearchCriteria number5(Integer num5) {
        this.num5 = num5;
        return this;
    }

    public LotteryTicketSearchCriteria number6(Integer num6) {
        this.num6 = num6;
        return this;
    }

    public LotteryTicketSearchCriteria company(String company) {
        this.company = company;
        return this;
    }

    public LotteryTicketSearchCriteria dateRange(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.specificDate = null; 
        this.useDateRange = Boolean.TRUE;
        return this;
    }

    public LotteryTicketSearchCriteria specificDate(LocalDate specificDate) {
        this.specificDate = specificDate;
        this.startDate = null; 
        this.endDate = null; 
        this.useDateRange = Boolean.FALSE;
        return this;
    }

    public LotteryTicketSearchCriteria clearDates() {
        this.startDate = null;
        this.endDate = null;
        this.specificDate = null;
        this.useDateRange = null;
        return this;
    }

    public LotteryTicketSearchCriteria published(Boolean published) {
        this.published = published;
        return this;
    }

    public LotteryTicketSearchCriteria ownerId(String ownerId) {
        this.ownerId = ownerId;
        return this;
    }

    public LotteryTicketSearchCriteria onlyUnpurchased(Boolean onlyUnpurchased) {
        this.onlyUnpurchased = onlyUnpurchased;
        return this;
    }

    public LotteryTicketSearchCriteria pagination(int offset, int limit) {
        this.offset = offset;
        this.limit = limit;
        return this;
    }

    public String getNumbers() {
        return numbers;
    }

    public Integer getNum1() {
        return num1;
    }

    public Integer getNum2() {
        return num2;
    }

    public Integer getNum3() {
        return num3;
    }

    public Integer getNum4() {
        return num4;
    }

    public Integer getNum5() {
        return num5;
    }

    public Integer getNum6() {
        return num6;
    }

    public String getCompany() {
        return company;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public LocalDate getSpecificDate() {
        return specificDate;
    }

    public Boolean getPublished() {
        return published;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public Boolean getOnlyUnpurchased() {
        return onlyUnpurchased;
    }

    public int getOffset() {
        return offset;
    }

    public int getLimit() {
        return limit;
    }

    public Boolean isUseDateRange() {
        return useDateRange;
    }

    public boolean shouldApplyDateFilter() {
        return useDateRange != null;
    }
}
