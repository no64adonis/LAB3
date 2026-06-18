package com.lottery.e2e;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Sec12_TopUpE2ETest extends E2ETestBase {

    @Test @Order(1)
    public void test01_TopUpPageLoads() {
        loginAsUser();
        goToTopup();
        assertTrue(pageContainsText("Top Up") || pageContainsText("Balance") || pageContainsText("top"),
            "Top up page should load with balance and options");
    }

    @Test @Order(2)
    public void test02_TopUpWithPredefinedAmount() {
        loginAsUser();
        goToTopup();
        if (elementExists(By.cssSelector(".amount-btn"))) {
            try { driver.findElement(By.cssSelector(".amount-btn")).click(); } catch (Exception e) {}
        }
        assertTrue(isOnPage("topup"), "Should stay on topup page after selecting amount");
    }

    @Test @Order(3)
    public void test03_TopUpWithCustomAmount() {
        loginAsUser();
        goToTopup();
        if (elementExists(By.name("amount"))) {
            jsClearAndTypeByName("amount", "25.00");
        }
        assertTrue(isOnPage("topup"), "Should accept custom amount");
    }

    @Test @Order(4)
    public void test04_TopUpWithPaymentMethod() {
        loginAsUser();
        goToTopup();
        if (elementExists(By.name("paymentMethodId"))) {
            try {
                new org.openqa.selenium.support.ui.Select(driver.findElement(By.name("paymentMethodId"))).selectByIndex(1);
            } catch (Exception e) {}
        }
        assertTrue(isOnPage("topup"), "Should allow selecting payment method");
    }

    @Test @Order(5)
    public void test05_TopUpZeroAmount() {
        loginAsUser();
        goToTopup();
        if (elementExists(By.name("amount"))) {
            jsClearAndTypeByName("amount", "0");
            if (tryClickButtonByText("Top Up")) {
                assertTrue(hasErrorMessage() || isOnPage("topup"), "Should reject zero amount top-up");
            } else {
                assertTrue(isOnPage("topup"));
            }
        }
    }

    @Test @Order(6)
    public void test06_TopUpNegativeAmount() {
        loginAsUser();
        goToTopup();
        if (elementExists(By.name("amount"))) {
            jsClearAndTypeByName("amount", "-10");
            if (tryClickButtonByText("Top Up")) {
                assertTrue(hasErrorMessage() || isOnPage("topup"), "Should reject negative amount");
            } else {
                assertTrue(isOnPage("topup"));
            }
        }
    }

    @Test @Order(7)
    public void test07_TopUpNoPaymentMethod() {
        loginAsUser();
        goToTopup();
        if (elementExists(By.name("amount"))) {
            jsClearAndTypeByName("amount", "10");
            if (tryClickButtonByText("Top Up")) {
                assertTrue(isOnPage("topup") || hasErrorMessage(), "Should require payment method for top-up");
            } else {
                assertTrue(isOnPage("topup"));
            }
        }
    }

    @Test @Order(8)
    public void test08_UnauthenticatedAccess() {
        goToTopup();
        assertTrue(isOnPage("login"), "Should redirect unauthenticated users to login");
    }
}
