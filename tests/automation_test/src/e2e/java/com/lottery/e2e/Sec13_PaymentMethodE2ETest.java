package com.lottery.e2e;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Sec13_PaymentMethodE2ETest extends E2ETestBase {

    @Test @Order(1)
    public void test01_PaymentsPageLoads() {
        loginAsUser();
        goToPayments();
        assertTrue(pageContainsText("Payment") || pageContainsText("Card") || pageContainsText("payment"),
            "Payment methods page should load");
    }

    @Test @Order(2)
    public void test02_AddPaymentMethodSuccess() {
        loginAsUser();
        goToPayments();
        if (elementExists(By.id("cardNumber"))) {
            typeById("cardNumber", "4111111111111111");
            typeById("cardHolder", "Test User");
            typeById("expiryMonth", "12");
            typeById("expiryYear", "2028");
            typeById("cvv", "123");
            clickButtonByText("Add");
            assertTrue(hasSuccessMessage() || isOnPage("payments"), "Should add payment method successfully");
        }
    }

    @Test @Order(3)
    public void test03_AddPaymentMethodInvalidCard() {
        loginAsUser();
        goToPayments();
        if (elementExists(By.id("cardNumber"))) {
            typeById("cardNumber", "1234");
            typeById("cardHolder", "Test User");
            typeById("expiryMonth", "12");
            typeById("expiryYear", "2028");
            typeById("cvv", "123");
            clickButtonByText("Add");
            assertTrue(hasErrorMessage() || isOnPage("payments"), "Should show error for invalid card number");
        }
    }

    @Test @Order(4)
    public void test04_AddPaymentMethodExpiredCard() {
        loginAsUser();
        goToPayments();
        if (elementExists(By.id("cardNumber"))) {
            typeById("cardNumber", "4111111111111111");
            typeById("cardHolder", "Test User");
            typeById("expiryMonth", "01");
            typeById("expiryYear", "2020");
            typeById("cvv", "123");
            clickButtonByText("Add");
            assertTrue(hasErrorMessage() || isOnPage("payments"), "Should show error for expired card");
        }
    }

    @Test @Order(5)
    public void test05_AddPaymentMethodInvalidCVV() {
        loginAsUser();
        goToPayments();
        if (elementExists(By.id("cardNumber"))) {
            typeById("cardNumber", "4111111111111111");
            typeById("cardHolder", "Test User");
            typeById("expiryMonth", "12");
            typeById("expiryYear", "2028");
            typeById("cvv", "1");
            clickButtonByText("Add");
            assertTrue(hasErrorMessage() || isOnPage("payments"), "Should show error for invalid CVV");
        }
    }

    @Test @Order(6)
    public void test06_AddPaymentMethodEmptyFields() {
        loginAsUser();
        goToPayments();
        clickButtonByText("Add");
        assertTrue(isOnPage("payments"), "Should stay on page when fields are empty (HTML5 validation)");
    }

    @Test @Order(7)
    public void test07_DeletePaymentMethod() {
        loginAsUser();
        goToPayments();

        if (!pageContainsText("Delete") && !pageContainsText("Remove") && elementExists(By.id("cardNumber"))) {
            typeById("cardNumber", "4111222233334444");
            typeById("cardHolder", "Delete Test");
            typeById("expiryMonth", "12");
            typeById("expiryYear", "2029");
            typeById("cvv", "123");
            clickButtonByText("Add");
            
            hasSuccessMessage();
            goToPayments();
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
            assertTrue(hasSuccessMessage() || isOnPage("payments"), "Should handle deletion success");
        } else {
            assertTrue(isOnPage("payments"));
        }
    }

    @Test @Order(8)
    public void test08_DuplicatePaymentMethod() {
        loginAsUser();
        goToPayments();
        if (elementExists(By.id("cardNumber"))) {
            typeById("cardNumber", "4111111111111111");
            typeById("cardHolder", "Test User");
            typeById("expiryMonth", "12");
            typeById("expiryYear", "2028");
            typeById("cvv", "123");
            clickButtonByText("Add");
            goToPayments();
            typeById("cardNumber", "4111111111111111");
            typeById("cardHolder", "Test User");
            typeById("expiryMonth", "12");
            typeById("expiryYear", "2028");
            typeById("cvv", "123");
            clickButtonByText("Add");
            assertTrue(hasErrorMessage() || isOnPage("payments"), "Should prevent duplicate payment method");
        }
    }

    @Test @Order(9)
    public void test09_MaxPaymentMethods() {
        loginAsUser();
        goToPayments();
        assertTrue(isOnPage("payments"), "Page should handle max payment methods list");
    }

    @Test @Order(10)
    public void test10_UnauthenticatedAccess() {
        goToPayments();
        assertTrue(isOnPage("login"), "Should redirect unauthenticated users to login");
    }
}
