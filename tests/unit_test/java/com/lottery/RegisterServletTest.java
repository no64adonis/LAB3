package com.lottery;

import com.lottery.db.DatabaseConfig;
import com.lottery.util.InputValidator;
import com.lottery.util.PasswordValidator;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("3. RegisterServlet (/register)")
class RegisterServletTest {

    private RegisterServlet servlet;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private RequestDispatcher dispatcher;
    @Mock private Connection connection;
    @Mock private PreparedStatement statement;
    @Mock private ResultSet resultSet;

    @BeforeEach
    void setUp() {
        servlet = new RegisterServlet();
    }

    @Test
    @DisplayName("#1 GET - Show registration page")
    void testDoGet_showRegisterPage() throws Exception {
        when(request.getRequestDispatcher("/register.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("#2 POST - SQL injection in email")
    void testDoPost_sqlInjectionEmail() throws Exception {
        when(request.getParameter("email")).thenReturn("'; DROP TABLE--");
        when(request.getParameter("firstName")).thenReturn("John");
        when(request.getParameter("lastName")).thenReturn("Doe");
        when(request.getParameter("phone")).thenReturn("123456");
        when(request.getParameter("password")).thenReturn("Pass1234!");
        when(request.getParameter("confirmPassword")).thenReturn("Pass1234!");
        when(request.getRequestDispatcher("/register.jsp")).thenReturn(dispatcher);

        servlet.doPost(request, response);

        verify(request).setAttribute("errorMessage", "Invalid input values.");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("#3 POST - SQL injection in firstName")
    void testDoPost_sqlInjectionFirstName() throws Exception {
        when(request.getParameter("email")).thenReturn("test@email.com");
        when(request.getParameter("firstName")).thenReturn("'; DROP TABLE users--");
        when(request.getParameter("lastName")).thenReturn("Doe");
        when(request.getParameter("phone")).thenReturn(null);
        when(request.getParameter("password")).thenReturn("Pass1234!");
        when(request.getParameter("confirmPassword")).thenReturn("Pass1234!");
        when(request.getRequestDispatcher("/register.jsp")).thenReturn(dispatcher);

        servlet.doPost(request, response);

        verify(request).setAttribute("errorMessage", "Invalid input values.");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("#4 POST - Email null/empty")
    void testDoPost_emailNull() throws Exception {
        when(request.getParameter("email")).thenReturn(null);
        when(request.getParameter("firstName")).thenReturn("John");
        when(request.getParameter("lastName")).thenReturn("Doe");
        when(request.getParameter("phone")).thenReturn(null);
        when(request.getParameter("password")).thenReturn("Pass1234!");
        when(request.getParameter("confirmPassword")).thenReturn("Pass1234!");
        when(request.getRequestDispatcher("/register.jsp")).thenReturn(dispatcher);

        servlet.doPost(request, response);

        verify(request).setAttribute("errorMessage", "Email is required");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("#5 POST - Invalid email format")
    void testDoPost_invalidEmailFormat() throws Exception {
        when(request.getParameter("email")).thenReturn("notanemail");
        when(request.getParameter("firstName")).thenReturn("John");
        when(request.getParameter("lastName")).thenReturn("Doe");
        when(request.getParameter("phone")).thenReturn(null);
        when(request.getParameter("password")).thenReturn("Pass1234!");
        when(request.getParameter("confirmPassword")).thenReturn("Pass1234!");
        when(request.getRequestDispatcher("/register.jsp")).thenReturn(dispatcher);

        servlet.doPost(request, response);

        verify(request).setAttribute("errorMessage", "Invalid email format.");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("#6 POST - Email with invalid domain (single-char TLD)")
    void testDoPost_emailNotAuthentic() throws Exception {
        when(request.getParameter("email")).thenReturn("user@x.y");
        when(request.getParameter("firstName")).thenReturn("John");
        when(request.getParameter("lastName")).thenReturn("Doe");
        when(request.getParameter("phone")).thenReturn(null);
        when(request.getParameter("password")).thenReturn("Pass1234!");
        when(request.getParameter("confirmPassword")).thenReturn("Pass1234!");
        when(request.getRequestDispatcher("/register.jsp")).thenReturn(dispatcher);

        servlet.doPost(request, response);

        verify(request).setAttribute("errorMessage", "Invalid email format.");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("#7 POST - Missing firstName")
    void testDoPost_missingFirstName() throws Exception {
        when(request.getParameter("email")).thenReturn("test@email.com");
        when(request.getParameter("firstName")).thenReturn(null);
        when(request.getParameter("lastName")).thenReturn("Doe");
        when(request.getParameter("phone")).thenReturn(null);
        when(request.getParameter("password")).thenReturn("Pass1234!");
        when(request.getParameter("confirmPassword")).thenReturn("Pass1234!");
        when(request.getRequestDispatcher("/register.jsp")).thenReturn(dispatcher);

        servlet.doPost(request, response);

        verify(request).setAttribute("errorMessage", "First name is required");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("#8 POST - Missing lastName")
    void testDoPost_missingLastName() throws Exception {
        when(request.getParameter("email")).thenReturn("test@email.com");
        when(request.getParameter("firstName")).thenReturn("John");
        when(request.getParameter("lastName")).thenReturn(null);
        when(request.getParameter("phone")).thenReturn(null);
        when(request.getParameter("password")).thenReturn("Pass1234!");
        when(request.getParameter("confirmPassword")).thenReturn("Pass1234!");
        when(request.getRequestDispatcher("/register.jsp")).thenReturn(dispatcher);

        servlet.doPost(request, response);

        verify(request).setAttribute("errorMessage", "Last name is required");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("#9 POST - Missing password")
    void testDoPost_missingPassword() throws Exception {
        when(request.getParameter("email")).thenReturn("test@email.com");
        when(request.getParameter("firstName")).thenReturn("John");
        when(request.getParameter("lastName")).thenReturn("Doe");
        when(request.getParameter("phone")).thenReturn(null);
        when(request.getParameter("password")).thenReturn(null);
        when(request.getParameter("confirmPassword")).thenReturn(null);
        when(request.getRequestDispatcher("/register.jsp")).thenReturn(dispatcher);

        servlet.doPost(request, response);

        verify(request).setAttribute("errorMessage", "Password is required");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("#10 POST - Weak password")
    void testDoPost_weakPassword() throws Exception {
        when(request.getParameter("email")).thenReturn("test@email.com");
        when(request.getParameter("firstName")).thenReturn("John");
        when(request.getParameter("lastName")).thenReturn("Doe");
        when(request.getParameter("phone")).thenReturn(null);
        when(request.getParameter("password")).thenReturn("123");
        when(request.getParameter("confirmPassword")).thenReturn("123");
        when(request.getRequestDispatcher("/register.jsp")).thenReturn(dispatcher);

        servlet.doPost(request, response);

        verify(request).setAttribute(eq("errorMessage"), anyString());
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("#11 POST - Password mismatch")
    void testDoPost_passwordMismatch() throws Exception {
        when(request.getParameter("email")).thenReturn("test@email.com");
        when(request.getParameter("firstName")).thenReturn("John");
        when(request.getParameter("lastName")).thenReturn("Doe");
        when(request.getParameter("phone")).thenReturn(null);
        when(request.getParameter("password")).thenReturn("StrongPass1!");
        when(request.getParameter("confirmPassword")).thenReturn("DifferentPass2!");
        when(request.getRequestDispatcher("/register.jsp")).thenReturn(dispatcher);

        servlet.doPost(request, response);

        verify(request).setAttribute("errorMessage", "Passwords do not match");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("#12 POST - Duplicate email")
    void testDoPost_duplicateEmail() throws Exception {
        when(request.getParameter("email")).thenReturn("existing@email.com");
        when(request.getParameter("firstName")).thenReturn("John");
        when(request.getParameter("lastName")).thenReturn("Doe");
        when(request.getParameter("phone")).thenReturn(null);
        when(request.getParameter("password")).thenReturn("StrongPass1!");
        when(request.getParameter("confirmPassword")).thenReturn("StrongPass1!");
        when(request.getRequestDispatcher("/register.jsp")).thenReturn(dispatcher);

        try (MockedStatic<DatabaseConfig> db = mockStatic(DatabaseConfig.class)) {
            db.when(DatabaseConfig::getConnection).thenReturn(connection);
            when(connection.prepareStatement(anyString())).thenReturn(statement);
            when(statement.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(true);
            when(resultSet.getInt(1)).thenReturn(1); 

            servlet.doPost(request, response);

            verify(request).setAttribute("errorMessage", "Email already registered. Please use a different email.");
            verify(dispatcher).forward(request, response);
        }
    }

    @Test
    @DisplayName("#14 POST - DB insert failure")
    void testDoPost_dbInsertFailure() throws Exception {
        when(request.getParameter("email")).thenReturn("new@email.com");
        when(request.getParameter("firstName")).thenReturn("John");
        when(request.getParameter("lastName")).thenReturn("Doe");
        when(request.getParameter("phone")).thenReturn(null);
        when(request.getParameter("password")).thenReturn("StrongPass1!");
        when(request.getParameter("confirmPassword")).thenReturn("StrongPass1!");
        when(request.getRequestDispatcher("/register.jsp")).thenReturn(dispatcher);

        try (MockedStatic<DatabaseConfig> db = mockStatic(DatabaseConfig.class)) {
            db.when(DatabaseConfig::getConnection).thenReturn(connection);
            when(connection.prepareStatement(anyString())).thenReturn(statement);
            
            when(statement.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(true);
            when(resultSet.getInt(1)).thenReturn(0);
            
            when(statement.executeUpdate()).thenReturn(0);

            servlet.doPost(request, response);

            verify(request).setAttribute("errorMessage", "Registration failed. Please try again.");
            verify(dispatcher).forward(request, response);
        }
    }

    @Test
    @DisplayName("#15 POST - Successful registration")
    void testDoPost_successfulRegistration() throws Exception {
        when(request.getParameter("email")).thenReturn("new@email.com");
        when(request.getParameter("firstName")).thenReturn("John");
        when(request.getParameter("lastName")).thenReturn("Doe");
        when(request.getParameter("phone")).thenReturn(null);
        when(request.getParameter("password")).thenReturn("StrongPass1!");
        when(request.getParameter("confirmPassword")).thenReturn("StrongPass1!");

        try (MockedStatic<DatabaseConfig> db = mockStatic(DatabaseConfig.class)) {
            db.when(DatabaseConfig::getConnection).thenReturn(connection);
            when(connection.prepareStatement(anyString())).thenReturn(statement);
            
            when(statement.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(true);
            when(resultSet.getInt(1)).thenReturn(0);
            
            when(statement.executeUpdate()).thenReturn(1);

            servlet.doPost(request, response);

            verify(response).sendRedirect("login?registrationSuccess=true");
        }
    }
}
