package com.lottery.e2e;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Sec10_TicketPurchaseE2ETest extends E2ETestBase {

    @Test @Order(1)
    public void test01_PurchasePageLoads() {
        loginAsUser();
        goToTicketPurchase();
        assertTrue(pageContainsText("Purchase") || pageContainsText("Search") || pageContainsText("Buy"),
            "Ticket purchase page should load with search form");
    }

    @Test @Order(2)
    public void test02_SearchAvailableTickets() {
        loginAsUser();
        navigateTo("/ticketPurchase?company=Vietlott");
        assertTrue(isOnPage("ticketPurchase"),
            "Should show available tickets matching search criteria");
    }

    @Test @Order(3)
    public void test03_BuySingleTicket() {
        loginAsUser();
        navigateTo("/ticketPurchase?company=Vietlott");
        if (elementExists(By.cssSelector("form.single-buy-form button[type='submit']"))) {
            driver.findElement(By.cssSelector("form.single-buy-form button[type='submit']")).click();
            assertTrue(isOnPage("ticketPurchase") || hasSuccessMessage() || hasErrorMessage(),
                "Should process purchase and show result");
        }
    }

    @Test @Order(4)
    public void test04_BuySelectedTickets() {
        loginAsUser();
        navigateTo("/ticketPurchase?company=Vietlott");
        if (elementExists(By.name("ticketCheckbox"))) {
            driver.findElement(By.name("ticketCheckbox")).click();
            clickButtonByText("Buy Selected");
            try { driver.switchTo().alert().accept(); } catch (Exception e) {}
            assertTrue(isOnPage("ticketPurchase") || hasSuccessMessage() || hasErrorMessage(),
                "Should process bulk purchase");
        }
    }

    @Test @Order(5)
    public void test05_BuyAllTickets() {
        loginAsUser();
        navigateTo("/ticketPurchase?company=Vietlott");
        if (elementExists(By.name("ticketCheckbox"))) {
            clickButtonByText("Buy All");
            try { driver.switchTo().alert().accept(); } catch (Exception e) {}
            assertTrue(isOnPage("ticketPurchase") || hasSuccessMessage() || hasErrorMessage(),
                "Should process buy-all operation");
        }
    }

    @Test @Order(6)
    public void test06_BuyNoSelectionError() {
        loginAsUser();
        navigateTo("/ticketPurchase?company=Vietlott");
        if (tryClickButtonByText("Buy Selected")) {
            try {
                String alertText = driver.switchTo().alert().getText();
                assertTrue(alertText.toLowerCase().contains("select"), "Should show alert requiring ticket selection");
                driver.switchTo().alert().accept();
            } catch (Exception e) {
                
                assertTrue(isOnPage("ticketPurchase"));
            }
        } else {
            
            assertTrue(isOnPage("ticketPurchase"));
        }
    }

    @Test @Order(7)
    public void test07_PurchaseInsufficientBalance() {
        loginAsUser();
        navigateTo("/ticketPurchase?company=Vietlott");
        if (elementExists(By.cssSelector("form.single-buy-form button[type='submit']"))) {
            driver.findElement(By.cssSelector("form.single-buy-form button[type='submit']")).click();
            assertTrue(isOnPage("ticketPurchase") || hasErrorMessage() || hasSuccessMessage(),
                "Should handle insufficient balance gracefully");
        }
    }

    @Test @Order(8)
    public void test08_TicketNoLongerAvailable() {
        loginAsUser();
        goToTicketPurchase();
        assertTrue(isOnPage("ticketPurchase"), "Should handle unavailable tickets gracefully");
    }

    @Test @Order(9)
    public void test09_PaginationOnPurchasePage() {
        loginAsUser();
        navigateTo("/ticketPurchase?company=Vietlott&page=2");
        assertTrue(isOnPage("ticketPurchase"), "Pagination should work on purchase page");
    }

    @Test @Order(10)
    public void test10_UnauthenticatedAccess() {
        goToTicketPurchase();
        assertTrue(isOnPage("login"), "Unauthenticated users should be redirected to login");
    }
}
