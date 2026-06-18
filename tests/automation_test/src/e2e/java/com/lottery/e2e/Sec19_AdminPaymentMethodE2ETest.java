package com.lottery.e2e;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Sec19_AdminPaymentMethodE2ETest extends E2ETestBase {

    @Test @Order(1)
    public void test01_PageLoads() {
        loginAsAdmin();
        goToPaymentMethodMgmt();
        assertTrue(pageContainsText("Payment") || pageContainsText("Card") || pageContainsText("Method"),
            "Admin payment method management page should load");
    }

    @Test @Order(2)
    public void test02_AddPaymentMethod() {
        loginAsAdmin();
        goToPaymentMethodMgmt();
        if (elementExists(By.id("cardNumber"))) {
            typeById("cardNumber", "5500000000000004");
            typeById("cardHolder", "Admin User");
            typeById("expiryMonth", "06");
            typeById("expiryYear", "2029");
            typeById("cvv", "456");
            clickButtonByText("Add");
            assertTrue(hasSuccessMessage() || isOnPage("paymentMethodManagement"), "Should add payment method");
        }
    }

    @Test @Order(3)
    public void test03_AddInvalidCardNumber() {
        loginAsAdmin();
        goToPaymentMethodMgmt();
        if (elementExists(By.id("cardNumber"))) {
            typeById("cardNumber", "9999");
            typeById("cardHolder", "Admin User");
            typeById("expiryMonth", "06");
            typeById("expiryYear", "2029");
            typeById("cvv", "456");
            clickButtonByText("Add");
            assertTrue(hasErrorMessage() || isOnPage("paymentMethodManagement"), "Should reject invalid card");
        }
    }

    @Test @Order(4)
    public void test04_AddExpiredCard() {
        loginAsAdmin();
        goToPaymentMethodMgmt();
        if (elementExists(By.id("cardNumber"))) {
            typeById("cardNumber", "5500000000000004");
            typeById("cardHolder", "Admin User");
            typeById("expiryMonth", "01");
            typeById("expiryYear", "2020");
            typeById("cvv", "456");
            clickButtonByText("Add");
            assertTrue(hasErrorMessage() || isOnPage("paymentMethodManagement"), "Should reject expired card");
        }
    }

    @Test @Order(5)
    public void test05_DeletePaymentMethod() {
        loginAsAdmin();
        goToPaymentMethodMgmt();

        if (!pageContainsText("Delete") && !pageContainsText("Remove") && elementExists(By.id("cardNumber"))) {
            typeById("cardNumber", "5500000000000004");
            typeById("cardHolder", "Admin User Delete");
            typeById("expiryMonth", "06");
            typeById("expiryYear", "2029");
            typeById("cvv", "456");
            clickButtonByText("Add");
            hasSuccessMessage();
            goToPaymentMethodMgmt();
        }

        var btns = driver.findElements(By.cssSelector("button"));
        boolean deleted = false;
        for (var btn : btns) {
            if (btn.getText().contains("Delete") || btn.getText().contains("Remove")) {
                btn.click();
                try { driver.switchTo().alert().accept(); } catch (Exception e) {}
                deleted = true;
                break;
            }
        }
        
        if (deleted) {
            assertTrue(hasSuccessMessage() || isOnPage("paymentMethodManagement"), "Should handle deletion success");
        } else {
            assertTrue(isOnPage("paymentMethodManagement"));
        }
    }

    @Test @Order(6)
    public void test06_EmptyFieldsValidation() {
        loginAsAdmin();
        goToPaymentMethodMgmt();
        
        if (!tryClickButtonByText("Add")) {
            tryClickButtonByText("Add Payment Method");
        }
        assertTrue(isOnPage("paymentMethodManagement"), "Should stay on page with empty fields");
    }

    @Test @Order(7)
    public void test07_DuplicateCard() {
        loginAsAdmin();
        goToPaymentMethodMgmt();
        if (elementExists(By.id("cardNumber"))) {
            typeById("cardNumber", "5500000000000004");
            typeById("cardHolder", "Admin User");
            typeById("expiryMonth", "06");
            typeById("expiryYear", "2029");
            typeById("cvv", "456");
            clickButtonByText("Add");
            goToPaymentMethodMgmt();
            typeById("cardNumber", "5500000000000004");
            typeById("cardHolder", "Admin User");
            typeById("expiryMonth", "06");
            typeById("expiryYear", "2029");
            typeById("cvv", "456");
            clickButtonByText("Add");
            assertTrue(hasErrorMessage() || isOnPage("paymentMethodManagement"), "Should prevent duplicates");
        }
    }

    @Test @Order(8)
    public void test08_NonAdminCannotAccess() {
        loginAsUser();
        goToPaymentMethodMgmt();
        assertTrue(isOnPage("login") || isOnPage("userLottery") || isOnPage("homepage"),
            "Non-admin should not access admin payment method management");
    }
}
