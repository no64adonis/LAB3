<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Fortuna Lotto - Payment Methods</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/variables.css?v=<%= System.currentTimeMillis() %>">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/lottery-style.css?v=<%= System.currentTimeMillis() %>">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/userShared.css?v=<%= System.currentTimeMillis() %>">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/payments.css?v=<%= System.currentTimeMillis() %>">
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@400;600;700&family=Poppins:wght@400;500;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css?v=<%= System.currentTimeMillis() %>">
    
</head>
<body class="dashboard-page">
    <div class="dashboard-container">
        <jsp:include page="/WEB-INF/fragments/userHeader.jsp">
            <jsp:param name="pageTitle" value="Payment Methods"/>
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
                    <h2>Add New Payment Method</h2>
                    <form id="paymentForm" method="post" action="payments">
                        <input type="hidden" name="action" value="addPayment">
                        <div class="form-group">
                            <label for="cardNumber">Card Number</label>
                            <input type="text" id="cardNumber" name="cardNumber" placeholder="Enter card number" required>
                        </div>
                        <div class="form-group">
                            <label for="cardHolder">Card Holder Name</label>
                            <input type="text" id="cardHolder" name="cardHolder" placeholder="Enter card holder name" required>
                        </div>
                        <div class="form-group">
                            <label for="expiryMonth">Expiry Date</label>
                            <div style="display: flex; gap: 10px;">
                                <input type="text" id="expiryMonth" name="expiryMonth" placeholder="MM" required style="flex: 1;">
                                <input type="text" id="expiryYear" name="expiryYear" placeholder="YY" required style="flex: 1;">
                            </div>
                        </div>
                        <div class="form-group">
                            <label for="cvv">CVV</label>
                            <input type="text" id="cvv" name="cvv" placeholder="CVV" required>
                        </div>
                        <button type="submit" class="btn lottery-btn">Add Payment Method</button>
                    </form>
                </div>
                
                <c:if test="${not empty paymentMethods}">
                    <div class="card">
                        <h2>Your Payment Methods</h2>
                        <table class="ticket-table">
                            <thead>
                                <tr>
                                    <th>Card Number</th>
                                    <th>Card Holder</th>
                                    <th>Expiry Date</th>
                                    <th>Action</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="payment" items="${paymentMethods}">
                                    <tr>
                                        <td data-label="Card Number">**** **** **** ${payment.lastFourDigits}</td>
                                        <td data-label="Card Holder">${payment.cardHolder}</td>
                                        <td data-label="Expiry Date">${payment.expiryDate}</td>
                                        <td data-label="Action">
                                            <form method="post" action="payments" style="display: inline;">
                                                <input type="hidden" name="action" value="deletePayment">
                                                <input type="hidden" name="paymentId" value="${payment.id}">
                                                <button type="submit" class="btn lottery-btn" onclick="return confirm('Are you sure you want to delete this payment method?')">Delete</button>
                                            </form>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:if>
            </div>
        </div>
    </div>

    <script>
        document.getElementById('paymentForm').addEventListener('submit', function(e) {
            
            const cardNumber = document.getElementById('cardNumber');
            const cardHolder = document.getElementById('cardHolder');
            const expiryMonth = document.getElementById('expiryMonth');
            const expiryYear = document.getElementById('expiryYear');
            const cvv = document.getElementById('cvv');
            
            
            clearErrors();
            
            let hasErrors = false;
            
            
            if (!cardNumber.value || cardNumber.value.trim() === '') {
                showError(cardNumber, 'Card number is required.');
                hasErrors = true;
            } else if (!luhnCheck(cardNumber.value.replace(/\s+/g, ''))) {
                showError(cardNumber, 'Invalid card number. Please check your card details.');
                hasErrors = true;
            }
            
            
            if (!cardHolder.value || cardHolder.value.trim() === '') {
                showError(cardHolder, 'Card holder name is required.');
                hasErrors = true;
            }
            
            
            if (!expiryMonth.value || expiryMonth.value.trim() === '') {
                showError(expiryMonth, 'Expiry month is required.');
                hasErrors = true;
            } else if (!/^\d{2}$/.test(expiryMonth.value) || parseInt(expiryMonth.value) < 1 || parseInt(expiryMonth.value) > 12) {
                showError(expiryMonth, 'Invalid expiry month. Use MM format (01-12).');
                hasErrors = true;
            }
            
            
            if (!expiryYear.value || expiryYear.value.trim() === '') {
                showError(expiryYear, 'Expiry year is required.');
                hasErrors = true;
            } else if (!/^\d{2}$/.test(expiryYear.value)) {
                showError(expiryYear, 'Invalid expiry year. Use YY format.');
                hasErrors = true;
            }
            
            
            if (!cvv.value || cvv.value.trim() === '') {
                showError(cvv, 'CVV is required.');
                hasErrors = true;
            } else if (!/^\d{3,4}$/.test(cvv.value)) {
                showError(cvv, 'Invalid CVV. Must be 3 or 4 digits.');
                hasErrors = true;
            }
            
            
            if (hasErrors) {
                e.preventDefault();
                return false;
            }
        });
        
        function showError(element, message) {
            
            let errorElement = element.parentNode.querySelector('.error-message');
            if (!errorElement) {
                errorElement = document.createElement('div');
                errorElement.className = 'error-message';
                errorElement.style.color = '#dc3545';
                errorElement.style.fontSize = '12px';
                errorElement.style.marginTop = '5px';
                element.parentNode.appendChild(errorElement);
            }
            errorElement.textContent = message;
            
            
            element.style.borderColor = '#dc3545';
        }
        
        function clearErrors() {
            
            const errorElements = document.querySelectorAll('.error-message');
            errorElements.forEach(element => element.remove());
            
            
            const inputs = document.querySelectorAll('#paymentForm input');
            inputs.forEach(input => input.style.borderColor = '#3a3f5a');
        }
        
        function luhnCheck(cardNumber) {
            let sum = 0;
            let isEven = false;
            
            
            for (let i = cardNumber.length - 1; i >= 0; i--) {
                let digit = parseInt(cardNumber.charAt(i), 10);
                
                if (isEven) {
                    digit *= 2;
                    if (digit > 9) {
                        digit -= 9;
                    }
                }
                
                sum += digit;
                isEven = !isEven;
            }
            
            return sum % 10 === 0;
        }
    </script>
            </div>
        </div>
    </div>
</body>
</html>
