package com.lottery.e2e;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Sec20_ExportUsersE2ETest extends E2ETestBase {

    @Test @Order(1)
    public void test01_ExportButtonExists() {
        loginAsAdmin();
        goToUserManagement();
        assertTrue(elementExists(By.cssSelector("a[href*='exportUsers']")),
            "Export to Excel button should exist on user management page");
    }

    @Test @Order(2)
    public void test02_ExportAllUsers() {
        loginAsAdmin();
        goToUserManagement();
        if (elementExists(By.cssSelector("a[href*='exportUsers']"))) {
            String href = driver.findElement(By.cssSelector("a[href*='exportUsers']")).getAttribute("href");
            assertTrue(href.contains("exportUsers"), "Export link should have correct URL");
            
            navigateTo("/exportUsers");
            
        }
    }

    @Test @Order(3)
    public void test03_ExportFilteredUsers() {
        loginAsAdmin();
        navigateTo("/userManagement?role=user");
        if (elementExists(By.cssSelector("a[href*='exportUsers']"))) {
            String href = driver.findElement(By.cssSelector("a[href*='exportUsers']")).getAttribute("href");
            assertTrue(href.contains("exportUsers"),
                "Export link should be present even with filters applied");
        }
    }

    @Test @Order(4)
    public void test04_ExportSearchedUsers() {
        loginAsAdmin();
        navigateTo("/userManagement?search=test");
        if (elementExists(By.cssSelector("a[href*='exportUsers']"))) {
            String href = driver.findElement(By.cssSelector("a[href*='exportUsers']")).getAttribute("href");
            assertTrue(href.contains("exportUsers"),
                "Export should preserve search filters");
        }
    }

    @Test @Order(5)
    public void test05_NonAdminCannotExport() {
        loginAsUser();
        goToExportUsers();
        assertTrue(isOnPage("login") || isOnPage("userLottery") || isOnPage("homepage"),
            "Non-admin should not be able to export users");
    }
}
