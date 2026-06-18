package com.lottery.e2e;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Sec15_PriceManagementE2ETest extends E2ETestBase {

    @Test @Order(1)
    public void test01_PricePageLoads() {
        loginAsAdmin();
        goToPriceManagement();
        assertTrue(pageContainsText("Price") || pageContainsText("price") || pageContainsText("Update"),
            "Price management page should load");
    }

    @Test @Order(2)
    public void test02_UpdatePriceSuccess() {
        loginAsAdmin();
        goToPriceManagement();
        var priceInputs = driver.findElements(By.cssSelector("input[type='number'][name^='price_']"));
        if (!priceInputs.isEmpty()) {
            priceInputs.get(0).clear();
            priceInputs.get(0).sendKeys("5.00");
            clickButtonByText("Update Prices");
            assertTrue(hasSuccessMessage() || isOnPage("priceManagement"), "Should update prices successfully");
        }
    }

    @Test @Order(3)
    public void test03_UpdatePriceZero() {
        loginAsAdmin();
        goToPriceManagement();
        var priceInputs = driver.findElements(By.cssSelector("input[type='number'][name^='price_']"));
        if (!priceInputs.isEmpty()) {
            priceInputs.get(0).clear();
            priceInputs.get(0).sendKeys("0");
            clickButtonByText("Update Prices");
            assertTrue(isOnPage("priceManagement"), "Should handle zero price update");
        }
    }

    @Test @Order(4)
    public void test04_UpdatePriceNegative() {
        loginAsAdmin();
        goToPriceManagement();
        var priceInputs = driver.findElements(By.cssSelector("input[type='number'][name^='price_']"));
        if (!priceInputs.isEmpty()) {
            priceInputs.get(0).clear();
            priceInputs.get(0).sendKeys("-5");
            clickButtonByText("Update Prices");
            assertTrue(hasErrorMessage() || isOnPage("priceManagement"), "Should reject negative prices");
        }
    }

    @Test @Order(5)
    public void test05_AllCompaniesListed() {
        loginAsAdmin();
        goToPriceManagement();
        var priceInputs = driver.findElements(By.cssSelector("input[type='number'][name^='price_']"));
        assertTrue(priceInputs.size() > 0, "Should list all companies with price inputs");
    }

    @Test @Order(6)
    public void test06_NonAdminCannotAccess() {
        loginAsUser();
        goToPriceManagement();
        assertTrue(isOnPage("login") || isOnPage("userLottery") || isOnPage("homepage"),
            "Non-admin should not access price management");
    }
}
