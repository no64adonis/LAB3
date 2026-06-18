<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page import="com.lottery.model.LotteryTicket" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Enumeration" %>
<%@ page import="java.time.LocalDate" %>

<%@ page import="java.time.format.DateTimeFormatter" %>

<!DOCTYPE html>
<html>

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Fortuna Lotto - Admin Management</title>
    <link rel="stylesheet"
        href="${pageContext.request.contextPath}/assets/css/variables.css?v=<%= System.currentTimeMillis() %>">
    <link rel="stylesheet"
        href="${pageContext.request.contextPath}/assets/css/lottery-style.css?v=<%= System.currentTimeMillis() %>">
    <link rel="stylesheet"
        href="${pageContext.request.contextPath}/assets/css/adminShared.css?v=<%= System.currentTimeMillis() %>">
    <link rel="stylesheet"
        href="${pageContext.request.contextPath}/assets/css/adminLottery.css?v=<%= System.currentTimeMillis() %>">
    <link
        href="https://fonts.googleapis.com/css2?family=Montserrat:wght@400;600;700&family=Poppins:wght@400;500;600&display=swap"
        rel="stylesheet">
    <link rel="stylesheet"
        href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css?v=<%= System.currentTimeMillis() %>">
    <script
        src="${pageContext.request.contextPath}/assets/components/LotteryCompanySelector/companySelector.js?v=<%= System.currentTimeMillis() %>"></script>

    
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
            <jsp:param name="pageTitle" value="Admin Lottery Management"/>
        </jsp:include>

        <div class="content-wrapper">
            <div class="main-content">
                <div class="sidebar">
                    <jsp:include page="/WEB-INF/fragments/adminSidebar.jsp" />
                </div>
                <div class="container">
                    
                    <div class="main-content-inner">
                        <c:if test="${user.role == 'admin'}">
                        
                        <div class="create-form">
                            <h3>Create New Lottery Ticket</h3>
                            <form method="post" action="adminLottery"
                                id="createTicketForm">
                                <input type="hidden" name="action" value="create">
                                <div
                                    style="display: flex; flex-wrap: wrap; width: 100%;">
                                    
                                    <div
                                        style="flex: 1; min-width: 300px; padding: 10px; display: flex; flex-direction: column; align-items: flex-start; justify-content: flex-start;">
                                        <input type="text" name="ticketID"
                                            placeholder="Ticket ID" required
                                            value="${not empty error ? param.ticketID : ''}"
                                            style="width: 200px; padding: 12px; margin-bottom: 15px; border: 1px solid #3a3f5a; border-radius: 8px; background-color: #1a1a2e; color: #ffffff; text-align: center; font-weight: 600;">
                                        <div
                                            style="display: grid; grid-template-columns: repeat(2, 1fr); gap: 10px; width: 100%; max-width: 300px;">
                                            <input type="number" name="num1"
                                                placeholder="1st number" min="1"
                                                max="99" required
                                                value="${not empty error ? param.num1 : ''}">
                                            <input type="number" name="num2"
                                                placeholder="2nd number" min="1"
                                                max="99" required
                                                value="${not empty error ? param.num2 : ''}">
                                            <input type="number" name="num3"
                                                placeholder="3rd number" min="1"
                                                max="99" required
                                                value="${not empty error ? param.num3 : ''}">
                                            <input type="number" name="num4"
                                                placeholder="4th number" min="1"
                                                max="99" required
                                                value="${not empty error ? param.num4 : ''}">
                                            <input type="number" name="num5"
                                                placeholder="5th number" min="1"
                                                max="99" required
                                                value="${not empty error ? param.num5 : ''}">
                                            <input type="number" name="num6"
                                                placeholder="6th number" min="1"
                                                max="99" required
                                                value="${not empty error ? param.num6 : ''}">
                                        </div>
                                    </div>

                                    
                                    <div
                                        style="flex: 1; min-width: 300px; padding: 10px; display: flex; flex-direction: column; align-items: center;">
                                        <div id="company-selector-container-create"
                                            style="width: 100%;"></div>
                                        <input type="hidden" name="selectedCompany"
                                            id="selectedCompanyInput"
                                            value="${not empty error ? param.selectedCompany : ''}">
                                    </div>

                                    
                                    <div
                                        style="flex: 1; min-width: 300px; padding: 10px; display: flex; flex-direction: column; align-items: center;">
                                        <div
                                            style="display: flex; flex-direction: column; align-items: center; gap: 15px; width: 100%;">
                                            <label
                                                style="display: flex; align-items: center; gap: 8px;">
                                                <input type="checkbox" name="published"
                                                    ${not empty error and
                                                    param.published=='on' ? 'checked'
                                                    : '' }> Published
                                            </label>

                                            
                                            <input type="hidden" name="company"
                                                value="${param.company}">
                                            <input type="hidden" name="num1_filter"
                                                value="${param.num1}">
                                            <input type="hidden" name="num2_filter"
                                                value="${param.num2}">
                                            <input type="hidden" name="num3_filter"
                                                value="${param.num3}">
                                            <input type="hidden" name="num4_filter"
                                                value="${param.num4}">
                                            <input type="hidden" name="num5_filter"
                                                value="${param.num5}">
                                            <input type="hidden" name="num6_filter"
                                                value="${param.num6}">
                                            <input type="hidden" name="startDate"
                                                value="${param.startDate}">
                                            <input type="hidden" name="endDate"
                                                value="${param.endDate}">
                                            <input type="hidden" name="specificDate"
                                                value="${param.specificDate}">

                                            <input type="hidden" name="page"
                                                value="${param.page}">

                                            <button type="submit"
                                                class="btn btn-emerald lottery-btn"
                                                style="margin-top: 20px;"
                                                onclick="return validateCreateForm()">Create
                                                Ticket</button>
                                        </div>
                                    </div>
                                </div>
                            </form>
                        </div>
                         
                        
                                                        <div class="create-form" style="margin-top: 20px;">
                                                            <h3>Bulk Insert Tickets</h3>
                                                            <p
                                                                style="color: #8a8f9e; font-size: 0.9em; margin-bottom: 15px; text-align: center;">
                                                                Format: TicketID, Num1, Num2, Num3, Num4, Num5, Num6,
                                                                Company, Published, <br>
                                                                Example: T1001, 1, 2, 3, 4, 5, 6, Vietlott, true, false,
                                                                false
                                                            </p>
                                                            <form method="post" action="adminLottery">
                                                                <input type="hidden" name="action" value="bulkInsert">
                                                                
                                                                <input type="hidden" name="company"
                                                                    value="${param.company}">
                                                                <input type="hidden" name="num1_filter"
                                                                    value="${param.num1}">
                                                                <input type="hidden" name="num2_filter"
                                                                    value="${param.num2}">
                                                                <input type="hidden" name="num3_filter"
                                                                    value="${param.num3}">
                                                                <input type="hidden" name="num4_filter"
                                                                    value="${param.num4}">
                                                                <input type="hidden" name="num5_filter"
                                                                    value="${param.num5}">
                                                                <input type="hidden" name="num6_filter"
                                                                    value="${param.num6}">
                                                                <input type="hidden" name="startDate"
                                                                    value="${param.startDate}">
                                                                <input type="hidden" name="endDate"
                                                                    value="${param.endDate}">
                                                                <input type="hidden" name="specificDate"
                                                                    value="${param.specificDate}">
                                                                
                                                                
                                                                <input type="hidden" name="page" value="${param.page}">

                                                                <textarea name="csvData"
                                                                    placeholder="T1001, 1, 2, 3, 4, 5, 6, Vietlott, true"
                                                                    style="width: 100%; height: 150px; padding: 12px; border: 1px solid #3a3f5a; border-radius: 8px; background-color: #1a1a2e; color: #ffffff; font-family: monospace;"></textarea>
                                                                <div
                                                                    style="display: flex; justify-content: center; margin-top: 15px;">
                                                                    <button type="submit"
                                                                        class="btn btn-emerald lottery-btn">Bulk
                                                                        Insert</button>
                                                                </div>
                                                            </form>
                                                        </div>

                                                        
                                                        <div class="create-form" style="margin-top: 20px;">
                                                            <h3>Company Price Management</h3>
                                                            <form method="post" action="adminLottery"
                                                                id="priceUpdateForm">
                                                                <input type="hidden" name="action" value="updatePrice">
                                                                
                                                                <input type="hidden" name="company"
                                                                    value="${param.company}">
                                                                <input type="hidden" name="num1_filter"
                                                                    value="${param.num1}">
                                                                <input type="hidden" name="num2_filter"
                                                                    value="${param.num2}">
                                                                <input type="hidden" name="num3_filter"
                                                                    value="${param.num3}">
                                                                <input type="hidden" name="num4_filter"
                                                                    value="${param.num4}">
                                                                <input type="hidden" name="num5_filter"
                                                                    value="${param.num5}">
                                                                <input type="hidden" name="num6_filter"
                                                                    value="${param.num6}">
                                                                <input type="hidden" name="startDate"
                                                                    value="${param.startDate}">
                                                                <input type="hidden" name="endDate"
                                                                    value="${param.endDate}">
                                                                <input type="hidden" name="specificDate"
                                                                    value="${param.specificDate}">
                                                                
                                                                
                                                                <input type="hidden" name="page" value="${param.page}">

                                                                <div
                                                                    style="display: flex; flex-wrap: wrap; width: 100%; align-items: flex-start; gap: 15px;">
                                                                    
                                                                    <div
                                                                        style="flex: 1; min-width: 300px; display: flex; flex-direction: column;">
                                                                        <div id="company-selector-container-price"
                                                                            style="width: 100%;"></div>
                                                                        <input type="hidden"
                                                                            name="selectedCompaniesPrice"
                                                                            id="selectedCompaniesInputPrice">
                                                                    </div>

                                                                    
                                                                    <div style="width: 150px;">
                                                                        <input type="number" name="newPrice"
                                                                            placeholder="Price" min="0" step="0.01"
                                                                            required
                                                                            style="width: 100%; padding: 10px; border: 1px solid #3a3f5a; border-radius: 8px; background-color: #1a1a2e; color: #ffffff; text-align: center; font-weight: 600; height: 42px; box-sizing: border-box;">
                                                                    </div>

                                                                    
                                                                    <div>
                                                                        <button type="submit"
                                                                            class="btn btn-emerald lottery-btn"
                                                                            style="height: 42px; width: 150px;">Update
                                                                            Price</button>
                                                                    </div>
                                                                </div>
                                                            </form>
                                                        </div>

                                                        
                                                        <div class="search-box" style="margin-top: 20px;">
                                                            <h3>Search Tickets</h3>
                                                            <form class="search-form" method="get" action="adminLottery"
                                                                id="searchForm">
                                                                <input type="hidden" name="action" value="search">
                                                                <input type="hidden" name="fromHistory" value="">
                                                                <input type="hidden" name="searchId" value="">
                                                                <input type="hidden" name="page" id="searchPageInput"
                                                                    value="${currentPage}">

                                                                <div
                                                                    style="display: flex; flex-wrap: wrap; width: 100%;">
                                                                    
                                                                    <div
                                                                        style="flex: 1; min-width: 300px; padding: 10px; display: flex; flex-direction: column; align-items: center;">
                                                                        <div id="company-selector-container-search"
                                                                            style="width: 100%;"></div>
                                                                        <input type="hidden" name="company"
                                                                            id="selected-companies-input-search"
                                                                            value="${param.company}">
                                                                    </div>

                                                                    
                                                                    <div
                                                                        style="flex: 1; min-width: 300px; padding: 10px; display: flex; flex-direction: column; align-items: center;">
                                                                        <div
                                                                            style="display: grid; grid-template-columns: repeat(2, 1fr); gap: 10px; width: 100%;">
                                                                            <input type="number" name="num1"
                                                                                placeholder="1st number" min="1"
                                                                                max="99" value="${param.num1}">
                                                                            <input type="number" name="num2"
                                                                                placeholder="2nd number" min="1"
                                                                                max="99" value="${param.num2}">
                                                                            <input type="number" name="num3"
                                                                                placeholder="3rd number" min="1"
                                                                                max="99" value="${param.num3}">
                                                                            <input type="number" name="num4"
                                                                                placeholder="4th number" min="1"
                                                                                max="99" value="${param.num4}">
                                                                            <input type="number" name="num5"
                                                                                placeholder="5th number" min="1"
                                                                                max="99" value="${param.num5}">
                                                                            <input type="number" name="num6"
                                                                                placeholder="6th number" min="1"
                                                                                max="99" value="${param.num6}">
                                                                        </div>
                                                                    </div>
                                                                    
                                                                    
                                                                    <div
                                                                        style="flex: 1; min-width: 300px; padding: 10px; display: flex; flex-direction: column; align-items: center;">
                                                                        <div
                                                                            style="display: flex; flex-direction: column; gap: 10px; margin-bottom: 15px; width: 100%; align-items: center;">

                                                                        </div>
                                                                        <div
                                                                            style="display: grid; grid-template-columns: 1fr; gap: 10px; width: 100%;">
                                                                            <input type="text" name="startDate"
                                                                                class="date-picker"
                                                                                value="${param.startDate}"
                                                                                placeholder="YYYY-MM-DD"
                                                                                style="padding: 12px; border: 1px solid #3a3f5a; border-radius: 8px; background-color: #1a1a2e; color: #ffffff; text-align: center;"
                                                                                onchange="handleDateSelection('range')">
                                                                            <input type="text" name="endDate"
                                                                                class="date-picker"
                                                                                value="${param.endDate}"
                                                                                placeholder="YYYY-MM-DD"
                                                                                style="padding: 12px; border: 1px solid #3a3f5a; border-radius: 8px; background-color: #1a1a2e; color: #ffffff; text-align: center;"
                                                                                onchange="handleDateSelection('range')">
                                                                            <input type="text" name="specificDate"
                                                                                class="date-picker"
                                                                                value="${param.specificDate}"
                                                                                placeholder="YYYY-MM-DD"
                                                                                style="padding: 12px; border: 1px solid #3a3f5a; border-radius: 8px; background-color: #1a1a2e; color: #ffffff; text-align: center;"
                                                                                onchange="handleDateSelection('specific')">
                                                                        </div>
                                                                    </div>
                                                                    
                                                                    
                                                                    <div style="width: 100%; display: flex; justify-content: center; gap: 15px; padding: 20px 0; border-top: 1px solid rgba(255,255,255,0.1); margin-top: 15px;">
                                                                        <button type="submit"
                                                                            class="btn lottery-btn btn-sapphire" style="min-width: 120px;">Search</button>
                                                                        <button type="button"
                                                                            class="btn lottery-btn btn-ruby"
                                                                            onclick="resetForm()" style="min-width: 120px;">Reset</button>
                                                                    </div>
                                                                </div>
                                                            </form>
                                                        </div>

                                                        <c:if test="${not empty tickets}">
                                                            <div class="search-box" style="margin-top: 20px;">
                                                                <h3>Search Results (${totalTickets} items)</h3>

                                                                <c:if test="${not empty sessionScope.message}">
                                                                    <div class="message success">${sessionScope.message}
                                                                    </div>
                                                                    <% session.removeAttribute("message"); %>
                                                                </c:if>
                                                                <c:if test="${not empty sessionScope.error}">
                                                                    <div class="message error">${sessionScope.error}
                                                                    </div>
                                                                    <% session.removeAttribute("error"); %>
                                                                </c:if>

                                                                
                                                                <div class="bulk-operations"
                                                                    style="margin: 20px 0; padding: 20px; border: 1px solid #3a3f5a; border-radius: 8px; background-color: #1a1a2e;">
                                                                    <h4
                                                                        style="color: #ffffff; margin-bottom: 15px; text-align: center;">
                                                                        Bulk Operations</h4>
                                                                    <form id="bulkActionForm" method="post"
                                                                        action="adminLottery">
                                                                        <input type="hidden" name="action"
                                                                            id="bulkActionType" value="bulkUpdate">
                                                                        <input type="hidden" name="selectedTickets"
                                                                            id="selectedTicketIds" value="">
                                                                        <input type="hidden" name="type"
                                                                            id="bulkUpdateType" value="">
                                                                        <input type="hidden" name="value"
                                                                            id="bulkUpdateValue" value="">

                                                                        
                                                                        <input type="hidden" name="company"
                                                                            value="${param.company}">
                                                                        <input type="hidden" name="num1"
                                                                            value="${param.num1}">
                                                                        <input type="hidden" name="num2"
                                                                            value="${param.num2}">
                                                                        <input type="hidden" name="num3"
                                                                            value="${param.num3}">
                                                                        <input type="hidden" name="num4"
                                                                            value="${param.num4}">
                                                                        <input type="hidden" name="num5"
                                                                            value="${param.num5}">
                                                                        <input type="hidden" name="num6"
                                                                            value="${param.num6}">
                                                                        <input type="hidden" name="startDate"
                                                                            value="${param.startDate}">
                                                                        <input type="hidden" name="endDate"
                                                                            value="${param.endDate}">
                                                                        <input type="hidden" name="specificDate"
                                                                            value="${param.specificDate}">
                                                                        
                                                                        
                                                                        <input type="hidden" name="page"
                                                                            value="${param.page}">

                                                                        <div
                                                                            style="display: flex; flex-wrap: wrap; gap: 10px; justify-content: center;">
                                                                            <button type="button"
                                                                                class="btn btn-success lottery-btn"
                                                                                style="flex: 1; min-width: 140px;"
                                                                                onclick="performBulkAction('publish', 'true')">Publish</button>
                                                                            <button type="button"
                                                                                class="btn btn-warning lottery-btn"
                                                                                style="flex: 1; min-width: 140px;"
                                                                                onclick="performBulkAction('publish', 'false')">Unpublish</button>
                                                                            
                                                                            
                                                                            
                                                                            
                                                                            <button type="button"
                                                                                class="btn btn-sapphire lottery-btn"
                                                                                style="flex: 1; min-width: 140px;"
                                                                                onclick="selectAllTickets()">Select
                                                                                All</button>
                                                                            <button type="button"
                                                                                class="btn btn-ruby lottery-btn"
                                                                                style="flex: 1; min-width: 140px;"
                                                                                onclick="clearAllSelections()">Clear</button>
                                                                        </div>
                                                                    </form>
                                                                </div>

                                                                <table class="ticket-table">
                                    <thead>
                                        <tr>
                                            <th><input type="checkbox"
                                                    id="selectAllCheckbox"
                                                    onchange="toggleAllCheckboxes(this)">
                                            </th>
                                            <th class="col-id">ID</th>
                                            <th>Numbers</th>
                                            <th class="col-company">Company</th>
                                            <th class="col-date">Creation Date</th>
                                            <th class="col-views">View Count</th>
                                            <th class="col-status">Status</th>

                                            <th>Actions</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="ticket" items="${tickets}">
                                            <tr>
                                                <td data-label="Select"><input type="checkbox"
                                                        name="ticketCheckbox"
                                                        value="${ticket.ticketID}"
                                                        onchange="updateSelectedTickets()">
                                                </td>
                                                <td data-label="ID" class="col-id">${ticket.ticketID}</td>
                                                <td data-label="Numbers" class="ticket-numbers-cell">
                                                    ${ticket.numbers}</td>
                                                <td data-label="Company" class="col-company">${ticket.company}</td>
                                                <td data-label="Creation Date" class="col-date">${ticket.creationDate}</td>
                                                <td data-label="View Count" class="col-views">${ticket.viewCount}</td>
                                                <td data-label="Status" class="col-status">
                                                    <c:choose>
                                                        <c:when
                                                            test="${ticket.published}">
                                                            <span
                                                                class="status-published">Published</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span
                                                                class="status-unpublished">Unpublished</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>

                                                <td data-label="Actions">
                                                    <div class="actions-container">
                                                        <div class="actions-header"
                                                            onclick="toggleActionsDropdown(this, event)">
                                                            Actions <i
                                                                class="fas fa-chevron-down dropdown-icon"></i>
                                                        </div>
                                                        <ul class="actions-list">
                                                            <li class="action-item"
                                                                onclick="showTicketDetail('${ticket.ticketID}', '${ticket.numbers}', '${ticket.company}', '${ticket.creationDate}', '${ticket.viewCount}', ${ticket.published})">
                                                                View Details
                                                            </li>
                                                            <li class="action-item"
                                                                onclick="handleActionChoice('updateStatus', '${ticket.ticketID}', '${ticket.published}')">
                                                                ${ticket.published ?
                                                                'Unpublish' : 'Publish'}
                                                            </li>
                                                        </ul>
                                                    </div>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>

                                <% StringBuilder baseUrl=new
                                    StringBuilder("adminLottery?"); Enumeration<String>
                                    paramNames = request.getParameterNames();
                                    while (paramNames.hasMoreElements()) {
                                    String name = paramNames.nextElement();
                                    if (!name.equals("page")) {
                                    String[] values = request.getParameterValues(name);
                                    for (String value : values) {
                                    if (value != null && !value.isEmpty()) {
                                    baseUrl.append(name).append("=").append(java.net.URLEncoder.encode(value,
                                    "UTF-8")).append("&");
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
                                    <c:set var="baseUrl" value="${computedBaseUrl}"
                                        scope="request" />
                                    <jsp:include
                                        page="/WEB-INF/fragments/pagination.jsp" />
                            </div>
                        </c:if>
                        </c:if>
                    </div> 
                </div> 
            </div> 
        </div> 

        <script>
            function toggleActionsDropdown(element, event) {
                if (event) event.stopPropagation();
                const list = element.nextElementSibling;
                const isActive = element.classList.contains('active');

                
                document.querySelectorAll('.actions-header.active').forEach(h => {
                    if (h !== element) {
                        h.classList.remove('active');
                        h.nextElementSibling.classList.remove('show');
                    }
                });

                element.classList.toggle('active');
                list.classList.toggle('show');

                
                event.stopPropagation();
            }

            
            document.addEventListener('click', function (event) {
                if (!event.target.closest('.actions-container')) {
                    document.querySelectorAll('.actions-header.active').forEach(h => {
                        h.classList.remove('active');
                        h.nextElementSibling.classList.remove('show');
                    });
                }
            });

            function handleActionChoice(actionType, ticketId, isPublished) {
                if (typeof isPublished === 'string') isPublished = (isPublished === 'true');
                if (!actionType) return;

                const confirmMsgs = {
                    'updateStatus': 'Change publication status?'
                };

                if (!confirm(confirmMsgs[actionType] || 'Perform this action?')) {
                    return;
                }

                const form = document.createElement('form');
                form.method = 'post';
                form.action = 'adminLottery';

                const actionInput = document.createElement('input');
                actionInput.type = 'hidden';
                actionInput.name = 'action';
                actionInput.value = actionType;
                form.appendChild(actionInput);

                const idInput = document.createElement('input');
                idInput.type = 'hidden';
                idInput.name = 'ticketId';
                idInput.value = ticketId;
                form.appendChild(idInput);

                if (actionType === 'updateStatus') {
                    const pubInput = document.createElement('input');
                    pubInput.type = 'hidden';
                    pubInput.name = 'published';
                    pubInput.value = !isPublished;
                    form.appendChild(pubInput);
                }

                
                const urlParams = new URLSearchParams(window.location.search);
                const searchParams = ['company', 'num1', 'num2', 'num3', 'num4', 'num5', 'num6', 'startDate', 'endDate', 'specificDate', 'page'];
                searchParams.forEach(paramName => {
                    const value = urlParams.get(paramName);
                    if (value !== null) {
                        const input = document.createElement('input');
                        input.type = 'hidden';
                        input.name = paramName;
                        input.value = value;
                        form.appendChild(input);
                    }
                });

                document.body.appendChild(form);
                form.submit();
            }

            

            
            window.allCompanies = [
                <c:forEach var="comp" items="${companies}" varStatus="status">
                    "${comp}"${!status.last ? ',' : ''}
                </c:forEach>
            ];

            let createCompanySelector, searchCompanySelector, priceCompanySelector;

            document.addEventListener('DOMContentLoaded', function () {
                
                createCompanySelector = new CompanySelector('company-selector-container-create', window.allCompanies, {
                    singleSelect: true
                });

                
                searchCompanySelector = new CompanySelector('company-selector-container-search', window.allCompanies);

                
                priceCompanySelector = new CompanySelector('company-selector-container-price', window.allCompanies);

                
                const urlParams = new URLSearchParams(window.location.search);
                const companyParam = urlParams.get('company');
                if (companyParam) {
                    searchCompanySelector.setSelectedCompanies(companyParam.split(','));
                }

                
                const createForm = document.getElementById('createTicketForm');
                if (createForm) {
                    createForm.addEventListener('submit', function () {
                        const selected = createCompanySelector.getSelectedCompanies();
                        document.getElementById('selectedCompanyInput').value = selected.length > 0 ? selected[0] : '';
                    });
                }

                const priceForm = document.getElementById('priceUpdateForm');
                if (priceForm) {
                    priceForm.addEventListener('submit', function (event) {
                        const selected = priceCompanySelector.getSelectedCompanies();
                        document.getElementById('selectedCompaniesInputPrice').value = selected.join(',');

                        if (selected.length === 0) {
                            alert("Please select at least one company.");
                            event.preventDefault();
                        }
                    });
                }

                const searchForm = document.getElementById('searchForm');
                if (searchForm) {
                    searchForm.addEventListener('submit', function () {
                        const selected = searchCompanySelector.getSelectedCompanies();
                        document.getElementById('selected-companies-input-search').value = selected.join(',');
                    });
                }

                
                document.addEventListener('submit', function () {
                    localStorage.setItem('scrollPosition', window.scrollY);
                });

                const savedPosition = localStorage.getItem('scrollPosition');
                if (savedPosition) {
                    const pos = parseInt(savedPosition);
                    const restoreScroll = () => {
                        window.scrollTo(0, pos);
                        if (window.scrollY !== pos && document.body.scrollHeight > window.innerHeight) {
                            requestAnimationFrame(restoreScroll);
                        } else {
                            localStorage.removeItem('scrollPosition');
                        }
                    };
                    setTimeout(restoreScroll, 100);
                    setTimeout(() => localStorage.removeItem('scrollPosition'), 2000);
                }
            });

            function validateCreateForm() {
                const form = document.getElementById('createTicketForm');
                
                if (createCompanySelector) {
                    const selected = createCompanySelector.getSelectedCompanies();
                    document.getElementById('selectedCompanyInput').value = selected.length > 0 ? selected[0] : '';
                }
                
                const ticketID = form.querySelector('input[name="ticketID"]').value;
                if (!ticketID) {
                    alert("Please enter a ticket ID.");
                    return false;
                }

                
                const numInputs = form.querySelectorAll('input[type="number"][name^="num"]');
                for (let i = 0; i < numInputs.length; i++) {
                    if (!numInputs[i].value) {
                        alert("Please enter all 6 numbers.");
                        return false;
                    }
                }

                
                const selectedCompany = document.getElementById('selectedCompanyInput').value;
                if (!selectedCompany) {
                    alert("Please select a company.");
                    return false;
                }

                return true;
            }

            
            window.addEventListener('DOMContentLoaded', function () {
                const selectAll = document.getElementById('selectAllCheckbox');
                if (selectAll) {
                    selectAll.addEventListener('change', function () {
                        const checkboxes = document.querySelectorAll('input[name="ticketCheckbox"]');
                        checkboxes.forEach(checkbox => {
                            checkbox.checked = this.checked;
                        });
                        updateSelectedTickets();
                    });
                }
            });

            var selectedTickets = [];
            function updateSelectedTickets() {
                selectedTickets = [];
                const checkboxes = document.querySelectorAll('input[name="ticketCheckbox"]:checked');
                checkboxes.forEach(checkbox => {
                    selectedTickets.push(checkbox.value);
                });

                const hiddenInput = document.getElementById('selectedTicketIds');
                if (hiddenInput) {
                    hiddenInput.value = selectedTickets.join(',');
                }
            }

            function selectAllTickets() {
                const checkboxes = document.querySelectorAll('input[name="ticketCheckbox"]');
                checkboxes.forEach(checkbox => {
                    checkbox.checked = true;
                });
                const selectAll = document.getElementById('selectAllCheckbox');
                if (selectAll) selectAll.checked = true;
                
                
                selectedTickets = ['ALL'];
                const hiddenInput = document.getElementById('selectedTicketIds');
                if (hiddenInput) {
                    hiddenInput.value = 'ALL';
                }
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

            function toggleAllCheckboxes(masterCheckbox) {
                const checkboxes = document.querySelectorAll('input[name="ticketCheckbox"]');
                checkboxes.forEach(checkbox => {
                    checkbox.checked = masterCheckbox.checked;
                });
                updateSelectedTickets();
            }

            function performBulkAction(type, value) {
                if (selectedTickets.length === 0) {
                    alert("Please select at least one ticket.");
                    return;
                }

                let actionName = "";
                if (type === 'publish') actionName = value === 'true' ? "publish" : "unpublish";

                let ticketCount = selectedTickets[0] === 'ALL' ? parseInt('${totalTickets}', 10) || 0 : selectedTickets.length;
                let confirmMessage = selectedTickets[0] === 'ALL' 
                    ? "Are you sure you want to " + actionName + " ALL " + ticketCount + " ticket(s) matching your search across all pages?"
                    : "Are you sure you want to " + actionName + " " + ticketCount + " ticket(s)?";

                if (confirm(confirmMessage)) {
                    const form = document.getElementById('bulkActionForm');
                    document.getElementById('bulkUpdateType').value = type;
                    document.getElementById('bulkUpdateValue').value = value;
                    form.submit();
                }
            }

        </script>

        <script>
            function repeatSearch(searchPrompt, searchId) {
                
                const params = {};
                const parts = searchPrompt.split(', ');

                parts.forEach(function (part) {
                    if (part.startsWith('Company: ')) {
                        params.company = part.substring('Company: '.length);
                    } else if (part.startsWith('Numbers: ')) {
                        const numbers = part.substring('Numbers: '.length).split(',');
                        params.num1 = numbers[0];
                        params.num2 = numbers[1];
                        params.num3 = numbers[2];
                        params.num4 = numbers[3];
                        params.num5 = numbers[4];
                        params.num6 = numbers[5];
                    } else if (part.startsWith('Date Range: ')) {
                        const dates = part.substring('Date Range: '.length).split(' to ');
                        params.startDate = dates[0];
                        params.endDate = dates[1];
                    } else if (part.startsWith('Date: ')) {
                        params.specificDate = part.substring('Date: '.length);
                    }
                });

                
                const searchForm = document.getElementById('searchForm');
                if (searchForm) {
                    searchForm.querySelector('input[name="num1"]').value = params.num1 || '';
                    searchForm.querySelector('input[name="num2"]').value = params.num2 || '';
                    searchForm.querySelector('input[name="num3"]').value = params.num3 || '';
                    searchForm.querySelector('input[name="num4"]').value = params.num4 || '';
                    searchForm.querySelector('input[name="num5"]').value = params.num5 || '';
                    searchForm.querySelector('input[name="num6"]').value = params.num6 || '';
                    searchForm.querySelector('input[name="startDate"]').value = params.startDate || '';
                    searchForm.querySelector('input[name="endDate"]').value = params.endDate || '';
                    searchForm.querySelector('input[name="specificDate"]').value = params.specificDate || '';

                    
                    searchForm.querySelector('input[name="fromHistory"]').value = 'true';
                    searchForm.querySelector('input[name="searchId"]').value = searchId;
                }

                
                if (searchCompanySelector) {
                    const selected = searchCompanySelector.getSelectedCompanies();
                    document.getElementById('selected-companies-input-search').value = selected.join(',');
                }

                
                localStorage.setItem('scrollPosition', window.scrollY);

                
                document.querySelector('.search-form').submit();
            }

            function resetForm() {
                const form = document.getElementById('searchForm');
                form.querySelector('input[name="num1"]').value = '';
                form.querySelector('input[name="num2"]').value = '';
                form.querySelector('input[name="num3"]').value = '';
                form.querySelector('input[name="num4"]').value = '';
                form.querySelector('input[name="num5"]').value = '';
                form.querySelector('input[name="num6"]').value = '';
                form.querySelector('input[name="startDate"]').value = '';
                form.querySelector('input[name="endDate"]').value = '';
                form.querySelector('input[name="specificDate"]').value = '';
                form.querySelector('input[name="fromHistory"]').value = '';
                form.querySelector('input[name="searchId"]').value = '';
                if (searchCompanySelector) searchCompanySelector.clearAllSelections();
            }

            function goToPage(page) {
                const pageInput = document.getElementById('searchPageInput');
                if (pageInput) {
                    pageInput.value = page;
                    
                    if (searchCompanySelector) {
                        const selected = searchCompanySelector.getSelectedCompanies();
                        document.getElementById('selected-companies-input-search').value = selected.join(',');
                    }
                    document.getElementById('searchForm').submit();
                }
            }

        </script>
        <script>
            window.addEventListener('DOMContentLoaded', function () {
                
                const elements = document.querySelectorAll('button, .btn');
                elements.forEach(element => {
                    if (!element.classList.contains('lottery-btn')) {
                        element.classList.add('lottery-btn');
                    }
                });

                
                const successMessage = document.querySelector('.message.success');
                const errorMessage = document.querySelector('.message.error');
                if (successMessage || errorMessage) {
                    
                    let targetElement = null;

                    
                    const urlParams = new URLSearchParams(window.location.search);
                    const searchAction = urlParams.get('action');

                    if (searchAction === 'search' || searchAction === 'clearHistory') {
                        
                        targetElement = document.querySelector('.search-box h3');
                    } else if (searchAction === 'create') {
                        
                        targetElement = document.querySelector('.create-form h3');
                    } else if (searchAction && searchAction.includes('bulk')) {
                        
                        const bulkHeaders = document.querySelectorAll('.create-form h3');
                        if (bulkHeaders.length > 1) {
                            targetElement = bulkHeaders[1]; 
                        } else {
                            targetElement = bulkHeaders[0];
                        }
                    } else {
                        
                        targetElement = document.querySelector('.ticket-table');
                    }

                    
                    if (!targetElement) {
                        targetElement = successMessage || errorMessage;
                    }

                    if (targetElement) {
                        targetElement.scrollIntoView({ behavior: 'smooth' });
                    }
                }
            });
        </script>
        <script>
            
            function renderLotteryBalls() {
                const cells = document.querySelectorAll('.ticket-numbers-cell');
                cells.forEach(cell => {
                    const content = cell.textContent.trim();
                    if (!content) return;
                    const numbers = content.split(/[\s,]+/);
                    cell.innerHTML = '';
                    
                    const container = document.createElement('div');
                    container.className = 'table-numbers';
                    
                    numbers.forEach((num, index) => {
                        const ball = document.createElement('span');
                        ball.className = `lottery-ball ball-${(index % 6) + 1}`;
                        ball.textContent = num;
                        container.appendChild(ball);

                        if (index < numbers.length - 1) {
                            const separator = document.createElement('span');
                            separator.className = 'ball-separator';
                            separator.textContent = ',';
                            container.appendChild(separator);
                        }
                    });
                    cell.appendChild(container);
                });
            }

            window.addEventListener('DOMContentLoaded', renderLotteryBalls);
        </script>

<div id="ticketDetailModal" class="ticket-detail-modal" onclick="if(event.target===this)closeTicketDetail()">
    <div class="ticket-detail-content">
        <h3>Ticket Details</h3>
        <div class="ticket-detail-row">
            <span class="ticket-detail-label">ID</span>
            <span class="ticket-detail-value" id="detail-id"></span>
        </div>
        <div class="ticket-detail-row">
            <span class="ticket-detail-label">Numbers</span>
            <span class="ticket-detail-value" id="detail-numbers"></span>
        </div>
        <div class="ticket-detail-row">
            <span class="ticket-detail-label">Company</span>
            <span class="ticket-detail-value" id="detail-company"></span>
        </div>
        <div class="ticket-detail-row">
            <span class="ticket-detail-label">Creation Date</span>
            <span class="ticket-detail-value" id="detail-date"></span>
        </div>
        <div class="ticket-detail-row">
            <span class="ticket-detail-label">View Count</span>
            <span class="ticket-detail-value" id="detail-views"></span>
        </div>
        <div class="ticket-detail-row">
            <span class="ticket-detail-label">Status</span>
            <span class="ticket-detail-value" id="detail-status"></span>
        </div>
        <button class="ticket-detail-close" onclick="closeTicketDetail()">Close</button>
    </div>
</div>

<script>
    function showTicketDetail(id, numbers, company, date, views, published) {
        document.getElementById('detail-id').textContent = id;
        document.getElementById('detail-numbers').textContent = numbers;
        document.getElementById('detail-company').textContent = company;
        document.getElementById('detail-date').textContent = date;
        document.getElementById('detail-views').textContent = views;
        document.getElementById('detail-status').textContent = published ? 'Published' : 'Unpublished';
        document.getElementById('ticketDetailModal').classList.add('active');
        
        document.querySelectorAll('.actions-header.active').forEach(function(h) {
            h.classList.remove('active');
            h.nextElementSibling.classList.remove('show');
        });
    }
    function closeTicketDetail() {
        document.getElementById('ticketDetailModal').classList.remove('active');
    }
</script>
</body>

</html>

