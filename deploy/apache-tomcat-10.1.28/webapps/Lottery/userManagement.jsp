<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
        <%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
            <%@ page import="com.lottery.model.User" %>
                <%@ page import="java.util.List" %>
                    <%@ page import="java.util.Enumeration" %>

                        <!DOCTYPE html>
                        <html>

                        <head>
                            <meta charset="UTF-8">
                            <meta name="viewport" content="width=device-width, initial-scale=1.0">
                            <title>Fortuna Lotto - User Management</title>
                            <link rel="stylesheet"
                                href="${pageContext.request.contextPath}/assets/css/variables.css?v=<%= System.currentTimeMillis() %>">
                            <link rel="stylesheet"
                                href="${pageContext.request.contextPath}/assets/css/lottery-style.css?v=<%= System.currentTimeMillis() %>">
                            <link rel="stylesheet"
                                href="${pageContext.request.contextPath}/assets/css/adminShared.css?v=<%= System.currentTimeMillis() %>">
                            <link rel="stylesheet"
                                href="${pageContext.request.contextPath}/assets/css/userManagement.css?v=<%= System.currentTimeMillis() %>">
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
                            <script
                                src="${pageContext.request.contextPath}/assets/js/custom-select.js?v=<%= System.currentTimeMillis() %>"></script>
                        </head>

                        <body class="admin-page">
                            <div class="dashboard-container">
                                <jsp:include page="/WEB-INF/fragments/adminHeader.jsp">
                                    <jsp:param name="pageTitle" value="User Management"/>
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

                                            <div class="search-box">
                                                <h3>Search Users</h3>
                                                <form class="search-form" method="get" action="userManagement">
                                                    <input type="hidden" name="action" id="filter-action"
                                                        value="${param.action}">
                                                    <input type="hidden" name="period" id="filter-period"
                                                        value="${param.period}">

                                                    <div class="search-controls"
                                                        style="display: flex; gap: 10px; align-items: flex-end; margin-bottom: 15px;">
                                                        <div style="flex: 1;">
                                                            <input type="text" name="search" placeholder="Search..."
                                                                value="${param.search}"
                                                                style="width: 100%; padding: 10px 15px; border-radius: 20px; border: 1px solid #3a3f5a; background-color: #222538; color: #ffffff; box-sizing: border-box;">
                                                        </div>
                                                        <button type="submit" class="btn btn-sapphire lottery-btn"
                                                            onclick="document.getElementById('filter-action').value=''; document.getElementById('filter-period').value='';"
                                                            style="height: 42px;">Search</button>
                                                    </div>

                                                    
                                                    <div class="filters" style="margin-bottom: 15px;">
                                                        <span
                                                            style="color: #ffffff; margin-right: 15px; display: block; margin-bottom: 8px; font-weight: 600;">Search
                                                            In Fields:</span>
                                                        <div
                                                            style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px; background: rgba(0,0,0,0.2); padding: 10px; border-radius: 10px; border: 1px solid #3a3f5a;">
                                                            <label
                                                                style="color: #ffffff; cursor: pointer; display: flex; align-items: center; gap: 8px;">
                                                                <input type="checkbox" id="selectAllFields" ${empty
                                                                    searchFields ? 'checked' : '' }
                                                                    style="accent-color: #4a90e2; width: 18px; height: 18px;">
                                                                <strong>All Fields</strong>
                                                            </label>
                                                            <label
                                                                style="color: #ffffff; cursor: pointer; display: flex; align-items: center; gap: 8px;">
                                                                <input type="checkbox" name="searchFields" value="Email"
                                                                    ${searchFields.contains('Email' ) ? 'checked' : '' }
                                                                    class="field-checkbox"
                                                                    style="accent-color: #4a90e2; width: 18px; height: 18px;">
                                                                Email
                                                            </label>
                                                            <label
                                                                style="color: #ffffff; cursor: pointer; display: flex; align-items: center; gap: 8px;">
                                                                <input type="checkbox" name="searchFields"
                                                                    value="FirstName"
                                                                    ${searchFields.contains('FirstName' ) ? 'checked'
                                                                    : '' } class="field-checkbox"
                                                                    style="accent-color: #4a90e2; width: 18px; height: 18px;">
                                                                First Name
                                                            </label>
                                                            <label
                                                                style="color: #ffffff; cursor: pointer; display: flex; align-items: center; gap: 8px;">
                                                                <input type="checkbox" name="searchFields"
                                                                    value="LastName" ${searchFields.contains('LastName'
                                                                    ) ? 'checked' : '' } class="field-checkbox"
                                                                    style="accent-color: #4a90e2; width: 18px; height: 18px;">
                                                                Last Name
                                                            </label>
                                                            <label
                                                                style="color: #ffffff; cursor: pointer; display: flex; align-items: center; gap: 8px;">
                                                                <input type="checkbox" name="searchFields" value="Phone"
                                                                    ${searchFields.contains('Phone' ) ? 'checked' : '' }
                                                                    class="field-checkbox"
                                                                    style="accent-color: #4a90e2; width: 18px; height: 18px;">
                                                                Phone
                                                            </label>
                                                            <label
                                                                style="color: #ffffff; cursor: pointer; display: flex; align-items: center; gap: 8px;">
                                                                <input type="checkbox" name="searchFields"
                                                                    value="UserID" ${searchFields.contains('UserID' )
                                                                    ? 'checked' : '' } class="field-checkbox"
                                                                    style="accent-color: #4a90e2; width: 18px; height: 18px;">
                                                                User ID
                                                            </label>
                                                        </div>
                                                    </div>

                                                    <hr
                                                        style="border: 0; border-top: 1px solid #3a3f5a; margin: 20px 0;">

                                                    
                                                    <div class="filters" style="margin-bottom: 15px;">
                                                        <span
                                                            style="color: #ffffff; margin-right: 10px; display: inline-flex; align-items: center; height: 36px; min-width: 120px;">Last
                                                            Login:</span>
                                                        <div class="custom-select-container" style="width: 200px;" data-onchange="setDateRange(parseInt(this.value));">
                                                            <input type="hidden" value="${empty lastLoginFrom and empty lastLoginTo ? '0' : (lastLoginFrom eq todayMinus1 ? '1' : (lastLoginFrom eq todayMinus7 ? '7' : (lastLoginFrom eq todayMinus30 ? '30' : (lastLoginFrom eq todayMinus90 ? '90' : (lastLoginFrom eq todayMinus365 ? '365' : '0')))))}">
                                                            <div class="custom-select-header" onclick="toggleCustomSelect(this, event)">
                                                                <span class="custom-select-text">All Users</span>
                                                                <i class="fas fa-chevron-down dropdown-icon"></i>
                                                            </div>
                                                            <ul class="custom-select-list">
                                                                <li class="custom-select-item" data-value="0" onclick="selectCustomItem(this, event)">All Users</li>
                                                                <li class="custom-select-item" data-value="1" onclick="selectCustomItem(this, event)">1 Day</li>
                                                                <li class="custom-select-item" data-value="7" onclick="selectCustomItem(this, event)">1 Week</li>
                                                                <li class="custom-select-item" data-value="30" onclick="selectCustomItem(this, event)">1 Month</li>
                                                                <li class="custom-select-item" data-value="90" onclick="selectCustomItem(this, event)">1 Quarter</li>
                                                                <li class="custom-select-item" data-value="365" onclick="selectCustomItem(this, event)">1 Year</li>
                                                            </ul>
                                                        </div>
                                                    </div>

                                                    
                                                    <div class="filters" style="margin-bottom: 15px;">
                                                        <span
                                                            style="color: #ffffff; margin-right: 10px; display: inline-flex; align-items: center; height: 36px; min-width: 120px;">Date
                                                            Range:</span>
                                                        <input type="text" id="lastLoginFrom" name="lastLoginFrom"
                                                            class="date-picker" value="${lastLoginFrom}"
                                                            placeholder="YYYY-MM-DD"
                                                            style="padding: 5px 15px; border-radius: 20px; border: 1px solid #3a3f5a; background-color: #222538; color: #ffffff; height: 36px; box-sizing: border-box;">
                                                        <label for="lastLoginTo"
                                                            style="color: #ffffff; margin: 0 15px; display: inline-flex; align-items: center; height: 36px;">to</label>
                                                        <input type="text" id="lastLoginTo" name="lastLoginTo"
                                                            class="date-picker" value="${lastLoginTo}"
                                                            placeholder="YYYY-MM-DD"
                                                            style="padding: 5px 15px; border-radius: 20px; border: 1px solid #3a3f5a; background-color: #222538; color: #ffffff; height: 36px; box-sizing: border-box;">
                                                    </div>

                                                    
                                                    <div class="filters" style="margin-bottom: 20px;">
                                                        <span
                                                            style="color: #ffffff; margin-right: 10px; display: inline-flex; align-items: center; height: 36px; min-width: 120px;">Role:</span>
                                                        <div class="custom-select-container" style="width: 200px;" data-onchange="this.closest('form').submit();">
                                                            <input type="hidden" name="role" value="${empty param.role ? '' : fn:escapeXml(param.role)}">
                                                            <div class="custom-select-header" onclick="toggleCustomSelect(this, event)">
                                                                <span class="custom-select-text">All Roles</span>
                                                                <i class="fas fa-chevron-down dropdown-icon"></i>
                                                            </div>
                                                            <ul class="custom-select-list">
                                                                <li class="custom-select-item" data-value="" onclick="selectCustomItem(this, event)">All Roles</li>
                                                                <li class="custom-select-item" data-value="admin" onclick="selectCustomItem(this, event)">Admin</li>
                                                                <li class="custom-select-item" data-value="user" onclick="selectCustomItem(this, event)">User</li>
                                                            </ul>
                                                        </div>
                                                    </div>

                                                    
                                                    <div style="display: flex; justify-content: flex-end;">
                                                        <button type="submit" class="btn btn-emerald lottery-btn"
                                                            onclick="document.getElementById('filter-action').value=''; document.getElementById('filter-period').value='';"
                                                            style="padding: 10px 30px; font-weight: 600;">Apply All
                                                            Filters</button>
                                                    </div>

                                                </form>
                                            </div>

                                            <div class="invitation-form" style="background-color: #1a1c29; padding: 25px; border-radius: 12px; border: 1px solid #3a3f5a; margin-bottom: 30px;">
                                                <h3 style="color: #ffffff; margin-top: 0; margin-bottom: 20px; font-size: 1.5rem;">Create New User</h3>
                                                <form method="post" action="userManagement" id="createUserForm">
                                                    <input type="hidden" name="action" value="createUser">
                                                    
                                                    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-bottom: 20px;">
                                                        <div class="form-group" style="margin-bottom: 0;">
                                                            <label for="firstName" style="color: #a0a5b5; display: block; margin-bottom: 8px; font-weight: 500;">First Name *</label>
                                                            <input type="text" id="firstName" name="firstName" placeholder="First Name" required
                                                                style="width: 100%; padding: 12px 15px; border-radius: 8px; border: 1px solid #3a3f5a; background-color: #222538; color: #ffffff; box-sizing: border-box; transition: all 0.3s ease;">
                                                        </div>
                                                        <div class="form-group" style="margin-bottom: 0;">
                                                            <label for="lastName" style="color: #a0a5b5; display: block; margin-bottom: 8px; font-weight: 500;">Last Name *</label>
                                                            <input type="text" id="lastName" name="lastName" placeholder="Last Name" required
                                                                style="width: 100%; padding: 12px 15px; border-radius: 8px; border: 1px solid #3a3f5a; background-color: #222538; color: #ffffff; box-sizing: border-box; transition: all 0.3s ease;">
                                                        </div>
                                                    </div>

                                                    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-bottom: 20px;">
                                                        <div class="form-group" style="margin-bottom: 0;">
                                                            <label for="email" style="color: #a0a5b5; display: block; margin-bottom: 8px; font-weight: 500;">Email Address *</label>
                                                            <input type="email" id="email" name="email" placeholder="Email Address" required
                                                                style="width: 100%; padding: 12px 15px; border-radius: 8px; border: 1px solid #3a3f5a; background-color: #222538; color: #ffffff; box-sizing: border-box; transition: all 0.3s ease;">
                                                        </div>
                                                        <div class="form-group" style="margin-bottom: 0;">
                                                            <label for="password" style="color: #a0a5b5; display: block; margin-bottom: 8px; font-weight: 500;">Initial Password *</label>
                                                            <input type="password" id="password" name="password" placeholder="Password" required
                                                                style="width: 100%; padding: 12px 15px; border-radius: 8px; border: 1px solid #3a3f5a; background-color: #222538; color: #ffffff; box-sizing: border-box; transition: all 0.3s ease;">
                                                        </div>
                                                    </div>

                                                    <div style="display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 20px; margin-bottom: 25px; align-items: end;">
                                                        <div class="form-group" style="margin-bottom: 0;">
                                                            <label for="role" style="color: #a0a5b5; display: block; margin-bottom: 8px; font-weight: 500;">Role *</label>
                                                            <div class="custom-select-container" style="width: 100%;">
                                                                <input type="hidden" id="role" name="role" value="user" required>
                                                                <div class="custom-select-header" onclick="toggleCustomSelect(this, event)">
                                                                    <span class="custom-select-text">User</span>
                                                                    <i class="fas fa-chevron-down dropdown-icon"></i>
                                                                </div>
                                                                <ul class="custom-select-list">
                                                                    <li class="custom-select-item" data-value="user" onclick="selectCustomItem(this, event)">User</li>
                                                                    <li class="custom-select-item" data-value="admin" onclick="selectCustomItem(this, event)">Admin</li>
                                                                </ul>
                                                            </div>
                                                        </div>
                                                        <div class="form-group" style="margin-bottom: 0;">
                                                            <label for="balance" style="color: #a0a5b5; display: block; margin-bottom: 8px; font-weight: 500;">Initial Balance *</label>
                                                            <div style="position: relative;">
                                                                <span style="position: absolute; left: 15px; top: 50%; transform: translateY(-50%); color: #a0a5b5;">$</span>
                                                                <input type="number" id="balance" name="balance" placeholder="0.00" value="0.00" step="0.01" min="0" required
                                                                    style="width: 100%; padding: 12px 15px 12px 30px; border-radius: 8px; border: 1px solid #3a3f5a; background-color: #222538; color: #ffffff; box-sizing: border-box; transition: all 0.3s ease;">
                                                            </div>
                                                        </div>
                                                        <div class="form-group" style="margin-bottom: 0; padding-bottom: 12px;">
                                                            <label style="display: flex; align-items: center; gap: 10px; color: #ffffff; cursor: pointer; user-select: none;">
                                                                <input type="checkbox" name="isActive" value="true" checked
                                                                    style="width: 20px; height: 20px; accent-color: #4a90e2; cursor: pointer;">
                                                                <span style="font-weight: 500;">Account Active</span>
                                                            </label>
                                                        </div>
                                                    </div>

                                                    <div style="display: flex; justify-content: flex-end;">
                                                        <button type="submit" class="btn btn-emerald lottery-btn" style="padding: 12px 30px; font-weight: 600; font-size: 1rem; border-radius: 8px;">
                                                            <i class="fas fa-user-plus" style="margin-right: 8px;"></i> Create User
                                                        </button>
                                                    </div>
                                                </form>
                                            </div>

                                            <% StringBuilder baseUrl = new StringBuilder("userManagement?");
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

                                            <c:if test="${not empty users}">
                                                <div class="search-box">
                                                    <div class="user-table-container">
                                                        <h3>Users (${totalUsers} total)</h3>
                                                        <form method="post" action="userManagement"
                                                            style="display: inline;">
                                                            <input type="hidden" name="action" value="bulkDeactivate">
                                                            <button type="submit" class="btn btn-ruby lottery-btn"
                                                                style="margin-bottom: 10px;" form="bulkForm">Deactivate
                                                                Selected</button>
                                                        </form>
                                                        <a href="exportUsers?${fn:escapeXml(computedBaseUrl.replace('userManagement?', ''))}"
                                                            class="btn btn-success lottery-btn"
                                                            style="margin-bottom: 10px;">Export to Excel</a>

                                                        <div class="user-table-wrapper">
                                                            <table class="user-table">
                                                                <thead>
                                                                    <tr>
                                                                        <th><input type="checkbox" id="selectAll">
                                                                        </th>
                                                                        <th>ID</th>
                                                                        <th>Email</th>
                                                                        <th>Name</th>
                                                                        <th>Phone</th>
                                                                        <th>Role</th>
                                                                        <th>Last Login</th>
                                                                        <th>Status</th>
                                                                        <th>Actions</th>
                                                                    </tr>
                                                                </thead>
                                                                <tbody>
                                                                    <c:forEach var="user" items="${users}">
                                                                        <tr>
                                                                            <td data-label="Select"><input type="checkbox"
                                                                                    name="selectedUsers"
                                                                                    value="${user.userID}"
                                                                                    form="bulkForm"></td>
                                                                            <td data-label="ID">${user.userID}</td>
                                                                            <td data-label="Email">${user.email}</td>
                                                                            <td data-label="Name">${user.firstName} ${user.lastName}
                                                                            </td>
                                                                            <td data-label="Phone">${user.phone}</td>
                                                                            <td data-label="Role">
                                                                                <form method="post"
                                                                                    action="userManagement"
                                                                                    style="display: inline;">
                                                                                    <input type="hidden" name="action"
                                                                                        value="updateRole">
                                                                                    <input type="hidden" name="userId"
                                                                                        value="${user.userID}">
                                                                                    <div class="custom-select-container" data-onchange="this.closest('form').submit();" style="width: 110px;">
                                                                                        <input type="hidden" name="role" value="${user.role}">
                                                                                        <div class="custom-select-header" onclick="toggleCustomSelect(this, event)" style="height: 40px; padding: 0 10px;">
                                                                                            <span class="custom-select-text"></span>
                                                                                            <i class="fas fa-chevron-down dropdown-icon"></i>
                                                                                        </div>
                                                                                        <ul class="custom-select-list">
                                                                                            <li class="custom-select-item" data-value="admin" onclick="selectCustomItem(this, event)" style="height: 40px; min-height: 40px;">Admin</li>
                                                                                            <li class="custom-select-item" data-value="user" onclick="selectCustomItem(this, event)" style="height: 40px; min-height: 40px;">User</li>
                                                                                        </ul>
                                                                                    </div>
                                                                                </form>
                                                                            </td>
                                                                            <td data-label="Last Login">${user.lastLoginDate}</td>
                                                                            <td data-label="Status">
                                                                                <span
                                                                                    class="${user.active ? 'status-active' : 'status-inactive'}">
                                                                                    ${user.active ? 'Active' :
                                                                                    'Inactive'}
                                                                                </span>
                                                                            </td>
                                                                            <td data-label="Actions" class="actions">
                                                                                <div class="actions-container">
                                                                                    <div class="actions-header" onclick="toggleActionsDropdown(this, event)">
                                                                                        Actions <i class="fas fa-chevron-down dropdown-icon"></i>
                                                                                    </div>
                                                                                    <ul class="actions-list">
                                                                                        <li class="action-item" onclick="handleUserAction('toggleActive', '${user.userID}', '${user.active}')">
                                                                                            ${user.active ? 'Deactivate' : 'Activate'}
                                                                                        </li>
                                                                                        <li class="action-item" onclick="window.location.href='changePassword?userId=${user.userID}&action=reset'">
                                                                                            Reset Password
                                                                                        </li>
                                                                                        <li class="action-item" onclick="openEditUserForm('${user.userID}', '${fn:escapeXml(user.firstName)}', '${fn:escapeXml(user.lastName)}', '${fn:escapeXml(user.email)}')">
                                                                                            Edit
                                                                                        </li>
                                                                                    </ul>
                                                                                </div>
                                                                            </td>
                                                                        </tr>
                                                                    </c:forEach>
                                                                </tbody>
                                                            </table>
                                                        </div>

                                                        <form id="bulkForm" method="post" action="userManagement">
                                                            <input type="hidden" name="action" value="bulkDeactivate">
                                                        </form>

                                                            <c:set var="baseUrl" value="${computedBaseUrl}"
                                                                scope="request" />
                                                            <jsp:include page="/WEB-INF/fragments/pagination.jsp" />
                                                    </div>
                                                </div>
                                            </c:if>

                                            <c:if test="${empty users}">
                                                <div class="search-box">
                                                    <div class="user-table-container">
                                                        <p>No users found.</p>
                                                    </div>
                                                </div>
                                            </c:if>
                                        </div> 
                                    </div> 
                                </div> 

                                <div id="editUserModal" class="modal-overlay" style="display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); z-index: 9999; justify-content: center; align-items: flex-start; padding-top: 50px;">
                                    <div class="invitation-form" style="background-color: #1a1c29; padding: 25px; border-radius: 12px; border: 1px solid #3a3f5a; width: 500px; max-width: 90%; animation: slideDown 0.3s ease-out;">
                                        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
                                            <h3 style="color: #ffffff; margin: 0; font-size: 1.5rem;">Edit User</h3>
                                            <span style="color: #a0a5b5; cursor: pointer; font-size: 1.5rem;" onclick="closeEditUserForm()">&times;</span>
                                        </div>
                                        <form method="post" action="userManagement" id="editUserForm">
                                            <input type="hidden" name="action" value="updateUserAccount">
                                            <input type="hidden" name="userId" id="editUserId">
                                            
                                            <div class="form-group" style="margin-bottom: 15px;">
                                                <label for="editFirstName" style="color: #a0a5b5; display: block; margin-bottom: 8px; font-weight: 500;">First Name *</label>
                                                <input type="text" id="editFirstName" name="firstName" required
                                                    style="width: 100%; padding: 12px 15px; border-radius: 8px; border: 1px solid #3a3f5a; background-color: #222538; color: #ffffff; box-sizing: border-box;">
                                            </div>
                                            
                                            <div class="form-group" style="margin-bottom: 15px;">
                                                <label for="editLastName" style="color: #a0a5b5; display: block; margin-bottom: 8px; font-weight: 500;">Last Name *</label>
                                                <input type="text" id="editLastName" name="lastName" required
                                                    style="width: 100%; padding: 12px 15px; border-radius: 8px; border: 1px solid #3a3f5a; background-color: #222538; color: #ffffff; box-sizing: border-box;">
                                            </div>
                                            
                                            <div class="form-group" style="margin-bottom: 15px;">
                                                <label for="editEmail" style="color: #a0a5b5; display: block; margin-bottom: 8px; font-weight: 500;">Email Address *</label>
                                                <input type="email" id="editEmail" name="email" required
                                                    style="width: 100%; padding: 12px 15px; border-radius: 8px; border: 1px solid #3a3f5a; background-color: #222538; color: #ffffff; box-sizing: border-box;">
                                            </div>
                                            
                                            <div class="form-group" style="margin-bottom: 25px;">
                                                <label for="editPassword" style="color: #a0a5b5; display: block; margin-bottom: 8px; font-weight: 500;">New Password (leave blank to keep current)</label>
                                                <input type="password" id="editPassword" name="password" placeholder="New Password"
                                                    style="width: 100%; padding: 12px 15px; border-radius: 8px; border: 1px solid #3a3f5a; background-color: #222538; color: #ffffff; box-sizing: border-box;">
                                            </div>
                                            
                                            <div style="display: flex; justify-content: flex-end; gap: 10px;">
                                                <button type="button" class="btn lottery-btn btn-ruby" onclick="closeEditUserForm()">Cancel</button>
                                                <button type="submit" class="btn btn-emerald lottery-btn">Save</button>
                                            </div>
                                        </form>
                                    </div>
                                </div>
                                <style>
                                @keyframes slideDown {
                                    from { transform: translateY(-50px); opacity: 0; }
                                    to { transform: translateY(0); opacity: 1; }
                                }
                                </style>

                                <script>
                                    function toggleActionsDropdown(element, event) {
                                        if (event) event.stopPropagation();
                                        const list = element.nextElementSibling;
                                        
                                        
                                        document.querySelectorAll('.actions-header.active').forEach(h => {
                                            if (h !== element) {
                                                h.classList.remove('active');
                                                h.nextElementSibling.classList.remove('show');
                                            }
                                        });

                                        element.classList.toggle('active');
                                        list.classList.toggle('show');
                                    }

                                    function handleUserAction(actionType, userId, isActive) {
                                        if (actionType === 'toggleActive') {
                                            const actionWord = (isActive === 'true') ? 'deactivate' : 'activate';
                                            if (!confirm('Are you sure you want to ' + actionWord + ' this user?')) return;
                                            
                                            const form = document.createElement('form');
                                            form.method = 'post';
                                            form.action = 'userManagement';
                                            
                                            const typeInput = document.createElement('input');
                                            typeInput.type = 'hidden';
                                            typeInput.name = 'action';
                                            typeInput.value = 'setActiveStatus';
                                            form.appendChild(typeInput);
                                            
                                            const idInput = document.createElement('input');
                                            idInput.type = 'hidden';
                                            idInput.name = 'userId';
                                            idInput.value = userId;
                                            form.appendChild(idInput);
                                            
                                            const activeInput = document.createElement('input');
                                            activeInput.type = 'hidden';
                                            activeInput.name = 'isActive';
                                            activeInput.value = (isActive === 'true') ? 'false' : 'true';
                                            form.appendChild(activeInput);
                                            
                                            document.body.appendChild(form);
                                            form.submit();
                                        }
                                    }

                                    function openEditUserForm(userId, firstName, lastName, email) {
                                        document.getElementById('editUserId').value = userId;
                                        document.getElementById('editFirstName').value = firstName;
                                        document.getElementById('editLastName').value = lastName;
                                        document.getElementById('editEmail').value = email;
                                        document.getElementById('editPassword').value = '';
                                        
                                        const modal = document.getElementById('editUserModal');
                                        modal.style.display = 'flex';
                                    }

                                    function closeEditUserForm() {
                                        const modal = document.getElementById('editUserModal');
                                        modal.style.display = 'none';
                                    }

                                    function setDateRange(days) {
                                        const fromInput = document.getElementById('lastLoginFrom');
                                        const toInput = document.getElementById('lastLoginTo');
                                        const actionInput = document.getElementById('filter-action');
                                        const periodInput = document.getElementById('filter-period');
                                        const searchForm = document.querySelector('.search-form');

                                        if (actionInput) actionInput.value = '';
                                        if (periodInput) periodInput.value = '';

                                        if (days === 0) {
                                            fromInput.value = '';
                                            toInput.value = '';
                                        } else {
                                            const today = new Date();
                                            const targetDate = new Date();

                                            if (days === 30) {
                                                targetDate.setMonth(today.getMonth() - 1);
                                            } else if (days === 365) {
                                                targetDate.setFullYear(today.getFullYear() - 1);
                                            } else {
                                                targetDate.setDate(today.getDate() - days);
                                            }

                                            
                                            const year = targetDate.getFullYear();
                                            const month = String(targetDate.getMonth() + 1).padStart(2, '0');
                                            const day = String(targetDate.getDate()).padStart(2, '0');
                                            fromInput.value = year + '-' + month + '-' + day;
                                            const toYear = today.getFullYear();
                                            const toMonth = String(today.getMonth() + 1).padStart(2, '0');
                                            const toDay = String(today.getDate()).padStart(2, '0');
                                            toInput.value = toYear + '-' + toMonth + '-' + toDay;
                                        }

                                        if (searchForm) {
                                            searchForm.submit();
                                        }
                                    }

                                    window.addEventListener('DOMContentLoaded', function () {
                                        
                                        const elements = document.querySelectorAll('button, .btn, .filters button, .pagination button, .pagination a, a.btn');
                                        elements.forEach(element => {
                                            if (!element.classList.contains('lottery-btn')) {
                                                element.classList.add('lottery-btn');
                                            }
                                        });

                                        document.getElementById('customMessageToggle').addEventListener('click', function () {
                                            const container = document.getElementById('customMessageContainer');
                                            container.style.display = container.style.display === 'none' ? 'block' : 'none';
                                        });

                                        
                                        const selectAll = document.getElementById('selectAll');
                                        if (selectAll) {
                                            selectAll.addEventListener('change', function () {
                                                const checkboxes = document.querySelectorAll('input[name="selectedUsers"]');
                                                checkboxes.forEach(checkbox => {
                                                    checkbox.checked = this.checked;
                                                });
                                            });
                                        }

                                        
                                        const selectAllFields = document.getElementById('selectAllFields');
                                        if (selectAllFields) {
                                            const fieldCheckboxes = document.querySelectorAll('.field-checkbox');

                                            selectAllFields.addEventListener('change', function () {
                                                fieldCheckboxes.forEach(cb => {
                                                    cb.checked = this.checked;
                                                });
                                            });

                                            fieldCheckboxes.forEach(cb => {
                                                cb.addEventListener('change', function () {
                                                    if (!this.checked) {
                                                        selectAllFields.checked = false;
                                                    } else {
                                                        const allChecked = Array.from(fieldCheckboxes).every(c => c.checked);
                                                        if (allChecked) selectAllFields.checked = true;
                                                    }
                                                });
                                            });
                                        }

                                        
                                        document.addEventListener('click', function (event) {
                                            if (!event.target.closest('.actions-container')) {
                                                document.querySelectorAll('.actions-header.active').forEach(h => {
                                                    h.classList.remove('active');
                                                    h.nextElementSibling.classList.remove('show');
                                                });
                                            }
                                        });

                                        
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
                                </script>
                        </body>

                        </html>
