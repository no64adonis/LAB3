package com.lottery;

import com.lottery.model.LotteryTicket;
import com.lottery.model.User;
import com.lottery.service.AdminService;
import com.lottery.service.TicketService;
import com.lottery.service.UserService;
import com.lottery.service.exception.ServiceException;
import com.lottery.service.exception.TicketNotFoundException;
import com.lottery.service.exception.UserNotFoundException;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.*;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("13. AdminLotteryServlet (/adminLottery)")
class AdminLotteryServletTest {

    private AdminLotteryServlet servlet;

    @Mock private AdminService adminService;
    @Mock private TicketService ticketService;
    @Mock private UserService userService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new AdminLotteryServlet();
        setField(servlet, "adminService", adminService);
        setField(servlet, "ticketService", ticketService);
        setField(servlet, "userService", userService);
    }

    private void setupDefaultGetParams() {
        when(request.getParameter("action")).thenReturn(null);
        when(request.getParameterValues("company")).thenReturn(null);
        when(request.getParameter("num1")).thenReturn(null);
        when(request.getParameter("num2")).thenReturn(null);
        when(request.getParameter("num3")).thenReturn(null);
        when(request.getParameter("num4")).thenReturn(null);
        when(request.getParameter("num5")).thenReturn(null);
        when(request.getParameter("num6")).thenReturn(null);
        when(request.getParameter("startDate")).thenReturn(null);
        when(request.getParameter("endDate")).thenReturn(null);
        when(request.getParameter("specificDate")).thenReturn(null);
        when(request.getParameter("page")).thenReturn(null);
        when(request.getParameter("fromHistory")).thenReturn(null);
        when(request.getParameter("searchId")).thenReturn(null);
        
        when(request.getParameter("company")).thenReturn(null);
    }

    @Test
    @DisplayName("#1 GET - Not logged in")
    void testDoGet_notLoggedIn() throws Exception {
        when(request.getSession(false)).thenReturn(null);
        servlet.doGet(request, response);
        verify(response).sendRedirect("login.jsp");
    }

    @Test
    @DisplayName("#2 GET - Non-admin user")
    void testDoGet_nonAdmin() throws Exception {
        User user = new User();
        user.setRole("user");

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("user@test.com");
        when(userService.getUserByEmail("user@test.com")).thenReturn(user);

        servlet.doGet(request, response);

        verify(response).sendRedirect("welcome.jsp");
    }

    @Test
    @DisplayName("#3 GET - Default page load (all tickets)")
    void testDoGet_defaultLoad() throws Exception {
        User admin = new User();
        admin.setRole("admin");

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("admin@test.com");
        when(userService.getUserByEmail("admin@test.com")).thenReturn(admin);
        setupDefaultGetParams();
        when(adminService.getAllTickets(0, 10)).thenReturn(Collections.emptyList());
        when(adminService.getTotalTicketCount()).thenReturn(0);
        when(adminService.getAllCompanies()).thenReturn(Collections.emptyList());
        when(request.getRequestDispatcher("/adminLottery.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute("tickets", Collections.emptyList());
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("#4 GET - User not found exception")
    void testDoGet_userNotFound() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("admin@test.com");
        when(userService.getUserByEmail("admin@test.com")).thenThrow(new UserNotFoundException("Not found"));

        servlet.doGet(request, response);

        verify(session).setAttribute("error", "User not found.");
        verify(response).sendRedirect("login.jsp");
    }

    @Test
    @DisplayName("#5 GET - Service exception")
    void testDoGet_serviceException() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("admin@test.com");
        when(userService.getUserByEmail("admin@test.com")).thenThrow(new ServiceException("DB error"));

        servlet.doGet(request, response);

        verify(session).setAttribute("error", "Error retrieving data: DB error");
        verify(response).sendRedirect("welcome.jsp");
    }

    @Test
    @DisplayName("#6 POST - Not logged in")
    void testDoPost_notLoggedIn() throws Exception {
        when(request.getSession(false)).thenReturn(null);
        servlet.doPost(request, response);
        verify(response).sendRedirect("login.jsp");
    }

    @Test
    @DisplayName("#7 POST - Non-admin user")
    void testDoPost_nonAdmin() throws Exception {
        User user = new User();
        user.setRole("user");

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("user@test.com");
        when(userService.getUserByEmail("user@test.com")).thenReturn(user);

        servlet.doPost(request, response);

        verify(response).sendRedirect("welcome.jsp");
    }

    @Test
    @DisplayName("#8 POST - No action")
    void testDoPost_noAction() throws Exception {
        User admin = new User();
        admin.setRole("admin");

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("admin@test.com");
        when(userService.getUserByEmail("admin@test.com")).thenReturn(admin);
        when(request.getParameter("action")).thenReturn(null);

        servlet.doPost(request, response);

        verify(response).sendRedirect("adminLottery");
    }

    @Test
    @DisplayName("#9 POST - Create ticket success")
    void testDoPost_createTicket() throws Exception {
        User admin = new User();
        admin.setRole("admin");
        LotteryTicket ticket = new LotteryTicket();

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("admin@test.com");
        when(userService.getUserByEmail("admin@test.com")).thenReturn(admin);
        when(request.getParameter("action")).thenReturn("create");
        when(request.getParameter("ticketID")).thenReturn("T001");
        when(request.getParameter("num1")).thenReturn("1");
        when(request.getParameter("num2")).thenReturn("2");
        when(request.getParameter("num3")).thenReturn("3");
        when(request.getParameter("num4")).thenReturn("4");
        when(request.getParameter("num5")).thenReturn("5");
        when(request.getParameter("num6")).thenReturn("6");
        when(request.getParameter("selectedCompany")).thenReturn("TestCo");
        when(request.getParameter("published")).thenReturn("true");
        when(adminService.processTicketCreation(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(ticket);
        when(request.getSession()).thenReturn(session);
        
        when(request.getParameter("company")).thenReturn(null);
        when(request.getParameter("startDate")).thenReturn(null);
        when(request.getParameter("endDate")).thenReturn(null);
        when(request.getParameter("specificDate")).thenReturn(null);
        when(request.getParameter("page")).thenReturn(null);

        servlet.doPost(request, response);

        verify(session).setAttribute("message", "Lottery ticket created successfully.");
    }

    @Test
    @DisplayName("#10 POST - Create ticket failure")
    void testDoPost_createTicketFailure() throws Exception {
        User admin = new User();
        admin.setRole("admin");

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("admin@test.com");
        when(userService.getUserByEmail("admin@test.com")).thenReturn(admin);
        when(request.getParameter("action")).thenReturn("create");
        when(request.getParameter("ticketID")).thenReturn(null);
        when(request.getParameter("num1")).thenReturn(null);
        when(request.getParameter("num2")).thenReturn(null);
        when(request.getParameter("num3")).thenReturn(null);
        when(request.getParameter("num4")).thenReturn(null);
        when(request.getParameter("num5")).thenReturn(null);
        when(request.getParameter("num6")).thenReturn(null);
        when(request.getParameter("selectedCompany")).thenReturn(null);
        when(request.getParameter("published")).thenReturn(null);
        when(adminService.processTicketCreation(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new ServiceException("Missing fields"));
        when(request.getSession()).thenReturn(session);
        when(request.getParameter("company")).thenReturn(null);
        when(request.getParameter("startDate")).thenReturn(null);
        when(request.getParameter("endDate")).thenReturn(null);
        when(request.getParameter("specificDate")).thenReturn(null);
        when(request.getParameter("page")).thenReturn(null);

        servlet.doPost(request, response);

        verify(session).setAttribute("error", "Missing fields");
    }

    @Test
    @DisplayName("#11 POST - Update status success")
    void testDoPost_updateStatus() throws Exception {
        User admin = new User();
        admin.setRole("admin");

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("admin@test.com");
        when(userService.getUserByEmail("admin@test.com")).thenReturn(admin);
        when(request.getParameter("action")).thenReturn("updateStatus");
        when(request.getParameter("ticketId")).thenReturn("T001");
        when(request.getParameter("published")).thenReturn("true");
        when(adminService.processTicketStatusUpdate("T001", "true")).thenReturn(true);
        when(request.getSession()).thenReturn(session);
        when(request.getParameter("company")).thenReturn(null);
        when(request.getParameter("num1")).thenReturn(null);
        when(request.getParameter("num2")).thenReturn(null);
        when(request.getParameter("num3")).thenReturn(null);
        when(request.getParameter("num4")).thenReturn(null);
        when(request.getParameter("num5")).thenReturn(null);
        when(request.getParameter("num6")).thenReturn(null);
        when(request.getParameter("startDate")).thenReturn(null);
        when(request.getParameter("endDate")).thenReturn(null);
        when(request.getParameter("specificDate")).thenReturn(null);
        when(request.getParameter("page")).thenReturn(null);

        servlet.doPost(request, response);

        verify(session).setAttribute("message", "Ticket status updated successfully.");
    }

    @Test
    @DisplayName("#12 POST - Update status - ticket not found")
    void testDoPost_updateStatus_ticketNotFound() throws Exception {
        User admin = new User();
        admin.setRole("admin");

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("admin@test.com");
        when(userService.getUserByEmail("admin@test.com")).thenReturn(admin);
        when(request.getParameter("action")).thenReturn("updateStatus");
        when(request.getParameter("ticketId")).thenReturn("INVALID");
        when(request.getParameter("published")).thenReturn("true");
        when(adminService.processTicketStatusUpdate("INVALID", "true"))
                .thenThrow(new TicketNotFoundException("Not found"));
        when(request.getSession()).thenReturn(session);
        when(request.getParameter("company")).thenReturn(null);
        when(request.getParameter("num1")).thenReturn(null);
        when(request.getParameter("num2")).thenReturn(null);
        when(request.getParameter("num3")).thenReturn(null);
        when(request.getParameter("num4")).thenReturn(null);
        when(request.getParameter("num5")).thenReturn(null);
        when(request.getParameter("num6")).thenReturn(null);
        when(request.getParameter("startDate")).thenReturn(null);
        when(request.getParameter("endDate")).thenReturn(null);
        when(request.getParameter("specificDate")).thenReturn(null);
        when(request.getParameter("page")).thenReturn(null);

        servlet.doPost(request, response);

        verify(session).setAttribute("error", "Ticket not found.");
    }

    @Test
    @DisplayName("#13 POST - Bulk update - no tickets selected")
    void testDoPost_bulkUpdate_noTickets() throws Exception {
        User admin = new User();
        admin.setRole("admin");

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("admin@test.com");
        when(userService.getUserByEmail("admin@test.com")).thenReturn(admin);
        when(request.getParameter("action")).thenReturn("bulkUpdate");
        when(request.getParameter("selectedTickets")).thenReturn(null);
        when(request.getParameter("type")).thenReturn("publish");
        when(request.getParameter("value")).thenReturn("true");
        when(request.getSession()).thenReturn(session);
        when(request.getParameter("company")).thenReturn(null);
        when(request.getParameter("num1")).thenReturn(null);
        when(request.getParameter("num2")).thenReturn(null);
        when(request.getParameter("num3")).thenReturn(null);
        when(request.getParameter("num4")).thenReturn(null);
        when(request.getParameter("num5")).thenReturn(null);
        when(request.getParameter("num6")).thenReturn(null);
        when(request.getParameter("startDate")).thenReturn(null);
        when(request.getParameter("endDate")).thenReturn(null);
        when(request.getParameter("specificDate")).thenReturn(null);
        when(request.getParameter("page")).thenReturn(null);

        servlet.doPost(request, response);

        verify(session).setAttribute("error", "No tickets selected.");
    }

    @Test
    @DisplayName("#14 POST - Bulk update success")
    void testDoPost_bulkUpdate_success() throws Exception {
        User admin = new User();
        admin.setRole("admin");

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("admin@test.com");
        when(userService.getUserByEmail("admin@test.com")).thenReturn(admin);
        when(request.getParameter("action")).thenReturn("bulkUpdate");
        when(request.getParameter("selectedTickets")).thenReturn("T1,T2,T3");
        when(request.getParameter("type")).thenReturn("publish");
        when(request.getParameter("value")).thenReturn("true");
        when(adminService.processBulkTicketStatusUpdate("T1,T2,T3", "true")).thenReturn(true);
        when(request.getSession()).thenReturn(session);
        when(request.getParameter("company")).thenReturn(null);
        when(request.getParameter("num1")).thenReturn(null);
        when(request.getParameter("num2")).thenReturn(null);
        when(request.getParameter("num3")).thenReturn(null);
        when(request.getParameter("num4")).thenReturn(null);
        when(request.getParameter("num5")).thenReturn(null);
        when(request.getParameter("num6")).thenReturn(null);
        when(request.getParameter("startDate")).thenReturn(null);
        when(request.getParameter("endDate")).thenReturn(null);
        when(request.getParameter("specificDate")).thenReturn(null);
        when(request.getParameter("page")).thenReturn(null);

        servlet.doPost(request, response);

        verify(session).setAttribute(eq("message"), contains("Bulk update"));
    }

    @Test
    @DisplayName("#15 POST - Bulk insert success")
    void testDoPost_bulkInsert() throws Exception {
        User admin = new User();
        admin.setRole("admin");
        Map<String, Object> results = new HashMap<>();
        results.put("successCount", 3);
        results.put("duplicates", Collections.emptyList());
        results.put("errors", Collections.emptyList());

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("admin@test.com");
        when(userService.getUserByEmail("admin@test.com")).thenReturn(admin);
        when(request.getParameter("action")).thenReturn("bulkInsert");
        when(request.getParameter("csvData")).thenReturn("csv data");
        when(adminService.processBulkTicketInsertion("csv data")).thenReturn(results);
        when(request.getSession()).thenReturn(session);
        when(request.getParameter("company")).thenReturn(null);
        when(request.getParameter("num1")).thenReturn(null);
        when(request.getParameter("num2")).thenReturn(null);
        when(request.getParameter("num3")).thenReturn(null);
        when(request.getParameter("num4")).thenReturn(null);
        when(request.getParameter("num5")).thenReturn(null);
        when(request.getParameter("num6")).thenReturn(null);
        when(request.getParameter("startDate")).thenReturn(null);
        when(request.getParameter("endDate")).thenReturn(null);
        when(request.getParameter("specificDate")).thenReturn(null);
        when(request.getParameter("page")).thenReturn(null);

        servlet.doPost(request, response);

        verify(session).setAttribute(eq("message"), contains("Successfully inserted: 3"));
    }

    @Test
    @DisplayName("#16 POST - Price update - no companies")
    void testDoPost_priceUpdate_noCompanies() throws Exception {
        User admin = new User();
        admin.setRole("admin");

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("admin@test.com");
        when(userService.getUserByEmail("admin@test.com")).thenReturn(admin);
        when(request.getParameter("action")).thenReturn("updatePrice");
        when(request.getParameter("selectedCompaniesPrice")).thenReturn(null);
        when(request.getParameter("newPrice")).thenReturn("10000");
        when(request.getSession()).thenReturn(session);
        when(request.getParameter("company")).thenReturn(null);
        when(request.getParameter("num1")).thenReturn(null);
        when(request.getParameter("num2")).thenReturn(null);
        when(request.getParameter("num3")).thenReturn(null);
        when(request.getParameter("num4")).thenReturn(null);
        when(request.getParameter("num5")).thenReturn(null);
        when(request.getParameter("num6")).thenReturn(null);
        when(request.getParameter("startDate")).thenReturn(null);
        when(request.getParameter("endDate")).thenReturn(null);
        when(request.getParameter("specificDate")).thenReturn(null);
        when(request.getParameter("page")).thenReturn(null);

        servlet.doPost(request, response);

        verify(session).setAttribute("error", "Please select at least one company.");
    }

    @Test
    @DisplayName("#17 POST - Default action")
    void testDoPost_defaultAction() throws Exception {
        User admin = new User();
        admin.setRole("admin");

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("admin@test.com");
        when(userService.getUserByEmail("admin@test.com")).thenReturn(admin);
        when(request.getParameter("action")).thenReturn("unknown");

        servlet.doPost(request, response);

        verify(response).sendRedirect("adminLottery");
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
