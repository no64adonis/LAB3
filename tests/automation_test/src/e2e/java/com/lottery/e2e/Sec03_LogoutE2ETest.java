package com.lottery.e2e;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Sec03_LogoutE2ETest extends E2ETestBase {

    @Test @Order(1)
    public void test01_LogoutSuccessfully() {
        loginAsUser();
        logout();
        
        assertTrue(isOnPage("index") || isOnPage("homepage") || isOnPage("login")
                || pageContainsText("Login") || pageContainsText("Fortuna"),
            "Should redirect to homepage or login after logout");
    }

    @Test @Order(2)
    public void test02_AccessProtectedPageAfterLogout() {
        loginAsUser();
        logout();
        goToUserLottery();
        assertTrue(isOnPage("login"),
            "Should redirect to login when accessing protected page after logout");
    }

    @Test @Order(3)
    public void test03_BackButtonAfterLogout() {
        loginAsUser();
        logout();
        driver.navigate().back();
        
        goToMyTickets();
        assertTrue(isOnPage("login"),
            "Should not access protected content after logout via back button");
    }
}
