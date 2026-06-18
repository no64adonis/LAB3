package com.lottery.e2e;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Sec21_AuthorizationAccessE2ETest extends E2ETestBase {

    @Test @Order(1)
    public void test01_UserCannotAccessAdminLottery() {
        loginAsUser();
        goToAdminLottery();
        assertFalse(isOnPage("adminLottery") && pageContainsText("Create"),
            "User should not see admin lottery content");
    }

    @Test @Order(2)
    public void test02_UserCannotAccessUserManagement() {
        loginAsUser();
        goToUserManagement();
        assertFalse(isOnPage("userManagement") && pageContainsText("Users"),
            "User should not access user management");
    }

    @Test @Order(3)
    public void test03_UserCannotAccessAdminTransactions() {
        loginAsUser();
        goToAdminTransactions();
        assertFalse(isOnPage("adminTransactions") && pageContainsText("Transaction"),
            "User should not access admin transactions");
    }

    @Test @Order(4)
    public void test04_UserCannotAccessPriceManagement() {
        loginAsUser();
        goToPriceManagement();
        assertFalse(isOnPage("priceManagement") && pageContainsText("Price"),
            "User should not access price management");
    }

    @Test @Order(5)
    public void test05_UnauthenticatedCannotAccessProfile() {
        goToProfile();
        assertTrue(isOnPage("login"),
            "Unauthenticated should be redirected to login from profile");
    }

    @Test @Order(6)
    public void test06_UnauthenticatedCannotAccessTicketPurchase() {
        goToTicketPurchase();
        assertTrue(isOnPage("login"),
            "Unauthenticated should be redirected to login from ticket purchase");
    }

    @Test @Order(7)
    public void test07_UnauthenticatedCannotAccessTopUp() {
        goToTopup();
        assertTrue(isOnPage("login"),
            "Unauthenticated should be redirected to login from top-up");
    }

    @Test @Order(8)
    public void test08_GuestCanAccessHomepage() {
        goToHomepage();
        assertTrue(pageContainsText("Lottery") || pageContainsText("Search"),
            "Guest should be able to access the public homepage");
    }
}
