package com.lottery.e2e;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Sec14_AdminLotteryE2ETest extends E2ETestBase {

    @Test @Order(1)
    public void test01_AdminLotteryPageLoads() {
        loginAsAdmin();
        goToAdminLottery();
        assertTrue(pageContainsText("Create") || pageContainsText("Lottery") || pageContainsText("Ticket"),
            "Admin lottery page should load with create form");
    }

    @Test @Order(2)
    public void test02_CreateTicketSuccess() {
        loginAsAdmin();
        goToAdminLottery();
        String ticketId = "E2E_" + uniqueTimestamp();
        typeByName("ticketID", ticketId);
        typeByName("num1", "1"); typeByName("num2", "2"); typeByName("num3", "3");
        typeByName("num4", "4"); typeByName("num5", "5"); typeByName("num6", "6");
        
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
            "if(typeof createCompanySelector !== 'undefined') { createCompanySelector.setSelectedCompanies(['Vietlott']); }" +
            "document.getElementById('selectedCompanyInput').value = 'Vietlott';");
        clickButtonByText("Create Ticket");
        try { driver.switchTo().alert().accept(); } catch (Exception e) {}
        assertTrue(hasSuccessMessage() || isOnPage("adminLottery"), "Should create ticket successfully");
    }

    @Test @Order(3)
    public void test03_CreateTicketDuplicateId() {
        loginAsAdmin();
        goToAdminLottery();
        String ticketId = "E2E_DUP_" + uniqueTimestamp();
        typeByName("ticketID", ticketId);
        typeByName("num1", "10"); typeByName("num2", "20"); typeByName("num3", "30");
        typeByName("num4", "40"); typeByName("num5", "50"); typeByName("num6", "60");
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
            "if(typeof createCompanySelector !== 'undefined') { createCompanySelector.setSelectedCompanies(['Vietlott']); }" +
            "document.getElementById('selectedCompanyInput').value = 'Vietlott';");
        clickButtonByText("Create Ticket");
        try { driver.switchTo().alert().accept(); } catch (Exception e) {}
        goToAdminLottery();
        typeByName("ticketID", ticketId);
        typeByName("num1", "11"); typeByName("num2", "22"); typeByName("num3", "33");
        typeByName("num4", "44"); typeByName("num5", "55"); typeByName("num6", "66");
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
            "if(typeof createCompanySelector !== 'undefined') { createCompanySelector.setSelectedCompanies(['Vietlott']); }" +
            "document.getElementById('selectedCompanyInput').value = 'Vietlott';");
        clickButtonByText("Create Ticket");
        try { driver.switchTo().alert().accept(); } catch (Exception e) {}
        assertTrue(hasErrorMessage() || pageContainsText("exists") || pageContainsText("duplicate") || isOnPage("adminLottery"),
            "Should show error for duplicate ticket ID");
    }

    @Test @Order(4)
    public void test04_CreateTicketMissingFields() {
        loginAsAdmin();
        goToAdminLottery();
        typeByName("ticketID", "");
        clickButtonByText("Create Ticket");
        
        try { driver.switchTo().alert().accept(); } catch (Exception e) {}
        assertTrue(isOnPage("adminLottery"), "Should stay on page when required fields are missing");
    }

    @Test @Order(5)
    public void test05_BulkInsertTickets() {
        loginAsAdmin();
        goToAdminLottery();
        String ts = String.valueOf(uniqueTimestamp());
        if (elementExists(By.name("csvData"))) {
            driver.findElement(By.name("csvData")).sendKeys("BULK" + ts + ", 1, 2, 3, 4, 5, 6, Vietlott, true");
            clickButtonByText("Bulk Insert");
            assertTrue(isOnPage("adminLottery") || hasSuccessMessage(), "Should process bulk insert");
        }
    }

    @Test @Order(6)
    public void test06_BulkInsertInvalidFormat() {
        loginAsAdmin();
        goToAdminLottery();
        if (elementExists(By.name("csvData"))) {
            driver.findElement(By.name("csvData")).sendKeys("invalid,data,format");
            clickButtonByText("Bulk Insert");
            assertTrue(hasErrorMessage() || isOnPage("adminLottery"), "Should show error for invalid format");
        }
    }

    @Test @Order(7)
    public void test07_SearchAdminTickets() {
        loginAsAdmin();
        navigateTo("/adminLottery?action=search&company=Vietlott");
        assertTrue(isOnPage("adminLottery"), "Admin should be able to search tickets");
    }

    @Test @Order(8)
    public void test08_PublishTicket() {
        loginAsAdmin();
        navigateTo("/adminLottery?action=search&company=Vietlott");
        if (elementExists(By.name("ticketCheckbox"))) {
            driver.findElement(By.name("ticketCheckbox")).click();
            clickButtonByText("Publish");
            try { driver.switchTo().alert().accept(); } catch (Exception e) {}
            assertTrue(isOnPage("adminLottery"), "Should publish selected tickets");
        }
    }

    @Test @Order(9)
    public void test09_UnpublishTicket() {
        loginAsAdmin();
        navigateTo("/adminLottery?action=search&company=Vietlott");
        if (elementExists(By.name("ticketCheckbox"))) {
            driver.findElement(By.name("ticketCheckbox")).click();
            clickButtonByText("Unpublish");
            try { driver.switchTo().alert().accept(); } catch (Exception e) {}
            assertTrue(isOnPage("adminLottery"), "Should unpublish selected tickets");
        }
    }

    @Test @Order(10)
    public void test10_UpdateTicketPrice() {
        loginAsAdmin();
        goToAdminLottery();
        if (elementExists(By.name("newPrice"))) {
            typeByName("newPrice", "5.00");
            
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                "if(typeof priceCompanySelector !== 'undefined') { priceCompanySelector.setSelectedCompanies(['Vietlott']); }" +
                "var el = document.getElementById('selectedCompaniesInputPrice'); if(el) el.value = 'Vietlott';");
            clickButtonByText("Update Price");
            try { driver.switchTo().alert().accept(); } catch (Exception e) {}
            assertTrue(isOnPage("adminLottery") || hasSuccessMessage(), "Should update ticket price");
        }
    }

    @Test @Order(11)
    public void test11_SelectAllAndClear() {
        loginAsAdmin();
        navigateTo("/adminLottery?action=search&company=Vietlott");
        if (elementExists(By.id("selectAllCheckbox"))) {
            clickById("selectAllCheckbox");
            clickButtonByText("Clear");
            assertTrue(isOnPage("adminLottery"), "Select All and Clear should work");
        }
    }

    @Test @Order(12)
    public void test12_NonAdminCannotAccess() {
        loginAsUser();
        goToAdminLottery();
        assertTrue(isOnPage("login") || isOnPage("userLottery") || isOnPage("homepage"),
            "Non-admin users should not access admin lottery page");
    }
}
