<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Fortuna Lotto - Set Password</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/lottery-style.css?v=<%= System.currentTimeMillis() %>">
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@400;600;700&family=Poppins:wght@400;500;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css?v=<%= System.currentTimeMillis() %>">
    
</head>

<body class="form-page">
    <div class="form-container">
        <h2>Set Your Password</h2>
        
        <div class="oauth-info">
            <p>Welcome! You've logged in with your <strong>${sessionScope.oauthProvider}</strong> account.</p>
            <p>Please set a password for future logins.</p>
        </div>
        
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
        
        <form action="setPassword" method="post">
            <div class="form-group">
                <label for="newPassword">New Password:</label>
                <input type="password" id="newPassword" name="newPassword" required>
            </div>

            <div class="form-group">
                <label for="confirmPassword">Confirm Password:</label>
                <input type="password" id="confirmPassword" name="confirmPassword" required>
            </div>

            <div class="button-group">
                <button type="submit" class="btn lottery-btn">Set Password</button>
            </div>
        </form>
        
        <div class="button-group">
            <form action="logout" method="post" style="margin: 0;">
                <button type="submit" class="btn lottery-btn">Logout</button>
            </form>
        </div>
    </div>

</body>
</html>
