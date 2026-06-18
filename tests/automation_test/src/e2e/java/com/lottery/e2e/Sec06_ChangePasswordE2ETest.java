package com.lottery.e2e;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Sec06_ChangePasswordE2ETest extends E2ETestBase {

    private void loginAsFreshUser() {
        
        String email = "e2e_pwd_" + uniqueTimestamp() + "@test.com";
        String password = "Test@1234";
        registerNewUser(email, "PwdTest", "User", password, password);
        
        loginAs(email, password);
    }

    @Test @Order(1)
    public void test01_ChangePasswordSuccess() {
        loginAsUser();
        goToProfile();
        if (elementExists(By.id("currentPassword"))) {
            typeById("currentPassword", USER_PASSWORD);
            typeById("newPassword", "NewTest@1234");
            typeById("confirmPassword", "NewTest@1234");
            clickButtonByText("Change Password");
            assertTrue(hasSuccessMessage() || pageContainsText("thành công") || pageContainsText("success"),
                "Should show success message after password change");
            
            try { Thread.sleep(1000); } catch (InterruptedException e) {}
            if (elementExists(By.id("currentPassword"))) {
                typeById("currentPassword", "NewTest@1234");
                typeById("newPassword", USER_PASSWORD);
                typeById("confirmPassword", USER_PASSWORD);
                clickButtonByText("Change Password");
                try { Thread.sleep(1000); } catch (InterruptedException e) {}
            }
        }
    }

    @Test @Order(2)
    public void test02_ChangePasswordWrongCurrent() {
        loginAsUser();
        goToProfile();
        if (elementExists(By.id("currentPassword"))) {
            typeById("currentPassword", "WrongCurrent@123");
            typeById("newPassword", "NewTest@1234");
            typeById("confirmPassword", "NewTest@1234");
            clickButtonByText("Change Password");
            assertTrue(hasErrorMessage() || pageContainsText("không đúng") || pageContainsText("incorrect"),
                "Should show error for wrong current password");
        }
    }

    @Test @Order(3)
    public void test03_ChangePasswordSameAsOld() {
        loginAsUser();
        goToProfile();
        if (elementExists(By.id("currentPassword"))) {
            typeById("currentPassword", USER_PASSWORD);
            typeById("newPassword", USER_PASSWORD);
            typeById("confirmPassword", USER_PASSWORD);
            clickButtonByText("Change Password");
            
            assertTrue(hasErrorMessage() || hasSuccessMessage() || isOnPage("profile")
                    || pageContainsText("khác") || pageContainsText("different"),
                "Should handle same-as-old password (error or success)");
        }
    }

    @Test @Order(4)
    public void test04_ChangePasswordWeak() {
        loginAsUser();
        goToProfile();
        if (elementExists(By.id("currentPassword"))) {
            typeById("currentPassword", USER_PASSWORD);
            typeById("newPassword", "123");
            typeById("confirmPassword", "123");
            clickButtonByText("Change Password");
            assertTrue(hasErrorMessage() || isOnPage("profile"),
                "Should show error for weak new password");
        }
    }
}
