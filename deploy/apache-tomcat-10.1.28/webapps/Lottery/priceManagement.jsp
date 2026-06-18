<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page import="java.util.List" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Fortuna Lotto - Price Management</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/variables.css?v=<%= System.currentTimeMillis() %>">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/lottery-style.css?v=<%= System.currentTimeMillis() %>">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/adminShared.css?v=<%= System.currentTimeMillis() %>">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/priceManagement.css?v=<%= System.currentTimeMillis() %>">
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@400;600;700&family=Poppins:wght@400;500;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css?v=<%= System.currentTimeMillis() %>">
</head>

<body class="admin-page">
    <div class="dashboard-container">
        <jsp:include page="/WEB-INF/fragments/adminHeader.jsp">
            <jsp:param name="pageTitle" value="Price Management"/>
        </jsp:include>

        <div class="content-wrapper">
            <div class="main-content">
                <div class="sidebar" id="sidebar">
                    <jsp:include page="/WEB-INF/fragments/adminSidebar.jsp" />
                </div>

                <div class="container">
        <c:if test="${not empty sessionScope.message}">
            <div class="message success">${sessionScope.message}</div>
            <% session.removeAttribute("message"); %>
        </c:if>
        
        <c:if test="${not empty sessionScope.error}">
            <div class="message error">${sessionScope.error}</div>
            <% session.removeAttribute("error"); %>
        </c:if>
        
        <div class="price-form">
            <h3>Update Lottery Ticket Prices</h3>
            <form method="post" action="priceManagement">
                <input type="hidden" name="action" value="updatePrices">
                <c:forEach var="company" items="${companies}">
                    <div style="margin-bottom: 15px;">
                        <label for="price_${company}">${company}:</label>
                        <input type="number" id="price_${company}" name="price_${company}" 
                               value="${companyPrices[company]}" min="0" step="0.01" required>
                    </div>
                </c:forEach>
                <button type="submit" class="btn lottery-btn">Update Prices</button>
            </form>
            </div> 
        </div> 
            </div> 
        </div> 
    </div> 
</body>
</html>
