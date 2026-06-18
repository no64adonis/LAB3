<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Fortuna Lotto - Reset User Password</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/lottery-style.css?v=<%= System.currentTimeMillis() %>">
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@400;600;700&family=Poppins:wght@400;500;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css?v=<%= System.currentTimeMillis() %>">
    
</head>

<body class="form-page">
    <div class="form-container">
        <h2>Reset User Password</h2>
        
        <c:if test="${not empty errorMessage}">
            <div class="message error">
                ${errorMessage}
            </div>
        </c:if>
        
        <c:if test="${not empty successMessage}">
            <div class="message success">
                ${successMessage}
            </div>
        </c:if>
        
        <form action="changePassword" method="post">
            <input type="hidden" name="action" value="resetUserPassword">
            <input type="hidden" name="userId" value="${resetUserId}">
            
            <div class="form-group">
                <label for="newPassword">New Password:</label>
                <input type="password" id="newPassword" name="newPassword" required>
            </div>
            
            <div class="form-group">
                <label for="confirmPassword">Confirm New Password:</label>
                <input type="password" id="confirmPassword" name="confirmPassword" required>
            </div>
            
            <button type="submit" class="btn lottery-btn">Reset Password</button>
            <a href="userManagement" class="btn lottery-btn" style="margin-top: 10px;">Cancel</a>
        </form>
        
        <div class="text-center mt-20">
            <a href="userManagement">Back to User Management</a>
        </div>
    </div>
</body>
</html>

