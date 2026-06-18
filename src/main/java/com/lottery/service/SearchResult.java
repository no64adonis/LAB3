package com.lottery.service;

import com.lottery.model.LotteryTicket;
import java.util.List;

public class SearchResult {
    private List<LotteryTicket> tickets;
    private int currentPage;
    private int totalPages;
    private int totalTickets;
    private String company;
    private String num1;
    private String num2;
    private String num3;
    private String num4;
    private String num5;
    private String num6;
    private String startDate;
    private String endDate;
    private String specificDate;

    public SearchResult() {
    }

    public List<LotteryTicket> getTickets() {
        return tickets;
    }

    public void setTickets(List<LotteryTicket> tickets) {
        this.tickets = tickets;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getTotalTickets() {
        return totalTickets;
    }

    public void setTotalTickets(int totalTickets) {
        this.totalTickets = totalTickets;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getNum1() {
        return num1;
    }

    public void setNum1(String num1) {
        this.num1 = num1;
    }

    public String getNum2() {
        return num2;
    }

    public void setNum2(String num2) {
        this.num2 = num2;
    }

    public String getNum3() {
        return num3;
    }

    public void setNum3(String num3) {
        this.num3 = num3;
    }

    public String getNum4() {
        return num4;
    }

    public void setNum4(String num4) {
        this.num4 = num4;
    }

    public String getNum5() {
        return num5;
    }

    public void setNum5(String num5) {
        this.num5 = num5;
    }

    public String getNum6() {
        return num6;
    }

    public void setNum6(String num6) {
        this.num6 = num6;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getSpecificDate() {
        return specificDate;
    }

    public void setSpecificDate(String specificDate) {
        this.specificDate = specificDate;
    }
}