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
    <title>Fortuna Lotto - Top Up Balance</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/variables.css?v=<%= System.currentTimeMillis() %>">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/lottery-style.css?v=<%= System.currentTimeMillis() %>">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/userShared.css?v=<%= System.currentTimeMillis() %>">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/topup.css?v=<%= System.currentTimeMillis() %>">
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@400;600;700&family=Poppins:wght@400;500;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css?v=<%= System.currentTimeMillis() %>">
    <script src="${pageContext.request.contextPath}/assets/js/custom-select.js?v=<%= System.currentTimeMillis() %>"></script>
</head>

<body class="dashboard-page">
    <div class="dashboard-container">
        <jsp:include page="/WEB-INF/fragments/userHeader.jsp">
            <jsp:param name="pageTitle" value="Top Up Balance"/>
        </jsp:include>

        <div class="content-wrapper">
            <div class="main-content">
                <div class="sidebar">
                    <jsp:include page="/WEB-INF/fragments/sidebar.jsp" />
                </div>
                        <div class="container">
                            <c:if test="${not empty sessionScope.message}">
                                <div class="message ${sessionScope.messageType}">${sessionScope.message}</div>
                                <% session.removeAttribute("message"); %>
                                    <% session.removeAttribute("messageType"); %>
                            </c:if>

                            <div class="card">
                                <h2>Your Current Balance: $${user.balance}</h2>
                            </div>

                            <c:if test="${not empty paymentMethods}">
                                <div class="card">
                                    <h2>Select Top Up Amount</h2>
                                    <form method="post" action="topup">
                                        <input type="hidden" name="action" value="topup">
                                        <div class="form-group">
                                            <label for="paymentMethod">Payment Method</label>
                                            <div class="custom-select-container" style="width: 100%;">
                                                <input type="hidden" name="paymentMethod" value="" required>
                                                <div class="custom-select-header" onclick="toggleCustomSelect(this, event)">
                                                    <span class="custom-select-text">Select a payment method</span>
                                                    <i class="fas fa-chevron-down dropdown-icon"></i>
                                                </div>
                                                <ul class="custom-select-list">
                                                    <li class="custom-select-item" data-value="" onclick="selectCustomItem(this, event)">Select a payment method</li>
                                                    <c:forEach var="payment" items="${paymentMethods}">
                                                        <li class="custom-select-item" data-value="${payment.id}" onclick="selectCustomItem(this, event)">**** **** **** ${payment.lastFourDigits} (${payment.cardHolder})</li>
                                                    </c:forEach>
                                                </ul>
                                            </div>
                                        </div>
                                        <div class="amount-btn-container">
                                            <c:forEach var="amount" items="${[1, 5, 10, 20, 50, 100, 200]}">
                                                <button type="submit" name="amount" value="${amount}"
                                                    class="btn lottery-btn amount-btn">$${amount}</button>
                                            </c:forEach>
                                        </div>
                                    </form>
                                </div>
                            </c:if>

                            <c:if test="${empty paymentMethods}">
                                <div class="card">
                                    <h2>No Payment Methods</h2>
                                    <p>You need to add a payment method before you can top up your balance.</p>
                                    <a href="payments" class="btn lottery-btn">Add Payment Method</a>
                                </div>
                            </c:if>

                            <c:if test="${not empty paymentMethods}">
                                <div class="card">
                                    <h2>Select Payment Method</h2>
                                    <form method="post" action="topup">
                                        <input type="hidden" name="action" value="topupCustom">
                                        <div class="form-group">
                                            <label for="amount">Custom Amount ($)</label>
                                            <input type="number" id="amount" name="amount" min="1" step="0.01"
                                                placeholder="Enter amount" required>
                                        </div>
                                        <div class="form-group">
                                            <label for="paymentMethod">Payment Method</label>
                                            <div class="custom-select-container" style="width: 100%;">
                                                <input type="hidden" name="paymentMethod" value="" required>
                                                <div class="custom-select-header" onclick="toggleCustomSelect(this, event)">
                                                    <span class="custom-select-text">Select a payment method</span>
                                                    <i class="fas fa-chevron-down dropdown-icon"></i>
                                                </div>
                                                <ul class="custom-select-list">
                                                    <li class="custom-select-item" data-value="" onclick="selectCustomItem(this, event)">Select a payment method</li>
                                                    <c:forEach var="payment" items="${paymentMethods}">
                                                        <li class="custom-select-item" data-value="${payment.id}" onclick="selectCustomItem(this, event)">**** **** **** ${payment.lastFourDigits} (${payment.cardHolder})</li>
                                                    </c:forEach>
                                                </ul>
                                            </div>
                                        </div>
                                        <button type="submit" class="btn lottery-btn">Top Up</button>
                                    </form>
                                </div>
                            </c:if>

                            
                            <div class="card" style="padding: 20px;">
                                <h2>Transaction History</h2>

                                <c:if test="${not empty transactions}">
                                    <table class="ticket-table">
                                        <thead>
                                            <tr>
                                                <th>Amount</th>
                                                <th>Payment Method</th>
                                                <th>Date (GMT+7)</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach var="txn" items="${transactions}">
                                                <tr>
                                                    <td>
                                                        <fmt:formatNumber value="${txn.amount}" type="currency" />
                                                    </td>
                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${not empty txn.lastFourDigits}">
                                                                **** **** **** ${txn.lastFourDigits}
                                                                <c:if test="${not empty txn.cardHolder}">
                                                                    (${txn.cardHolder})
                                                                </c:if>
                                                            </c:when>
                                                            <c:otherwise>
                                                                N/A
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                    <td>${txn.formattedTransactionDate}</td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>

                                    <% StringBuilder baseUrl = new StringBuilder("topup?");
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
                                </c:if>

                                <c:if test="${empty transactions}">
                                    <p style="color: #a0a0b0; text-align: center; padding: 20px;">No transactions yet.</p>
                                </c:if>
                            </div>

                        </div> 
                    </div> 
                </div> 
            </div> 
        </body>

        </html>
