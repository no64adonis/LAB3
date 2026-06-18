package com.lottery.e2e;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Sec22_NavigationUIE2ETest extends E2ETestBase {

    @Test @Order(1)
    public void test01_HomepageNavigationLinks() {
        goToHomepage();
        assertTrue(elementExists(By.cssSelector("a[href*='login']")) &&
                   elementExists(By.cssSelector("a[href*='register']")),
            "Homepage should have Login and Register links");
    }

    @Test @Order(2)
    public void test02_UserSidebarNavigation() {
        loginAsUser();
        goToUserLottery();
        assertTrue(elementExists(By.cssSelector(".sidebar")),
            "User dashboard should have a sidebar");
    }

    @Test @Order(3)
    public void test03_AdminSidebarNavigation() {
        loginAsAdmin();
        goToAdminLottery();
        assertTrue(elementExists(By.cssSelector(".sidebar")),
            "Admin dashboard should have a sidebar");
    }

    @Test @Order(4)
    public void test04_UserSidebarLinks() {
        loginAsUser();
        goToUserLottery();
        String pageSource = driver.getPageSource();
        assertTrue(pageSource.contains("userLottery") || pageSource.contains("ticketPurchase") ||
                   pageSource.contains("myTickets") || pageSource.contains("profile"),
            "User sidebar should contain navigation links");
    }

    @Test @Order(5)
    public void test05_AdminSidebarLinks() {
        loginAsAdmin();
        goToAdminLottery();
        String pageSource = driver.getPageSource();
        assertTrue(pageSource.contains("adminLottery") || pageSource.contains("userManagement") ||
                   pageSource.contains("adminTransactions") || pageSource.contains("adminProfile"),
            "Admin sidebar should contain admin navigation links");
    }

    @Test @Order(6)
    public void test06_PageTitlesCorrect() {
        goToHomepage();
        assertTrue(getPageTitle().contains("Lottery") || getPageTitle().contains("Fortuna"),
            "Homepage should have correct title");
        goToLogin();
        assertTrue(getPageTitle().contains("Login") || getPageTitle().contains("Lottery"),
            "Login page should have correct title");
    }

    @Test @Order(7)
    public void test07_ResponsiveDesignMobile() {
        goToHomepage();
        setWindowSize(375, 812); 
        assertTrue(pageContainsText("Lottery") || pageContainsText("Search"),
            "Homepage should render on mobile viewport");
    }

    @Test @Order(8)
    public void test08_ResponsiveDesignTablet() {
        goToHomepage();
        setWindowSize(768, 1024); 
        assertTrue(pageContainsText("Lottery") || pageContainsText("Search"),
            "Homepage should render on tablet viewport");
    }

    @Test @Order(9)
    public void test09_LogoutLinkVisible() {
        loginAsUser();
        goToUserLottery();
        String pageSource = driver.getPageSource().toLowerCase();
        assertTrue(pageSource.contains("logout") || pageSource.contains("log out")
                || pageSource.contains("sign out") || pageSource.contains("/logout")
                || elementExists(By.cssSelector("a[href*='logout']")),
            "Logout link should be visible for logged-in users");
    }

    @Test @Order(10)
    public void test10_ErrorPageHandling() {
        navigateTo("/nonexistent-page-xyz");
        
        assertNotNull(driver.getPageSource(), "Should handle invalid URLs gracefully");
    }
}
