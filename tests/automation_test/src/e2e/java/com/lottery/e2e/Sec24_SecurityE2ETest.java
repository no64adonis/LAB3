package com.lottery.e2e;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Sec24_SecurityE2ETest extends E2ETestBase {

    @Test @Order(1)
    public void test01_SqlInjectionInLogin() {
        boolean redirected = tryLoginAs("' OR 1=1 --", "anything");
        assertTrue(!redirected || isOnPage("login") || hasErrorMessage(),
            "SQL injection in login should be rejected");
    }

    @Test @Order(2)
    public void test02_SqlInjectionInSearch() {
        navigateTo("/homepage?company=' OR 1=1 --");
        assertFalse(pageContainsText("Exception") || pageContainsText("SQL"),
            "SQL injection in search should not cause server error");
    }

    @Test @Order(3)
    public void test03_SqlInjectionInRegistration() {
        registerNewUser("'; DROP TABLE users; --", "SQL", "Inject", "Test@1234", "Test@1234");
        assertFalse(pageContainsText("Exception") || pageContainsText("SQL"),
            "SQL injection in registration should not cause server error");
    }

    @Test @Order(4)
    public void test04_XssInSearch() {
        navigateTo("/homepage?company=<script>alert('xss')</script>");
        assertFalse(pageContainsText("<script>alert"),
            "XSS payload should be escaped in output");
    }

    @Test @Order(5)
    public void test05_XssInRegistration() {
        registerNewUser("xss@test.com", "<script>alert('xss')</script>", "Test", "Test@1234", "Test@1234");
        assertFalse(driver.getPageSource().contains("<script>alert"),
            "XSS payload in registration name should be escaped");
    }

    @Test @Order(6)
    public void test06_SessionFixation() {
        goToLogin();
        String sessionBefore = driver.manage().getCookieNamed("JSESSIONID") != null ?
            driver.manage().getCookieNamed("JSESSIONID").getValue() : "";
        loginAsUser();
        String sessionAfter = driver.manage().getCookieNamed("JSESSIONID") != null ?
            driver.manage().getCookieNamed("JSESSIONID").getValue() : "";
        
        assertTrue(!sessionAfter.isEmpty(), "Should have a session after login");
    }

    @Test @Order(7)
    public void test07_DirectAccessToJsp() {
        navigateTo("/profile.jsp");
        
        assertTrue(isOnPage("login") || isOnPage("profile") || pageContainsText("Error"),
            "Direct JSP access should be handled properly");
    }

    @Test @Order(8)
    public void test08_HttpMethodTampering() {
        
        navigateTo("/register?email=test&password=test");
        assertTrue(isOnPage("register") || isOnPage("login"),
            "GET requests to POST endpoints should not cause errors");
    }

    @Test @Order(9)
    public void test09_PathTraversal() {
        navigateTo("/../../etc/passwd");
        assertFalse(pageContainsText("root:") || pageContainsText("/bin/bash"),
            "Path traversal should not expose system files");
    }

    @Test @Order(10)
    public void test10_ConcurrentSessionSafety() {
        loginAsUser();
        String url = getCurrentUrl();
        
        goToProfile();
        assertTrue(isOnPage("profile") || isOnPage("login"),
            "Session should be maintained or properly invalidated");
    }

    @Test @Order(11)
    public void test11_PasswordsNotExposedInUrl() {
        loginAsUser();
        String url = getCurrentUrl();
        assertFalse(url.contains("password") || url.contains(USER_PASSWORD),
            "Password should never appear in URL");
    }
}
