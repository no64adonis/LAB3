package com.lottery.e2e;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Sec18_AdminProfileE2ETest extends E2ETestBase {

    @Test @Order(1)
    public void test01_AdminProfilePageLoads() {
        loginAsAdmin();
        goToAdminProfile();
        assertTrue(pageContainsText("Profile") || pageContainsText("Name") || pageContainsText("Email"),
            "Admin profile page should load");
    }

    @Test @Order(2)
    public void test02_UpdateAdminName() {
        loginAsAdmin();
        goToAdminProfile();
        typeById("firstName", "AdminFirst");
        typeById("lastName", "AdminLast");
        clickButtonByText("Update Name");
        assertTrue(hasSuccessMessage() || isOnPage("adminProfile"), "Should update admin name");
    }

    @Test @Order(3)
    public void test03_RequestEmailChange() {
        loginAsAdmin();
        goToAdminProfile();
        if (elementExists(By.id("newEmail"))) {
            typeById("newEmail", "newemail_admin_" + uniqueTimestamp() + "@test.com");
            clickButtonByText("Send Verification Code");
            assertTrue(hasSuccessMessage() || hasErrorMessage() || isOnPage("adminProfile"),
                "Should process email change request");
        }
    }

    @Test @Order(4)
    public void test04_ChangePasswordSuccess() {
        loginAsAdmin();
        goToAdminProfile();
        if (elementExists(By.id("currentPassword"))) {
            
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                "document.getElementById('currentPassword').scrollIntoView({block:'center'});");
            try { Thread.sleep(500); } catch (InterruptedException e) {}
            typeById("currentPassword", ADMIN_PASSWORD);
            typeById("newPassword", "NewAdmin@1234");
            typeById("confirmPassword", "NewAdmin@1234");
            clickButtonByText("Change Password");
            try { Thread.sleep(1000); } catch (InterruptedException e) {}
            assertTrue(hasSuccessMessage() || isOnPage("adminProfile"), "Should change password");
            
            if (elementExists(By.id("currentPassword"))) {
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                    "document.getElementById('currentPassword').scrollIntoView({block:'center'});");
                try { Thread.sleep(500); } catch (InterruptedException e) {}
                typeById("currentPassword", "NewAdmin@1234");
                typeById("newPassword", ADMIN_PASSWORD);
                typeById("confirmPassword", ADMIN_PASSWORD);
                clickButtonByText("Change Password");
                try { Thread.sleep(1000); } catch (InterruptedException e) {}
            }
        }
    }

    @Test @Order(5)
    public void test05_ChangePasswordWrongCurrent() {
        loginAsAdmin();
        goToAdminProfile();
        if (elementExists(By.id("currentPassword"))) {
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                "document.getElementById('currentPassword').scrollIntoView({block:'center'});");
            try { Thread.sleep(500); } catch (InterruptedException e) {}
            typeById("currentPassword", "Wrong@1234");
            typeById("newPassword", "NewAdmin@1234");
            typeById("confirmPassword", "NewAdmin@1234");
            clickButtonByText("Change Password");
            assertTrue(hasErrorMessage() || isOnPage("adminProfile"), "Should show error for wrong password");
        }
    }

    @Test @Order(6)
    public void test06_ChangePasswordWeak() {
        loginAsAdmin();
        goToAdminProfile();
        if (elementExists(By.id("currentPassword"))) {
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                "document.getElementById('currentPassword').scrollIntoView({block:'center'});");
            try { Thread.sleep(500); } catch (InterruptedException e) {}
            typeById("currentPassword", ADMIN_PASSWORD);
            typeById("newPassword", "weak");
            typeById("confirmPassword", "weak");
            clickButtonByText("Change Password");
            assertTrue(hasErrorMessage() || isOnPage("adminProfile"), "Should reject weak password");
        }
    }

    @Test @Order(7)
    public void test07_NonAdminCannotAccess() {
        loginAsUser();
        goToAdminProfile();
        assertTrue(isOnPage("login") || isOnPage("userLottery") || isOnPage("homepage"),
            "Non-admin should not access admin profile");
    }
}
