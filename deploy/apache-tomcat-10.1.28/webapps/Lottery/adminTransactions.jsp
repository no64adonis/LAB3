<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page import="java.util.Enumeration" %>

<!DOCTYPE html>
<html>

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Fortuna Lotto - Transactions Management</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/variables.css?v=<%= System.currentTimeMillis() %>">
    <link rel="stylesheet"
        href="${pageContext.request.contextPath}/assets/css/lottery-style.css?v=<%= System.currentTimeMillis() %>">
    <link rel="stylesheet"
        href="${pageContext.request.contextPath}/assets/css/adminShared.css?v=<%= System.currentTimeMillis() %>">
    <link rel="stylesheet"
        href="${pageContext.request.contextPath}/assets/css/adminTransactions.css?v=<%= System.currentTimeMillis() %>">
    <link
        href="https://fonts.googleapis.com/css2?family=Montserrat:wght@400;600;700&family=Poppins:wght@400;500;600&display=swap"
        rel="stylesheet">
    <link rel="stylesheet"
        href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css?v=<%= System.currentTimeMillis() %>">

    
    <link rel="stylesheet"
        href="${pageContext.request.contextPath}/assets/components/DatePicker/flatpickr.min.css?v=<%= System.currentTimeMillis() %>">
    <link rel="stylesheet"
        href="${pageContext.request.contextPath}/assets/components/DatePicker/datepicker-theme.css?v=<%= System.currentTimeMillis() %>">
    <script
        src="${pageContext.request.contextPath}/assets/components/DatePicker/flatpickr.min.js?v=<%= System.currentTimeMillis() %>"></script>
    <script
        src="${pageContext.request.contextPath}/assets/components/DatePicker/datepicker-init.js?v=<%= System.currentTimeMillis() %>"></script>
</head>

