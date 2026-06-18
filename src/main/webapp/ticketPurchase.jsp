<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page import="com.lottery.model.LotteryTicket" %>
<%@ page import="java.util.List" %>
<%@ page import="java.time.LocalDate" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<!doctype html>
<html>

<head>
    <meta charset="UTF-8" />
    <meta content="width=device-width,initial-scale=1" name="viewport" />
    <title>Fortuna Lotto - Purchase Tickets</title>
    <link href="${pageContext.request.contextPath}/assets/css/variables.css?v=<%= System.currentTimeMillis() %>" rel="stylesheet" />
    <link href="${pageContext.request.contextPath}/assets/css/lottery-style.css?v=<%= System.currentTimeMillis() %>" rel="stylesheet" />
    <link href="${pageContext.request.contextPath}/assets/css/userShared.css?v=<%= System.currentTimeMillis() %>" rel="stylesheet" />
    <link rel="stylesheet"
        href="https://fonts.googleapis.com/css2?family=Montserrat:wght@400;600;700&family=Poppins:wght@400;500;600&display=swap" />
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css?v=<%= System.currentTimeMillis() %>" />
    <link href="${pageContext.request.contextPath}/assets/components/DatePicker/flatpickr.min.css?v=<%= System.currentTimeMillis() %>" rel="stylesheet" />
    <link href="${pageContext.request.contextPath}/assets/components/DatePicker/datepicker-theme.css?v=<%= System.currentTimeMillis() %>"
        rel="stylesheet" />
    <script src="${pageContext.request.contextPath}/assets/components/DatePicker/flatpickr.min.js?v=<%= System.currentTimeMillis() %>"></script>
    <script src="${pageContext.request.contextPath}/assets/components/DatePicker/datepicker-init.js?v=<%= System.currentTimeMillis() %>"></script>
    <style>
        @media (max-width: 1024px) {
            .ticket-numbers-cell .balls-wrapper {
                display: grid;
                grid-template-columns: repeat(3, min-content);
                gap: 5px 8px;
                justify-content: center;
            }
            .ticket-numbers-cell .ball-separator {
                display: none !important;
            }
            .ticket-numbers-cell .lottery-ball {
                width: 38px !important;
                height: 38px !important;
                line-height: 38px !important;
                font-size: 16px !important;
                margin: 0 !important;
            }
        }
        @media (max-width: 768px) {
            .ticket-numbers-cell .lottery-ball {
                width: 36px !important;
                height: 36px !important;
                line-height: 36px !important;
                font-size: 15px !important;
                margin: 0 !important;
            }
        }
        @media (max-width: 640px) {
            .ticket-numbers-cell .balls-wrapper {
                display: grid;
                grid-template-columns: repeat(3, min-content);
                gap: 5px 8px;
                justify-content: center;
            }
            .ticket-numbers-cell .ball-separator {
                display: none !important;
            }
            .ticket-numbers-cell .lottery-ball {
                width: 34px !important;
                height: 34px !important;
                line-height: 34px !important;
                font-size: 14px !important;
            }
            .ticket-table th:nth-child(3),
            .ticket-table td:nth-child(3) {
                display: none; 
            }
            
            .ticket-table.purchase-card td.ticket-numbers-cell {
                width: 40% !important;
            }
            .ticket-table.purchase-card td {
                width: 30% !important;
            }
        }
        @media (max-width: 480px) {
            .ticket-table.purchase-card td.ticket-numbers-cell {
                width: 45% !important;
            }
            .ticket-table.purchase-card td {
                width: 27.5% !important;
            }
            .ticket-numbers-cell .ball-separator {
                display: none !important;
            }
            .ticket-numbers-cell .lottery-ball {
                margin: 0 !important;
                
                width: 32px !important;
                height: 32px !important;
                line-height: 32px !important;
                font-size: 14px !important;
            }
        }
    </style>
    <script
        src="${pageContext.request.contextPath}/assets/components/LotteryCompanySelector/companySelector.js?v=<%= System.currentTimeMillis () %>"></script>
</head>

