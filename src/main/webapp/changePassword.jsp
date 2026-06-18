<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Fortuna Lotto - Change Password</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/lottery-style.css?v=<%= System.currentTimeMillis() %>">
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@400;600;700&family=Poppins:wght@400;500;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css?v=<%= System.currentTimeMillis() %>">
    
</head>

<body class="form-page">
    <div class="form-container">
        <h2>Change Password</h2>
        
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
            <input type="hidden" name="action" value="changeOwnPassword">

            <div class="form-group">
                <label for="currentPassword">Current Password:</label>
                <input type="password" id="currentPassword" name="currentPassword" required>
            </div>

            <div class="form-group">
                <label for="newPassword">New Password:</label>
                <input type="password" id="newPassword" name="newPassword" required>
            </div>

            <div class="form-group">
                <label for="confirmPassword">Confirm New Password:</label>
                <input type="password" id="confirmPassword" name="confirmPassword" required>
            </div>

            <div class="button-group">
                <button type="submit" class="btn lottery-btn">Change Password</button>
            </div>
        </form>
        
        <div class="button-group">
            <form action="changePassword" method="post" style="margin: 0;">
                <input type="hidden" name="action" value="cancel">
                <button type="submit" class="btn lottery-btn">Cancel</button>
            </form>

    </div>

</body>
</html>

