<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
        <%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
            <%@ page import="com.lottery.model.PaymentMethod" %>
                <%@ page import="java.util.List" %>

                    <!DOCTYPE html>
                    <html>

                    <head>
                        <meta charset="UTF-8">
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        <title>Fortuna Lotto - Payment Method Management</title>
                        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/variables.css?v=<%= System.currentTimeMillis() %>">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/lottery-style.css?v=<%= System.currentTimeMillis() %>">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/adminShared.css?v=<%= System.currentTimeMillis() %>">
                        <link rel="stylesheet"
                            href="${pageContext.request.contextPath}/assets/css/paymentMethodManagement.css?v=<%= System.currentTimeMillis() %>">
                        <link
                            href="https://fonts.googleapis.com/css2?family=Montserrat:wght@400;600;700&family=Poppins:wght@400;500;600&display=swap"
                            rel="stylesheet">
                        <link rel="stylesheet"
                            href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css?v=<%= System.currentTimeMillis() %>">
                    </head>

                    <body class="admin-page">
                        <div class="dashboard-container">
                            <jsp:include page="/WEB-INF/fragments/adminHeader.jsp">
                                <jsp:param name="pageTitle" value="Payment Method Management"/>
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

                                        <div class="search-box"
                                            style="background: linear-gradient(135deg, #2a2c42, #1e2030); border: 1px solid #3a3f5a; border-radius: 15px; padding: 30px; margin: 20px 0; box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);">
                                            <h3>Payment Methods (${totalPaymentMethods} total)</h3>
                                            <form method="post" action="paymentMethodManagement"
                                                style="display: inline;">
                                                <input type="hidden" name="action" value="bulkDelete">
                                                <button type="submit" class="btn btn-danger lottery-btn"
                                                    style="margin-right: 10px;">Delete Selected</button>
                                            </form>
                                        </div>

                                        <div class="user-table-container"
                                            style="background: linear-gradient(135deg, #2a2c42, #1e2030); border: 1px solid #3a3f5a; border-radius: 15px; padding: 30px; margin: 20px 0; box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);">
                                            <table class="payment-method-table">
                                                <thead>
                                                    <tr>
                                                        <th><input type="checkbox" id="selectAll"></th>
                                                        <th>ID</th>
                                                        <th>User ID</th>
                                                        <th>Last Four Digits</th>
                                                        <th>Card Holder</th>
                                                        <th>Expiry Date</th>
                                                        <th>Actions</th>
                                                    </tr>
                                                </thead>
                                                <tbody>
                                                    <c:forEach var="paymentMethod" items="${paymentMethods}">
                                                        <tr>
                                                            <td data-label="Select"><input type="checkbox" name="selectedPaymentMethods"
                                                                    value="${paymentMethod.id}" form="bulkForm"></td>
                                                            <td data-label="ID">${paymentMethod.id}</td>
                                                            <td data-label="User ID">${paymentMethod.userId}</td>
                                                            <td data-label="Last Four Digits">${paymentMethod.lastFourDigits}</td>
                                                            <td data-label="Card Holder">${paymentMethod.cardHolder}</td>
                                                            <td data-label="Expiry Date">${paymentMethod.expiryDate}</td>
                                                            <td data-label="Actions" class="actions">
                                                                <div class="actions-container">
                                                                    <div class="actions-dropdown">
                                                                        <div class="actions-header" onclick="this.closest('.actions-container').classList.toggle('active')">
                                                                            Actions <i class="fas fa-chevron-down dropdown-icon"></i>
                                                                        </div>
                                                                        <div class="actions-menu">
                                                                            <button type="button" class="action-item" onclick="openEditModal('${paymentMethod.id}', '${paymentMethod.lastFourDigits}', '${paymentMethod.cardHolder}', '${paymentMethod.expiryDate}')">
                                                                                <i class="fas fa-edit"></i> Edit
                                                                            </button>
                                                                            <form method="post" action="paymentMethodManagement" style="display: flex; width: 100%;">
                                                                                <input type="hidden" name="action" value="deletePaymentMethod">
                                                                                <input type="hidden" name="paymentMethodId" value="${paymentMethod.id}">
                                                                                <button type="submit" class="action-item" onclick="return confirm('Are you sure you want to delete this payment method?')">
                                                                                    <i class="fas fa-trash"></i> Delete
                                                                                </button>
                                                                            </form>
                                                                        </div>
                                                                    </div>
                                                                </div>
                                                            </td>
                                                        </tr>
                                                    </c:forEach>
                                                </tbody>
                                            </table>

                                            <form id="bulkForm" method="post" action="paymentMethodManagement">
                                                <input type="hidden" name="action" value="bulkDelete">
                                            </form>

                                            <% StringBuilder baseUrl = new StringBuilder("paymentMethodManagement?");
                                                java.util.Enumeration<String> paramNames = request.getParameterNames();
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

                                        <c:if test="${empty paymentMethods}">
                                            <div class="user-table-container"
                                                style="background: linear-gradient(135deg, #2a2c42, #1e2030); border: 1px solid #3a3f5a; border-radius: 15px; padding: 30px; margin: 20px 0; box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);">
                                                <p>No payment methods found.</p>
                                            </div>
                                        </c:if>
                                    </div> 
                                </div> 
                            </div> 
                        </div> 

                            
                            <div id="editModal" class="modal">
                                <div class="modal-content">
                                    <span class="close" onclick="closeEditModal()">&times;</span>
                                    <h3>Edit Payment Method</h3>
                                    <form method="post" action="paymentMethodManagement">
                                        <input type="hidden" name="action" value="updatePaymentMethod">
                                        <input type="hidden" id="editPaymentMethodId" name="paymentMethodId">
                                        <div class="form-group">
                                            <label for="editLastFourDigits">Last Four Digits:</label>
                                            <input type="text" id="editLastFourDigits" name="lastFourDigits" required>
                                        </div>
                                        <div class="form-group">
                                            <label for="editCardHolder">Card Holder:</label>
                                            <input type="text" id="editCardHolder" name="cardHolder" required>
                                        </div>
                                        <div class="form-group">
                                            <label for="editExpiryDate">Expiry Date:</label>
                                            <input type="text" id="editExpiryDate" name="expiryDate" placeholder="MM/YY"
                                                required>
                                        </div>
                                        <div class="form-actions">
                                            <button type="submit" class="btn btn-primary lottery-btn">Update</button>
                                            <button type="button" class="btn btn-danger lottery-btn"
                                                onclick="closeEditModal()">Cancel</button>
                                        </div>
                                    </form>
                                </div>
                            </div>

                            <script>
                                window.addEventListener('DOMContentLoaded', function () {
                                    
                                    const elements = document.querySelectorAll('button, .btn, .pagination a');
                                    elements.forEach(element => {
                                        if (!element.classList.contains('lottery-btn')) {
                                            element.classList.add('lottery-btn');
                                        }
                                    });

                                    
                                    document.getElementById('selectAll').addEventListener('change', function () {
                                        const checkboxes = document.querySelectorAll('input[name="selectedPaymentMethods"]');
                                        checkboxes.forEach(checkbox => {
                                            checkbox.checked = this.checked;
                                        });
                                    });
                                });

                                
                                function openEditModal(id, lastFourDigits, cardHolder, expiryDate) {
                                    document.getElementById('editPaymentMethodId').value = id;
                                    document.getElementById('editLastFourDigits').value = lastFourDigits;
                                    document.getElementById('editCardHolder').value = cardHolder;
                                    document.getElementById('editExpiryDate').value = expiryDate;
                                    document.getElementById('editModal').style.display = 'block';
                                }

                                function closeEditModal() {
                                    document.getElementById('editModal').style.display = 'none';
                                }

                                
                                window.onclick = function (event) {
                                    const modal = document.getElementById('editModal');
                                    if (event.target == modal) {
                                        closeEditModal();
                                    }
                                }
                            </script>
                    </body>

                    </html>
