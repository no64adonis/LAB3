package com.lottery.e2e;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Sec01_RegistrationE2ETest extends E2ETestBase {

    @Test @Order(1)
    public void test01_RegisterSuccessfully() {
        String email = "e2e_reg_" + uniqueTimestamp() + "@test.com";
        registerNewUser(email, "Test", "User", "Test@1234", "Test@1234");
        assertTrue(isOnPage("login") || pageContainsText("thành công") || hasSuccessMessage(),
            "Should redirect to login or show success after registration");
    }

    @Test @Order(2)
    public void test02_RegisterWithExistingEmail() {
        
        String email = "e2e_dup_" + uniqueTimestamp() + "@test.com";
        registerNewUser(email, "First", "User", "Test@1234", "Test@1234");
        
        registerNewUser(email, "Second", "User", "Test@1234", "Test@1234");
        assertTrue(hasErrorMessage() || pageContainsText("đã được đăng ký") || pageContainsText("already"),
            "Should show error for duplicate email");
    }

    @Test @Order(3)
    public void test03_RegisterWithWeakPassword() {
        String email = "e2e_weak_" + uniqueTimestamp() + "@test.com";
        registerNewUser(email, "Weak", "Pass", "123", "123");
        assertTrue(hasErrorMessage() || isOnPage("register"),
            "Should show error for weak password and stay on register page");
    }

    @Test @Order(4)
    public void test04_RegisterWithMismatchedPasswords() {
        String email = "e2e_mismatch_" + uniqueTimestamp() + "@test.com";
        registerNewUser(email, "Mis", "Match", "Test@1234", "Test@9999");
        assertTrue(hasErrorMessage() || pageContainsText("không khớp") || pageContainsText("match"),
            "Should show password mismatch error");
    }

    @Test @Order(5)
    public void test05_RegisterWithInvalidEmail() {
        goToRegister();
        typeById("email", "notanemail");
        typeById("firstName", "Bad");
        typeById("lastName", "Email");
        typeById("password", "Test@1234");
        typeById("confirmPassword", "Test@1234");
        clickButtonByText("Register");
        
        assertTrue(isOnPage("register") || hasErrorMessage(),
            "Should stay on register page with invalid email");
    }

    @Test @Order(6)
    public void test06_RegisterWithEmptyFields() {
        goToRegister();
        clickButtonByText("Register");
        
        assertTrue(isOnPage("register"),
            "Should stay on register page when fields are empty");
    }

    @Test @Order(7)
    public void test07_RegisterWithSqlInjection() {
        goToRegister();
        typeById("email", "' OR 1=1 --");
        typeById("firstName", "SQL");
        typeById("lastName", "Inject");
        typeById("password", "Test@1234");
        typeById("confirmPassword", "Test@1234");
        clickButtonByText("Register");
        assertTrue(hasErrorMessage() || isOnPage("register"),
            "Should show error for SQL injection attempt, system unaffected");
    }
}
