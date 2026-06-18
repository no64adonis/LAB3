package com.lottery.model;

import java.time.LocalDate;

public class LotteryTicket {

    private String ticketID;
    private String numbers;
    private String company;
    private LocalDate creationDate;
    private boolean published;
    private int viewCount;
    private double price;
    private String ownerId;

    public LotteryTicket() {
    }

    public LotteryTicket(String numbers, String company) {
        this.numbers = numbers;
        this.company = company;
        this.creationDate = LocalDate.now();
        this.published = false;
        this.viewCount = 0;
        this.price = 10.0; 
        this.ownerId = null; 
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

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }
}
