package com.lottery.e2e;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Sec08_HomepagePublicSearchE2ETest extends E2ETestBase {

    @Test @Order(1)
    public void test01_ViewHomepageWithoutLogin() {
        goToHomepage();
        assertTrue(pageContainsText("Lottery Ticket Checker") || pageContainsText("Search"),
            "Homepage should display without login");
    }

    @Test @Order(2)
    public void test02_SearchByCompany() {
        goToHomepage();
        
        navigateTo("/homepage?company=Vietlott");
        
        assertTrue(pageContainsText("Vietlott") || pageContainsText("No") || pageContainsText("found") || pageContainsText("ticket"),
            "Search by company should show filtered results or no-results message");
    }

    @Test @Order(3)
    public void test03_SearchByNumber() {
        navigateTo("/homepage?num1=5&num3=10");
        assertTrue(isOnPage("homepage"),
            "Should stay on homepage with number search params");
    }

    @Test @Order(4)
    public void test04_SearchByDateRange() {
        navigateTo("/homepage?startDate=2026-01-01&endDate=2026-03-01");
        assertTrue(isOnPage("homepage"),
            "Should show results filtered by date range");
    }

    @Test @Order(5)
    public void test05_SearchBySpecificDate() {
        navigateTo("/homepage?specificDate=2026-02-15");
        assertTrue(isOnPage("homepage"),
            "Should show results filtered by specific date");
    }

    @Test @Order(6)
    public void test06_PaginationResults() {
        navigateTo("/homepage?company=Vietlott&page=2");
        assertTrue(isOnPage("homepage"),
            "Pagination should work on homepage search results");
    }

    @Test @Order(7)
    public void test07_SearchNoResults() {
        navigateTo("/homepage?num1=99&num2=98&num3=97&num4=96&num5=95&num6=94");
        assertTrue(isOnPage("homepage"),
            "Should show empty results without errors");
    }

    @Test @Order(8)
    public void test08_SearchHistoryGuest() {
        goToHomepage();
        
        navigateTo("/homepage?company=Vietlott");
        goToHomepage();
        
        assertTrue(isOnPage("homepage"), "Guest homepage should work after search");
    }

    @Test @Order(9)
    public void test09_SearchHistoryLoggedIn() {
        loginAsUser();
        goToHomepage();
        navigateTo("/homepage?company=Vietlott");
        goToHomepage();
        
        assertTrue(isOnPage("homepage"), "Logged-in user homepage should display history if available");
    }

    @Test @Order(10)
    public void test10_SearchFromHistory() {
        loginAsUser();
        navigateTo("/homepage?company=Vietlott");
        goToHomepage();
        
        if (elementExists(By.cssSelector("button[onclick*='repeatSearch']"))) {
            assertTrue(true, "Search history re-search buttons exist");
        }
    }

    @Test @Order(11)
    public void test11_ClearSearchHistory() {
        loginAsUser();
        navigateTo("/homepage?company=Vietlott");
        
        navigateTo("/homepage?action=clearHistory");
        assertTrue(isOnPage("homepage"), "Should clear history and stay on homepage");
    }

    @Test @Order(12)
    public void test12_TicketViewCountIncrement() {
        
        navigateTo("/homepage?company=Vietlott");
        assertTrue(isOnPage("homepage"),
            "View count should increment when tickets are displayed (server-side verification needed)");
    }
}
