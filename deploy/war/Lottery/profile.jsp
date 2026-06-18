<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.lottery.model.User" %>
<%@ page import="jakarta.servlet.http.HttpSession" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%
    
    if (request.getAttribute("user") == null && session.getAttribute("user") != null) {
        request.setAttribute("user", session.getAttribute("user"));
    }
    
    User user = (User) session.getAttribute("user");
    String successMessage = (String) session.getAttribute("successMessage");
    String errorMessage = (String) session.getAttribute("errorMessage");
    String pendingEmail = (String) session.getAttribute("pendingEmail");
    
    
    if (successMessage != null) session.removeAttribute("successMessage");
    if (errorMessage != null) session.removeAttribute("errorMessage");
    
    String displayName = (user != null && user.getDisplayName() != null) ? user.getDisplayName() :
                        (user != null ? user.getFirstName() + " " + user.getLastName() : "");
%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>My Profile - Lottery</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/variables.css?v=<%= System.currentTimeMillis() %>">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/lottery-style.css?v=<%= System.currentTimeMillis() %>">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/userShared.css?v=<%= System.currentTimeMillis() %>">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/userLottery.css?v=<%= System.currentTimeMillis() %>">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/profile.css?v=<%= System.currentTimeMillis() %>">
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@400;600;700&family=Poppins:wght@400;500;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css?v=<%= System.currentTimeMillis() %>">
</head>
<body class="dashboard-page">
    <div class="dashboard-container">
        <jsp:include page="/WEB-INF/fragments/userHeader.jsp">
            <jsp:param name="pageTitle" value="My Profile"/>
        </jsp:include>

        <div class="content-wrapper">
            <div class="main-content">
                <div class="sidebar">
                    <jsp:include page="/WEB-INF/fragments/sidebar.jsp" />
                </div>
                
                <div class="container">
                    <div class="profile-container">
                        
                        <% if (successMessage != null) { %>
                            <div class="message success"><%= successMessage %></div>
                        <% } %>
                        
                        <% if (errorMessage != null) { %>
                            <div class="message error"><%= errorMessage %></div>
                        <% } %>
                    
                        
                        <div class="profile-section">
                            <h2>Name</h2>
                            <form action="profile" method="post">
                                <input type="hidden" name="action" value="updateName">
                                <div class="form-group">
                                    <label for="firstName">First Name</label>
                                    <input type="text" id="firstName" name="firstName" 
                                           value="<%= user != null && user.getFirstName() != null ? user.getFirstName() : "" %>" maxlength="50" required>
                                </div>
                                <div class="form-group">
                                    <label for="lastName">Last Name</label>
                                    <input type="text" id="lastName" name="lastName" 
                                           value="<%= user != null && user.getLastName() != null ? user.getLastName() : "" %>" maxlength="50" required>
                                </div>
                                <button type="submit" class="btn">Update Name</button>
                            </form>
                        </div>
                    
                        
                        <div class="profile-section">
                            <h2>Email Address</h2>
                            <p>Current email: <strong><%= user != null && user.getEmail() != null && !user.getEmail().isEmpty() 
                                ? user.getEmail() 
                                : "No email set (logged in via Google)" %></strong></p>
                            
                            <form action="profile" method="post">
                                <input type="hidden" name="action" value="requestEmailChange">
                                <div class="form-group">
                                    <label for="newEmail">New Email Address</label>
                                    <input type="email" id="newEmail" name="newEmail" required>
                                </div>
                                <button type="submit" class="btn">Send Verification Code</button>
                            </form>
                            
                            <% if (pendingEmail != null) { %>
                            <div class="verification-section">
                                <p>A verification code was sent to <strong><%= pendingEmail %></strong></p>
                                <form action="profile" method="post">
                                    <input type="hidden" name="action" value="verifyEmailCode">
                                    <div class="form-group">
                                        <label for="verificationCode">Verification Code</label>
                                        <input type="text" id="verificationCode" name="verificationCode" 
                                               placeholder="Enter 6-digit code" maxlength="6" required>
                                    </div>
                                    <button type="submit" class="btn">Verify & Update Email</button>
                                </form>
                            </div>
                            <% } %>
                        </div>
                    
                        
                        <div class="profile-section">
                            <h2>Change Password</h2>
                            <form action="profile" method="post">
                                <input type="hidden" name="action" value="changePassword">
                                <div class="form-group">
                                    <label for="currentPassword">Current Password</label>
                                    <input type="password" id="currentPassword" name="currentPassword" required>
                                </div>
                                <div class="form-group">
                                    <label for="newPassword">New Password</label>
                                    <input type="password" id="newPassword" name="newPassword" required>
                                    <p class="password-hint">Min 8 characters. Must include: uppercase, lowercase, number, and special character (!@#$%^&* etc).</p>
                                </div>
                                <div class="form-group">
                                    <label for="confirmPassword">Confirm New Password</label>
                                    <input type="password" id="confirmPassword" name="confirmPassword" required>
                                </div>
                                <button type="submit" class="btn">Change Password</button>
                            </form>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</body>
</html>

