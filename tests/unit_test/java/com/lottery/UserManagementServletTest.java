package com.lottery;

import com.lottery.model.User;
import com.lottery.service.AdminService;
import com.lottery.service.UserService;
import com.lottery.service.exception.ServiceException;

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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("15. UserManagementServlet (/userManagement)")
class UserManagementServletTest {

    private UserManagementServlet servlet;

    @Mock private UserService userService;
    @Mock private AdminService adminService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;

    private User adminUser;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new UserManagementServlet();
        setField(servlet, "userService", userService);
        setField(servlet, "adminService", adminService);
        adminUser = new User();
        adminUser.setRole("admin");
    }

    private void setupAdminGet() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("admin@test.com");
        when(userService.getUserByEmail("admin@test.com")).thenReturn(adminUser);
    }

    private void setupDefaultGetParams() {
        when(request.getParameter("action")).thenReturn(null);
        when(request.getParameter("search")).thenReturn(null);
        when(request.getParameter("page")).thenReturn(null);
        when(request.getParameter("lastLoginFrom")).thenReturn(null);
        when(request.getParameter("lastLoginTo")).thenReturn(null);
        when(request.getParameter("role")).thenReturn(null);
        when(request.getParameterValues("searchFields")).thenReturn(null);
    }

    private void setupAdminPost() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("admin@test.com");
        when(userService.getUserByEmail("admin@test.com")).thenReturn(adminUser);
        when(request.getSession()).thenReturn(session);
    }

    @Test @DisplayName("#1 GET - Not logged in")
    void testDoGet_notLoggedIn() throws Exception {
        when(request.getSession(false)).thenReturn(null);
        servlet.doGet(request, response);
        verify(response).sendRedirect("login.jsp");
    }

    @Test @DisplayName("#2 GET - Non-admin")
    void testDoGet_nonAdmin() throws Exception {
        User user = new User(); user.setRole("user");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("user@test.com");
        when(userService.getUserByEmail("user@test.com")).thenReturn(user);
        servlet.doGet(request, response);
        verify(response).sendRedirect("welcome.jsp");
    }

    @Test @DisplayName("#3 GET - Default load (all users)")
    void testDoGet_defaultLoad() throws Exception {
        setupAdminGet(); setupDefaultGetParams();
        when(userService.getAllUsers(0, 20)).thenReturn(Collections.emptyList());
        when(userService.getUserCount()).thenReturn(0);
        when(request.getRequestDispatcher("/userManagement.jsp")).thenReturn(dispatcher);
        servlet.doGet(request, response);
        verify(dispatcher).forward(request, response);
    }

    @Test @DisplayName("#4 GET - Search by keyword")
    void testDoGet_searchKeyword() throws Exception {
        setupAdminGet(); setupDefaultGetParams();
        when(request.getParameter("search")).thenReturn("john");
        when(userService.searchUsers(eq("john"), any(), any(), any(), any(), eq(0), eq(20)))
                .thenReturn(Collections.emptyList());
        when(userService.getSearchUserCount(eq("john"), any(), any(), any(), any())).thenReturn(0);
        when(request.getRequestDispatcher("/userManagement.jsp")).thenReturn(dispatcher);
        servlet.doGet(request, response);
        verify(dispatcher).forward(request, response);
    }

    @Test @DisplayName("#5 GET - Search by role filter")
    void testDoGet_searchByRole() throws Exception {
        setupAdminGet(); setupDefaultGetParams();
        when(request.getParameter("role")).thenReturn("admin");
        when(userService.searchUsers(any(), any(), any(), any(), eq("admin"), eq(0), eq(20)))
                .thenReturn(Collections.emptyList());
        when(userService.getSearchUserCount(any(), any(), any(), any(), eq("admin"))).thenReturn(0);
        when(request.getRequestDispatcher("/userManagement.jsp")).thenReturn(dispatcher);
        servlet.doGet(request, response);
        verify(dispatcher).forward(request, response);
    }

    @Test @DisplayName("#6 GET - Inactive users action")
    void testDoGet_inactiveAction() throws Exception {
        setupAdminGet(); setupDefaultGetParams();
        when(request.getParameter("action")).thenReturn("inactive");
        when(request.getParameter("period")).thenReturn(null);
        when(adminService.getInactiveUsers("30 days")).thenReturn(Collections.emptyList());
        when(request.getRequestDispatcher("/userManagement.jsp")).thenReturn(dispatcher);
        servlet.doGet(request, response);
        verify(adminService).getInactiveUsers("30 days");
    }

    @Test @DisplayName("#7 GET - Service exception")
    void testDoGet_serviceException() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("admin@test.com");
        when(userService.getUserByEmail("admin@test.com")).thenThrow(new ServiceException("DB error"));
        servlet.doGet(request, response);
        verify(session).setAttribute("error", "Error retrieving users: DB error");
        verify(response).sendRedirect("welcome.jsp");
    }

    @Test @DisplayName("#8 POST - Not logged in")
    void testDoPost_notLoggedIn() throws Exception {
        when(request.getSession(false)).thenReturn(null);
        servlet.doPost(request, response);
        verify(response).sendRedirect("login.jsp");
    }

    @Test @DisplayName("#9 POST - Non-admin")
    void testDoPost_nonAdmin() throws Exception {
        User user = new User(); user.setRole("user");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("email")).thenReturn("user@test.com");
        when(userService.getUserByEmail("user@test.com")).thenReturn(user);
        servlet.doPost(request, response);
        verify(response).sendRedirect("welcome.jsp");
    }

    @Test @DisplayName("#10 POST - updateRole success")
    void testDoPost_updateRoleSuccess() throws Exception {
        setupAdminPost();
        when(request.getParameter("action")).thenReturn("updateRole");
        when(request.getParameter("userId")).thenReturn("5");
        when(request.getParameter("role")).thenReturn("admin");
        when(adminService.processUserRoleUpdate("5", "admin")).thenReturn(true);
        servlet.doPost(request, response);
        verify(session).setAttribute("message", "User role updated successfully.");
    }

    @Test @DisplayName("#11 POST - updateRole failure")
    void testDoPost_updateRoleFailure() throws Exception {
        setupAdminPost();
        when(request.getParameter("action")).thenReturn("updateRole");
        when(request.getParameter("userId")).thenReturn("5");
        when(request.getParameter("role")).thenReturn("admin");
        when(adminService.processUserRoleUpdate("5", "admin")).thenReturn(false);
        servlet.doPost(request, response);
        verify(session).setAttribute("error", "Failed to update user role.");
    }

    @Test @DisplayName("#12 POST - setActiveStatus success")
    void testDoPost_setActiveStatusSuccess() throws Exception {
        setupAdminPost();
        when(request.getParameter("action")).thenReturn("setActiveStatus");
        when(request.getParameter("userId")).thenReturn("5");
        when(request.getParameter("isActive")).thenReturn("true");
        when(adminService.processUserActiveStatusUpdate("5", "true")).thenReturn(true);
        servlet.doPost(request, response);
        verify(session).setAttribute("message", "User status updated successfully.");
    }

    @Test @DisplayName("#13 POST - updateUser success")
    void testDoPost_updateUserSuccess() throws Exception {
        setupAdminPost();
        when(request.getParameter("action")).thenReturn("updateUser");
        when(request.getParameter("userId")).thenReturn("5");
        when(request.getParameter("email")).thenReturn("u@t.com");
        when(request.getParameter("firstName")).thenReturn("F");
        when(request.getParameter("lastName")).thenReturn("L");
        when(request.getParameter("phone")).thenReturn("123");
        when(adminService.processUserDetailsUpdate("5", "u@t.com", "F", "L", "123")).thenReturn(true);
        servlet.doPost(request, response);
        verify(session).setAttribute("message", "User details updated successfully.");
    }

    @Test @DisplayName("#14 POST - updateUserAccount with password")
    void testDoPost_updateUserAccountWithPassword() throws Exception {
        setupAdminPost();
        when(request.getParameter("action")).thenReturn("updateUserAccount");
        when(request.getParameter("userId")).thenReturn("5");
        when(request.getParameter("email")).thenReturn("u@t.com");
        when(request.getParameter("firstName")).thenReturn("F");
        when(request.getParameter("lastName")).thenReturn("L");
        when(request.getParameter("password")).thenReturn("NewPass1!");
        User existingUser = new User(); existingUser.setPhone("123");
        when(userService.getUserById(5)).thenReturn(existingUser);
        when(adminService.processUserDetailsUpdate("5", "u@t.com", "F", "L", "123")).thenReturn(true);
        servlet.doPost(request, response);
        verify(userService).updateUserPassword(5, "NewPass1!");
        verify(session).setAttribute("message", "User account updated successfully.");
    }

    @Test @DisplayName("#15 POST - updateUserAccount without password")
    void testDoPost_updateUserAccountNoPassword() throws Exception {
        setupAdminPost();
        when(request.getParameter("action")).thenReturn("updateUserAccount");
        when(request.getParameter("userId")).thenReturn("5");
        when(request.getParameter("email")).thenReturn("u@t.com");
        when(request.getParameter("firstName")).thenReturn("F");
        when(request.getParameter("lastName")).thenReturn("L");
        when(request.getParameter("password")).thenReturn("");
        User existingUser = new User(); existingUser.setPhone("123");
        when(userService.getUserById(5)).thenReturn(existingUser);
        when(adminService.processUserDetailsUpdate("5", "u@t.com", "F", "L", "123")).thenReturn(true);
        servlet.doPost(request, response);
        verify(userService, never()).updateUserPassword(anyInt(), anyString());
    }

    @Test @DisplayName("#16 POST - updateUserAccount invalid userId")
    void testDoPost_updateUserAccountInvalidId() throws Exception {
        setupAdminPost();
        when(request.getParameter("action")).thenReturn("updateUserAccount");
        when(request.getParameter("userId")).thenReturn("abc");
        when(request.getParameter("email")).thenReturn("u@t.com");
        when(request.getParameter("firstName")).thenReturn("F");
        when(request.getParameter("lastName")).thenReturn("L");
        when(request.getParameter("password")).thenReturn("");
        servlet.doPost(request, response);
        verify(session).setAttribute(eq("error"), any(String.class));
    }

    @Test @DisplayName("#17 POST - createUser success")
    void testDoPost_createUserSuccess() throws Exception {
        setupAdminPost();
        when(request.getParameter("action")).thenReturn("createUser");
        when(request.getParameter("email")).thenReturn("new@test.com");
        when(request.getParameter("password")).thenReturn("Pass1!");
        when(request.getParameter("firstName")).thenReturn("New");
        when(request.getParameter("lastName")).thenReturn("User");
        when(request.getParameter("role")).thenReturn("user");
        when(request.getParameter("balance")).thenReturn("100");
        when(request.getParameter("isActive")).thenReturn("true");
        when(adminService.processUserCreation("new@test.com", "Pass1!", "New", "User", "user", "100", true))
                .thenReturn(true);
        servlet.doPost(request, response);
        verify(session).setAttribute(eq("message"), contains("created successfully"));
    }

    @Test @DisplayName("#18 POST - createUser failure")
    void testDoPost_createUserFailure() throws Exception {
        setupAdminPost();
        when(request.getParameter("action")).thenReturn("createUser");
        when(request.getParameter("email")).thenReturn("new@test.com");
        when(request.getParameter("password")).thenReturn("Pass1!");
        when(request.getParameter("firstName")).thenReturn("New");
        when(request.getParameter("lastName")).thenReturn("User");
        when(request.getParameter("role")).thenReturn("user");
        when(request.getParameter("balance")).thenReturn("100");
        when(request.getParameter("isActive")).thenReturn("true");
        when(adminService.processUserCreation("new@test.com", "Pass1!", "New", "User", "user", "100", true))
                .thenReturn(false);
        servlet.doPost(request, response);
        verify(session).setAttribute(eq("error"), contains("Failed to create"));
    }

    @Test @DisplayName("#19 POST - bulkDeactivate success")
    void testDoPost_bulkDeactivateSuccess() throws Exception {
        setupAdminGet();
        when(request.getParameter("action")).thenReturn("bulkDeactivate");
        String[] ids = {"1", "2", "3"};
        when(request.getParameterValues("selectedUsers")).thenReturn(ids);
        when(adminService.processBulkUserDeactivation(ids)).thenReturn(true);
        servlet.doPost(request, response);
        verify(response).sendRedirect("userManagement");
    }

    @Test @DisplayName("#20 POST - bulkDeactivate failure")
    void testDoPost_bulkDeactivateFailure() throws Exception {
        setupAdminPost();
        when(request.getParameter("action")).thenReturn("bulkDeactivate");
        String[] ids = {"1", "2"};
        when(request.getParameterValues("selectedUsers")).thenReturn(ids);
        when(adminService.processBulkUserDeactivation(ids)).thenReturn(false);
        servlet.doPost(request, response);
        verify(session).setAttribute("error", "Failed to deactivate users.");
    }

    @Test @DisplayName("#21 POST - unknown action")
    void testDoPost_unknownAction() throws Exception {
        setupAdminGet();
        when(request.getParameter("action")).thenReturn("unknown");
        servlet.doPost(request, response);
        verify(response).sendRedirect("userManagement");
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
