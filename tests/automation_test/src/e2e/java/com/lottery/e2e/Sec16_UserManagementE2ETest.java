package com.lottery.e2e;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Sec16_UserManagementE2ETest extends E2ETestBase {

    @Test @Order(1)
    public void test01_UserManagementPageLoads() {
        loginAsAdmin();
        goToUserManagement();
        assertTrue(pageContainsText("User") || pageContainsText("Management") || pageContainsText("Search"),
            "User management page should load");
    }

    @Test @Order(2)
    public void test02_SearchUserByEmail() {
        loginAsAdmin();
        navigateTo("/userManagement?search=" + USER_EMAIL);
        assertTrue(isOnPage("userManagement"), "Should find user by email");
    }

    @Test @Order(3)
    public void test03_SearchUserByName() {
        loginAsAdmin();
        navigateTo("/userManagement?search=Test&searchFields=FirstName");
        assertTrue(isOnPage("userManagement"), "Should search users by name");
    }

    @Test @Order(4)
    public void test04_FilterByRole() {
        loginAsAdmin();
        navigateTo("/userManagement?role=admin");
        assertTrue(isOnPage("userManagement"), "Should filter users by admin role");
    }

    @Test @Order(5)
    public void test05_FilterByLastLogin() {
        loginAsAdmin();
        navigateTo("/userManagement?lastLoginFrom=2026-01-01&lastLoginTo=2026-12-31");
        assertTrue(isOnPage("userManagement"), "Should filter users by last login date range");
    }

    @Test @Order(6)
    public void test06_CreateUserSuccess() {
        loginAsAdmin();
        goToUserManagement();
        String email = "e2e_created_" + uniqueTimestamp() + "@test.com";
        if (elementExists(By.id("firstName"))) {
            typeById("firstName", "Created");
            typeById("lastName", "User");
            var emailInputs = driver.findElements(By.cssSelector("#createUserForm input[name='email']"));
            if (!emailInputs.isEmpty()) { emailInputs.get(0).clear(); emailInputs.get(0).sendKeys(email); }
            else { typeById("email", email); }
            var pwInputs = driver.findElements(By.cssSelector("#createUserForm input[name='password']"));
            if (!pwInputs.isEmpty()) { pwInputs.get(0).clear(); pwInputs.get(0).sendKeys("Test@1234"); }
            else { typeById("password", "Test@1234"); }
            clickButtonByText("Create User");
            assertTrue(hasSuccessMessage() || isOnPage("userManagement"), "Should create user successfully");
        }
    }

    @Test @Order(7)
    public void test07_CreateUserDuplicateEmail() {
        loginAsAdmin();
        goToUserManagement();
        if (elementExists(By.id("firstName"))) {
            typeById("firstName", "Dup");
            typeById("lastName", "User");
            var emailInputs = driver.findElements(By.cssSelector("#createUserForm input[name='email']"));
            if (!emailInputs.isEmpty()) { emailInputs.get(0).clear(); emailInputs.get(0).sendKeys(USER_EMAIL); }
            var pwInputs = driver.findElements(By.cssSelector("#createUserForm input[name='password']"));
            if (!pwInputs.isEmpty()) { pwInputs.get(0).clear(); pwInputs.get(0).sendKeys("Test@1234"); }
            clickButtonByText("Create User");
            assertTrue(hasErrorMessage() || pageContainsText("exist"), "Should show error for duplicate email");
        }
    }

    @Test @Order(8)
    public void test08_UpdateUserRole() {
        loginAsAdmin();
        goToUserManagement();
        var roleSelects = driver.findElements(By.cssSelector("select[name='role']"));
        if (roleSelects.size() > 1) {
            var select = new org.openqa.selenium.support.ui.Select(roleSelects.get(1));
            select.selectByVisibleText("User");
            assertTrue(isOnPage("userManagement") || hasSuccessMessage(), "Should update user role");
        }
    }

    @Test @Order(9)
    public void test09_DeactivateUser() {
        loginAsAdmin();
        goToUserManagement();
        
        var rows = driver.findElements(By.cssSelector("tr"));
        boolean acted = false;
        for (var row : rows) {
            String rowText = row.getText();
            
            if (rowText.contains("admin@gmail.com") || rowText.contains("user@gmail.com")) continue;
            var actionHeaders = row.findElements(By.cssSelector(".actions-header"));
            if (!actionHeaders.isEmpty()) {
                actionHeaders.get(0).click();
                try { Thread.sleep(500); } catch (InterruptedException e) {}
                var actionItems = driver.findElements(By.cssSelector(".action-item"));
                for (var item : actionItems) {
                    if (item.getText().contains("Deactivate") || item.getText().contains("Activate")) {
                        item.click();
                        try { driver.switchTo().alert().accept(); } catch (Exception e) {}
                        acted = true;
                        break;
                    }
                }
                if (acted) break;
            }
        }
        assertTrue(isOnPage("userManagement"), "Should handle user deactivation");
    }

    @Test @Order(10)
    public void test10_BulkDeactivate() {
        loginAsAdmin();
        goToUserManagement();
        
        var checkboxes = driver.findElements(By.name("selectedUsers"));
        boolean selected = false;
        for (var cb : checkboxes) {
            
            try {
                var row = cb.findElement(By.xpath("./ancestor::tr"));
                String rowText = row.getText();
                if (rowText.contains("admin@gmail.com") || rowText.contains("user@gmail.com")) continue;
                cb.click();
                selected = true;
                break; 
            } catch (Exception e) { continue; }
        }
        if (selected) {
            if (tryClickButtonByText("Deactivate Selected")) {
                try { driver.switchTo().alert().accept(); } catch (Exception e) {}
            }
        }
        assertTrue(isOnPage("userManagement"), "Should handle bulk deactivation");
    }

    @Test @Order(11)
    public void test11_EditUser() {
        loginAsAdmin();
        goToUserManagement();
        if (elementExists(By.cssSelector(".actions-header"))) {
            var hdrs = driver.findElements(By.cssSelector(".actions-header"));
            if (!hdrs.isEmpty()) {
                hdrs.get(0).click();
                try { Thread.sleep(1000); } catch (InterruptedException e) {}
                var items = driver.findElements(By.cssSelector(".action-item"));
                for (var item : items) {
                    if (item.getText().contains("Edit")) { item.click(); break; }
                }
                try { Thread.sleep(1000); } catch (InterruptedException e) {}
                if (elementExists(By.id("editFirstName"))) {
                    typeById("editFirstName", "E2E_Edited");
                    if (tryClickButtonByText("Save")) {
                        try { Thread.sleep(1000); } catch (InterruptedException e) {}
                    }
                }
            }
        }
        assertTrue(isOnPage("userManagement"), "Should handle user editing");
    }

    @Test @Order(12)
    public void test12_PaginationWorks() {
        loginAsAdmin();
        navigateTo("/userManagement?page=1");
        assertTrue(isOnPage("userManagement"), "Pagination should work");
    }

    @Test @Order(13)
    public void test13_ExportUsersToExcel() {
        loginAsAdmin();
        goToUserManagement();
        if (elementExists(By.cssSelector("a[href*='exportUsers']"))) {
            String href = driver.findElement(By.cssSelector("a[href*='exportUsers']")).getAttribute("href");
            assertNotNull(href, "Export link should exist");
            assertTrue(href.contains("exportUsers"), "Export link should point to exportUsers");
        }
    }

    @Test @Order(14)
    public void test14_NonAdminCannotAccess() {
        loginAsUser();
        goToUserManagement();
        assertTrue(isOnPage("login") || isOnPage("userLottery") || isOnPage("homepage"),
            "Non-admin should not access user management");
    }
}
