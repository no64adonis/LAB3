package com.lottery.e2e;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Sec23_PaginationE2ETest extends E2ETestBase {

    @Test @Order(1)
    public void test01_HomepagePagination() {
        navigateTo("/homepage?company=Vietlott&page=1");
        assertTrue(isOnPage("homepage"), "Homepage should support page=1");
    }

    @Test @Order(2)
    public void test02_HomepagePaginationPage2() {
        navigateTo("/homepage?company=Vietlott&page=2");
        assertTrue(isOnPage("homepage"), "Homepage should support page=2");
    }

    @Test @Order(3)
    public void test03_UserLotteryPagination() {
        loginAsUser();
        navigateTo("/userLottery?company=Vietlott&page=1");
        assertTrue(isOnPage("userLottery"), "User lottery should support pagination");
    }

    @Test @Order(4)
    public void test04_TicketPurchasePagination() {
        loginAsUser();
        navigateTo("/ticketPurchase?company=Vietlott&page=1");
        assertTrue(isOnPage("ticketPurchase"), "Ticket purchase should support pagination");
    }

    @Test @Order(5)
    public void test05_MyTicketsPagination() {
        loginAsUser();
        navigateTo("/myTickets?page=1");
        assertTrue(isOnPage("myTickets"), "My tickets should support pagination");
    }

    @Test @Order(6)
    public void test06_AdminLotteryPagination() {
        loginAsAdmin();
        navigateTo("/adminLottery?action=search&company=Vietlott&page=1");
        assertTrue(isOnPage("adminLottery"), "Admin lottery should support pagination");
    }

    @Test @Order(7)
    public void test07_UserManagementPagination() {
        loginAsAdmin();
        navigateTo("/userManagement?page=1");
        assertTrue(isOnPage("userManagement"), "User management should support pagination");
    }

    @Test @Order(8)
    public void test08_AdminTransactionsPagination() {
        loginAsAdmin();
        navigateTo("/adminTransactions?page=1");
        assertTrue(isOnPage("adminTransactions"), "Admin transactions should support pagination");
    }
}
