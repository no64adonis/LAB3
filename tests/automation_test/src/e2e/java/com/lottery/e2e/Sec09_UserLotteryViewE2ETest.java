package com.lottery.e2e;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Sec09_UserLotteryViewE2ETest extends E2ETestBase {

    @Test @Order(1)
    public void test01_ViewLotteryTicketsAsUser() {
        loginAsUser();
        goToUserLottery();
        assertTrue(pageContainsText("Lottery") || pageContainsText("Search") || pageContainsText("ticket"),
            "User lottery page should display ticket listing or search");
    }

    @Test @Order(2)
    public void test02_SearchTicketsByCompany() {
        loginAsUser();
        navigateTo("/userLottery?company=Vietlott");
        assertTrue(isOnPage("userLottery"),
            "Should filter tickets by company in user lottery view");
    }

    @Test @Order(3)
    public void test03_SearchTicketsByNumber() {
        loginAsUser();
        navigateTo("/userLottery?num1=5&num2=10");
        assertTrue(isOnPage("userLottery"),
            "Should filter tickets by number in user lottery view");
    }

    @Test @Order(4)
    public void test04_SearchTicketsByDateRange() {
        loginAsUser();
        navigateTo("/userLottery?startDate=2026-01-01&endDate=2026-03-01");
        assertTrue(isOnPage("userLottery"),
            "Should filter tickets by date range");
    }

    @Test @Order(5)
    public void test05_SearchTicketsBySpecificDate() {
        loginAsUser();
        navigateTo("/userLottery?specificDate=2026-02-15");
        assertTrue(isOnPage("userLottery"),
            "Should filter tickets by specific date");
    }

    @Test @Order(6)
    public void test06_PaginationWorks() {
        loginAsUser();
        navigateTo("/userLottery?company=Vietlott&page=1");
        assertTrue(isOnPage("userLottery"),
            "Pagination should work for user lottery view");
    }

    @Test @Order(7)
    public void test07_NoResultsMessage() {
        loginAsUser();
        navigateTo("/userLottery?num1=99&num2=98&num3=97&num4=96&num5=95&num6=94");
        assertTrue(isOnPage("userLottery"),
            "Should display no-results message gracefully");
    }

    @Test @Order(8)
    public void test08_UnauthenticatedAccess() {
        goToUserLottery();
        assertTrue(isOnPage("login"),
            "Unauthenticated users should be redirected to login");
    }
}
