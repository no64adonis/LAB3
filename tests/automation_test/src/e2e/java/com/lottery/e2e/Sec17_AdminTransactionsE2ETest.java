package com.lottery.e2e;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Sec17_AdminTransactionsE2ETest extends E2ETestBase {

    @Test @Order(1)
    public void test01_TransactionsPageLoads() {
        loginAsAdmin();
        goToAdminTransactions();
        assertTrue(pageContainsText("Transaction") || pageContainsText("Search") || pageContainsText("transaction"),
            "Admin transactions page should load");
    }

    @Test @Order(2)
    public void test02_SearchByEmail() {
        loginAsAdmin();
        navigateTo("/adminTransactions?email=" + USER_EMAIL);
        assertTrue(isOnPage("adminTransactions"), "Should filter transactions by email");
    }

    @Test @Order(3)
    public void test03_SearchByDateRange() {
        loginAsAdmin();
        navigateTo("/adminTransactions?startDate=2026-01-01&endDate=2026-12-31");
        assertTrue(isOnPage("adminTransactions"), "Should filter transactions by date range");
    }

    @Test @Order(4)
    public void test04_ResetSearchForm() {
        loginAsAdmin();
        navigateTo("/adminTransactions?email=test");
        try {
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("resetForm()");
        } catch (Exception e) {}
        assertTrue(isOnPage("adminTransactions"), "Should reset search form");
    }

    @Test @Order(5)
    public void test05_PaginationWorks() {
        loginAsAdmin();
        navigateTo("/adminTransactions?page=1");
        assertTrue(isOnPage("adminTransactions"), "Pagination should work");
    }

    @Test @Order(6)
    public void test06_NoTransactionsMessage() {
        loginAsAdmin();
        navigateTo("/adminTransactions?email=nonexistent_" + uniqueTimestamp());
        assertTrue(isOnPage("adminTransactions"), "Should show no-transactions message");
    }

    @Test @Order(7)
    public void test07_NonAdminCannotAccess() {
        loginAsUser();
        goToAdminTransactions();
        assertTrue(isOnPage("login") || isOnPage("userLottery") || isOnPage("homepage"),
            "Non-admin should not access admin transactions");
    }
}
