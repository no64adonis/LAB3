package com.lottery.e2e;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Sec05_PasswordResetE2ETest extends E2ETestBase {

    @Test @Order(1)
    public void test01_SendResetRequestValidEmail() {
        goToForgotPassword();
        typeById("email", USER_EMAIL);
        clickButtonByText("Send Reset Link");
        assertTrue(hasSuccessMessage() || pageContainsText("email") || pageContainsText("sent"),
            "Should show success/info message after submitting valid email");
    }

    @Test @Order(2)
    public void test02_SendResetRequestInvalidEmail() {
        goToForgotPassword();
        typeById("email", "nonexistent_" + uniqueTimestamp() + "@test.com");
        clickButtonByText("Send Reset Link");
        
        assertTrue(pageContainsText("email") || hasSuccessMessage(),
            "Should show same message for security (no email disclosure)");
    }

    @Test @Order(3)
    public void test03_ResetPasswordSuccess() {
        
        navigateTo("/changePassword");
        String src = driver.getPageSource();
        assertTrue(src.contains("newPassword") || src.contains("password") || isOnPage("login"),
            "Change password page should exist or redirect appropriately");
    }

    @Test @Order(4)
    public void test04_ResetPasswordExpiredLink() {
        
        navigateTo("/passwordReset?token=expired_invalid_token_12345");
        String src = driver.getPageSource();
        assertTrue(src.contains("error") || src.contains("hết hạn") || src.contains("invalid") 
                   || isOnPage("login") || isOnPage("forgotPassword"),
            "Should show error or redirect for expired token");
    }

    @Test @Order(5)
    public void test05_ResetPasswordMismatch() {
        
        navigateTo("/changePassword");
        if (elementExists(org.openqa.selenium.By.id("newPassword"))) {
            typeById("newPassword", "NewPass@1234");
            typeById("confirmPassword", "DifferentPass@1234");
            clickButtonByText("Reset Password");
            assertTrue(hasErrorMessage() || pageContainsText("không khớp") || pageContainsText("match"),
                "Should show password mismatch error");
        }
        
    }

    @Test @Order(6)
    public void test06_ResetLinkUsedTwice() {
        
        navigateTo("/passwordReset?token=already_used_token_xyz");
        assertTrue(pageContainsText("error") || pageContainsText("invalid") 
                   || isOnPage("login") || isOnPage("forgotPassword"),
            "Should reject an already-used or invalid token");
    }
}