<body class="dashboard-page">
    <div class="dashboard-container">
        <jsp:include page="/WEB-INF/fragments/userHeader.jsp">
            <jsp:param name="pageTitle" value="Purchase Tickets"/>
        </jsp:include>
        <div class="content-wrapper">
            <div class="main-content">
                <div class="sidebar">
                    <jsp:include page="/WEB-INF/fragments/sidebar.jsp" />
                </div>
                <div class="container">
                    <c:if test="${not empty errorMessage}">
                        <div class="message error">${errorMessage}</div>
                    </c:if>
                    <c:if test="${not empty successMessage}">
                        <div class="message success">${successMessage}</div>
                        <c:if test="${not empty purchasedTicket}">
                            <div class="message info">
                                <h4>Ticket Details:</h4>
                                <p><strong>Company:</strong> ${purchasedTicket.company}</p>
                                <p><strong>Numbers:</strong> ${purchasedTicket.numbers}</p>
                                <p><strong>Price:</strong> $${purchasedTicket.price}</p>
                                <p><strong>Date:</strong> ${purchasedTicket.creationDate}</p>
                            </div>
                        </c:if>

                    </c:if>
                    <div class="search-box">
                        <h3>Search Lottery Tickets</h3>
                        <form action="ticketPurchase" method="get" class="search-form">
                            <div style="display: flex; flex-wrap: wrap; width: 100%;">
                                <div
                                    style="flex: 1; min-width: 300px; padding: 10px; display: flex; flex-direction: column; align-items: flex-start; justify-content: flex-start;">
                                    <div style="width: 100%;">
                                        <label for="company">Company:</label>
                                        <div style="width: 100%;" id="company-selector-container-search"></div>
                                        <input type="hidden" name="company" value="${param.company}"
                                            id="selectedCompanyInput" />
                                    </div>
                                </div>
                                <div
                                    style="flex: 1; min-width: 300px; padding: 10px; display: flex; flex-direction: column; align-items: center;">
                                    <div
                                        style="display: grid; grid-template-columns: repeat(2, 1fr); gap: 10px; width: 100%;">
                                        <input type="number" name="num1" value="${param.num1}" placeholder="1st number"
                                            style="padding: 12px; border: 1px solid #3a3f5a; border-radius: 8px; background-color: #1a1a2e; color: #ffffff; text-align: center; font-weight: 600;"
                                            max="99" min="1" />
                                        <input type="number" name="num2" value="${param.num2}" placeholder="2nd number"
                                            style="padding: 12px; border: 1px solid #3a3f5a; border-radius: 8px; background-color: #1a1a2e; color: #ffffff; text-align: center; font-weight: 600;"
                                            max="99" min="1" />
                                        <input type="number" name="num3" value="${param.num3}" placeholder="3rd number"
                                            style="padding: 12px; border: 1px solid #3a3f5a; border-radius: 8px; background-color: #1a1a2e; color: #ffffff; text-align: center; font-weight: 600;"
                                            max="99" min="1" />
                                        <input type="number" name="num4" value="${param.num4}" placeholder="4th number"
                                            style="padding: 12px; border: 1px solid #3a3f5a; border-radius: 8px; background-color: #1a1a2e; color: #ffffff; text-align: center; font-weight: 600;"
                                            max="99" min="1" />
                                        <input type="number" name="num5" value="${param.num5}" placeholder="5th number"
                                            style="padding: 12px; border: 1px solid #3a3f5a; border-radius: 8px; background-color: #1a1a2e; color: #ffffff; text-align: center; font-weight: 600;"
                                            max="99" min="1" />
                                        <input type="number" name="num6" value="${param.num6}" placeholder="6th number"
                                            style="padding: 12px; border: 1px solid #3a3f5a; border-radius: 8px; background-color: #1a1a2e; color: #ffffff; text-align: center; font-weight: 600;"
                                            max="99" min="1" />
                                    </div>
                                    <div
                                        style="margin-top: auto; display: flex; justify-content: center; width: 100%; padding: 20px 0;">
                                        <button class="btn lottery-btn" type="submit">Search</button>
                                    </div>
                                </div>
                                <div
                                    style="flex: 1; min-width: 300px; padding: 10px; display: flex; flex-direction: column; align-items: center; position: relative;">
                                    <div
                                        style="display: grid; grid-template-columns: 1fr; gap: 10px; margin-top: 15px; width: 100%;">
                                        <label style="text-align: center; color: #ffffff;">From:</label>
                                        <input type="text" name="startDate" value="${param.startDate}"
                                            placeholder="YYYY-MM-DD"
                                            style="padding: 12px; border: 1px solid #3a3f5a; border-radius: 8px; background-color: #1a1a2e; color: #ffffff; text-align: center;"
                                            class="date-picker" />
                                        <label style="text-align: center; color: #ffffff;">To:</label>
                                        <input type="text" name="endDate" value="${param.endDate}"
                                            placeholder="YYYY-MM-DD"
                                            style="padding: 12px; border: 1px solid #3a3f5a; border-radius: 8px; background-color: #1a1a2e; color: #ffffff; text-align: center;"
                                            class="date-picker" />
                                        <label style="text-align: center; color: #ffffff;">On:</label>
                                        <input type="text" name="specificDate" value="${param.specificDate}"
                                            placeholder="YYYY-MM-DD"
                                            style="padding: 12px; border: 1px solid #3a3f5a; border-radius: 8px; background-color: #1a1a2e; color: #ffffff; text-align: center;"
                                            class="date-picker" />
                                    </div>
                                </div>
                            </div>
                        </form>
                    </div>
                    <c:if test="${not empty tickets}">
                        <% StringBuilder baseUrl=new StringBuilder("ticketPurchase?"); java.util.Enumeration<String>
                            paramNames =
                            request.getParameterNames();
                            while (paramNames.hasMoreElements()) {
                            String name = paramNames.nextElement();
                            if (!name.equals("page")) {
                            String[] values = request.getParameterValues(name);
                            for (String value : values) {
                            if (value != null && !value.isEmpty()) {
                            baseUrl.append(name).append("=")
                            .append(java.net.URLEncoder.encode(value, "UTF-8"))
                            .append("&");
                            }
                            }
                            }
                            }
                            String base = baseUrl.toString();
                            if (base.endsWith("&") || base.endsWith("?")) {
                            base = base.substring(0, base.length() - 1);
                            }
                            request.setAttribute("baseUrl", base);
                            %>
                            <div class="search-box">
                                <div
                                    style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
                                    <h3>Published Lottery Tickets</h3>
                                    <div style="display: flex; gap: 10px;">
                                        <button class="btn lottery-btn btn-sapphire" type="button"
                                            onclick="buySelectedTickets()">Buy
                                            Selected</button>
                                        <button class="btn lottery-btn btn-emerald" type="button"
                                            onclick="buyAllTickets()">Buy
                                            All</button>
                                        <button class="btn lottery-btn" type="button"
                                            onclick="clearAllSelections()">Clear</button>
                                    </div>
                                </div>
                                <table class="ticket-table purchase-card">
                                    <thead>
                                        <tr>
                                            <th style="width: 40px; text-align: center;">
                                                <input type="checkbox" id="selectAllCheckbox"
                                                    onchange="toggleAllCheckboxes(this)"
                                                    title="Select all on current page" />
                                            </th>
                                            <th>Numbers</th>
                                            <th>Company</th>
                                            <th>Creation Date</th>
                                            <th>Action</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach items="${tickets}" var="ticket">
                                            <tr>
                                                <td data-label="Select" style="text-align: center;">
                                                    <input type="checkbox" name="ticketCheckbox"
                                                        value="${ticket.ticketID}" data-company="${ticket.company}"
                                                        data-price="${ticket.price}"
                                                        onchange="updateSelectedTickets()" />
                                                </td>
                                                <td data-label="Numbers" class="ticket-numbers-cell">
                                                    ${ticket.numbers}</td>
                                                <td data-label="Company">${ticket.company}</td>
                                                <td data-label="Creation Date">${ticket.creationDate}</td>
                                                <td data-label="Action">
                                                    <form action="ticketPurchase" method="post" class="single-buy-form">
                                                        <input type="hidden" name="ticketId"
                                                            value="${ticket.ticketID}" />
                                                        <input type="hidden" name="company" value="${param.company}" />
                                                        <input type="hidden" name="num1" value="${param.num1}" />
                                                        <input type="hidden" name="num2" value="${param.num2}" />
                                                        <input type="hidden" name="num3" value="${param.num3}" />
                                                        <input type="hidden" name="num4" value="${param.num4}" />
                                                        <input type="hidden" name="num5" value="${param.num5}" />
                                                        <input type="hidden" name="num6" value="${param.num6}" />
                                                        <input type="hidden" name="startDate"
                                                            value="${param.startDate}" />
                                                        <input type="hidden" name="endDate" value="${param.endDate}" />
                                                        <input type="hidden" name="specificDate"
                                                            value="${param.specificDate}" />
                                                        <input type="hidden" name="page" value="${param.page}" />
                                                        <button class="btn lottery-btn btn-sapphire"
                                                            type="submit">Buy</button>
                                                    </form>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                                <form action="ticketPurchase" method="post" id="bulkPurchaseForm"
                                    style="display: none;">
                                    <input type="hidden" name="action" value="bulkPurchase" />
                                    <input type="hidden" name="ticketIds" id="bulkTicketIds" />
                                    <input type="hidden" name="company" value="${param.company}" />
                                    <input type="hidden" name="num1" value="${param.num1}" />
                                    <input type="hidden" name="num2" value="${param.num2}" />
                                    <input type="hidden" name="num3" value="${param.num3}" />
                                    <input type="hidden" name="num4" value="${param.num4}" />
                                    <input type="hidden" name="num5" value="${param.num5}" />
                                    <input type="hidden" name="num6" value="${param.num6}" />
                                    <input type="hidden" name="startDate" value="${param.startDate}" />
                                    <input type="hidden" name="endDate" value="${param.endDate}" />
                                    <input type="hidden" name="specificDate" value="${param.specificDate}" />
                                    <input type="hidden" name="page" value="${param.page}" />
                                </form>
                                <jsp:include page="/WEB-INF/fragments/pagination.jsp" />
                            </div>
                    </c:if>
                    <c:if test="${searchPerformed and empty tickets}">
                        <p>No published tickets found matching your search criteria.</p>
                    </c:if>
                    <div class="card">
                        <h3>How to Purchase Tickets</h3>
                        <p>1. Select a company (optional)</p>
                        <p>2. Enter your preferred numbers (optional)</p>
                        <p>3. Specify a date range or specific date (optional)</p>
                        <p>4. Click "Search" to see matching tickets available for purchase</p>
                        <p>5. Click "Buy" on a ticket to complete the purchase</p>
                    </div>
                </div>
            </div>
        </div>
        <script>
            window.allCompanies = [
                <c:forEach var="comp" items="${companies}" varStatus="status">
                    "${comp}"${!status.last ? ',' : ''}
                </c:forEach>
            ];
            let companySelector;
            document.addEventListener('DOMContentLoaded', function () {
                companySelector = new CompanySelector('company-selector-container-search', window.allCompanies, {
                    singleSelect: false
                });
                const initialValue = document.getElementById('selectedCompanyInput').value;
                if (initialValue) {
                    const initialCompanies = initialValue.split(',')
                        .map(company => company.trim())
                        .filter(company => company.length > 0);
                    if (initialCompanies.length > 0) {
                        companySelector.setSelectedCompanies(initialCompanies);
                    }
                }
                const form = document.querySelector('.search-form');
                if (form) {
                    form.addEventListener('submit', function () {
                        const selected = companySelector.getSelectedCompanies();
                        document.getElementById('selectedCompanyInput').value = selected.join(',');
                    });
                }
            });
            function handleDateSelection(type) {
                if (type === 'specific') {
                    document.querySelector('input[name="startDate"]').value = '';
                    document.querySelector('input[name="endDate"]').value = '';
                }
                else if (type === 'range') {
                    document.querySelector('input[name="specificDate"]').value = '';
                }
            }
            
            document.addEventListener('DOMContentLoaded', function () {
                const startDateInput = document.querySelector('input[name="startDate"]');
                const endDateInput = document.querySelector('input[name="endDate"]');
                const specificDateInput = document.querySelector('input[name="specificDate"]');
                if (startDateInput) {
                    startDateInput.addEventListener('change', function () {
                        handleDateSelection('range');
                    });
                }
                if (endDateInput) {
                    endDateInput.addEventListener('change', function () {
                        handleDateSelection('range');
                    });
                }
                if (specificDateInput) {
                    specificDateInput.addEventListener('change', function () {
                        handleDateSelection('specific');
                    });
                }
            });
            function renderLotteryBalls() {
                const cells = document.querySelectorAll('.ticket-numbers-cell');
                cells.forEach(cell => {
                    const content = cell.textContent.trim();
                    const numbers = content.split(/[\s,]+/);
                    cell.innerHTML = '';
                    const wrapper = document.createElement('div');
                    wrapper.className = 'balls-wrapper';
                    numbers.forEach((num, index) => {
                        const ball = document.createElement('span');
                        ball.className = `lottery-ball ball-${(index % 6) + 1}`;
                        ball.textContent = num;
                        wrapper.appendChild(ball);
                        if (index < numbers.length - 1) {
                            const separator = document.createElement('span');
                            separator.className = 'ball-separator';
                            separator.textContent = ',';
                            wrapper.appendChild(separator);
                        }
                    });
                    cell.appendChild(wrapper);
                });
            }
            window.addEventListener('DOMContentLoaded', renderLotteryBalls);
            var selectedTickets = [];
            function updateSelectedTickets() {
                selectedTickets = [];
                const checkboxes = document.querySelectorAll('input[name="ticketCheckbox"]:checked');
                checkboxes.forEach(checkbox => {
                    selectedTickets.push(checkbox.value);
                });
            }
            function toggleAllCheckboxes(masterCheckbox) {
                const checkboxes = document.querySelectorAll('input[name="ticketCheckbox"]');
                checkboxes.forEach(checkbox => {
                    checkbox.checked = masterCheckbox.checked;
                });
                updateSelectedTickets();
            }
            function clearAllSelections() {
                const checkboxes = document.querySelectorAll('input[name="ticketCheckbox"]');
                checkboxes.forEach(checkbox => {
                    checkbox.checked = false;
                });
                const selectAll = document.getElementById('selectAllCheckbox');
                if (selectAll) selectAll.checked = false;
                updateSelectedTickets();
            }
            function buySelectedTickets() {
                updateSelectedTickets();
                if (selectedTickets.length === 0) {
                    alert("Please select at least one ticket to purchase.");
                    return;
                }
                if (confirm("Are you sure you want to purchase " + selectedTickets.length + " ticket(s)?")) {
                    document.getElementById('bulkTicketIds').value = selectedTickets.join(',');
                    document.getElementById('bulkPurchaseForm').submit();
                }
            }
            function buyAllTickets() {
                const allCheckboxes = document.querySelectorAll('input[name="ticketCheckbox"]');
                if (allCheckboxes.length === 0) {
                    alert("No tickets available to purchase.");
                    return;
                }
                const totalPages = parseInt('${totalPages}');
                const currentPage = parseInt('${currentPage}');
                if (totalPages > 1) {
                    fetchAllTicketIds().then(allTicketIds => {
                        if (allTicketIds.length === 0) {
                            alert("No tickets available to purchase.");
                            return;
                        }
                        if (confirm("Are you sure you want to purchase all " + allTicketIds.length + " ticket(s) in the search result?")) {
                            document.getElementById('bulkTicketIds').value = allTicketIds.join(',');
                            document.getElementById('bulkPurchaseForm').submit();
                        }
                    });
                }
                else {
                    const allTicketIds = [];
                    allCheckboxes.forEach(checkbox => {
                        allTicketIds.push(checkbox.value);
                    });
                    if (confirm("Are you sure you want to purchase all " + allTicketIds.length + " ticket(s)?")) {
                        document.getElementById('bulkTicketIds').value = allTicketIds.join(',');
                        document.getElementById('bulkPurchaseForm').submit();
                    }
                }
            }
            function fetchAllTicketIds() {
                return new Promise((resolve, reject) => {
                    const allTicketIds = [];
                    const totalPages = parseInt('${totalPages}');
                    const currentCheckboxes = document.querySelectorAll('input[name="ticketCheckbox"]');
                    currentCheckboxes.forEach(checkbox => {
                        allTicketIds.push(checkbox.value);
                    });
                    if (totalPages <= 1) {
                        resolve(allTicketIds);
                        return;
                    }
                    const baseUrl = '${baseUrl}';
                    const promises = [];
                    for (let page = 1; page <= totalPages; page++) {
                        if (page === parseInt('${currentPage}')) continue;
                        promises.push(
                            fetch(baseUrl + (baseUrl.includes('?') ? '&' : '?') + 'page=' + page)
                                .then(response => response.text())
                                .then(html => {
                                    const parser = new DOMParser();
                                    const doc = parser.parseFromString(html, 'text/html');
                                    const checkboxes = doc.querySelectorAll('input[name="ticketCheckbox"]');
                                    checkboxes.forEach(checkbox => {
                                        allTicketIds.push(checkbox.value);
                                    });
                                })
                                .catch(error => {
                                    console.error('Error fetching page ' + page + ':', error);
                                })
                        );
                    }
                    Promise.all(promises).then(() => {
                        resolve(allTicketIds);
                    }).catch(error => {
                        console.error('Error fetching all ticket IDs:', error);
                        resolve(allTicketIds);
                    });
                });
            }
        </script>

</body>

</html>