<body class="admin-page">
    <div class="dashboard-container">
        <jsp:include page="/WEB-INF/fragments/adminHeader.jsp">
            <jsp:param name="pageTitle" value="Transactions Management"/>
        </jsp:include>

        <div class="content-wrapper">
            <div class="main-content">
                <div class="sidebar">
                    <jsp:include page="/WEB-INF/fragments/adminSidebar.jsp" />
                </div>
                <div class="container">

                    <div class="main-content-inner">
                        <c:if test="${user.role == 'admin'}">

                            <c:if test="${not empty sessionScope.message}">
                                <div class="message success">${sessionScope.message}</div>
                                <% session.removeAttribute("message"); %>
                            </c:if>
                            <c:if test="${not empty sessionScope.error}">
                                <div class="message error">${sessionScope.error}</div>
                                <% session.removeAttribute("error"); %>
                            </c:if>

                            
                            <div class="search-box">
                                <h3>Search Transactions</h3>
                                <form method="get" action="adminTransactions" id="searchForm"
                                    class="search-form">

                                    <div class="transactions-form-container" style="display: flex; flex-wrap: wrap; gap: 15px; align-items: flex-end;">
                                        
                                        <div class="form-group" style="min-width: 200px;">
                                            <label for="email">User Email</label>
                                            <input type="text" name="email" id="email"
                                                value="${searchEmail}"
                                                placeholder="Search by email..."
                                                style="width: 100%; padding: 10px; border: 1px solid #3a3f5a; border-radius: 8px; background-color: #1a1a2e; color: #ffffff;">
                                        </div>

                                        
                                        <div class="form-group" style="min-width: 150px;">
                                            <label for="startDate">Start Date</label>
                                            <input type="text" name="startDate" id="startDate"
                                                value="${startDate}"
                                                placeholder="YYYY-MM-DD" class="date-picker"
                                                style="width: 100%; padding: 10px; border: 1px solid #3a3f5a; border-radius: 8px; background-color: #1a1a2e; color: #ffffff;">
                                        </div>

                                        <div class="form-group" style="min-width: 150px;">
                                            <label for="endDate">End Date</label>
                                            <input type="text" name="endDate" id="endDate"
                                                value="${endDate}"
                                                placeholder="YYYY-MM-DD" class="date-picker"
                                                style="width: 100%; padding: 10px; border: 1px solid #3a3f5a; border-radius: 8px; background-color: #1a1a2e; color: #ffffff;">
                                        </div>

                                        
                                        <div style="display: flex; gap: 10px;">
                                            <button type="submit"
                                                class="btn btn-emerald lottery-btn">Search</button>
                                            <button type="button"
                                                class="btn lottery-btn"
                                                onclick="resetForm()">Reset</button>
                                        </div>
                                    </div>
                                </form>
                            </div>

                            
                            <div class="search-box">
                            <div style="padding: 10px 0; color: #a0a0b0; font-size: 0.9em;">
                                Showing ${fn:length(transactions)} of ${totalTransactions} transactions
                                (Page ${currentPage} of ${totalPages > 0 ? totalPages : 1})
                            </div>

                            
                            <table class="ticket-table">
                                <thead>
                                    <tr>
                                        <th>ID</th>
                                        <th>User Email</th>
                                        <th>Amount</th>
                                        <th>Payment Method</th>
                                        <th>Date (GMT+7)</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:choose>
                                        <c:when test="${not empty transactions}">
                                            <c:forEach var="txn" items="${transactions}">
                                                <tr>
                                                    <td data-label="ID">${txn.transactionId}</td>
                                                    <td data-label="User Email">${txn.userEmail}</td>
                                                    <td data-label="Amount">
                                                        <fmt:formatNumber value="${txn.amount}"
                                                            type="currency" />
                                                    </td>
                                                    <td data-label="Payment Method">
                                                        <c:choose>
                                                            <c:when
                                                                test="${not empty txn.lastFourDigits}">
                                                                **** ${txn.lastFourDigits}
                                                                <c:if
                                                                    test="${not empty txn.cardHolder}">
                                                                    (${txn.cardHolder})
                                                                </c:if>
                                                            </c:when>
                                                            <c:otherwise>
                                                                N/A
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                    <td data-label="Date (GMT+7)">${txn.formattedTransactionDate}</td>
                                                </tr>
                                            </c:forEach>
                                        </c:when>
                                        <c:otherwise>
                                            <tr>
                                                <td colspan="5"
                                                    style="text-align: center; padding: 20px; color: #a0a0b0;">
                                                    No transactions found.
                                                </td>
                                            </tr>
                                        </c:otherwise>
                                    </c:choose>
                                </tbody>
                            </table>

                            <% StringBuilder baseUrl = new StringBuilder("adminTransactions?");
                                Enumeration<String> paramNames = request.getParameterNames();
                                while (paramNames.hasMoreElements()) {
                                    String name = paramNames.nextElement();
                                    if (!name.equals("page")) {
                                        String[] values = request.getParameterValues(name);
                                        for (String value : values) {
                                            if (value != null && !value.isEmpty()) {
                                                baseUrl.append(name).append("=").append(java.net.URLEncoder.encode(value, "UTF-8")).append("&");
                                            }
                                        }
                                    }
                                }
                                String base = baseUrl.toString();
                                if (base.endsWith("&") || base.endsWith("?")) {
                                    base = base.substring(0, base.length() - 1);
                                }
                                request.setAttribute("computedBaseUrl", base);
                            %>
                            <c:set var="baseUrl" value="${computedBaseUrl}" scope="request" />
                            <jsp:include page="/WEB-INF/fragments/pagination.jsp" />
                            </div> 

                        </c:if>
                    </div> 
                </div> 
            </div> 
        </div> 
    </div> 

    <script>
        function resetForm() {
            document.getElementById('email').value = '';
            document.getElementById('startDate').value = '';
            document.getElementById('endDate').value = '';

            
            const startPicker = document.getElementById('startDate')._flatpickr;
            const endPicker = document.getElementById('endDate')._flatpickr;
            if (startPicker) startPicker.clear();
            if (endPicker) endPicker.clear();

            
            window.location.href = 'adminTransactions';
        }
    </script>
</body>

</html>

