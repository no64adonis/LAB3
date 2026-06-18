<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Fortuna Lotto - Login</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/lottery-style.css?v=<%= System.currentTimeMillis() %>">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/login.css?v=<%= System.currentTimeMillis() %>">
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@400;600;700&family=Poppins:wght@400;500;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css?v=<%= System.currentTimeMillis() %>">
</head>

<body class="form-page">
    <div class="form-container">
        <h2>Login</h2>

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

        <form action="login" method="post">
            <div class="form-group">
                <label for="email">Email Address:</label>
                <input type="email" id="email" name="email" required>
            </div>

            <div class="form-group">
                <label for="password">Password:</label>
                <input type="password" id="password" name="password" required>
            </div>

            <div style="display: flex; gap: 10px; margin-top: 15px;">
                <button type="submit" class="btn lottery-btn" style="flex: 1;">Login</button>
                <a href="register.jsp" class="btn lottery-btn" style="flex: 1; text-align: center; display: flex; align-items: center; justify-content: center;">Register</a>
            </div>
        </form>

        <div class="text-center mt-20">
            <p><a href="forgotPassword.jsp">Forgot Password?</a></p>
        </div>

        <div class="text-center mt-20">
            <p>Or login with:</p>
            <div style="display: flex; justify-content: center; gap: 15px; margin-top: 10px;">
                <% String googleRedirectUri=com.lottery.util.OAuthConfig.getInstance().getGoogleRedirectUri(); if (googleRedirectUri==null) { googleRedirectUri="" ; } %>
                    <a href="https://accounts.google.com/o/oauth2/v2/auth?client_id=<%= com.lottery.util.OAuthConfig.getInstance().getGoogleClientId() %>&redirect_uri=<%= java.net.URLEncoder.encode(googleRedirectUri, "UTF-8" ) %>&response_type=code&scope=email%20profile"
                        class="btn lottery-btn" style="background-color: #4285F4; color: white; border: none;">
                        <i class="fab fa-google"></i> Google
                    </a>
            </div>
        </div>
    </div>
</body>

</html>
