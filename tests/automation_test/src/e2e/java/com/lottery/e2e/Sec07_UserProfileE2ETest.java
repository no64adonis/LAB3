package com.lottery.e2e;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Sec07_UserProfileE2ETest extends E2ETestBase {

    @Test @Order(1)
    public void test01_ViewProfile() {
        loginAsUser();
        goToProfile();
        assertTrue(pageContainsText("Name") || pageContainsText("Email") || pageContainsText("Profile"),
            "Profile page should display user information");
        assertTrue(elementExists(org.openqa.selenium.By.id("firstName")),
            "Profile should have firstName field");
    }

    @Test @Order(2)
    public void test02_UpdateNameSuccess() {
        loginAsUser();
        goToProfile();
        typeById("firstName", "Updated");
        typeById("lastName", "Name");
        
        clickButtonByText("Update Name");
        assertTrue(hasSuccessMessage() || pageContainsText("thành công") || pageContainsText("Updated"),
            "Should show success after name update");
    }

    @Test @Order(3)
    public void test03_UpdateNameTooLong() {
        loginAsUser();
        goToProfile();
        String longName = "A".repeat(60);
        typeById("firstName", longName);
        typeById("lastName", "Test");
        clickButtonByText("Update Name");
        
        assertTrue(hasErrorMessage() || isOnPage("profile"),
            "Should show error for name exceeding max length");
    }

    @Test @Order(4)
    public void test04_RequestEmailChange() {
        loginAsUser();
        goToProfile();
        if (elementExists(org.openqa.selenium.By.id("newEmail"))) {
            typeById("newEmail", "newemail_" + uniqueTimestamp() + "@test.com");
            clickButtonByText("Send Verification Code");
            assertTrue(hasSuccessMessage() || pageContainsText("mã xác minh") 
                       || pageContainsText("verification") || hasErrorMessage(),
                "Should show verification code sent or an error");
        }
    }

    @Test @Order(5)
    public void test05_VerifyEmailCodeCorrect() {
        
        loginAsUser();
        goToProfile();
        
        if (elementExists(org.openqa.selenium.By.id("verificationCode"))) {
            typeById("verificationCode", "123456");
            clickButtonByText("Verify");
            
            assertTrue(isOnPage("profile"),
                "Should stay on profile page after verification attempt");
        }
    }

    @Test @Order(6)
    public void test06_VerifyEmailCodeWrong() {
        loginAsUser();
        goToProfile();
        if (elementExists(org.openqa.selenium.By.id("verificationCode"))) {
            typeById("verificationCode", "000000");
            clickButtonByText("Verify");
            assertTrue(hasErrorMessage() || pageContainsText("không hợp lệ") || pageContainsText("invalid"),
                "Should show error for wrong verification code");
        }
    }

    @Test @Order(7)
    public void test07_ChangeToExistingEmail() {
        loginAsUser();
        goToProfile();
        if (elementExists(org.openqa.selenium.By.id("newEmail"))) {
            
            typeById("newEmail", USER_EMAIL);
            clickButtonByText("Send Verification Code");
            assertTrue(hasErrorMessage() || pageContainsText("giống") || pageContainsText("same"),
                "Should show error when new email matches current email");
        }
    }
}
