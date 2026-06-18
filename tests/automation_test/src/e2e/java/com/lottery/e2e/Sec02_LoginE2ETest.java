package com.lottery.e2e;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Sec02_LoginE2ETest extends E2ETestBase {

    @Test
    @Order(1)
    public void test01_LoginSuccessAsUser() {
        loginAsUser();
        assertTrue(isOnPage("userLottery") || isOnPage("homepage") || pageContainsText("Lottery"),
                "User should be redirected to lottery page after login");
    }

    @Test
    @Order(2)
    public void test02_LoginSuccessAsAdmin() {
        loginAsAdmin();
        assertTrue(isOnPage("userManagement") || isOnPage("adminLottery") || pageContainsText("Admin"),
                "Admin should be redirected to admin page after login");
    }

    @Test
    @Order(3)
    public void test03_LoginWithWrongPassword() {
        boolean redirected = tryLoginAs(USER_EMAIL, "WrongPassword123!");
        assertTrue(!redirected || hasErrorMessage() || isOnPage("login"),
                "Should show error for wrong password");
    }

    @Test
    @Order(4)
    public void test04_LoginWithNonExistentEmail() {
        boolean redirected = tryLoginAs("nonexistent_" + uniqueTimestamp() + "@test.com", "Test@1234");
        assertTrue(!redirected || hasErrorMessage() || isOnPage("login"),
                "Should show error for non-existent email");
    }

    @Test
    @Order(5)
    public void test05_LoginWithEmptyFields() {
        goToLogin();
        clickButtonByText("Login");
        assertTrue(isOnPage("login"),
                "Should stay on login page when fields are empty");
    }

    @Test
    @Order(6)
    public void test06_SuccessMessageAfterRegistration() {
        String email = "e2e_login_msg_" + uniqueTimestamp() + "@test.com";
        registerNewUser(email, "Msg", "Test", "Test@1234", "Test@1234");
        assertTrue(isOnPage("login") && (hasSuccessMessage()
                || pageContainsText("success") || pageContainsText("thành công")
                || pageContainsText("Success") || pageContainsText("Registration")
                || isOnPage("registrationSuccess")),
                "Login page should show success message after registration");
    }
}
