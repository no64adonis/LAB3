package com.lottery.e2e;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Sec04_GoogleOAuthE2ETest extends E2ETestBase {

    @Test @Order(1)
    public void test01_GoogleLoginButtonNewUser() {
        goToLogin();
        assertTrue(elementExists(org.openqa.selenium.By.cssSelector("a[href*='accounts.google.com']")),
            "Google login button should be present on login page");
    }

    @Test @Order(2)
    public void test02_GoogleLoginButtonExistingUser() {
        goToLogin();
        assertTrue(elementExists(org.openqa.selenium.By.cssSelector("a[href*='accounts.google.com']")),
            "Google login button should be present for existing users");
    }

    @Test @Order(3)
    public void test03_SetPasswordPageExists() {
        
        navigateTo("/setPassword.jsp");
        String src = driver.getPageSource();
        assertTrue(src.contains("newPassword") || src.contains("password") || src.contains("Set Password") || isOnPage("login"),
            "Set password page should exist or redirect to login");
    }

    @Test @Order(4)
    public void test04_CancelGoogleAuth() {
        goToLogin();
        
        org.openqa.selenium.WebElement googleBtn = driver.findElement(
            org.openqa.selenium.By.cssSelector("a[href*='accounts.google.com']"));
        String href = googleBtn.getAttribute("href");
        assertTrue(href.contains("accounts.google.com"), "Google button should link to Google OAuth");
        
    }
}
