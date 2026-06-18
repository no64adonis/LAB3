package com.lottery;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.lottery.db.DatabaseConfig;
import com.lottery.util.InputValidator;
import com.lottery.util.PasswordValidator;
public class RegisterServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.getRequestDispatcher("/register.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = request.getParameter("email");
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String phone = request.getParameter("phone");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        if (email != null) {
            email = InputValidator.sanitizeString(email);
        }
        if (firstName != null) {
            firstName = InputValidator.sanitizeString(firstName);
        }
        if (lastName != null) {
            lastName = InputValidator.sanitizeString(lastName);
        }
        if (phone != null) {
            phone = InputValidator.sanitizeString(phone);
        }
        if (password != null) {
            password = InputValidator.sanitizeString(password);
        }
        if (confirmPassword != null) {
            confirmPassword = InputValidator.sanitizeString(confirmPassword);
        }

        if ((email != null && InputValidator.containsSQLInjectionPatterns(email)) ||
                (firstName != null && InputValidator.containsSQLInjectionPatterns(firstName)) ||
                (lastName != null && InputValidator.containsSQLInjectionPatterns(lastName)) ||
                (phone != null && InputValidator.containsSQLInjectionPatterns(phone)) ||
                (password != null && InputValidator.containsSQLInjectionPatterns(password)) ||
                (confirmPassword != null && InputValidator.containsSQLInjectionPatterns(confirmPassword))) {
            request.setAttribute("errorMessage", "Invalid input values.");
            request.getRequestDispatcher("/register.jsp").forward(request, response);
            return;
        }

        if (email == null || email.trim().isEmpty()) {
            request.setAttribute("errorMessage", "Email is required");
            request.getRequestDispatcher("/register.jsp").forward(request, response);
            return;
        }

        if (!InputValidator.isValidEmail(email)) {
            request.setAttribute("errorMessage", "Invalid email format.");
            request.getRequestDispatcher("/register.jsp").forward(request, response);
            return;
        }

        if (!InputValidator.isAuthenticEmail(email)) {
            request.setAttribute("errorMessage", "Email address does not appear to be valid.");
            request.getRequestDispatcher("/register.jsp").forward(request, response);
            return;
        }

        if (firstName == null || firstName.trim().isEmpty()) {
            request.setAttribute("errorMessage", "First name is required");
            request.getRequestDispatcher("/register.jsp").forward(request, response);
            return;
        }

        if (lastName == null || lastName.trim().isEmpty()) {
            request.setAttribute("errorMessage", "Last name is required");
            request.getRequestDispatcher("/register.jsp").forward(request, response);
            return;
        }

        if (password == null || password.trim().isEmpty()) {
            request.setAttribute("errorMessage", "Password is required");
            request.getRequestDispatcher("/register.jsp").forward(request, response);
            return;
        }

        String passwordError = PasswordValidator.validate(password);
        if (passwordError != null) {
            request.setAttribute("errorMessage", passwordError);
            request.getRequestDispatcher("/register.jsp").forward(request, response);
            return;
        }

        if (!password.equals(confirmPassword)) {
            request.setAttribute("errorMessage", "Passwords do not match");
            request.getRequestDispatcher("/register.jsp").forward(request, response);
            return;
        }

        if (isEmailExists(email)) {
            request.setAttribute("errorMessage", "Email already registered. Please use a different email.");
            request.getRequestDispatcher("/register.jsp").forward(request, response);
            return;
        }

        String hashedPassword = hashPassword(password);
        if (hashedPassword == null) {
            request.setAttribute("errorMessage", "Error processing password. Please try again.");
            request.getRequestDispatcher("/register.jsp").forward(request, response);
            return;
        }

        if (insertUser(email, hashedPassword, email, firstName, lastName, phone)) {
            
            response.sendRedirect("login?registrationSuccess=true");
        } else {
            request.setAttribute("errorMessage", "Registration failed. Please try again.");
            request.getRequestDispatcher("/register.jsp").forward(request, response);
        }

    }

    private boolean isEmailExists(String email) {
        String query = "SELECT COUNT(*) FROM Users WHERE Email = ?";

        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, email);
            var resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean insertUser(String emailForUsername, String hashedPassword, String email,
            String firstName, String lastName, String phone) {
        String query = "INSERT INTO Users (PasswordHash, Email, FirstName, LastName, Phone, Role, CreationDate, IsActive, password_set) VALUES (?, ?, ?, ?, ?, ?, GETDATE(), ?, ?)";

        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, hashedPassword);
            statement.setString(2, email);
            statement.setString(3, firstName);
            statement.setString(4, lastName);
            statement.setString(5, phone);
            statement.setString(6, "user"); 
            statement.setBoolean(7, true); 
            statement.setBoolean(8, true); 

            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}
