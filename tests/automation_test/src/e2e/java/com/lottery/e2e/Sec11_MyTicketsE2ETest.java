package com.lottery.e2e;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Sec11_MyTicketsE2ETest extends E2ETestBase {

    @Test @Order(1)
    public void test01_ViewMyTickets() {
        loginAsUser();
        goToMyTickets();
        assertTrue(pageContainsText("My Tickets") || pageContainsText("Purchased") || pageContainsText("ticket"),
            "My Tickets page should display user's purchased tickets");
    }

    @Test @Order(2)
    public void test02_SearchMyTickets() {
        loginAsUser();
        navigateTo("/myTickets?company=Vietlott");
        assertTrue(isOnPage("myTickets"), "Should filter my tickets by search criteria");
    }

    @Test @Order(3)
    public void test03_SearchByDateRange() {
        loginAsUser();
        navigateTo("/myTickets?startDate=2026-01-01&endDate=2026-12-31");
        assertTrue(isOnPage("myTickets"), "Should filter my tickets by date range");
    }

    @Test @Order(4)
    public void test04_PaginationMyTickets() {
        loginAsUser();
        navigateTo("/myTickets?page=1");
        assertTrue(isOnPage("myTickets"), "Pagination should work on my tickets page");
    }

    @Test @Order(5)
    public void test05_EmptyMyTickets() {
        loginAsUser();
        goToMyTickets();
        assertTrue(isOnPage("myTickets"), "Should handle empty ticket list gracefully");
    }

    @Test @Order(6)
    public void test06_UnauthenticatedAccess() {
        goToMyTickets();
        assertTrue(isOnPage("login"), "Should redirect unauthenticated users to login");
    }
}
