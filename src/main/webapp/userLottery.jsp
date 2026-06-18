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
    <title>Fortuna Lotto - Lottery Search</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/variables.css?v=<%= System.currentTimeMillis() %>">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/lottery-style.css?v=<%= System.currentTimeMillis() %>">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/userShared.css?v=<%= System.currentTimeMillis() %>">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/userLottery.css?v=<%= System.currentTimeMillis() %>">
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@400;600;700&family=Poppins:wght@400;500;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css?v=<%= System.currentTimeMillis() %>">
    <script src="${pageContext.request.contextPath}/assets/components/LotteryCompanySelector/companySelector.js?v=<%= System.currentTimeMillis() %>"></script>
    <script src="https://html2canvas.hertzen.com/dist/html2canvas.min.js?v=<%= System.currentTimeMillis() %>"></script>

    
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/components/DatePicker/flatpickr.min.css?v=<%= System.currentTimeMillis() %>">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/components/DatePicker/datepicker-theme.css?v=<%= System.currentTimeMillis() %>">
    <script src="${pageContext.request.contextPath}/assets/components/DatePicker/flatpickr.min.js?v=<%= System.currentTimeMillis() %>"></script>
    <script src="${pageContext.request.contextPath}/assets/components/DatePicker/datepicker-init.js?v=<%= System.currentTimeMillis() %>"></script>

</head>

<body class="dashboard-page">
    <div class="dashboard-container">
        <jsp:include page="/WEB-INF/fragments/userHeader.jsp">
            <jsp:param name="pageTitle" value="Lottery Search"/>
        </jsp:include>

        <div class="content-wrapper">
            <div class="main-content">
                <div class="sidebar">
                    <jsp:include page="/WEB-INF/fragments/sidebar.jsp" />
                </div>
                                                <div class="container">
                                                    <c:if test="${not empty sessionScope.message}">
                                                        <div class="message info">${sessionScope.message}</div>
                                                        <% session.removeAttribute("message"); %>
                                                    </c:if>

                                                    <div class="search-box">
                                                        <h3>Search Published Lottery Tickets</h3>
                                                        <form class="search-form" method="get" action="userLottery"
                                                            id="userSearchForm">
                                                            <input type="hidden" name="fromHistory" value="">
                                                            <input type="hidden" name="searchId" value="">

                                                            <div style="display: flex; flex-wrap: wrap; width: 100%;">
                                                                
                                                                <div
                                                                    style="flex: 1; min-width: 300px; padding: 10px; display: flex; flex-direction: column; align-items: flex-start; justify-content: flex-start; position: relative;">
                                                                    <div id="company-selector-container"
                                                                        style="width: 100%;"></div>
                                                                    
                                                                    <input type="hidden" name="company"
                                                                        id="selected-companies-input"
                                                                        value="${param.company}">
                                                                </div>

                                                                
                                                                <div
                                                                    style="flex: 1; min-width: 300px; padding: 10px; display: flex; flex-direction: column; align-items: center;">
                                                                    <div
                                                                        style="display: grid; grid-template-columns: repeat(2, 1fr); gap: 10px; width: 100%;">
                                                                        <input type="number" name="num1"
                                                                            placeholder="1st number" min="1" max="99"
                                                                            value="${param.num1}"
                                                                            style="padding: 12px; border: 1px solid #3a3f5a; border-radius: 8px; background-color: #1a1a2e; color: #ffffff; text-align: center; font-weight: 600;">
                                                                        <input type="number" name="num2"
                                                                            placeholder="2nd number" min="1" max="99"
                                                                            value="${param.num2}"
                                                                            style="padding: 12px; border: 1px solid #3a3f5a; border-radius: 8px; background-color: #1a1a2e; color: #ffffff; text-align: center; font-weight: 600;">
                                                                        <input type="number" name="num3"
                                                                            placeholder="3rd number" min="1" max="99"
                                                                            value="${param.num3}"
                                                                            style="padding: 12px; border: 1px solid #3a3f5a; border-radius: 8px; background-color: #1a1a2e; color: #ffffff; text-align: center; font-weight: 600;">
                                                                        <input type="number" name="num4"
                                                                            placeholder="4th number" min="1" max="99"
                                                                            value="${param.num4}"
                                                                            style="padding: 12px; border: 1px solid #3a3f5a; border-radius: 8px; background-color: #1a1a2e; color: #ffffff; text-align: center; font-weight: 600;">
                                                                        <input type="number" name="num5"
                                                                            placeholder="5th number" min="1" max="99"
                                                                            value="${param.num5}"
                                                                            style="padding: 12px; border: 1px solid #3a3f5a; border-radius: 8px; background-color: #1a1a2e; color: #ffffff; text-align: center; font-weight: 600;">
                                                                        <input type="number" name="num6"
                                                                            placeholder="6th number" min="1" max="99"
                                                                            value="${param.num6}"
                                                                            style="padding: 12px; border: 1px solid #3a3f5a; border-radius: 8px; background-color: #1a1a2e; color: #ffffff; text-align: center; font-weight: 600;">
                                                                    </div>

                                                                </div>

                                                                
                                                                <div
                                                                    style="flex: 1; min-width: 300px; padding: 10px; display: flex; flex-direction: column; align-items: center; position: relative;">
                                                                    <div
                                                                        style="display: grid; grid-template-columns: 1fr; gap: 10px; margin-top: 15px; width: 100%;">
                                                                        <label
                                                                            style="text-align: center; color: #ffffff;">From:</label>
                                                                        <input type="text" name="startDate"
                                                                            class="date-picker"
                                                                            value="${param.startDate}"
                                                                            placeholder="YYYY-MM-DD"
                                                                            style="padding: 12px; border: 1px solid #3a3f5a; border-radius: 8px; background-color: #1a1a2e; color: #ffffff; text-align: center;"
                                                                            onchange="handleDateSelection('range')">
                                                                        <label
                                                                            style="text-align: center; color: #ffffff;">To:</label>
                                                                        <input type="text" name="endDate"
                                                                            class="date-picker" value="${param.endDate}"
                                                                            placeholder="YYYY-MM-DD"
                                                                            style="padding: 12px; border: 1px solid #3a3f5a; border-radius: 8px; background-color: #1a1a2e; color: #ffffff; text-align: center;"
                                                                            onchange="handleDateSelection('range')">
                                                                        <label
                                                                            style="text-align: center; color: #ffffff;">On:</label>
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
                                                        <div class="search-box">
                                                            <div
                                                                style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
                                                                <h3>Published Lottery Tickets (${totalTickets} found)
                                                                </h3>
                                                            </div>

                                                            <table class="ticket-table compact-card">
                                                                <thead>
                                                                    <tr>
                                                                        <th>Numbers</th>
                                                                        <th>Company</th>
                                                                        <th>Creation Date</th>
                                                                    </tr>
                                                                </thead>
                                                                <tbody>
                                                                    <c:forEach var="ticket" items="${tickets}">
                                                                        <tr>
                                                                            <td data-label="Numbers" class="ticket-numbers-cell">
                                                                                ${ticket.numbers}</td>
                                                                            <td data-label="Company">${ticket.company}</td>
                                                                            <td data-label="Creation Date">${ticket.creationDate}</td>
                                                                        </tr>
                                                                    </c:forEach>
                                                                </tbody>
                                                            </table>

                                                            <% StringBuilder baseUrl=new StringBuilder("userLottery?");
                                                                Enumeration<String> paramNames =
                                                                request.getParameterNames();
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
                                                                <jsp:include page="/WEB-INF/fragments/pagination.jsp" />
                                                        </div>
                                                    </c:if>

                                                    <c:if test="${empty tickets && not empty param.company}">
                                                        <p>No published tickets found matching your search criteria.</p>
                                                    </c:if>

                                                    <c:if test="${empty param.company && empty tickets}">
                                                        <p>Use the search form above to find published lottery tickets.
                                                        </p>
                                                    </c:if>

                                                    
                                                    <c:if test="${not empty searchHistory}">
                                                        <div class="search-box" style="margin-top: 40px;">
                                                            <div
                                                                style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
                                                                <h3>Previous Searches</h3>
                                                                <form method="get" action="userLottery"
                                                                    style="margin: 0;">
                                                                    <input type="hidden" name="action"
                                                                        value="clearHistory">
                                                                    <button type="submit"
                                                                        onclick="return confirm('Are you sure you want to clear all search history?')"
                                                                        class="btn lottery-btn">
                                                                        Clear History
                                                                    </button>
                                                                </form>
                                                            </div>
                                                            <div style="max-height: 300px; overflow-y: auto;">
                                                                <c:forEach var="history" items="${searchHistory}">
                                                                    <div
                                                                        style="border-bottom: 1px solid #3a3f5a; padding: 10px 0; display: flex; justify-content: space-between; align-items: center;">
                                                                        <div>
                                                                            <strong>${history.searchPrompt}</strong><br>
                                                                            <small
                                                                                style="color: #a0a0b0;">${history.searchDate}</small>
                                                                        </div>
                                                                        <button
                                                                            onclick="repeatSearch('${history.searchPrompt}', '${history.searchId}')"
                                                                            class="btn lottery-btn">
                                                                            Search Again
                                                                        </button>
                                                                    </div>
                                                                </c:forEach>
                                                            </div>
                                                        </div>
                                                    </c:if>

                                                    
                                                    <c:if test="${not empty pastRounds}">
                                                        <div class="card" style="margin-top: 30px;">
                                                            <h3>Past Winning Results</h3>
                                                            <table class="ticket-table">
                                                                <thead>
                                                                    <tr>
                                                                        <th>Round ID</th>
                                                                        <th>Date</th>
                                                                        <th>Winning Numbers</th>
                                                                    </tr>
                                                                </thead>
                                                                <tbody>
                                                                    <c:forEach var="round" items="${pastRounds}">
                                                                        <tr>
                                                                            <td data-label="Round ID">${round.roundID}</td>
                                                                            <td data-label="Date">${round.startDate} - ${round.endDate}</td>
                                                                            <td data-label="Winning Numbers" class="ticket-numbers-cell">${round.winningNumbers}</td>
                                                                        </tr>
                                                                    </c:forEach>
                                                                </tbody>
                                                            </table>

                                                            
                                                            <c:if test="${roundsTotalPages > 1}">
                                                                <%
                                                                    
                                                                    int rcp = 1;
                                                                    try { rcp = (Integer) request.getAttribute("roundsCurrentPage"); } catch (Exception e) {}
                                                                    int rtp = 0;
                                                                    try { rtp = (Integer) request.getAttribute("roundsTotalPages"); } catch (Exception e) {}
                                                                    if (rtp > 1) {
                                                                        java.util.TreeSet<Integer> roundPages = new java.util.TreeSet<>();
                                                                        for (int i = 1; i <= 3 && i <= rtp; i++) roundPages.add(i);
                                                                        for (int i = rcp - 1; i <= rcp + 1; i++) { if (i >= 1 && i <= rtp) roundPages.add(i); }
                                                                        for (int i = rtp - 2; i <= rtp; i++) { if (i >= 1) roundPages.add(i); }
                                                                        request.setAttribute("roundPageArray", roundPages);
                                                                    }
                                                                %>
                                                                <c:set var="roundsBaseUrl" value="userLottery" />
                                                                <div class="pagination-container">
                                                                    <div class="pagination">
                                                                        <a href="${roundsBaseUrl}?roundsPage=1"
                                                                            class="lottery-btn ${roundsCurrentPage == 1 ? 'disabled' : ''}" title="First Page">
                                                                            <i class="fas fa-angle-double-left"></i> <span class="pagination-text">First</span>
                                                                        </a>

                                                                        <c:if test="${roundsCurrentPage > 1}">
                                                                            <a href="${roundsBaseUrl}?roundsPage=${roundsCurrentPage - 1}"
                                                                                class="lottery-btn">
                                                                                <i class="fas fa-angle-left"></i>
                                                                            </a>
                                                                        </c:if>

                                                                        <c:set var="prevRoundPage" value="0" />
                                                                        <c:forEach var="pageNum" items="${roundPageArray}">
                                                                            <c:if test="${prevRoundPage > 0 && pageNum - prevRoundPage > 1}">
                                                                                <span class="pagination-ellipsis">...</span>
                                                                            </c:if>
                                                                            <a href="${roundsBaseUrl}?roundsPage=${pageNum}"
                                                                                class="lottery-btn ${pageNum == roundsCurrentPage ? 'active' : ''}">
                                                                                ${pageNum}
                                                                            </a>
                                                                            <c:set var="prevRoundPage" value="${pageNum}" />
                                                                        </c:forEach>

                                                                        <c:if test="${roundsCurrentPage < roundsTotalPages}">
                                                                            <a href="${roundsBaseUrl}?roundsPage=${roundsCurrentPage + 1}"
                                                                                class="lottery-btn">
                                                                                <i class="fas fa-angle-right"></i>
                                                                            </a>
                                                                        </c:if>

                                                                        <a href="${roundsBaseUrl}?roundsPage=${roundsTotalPages}"
                                                                            class="lottery-btn ${roundsCurrentPage == roundsTotalPages ? 'disabled' : ''}" title="Last Page">
                                                                            <span class="pagination-text">Last</span> <i class="fas fa-angle-double-right"></i>
                                                                        </a>

                                                                        <div class="pagination-jump">
                                                                            <input type="number" id="jumpRoundsPageInput" class="jump-input" min="1"
                                                                                max="${roundsTotalPages}" placeholder="Page"
                                                                                onkeypress="if(event.key === 'Enter') jumpToRoundsPage()"
                                                                                style="width:60px;height:40px;padding:0 10px;border-radius:8px;background-color:#2a2c42;border:1px solid #3a3f5a;color:#fff;text-align:center;font-weight:500;font-size:14px;">
                                                                            <button type="button" class="lottery-btn jump-btn"
                                                                                onclick="jumpToRoundsPage()" title="Go to page">
                                                                                Go
                                                                            </button>
                                                                        </div>
                                                                    </div>

                                                                    <script>
                                                                        function jumpToRoundsPage() {
                                                                            const input = document.getElementById('jumpRoundsPageInput');
                                                                            const page = parseInt(input.value);
                                                                            const totalPages = parseInt('${roundsTotalPages}');
                                                                            if (isNaN(page) || page < 1 || page > totalPages) {
                                                                                alert('Please enter a valid page number between 1 and ' + totalPages);
                                                                                return;
                                                                            }
                                                                            window.location.href = 'userLottery?roundsPage=' + page;
                                                                        }
                                                                    </script>
                                                                </div>
                                                            </c:if>
                                                        </div>
                                                    </c:if>

                                                </div>
                                            </div>
                                        </div>

                                        <script>
                                            
                                            let userCompanySelector;

                                            
                                            window.allCompanies = [
                                                <c:forEach var="comp" items="${companies}" varStatus="status">
                                                    "${comp}"${!status.last ? ',' : ''}
                                                </c:forEach>
                                            ];

                                            document.addEventListener('DOMContentLoaded', function () {
                                                
                                                userCompanySelector = new CompanySelector('company-selector-container', window.allCompanies);

                                                
                                                const urlParams = new URLSearchParams(window.location.search);
                                                const companyParam = urlParams.get('company');
                                                if (companyParam) {
                                                    userCompanySelector.setSelectedCompanies(companyParam.split(','));
                                                }

                                                
                                                const searchForm = document.getElementById('userSearchForm');
                                                const hiddenInput = document.getElementById('selected-companies-input');
                                                if (searchForm && hiddenInput) {
                                                    searchForm.addEventListener('submit', function () {
                                                        const selected = userCompanySelector.getSelectedCompanies();
                                                        hiddenInput.value = selected.join(',');
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

                                                
                                                if (userCompanySelector && params.company) {
                                                    userCompanySelector.setSelectedCompanies(params.company.split(','));
                                                }

                                                
                                                document.querySelector('input[name="num1"]').value = params.num1 || '';
                                                document.querySelector('input[name="num2"]').value = params.num2 || '';
                                                document.querySelector('input[name="num3"]').value = params.num3 || '';
                                                document.querySelector('input[name="num4"]').value = params.num4 || '';
                                                document.querySelector('input[name="num5"]').value = params.num5 || '';
                                                document.querySelector('input[name="num6"]').value = params.num6 || '';
                                                document.querySelector('input[name="startDate"]').value = params.startDate || '';
                                                document.querySelector('input[name="endDate"]').value = params.endDate || '';
                                                document.querySelector('input[name="specificDate"]').value = params.specificDate || '';

                                                
                                                document.querySelector('input[name="fromHistory"]').value = 'true';
                                                document.querySelector('input[name="searchId"]').value = searchId;

                                                
                                                if (userCompanySelector) {
                                                    const selected = userCompanySelector.getSelectedCompanies();
                                                    const hiddenInput = document.getElementById('selected-companies-input');
                                                    if (hiddenInput) {
                                                        hiddenInput.value = selected.join(',');
                                                    }
                                                }

                                                
                                                localStorage.setItem('scrollPosition', window.scrollY);

                                                
                                                document.querySelector('.search-form').submit();
                                            }

                                            function resetForm() {
                                                
                                                document.querySelector('input[name="num1"]').value = '';
                                                document.querySelector('input[name="num2"]').value = '';
                                                document.querySelector('input[name="num3"]').value = '';
                                                document.querySelector('input[name="num4"]').value = '';
                                                document.querySelector('input[name="num5"]').value = '';
                                                document.querySelector('input[name="num6"]').value = '';
                                                document.querySelector('input[name="startDate"]').value = '';
                                                document.querySelector('input[name="endDate"]').value = '';
                                                document.querySelector('input[name="specificDate"]').value = '';

                                                
                                                document.querySelector('input[name="fromHistory"]').value = '';
                                                document.querySelector('input[name="searchId"]').value = '';

                                                
                                                if (userCompanySelector) {
                                                    userCompanySelector.setSelectedCompanies([]);
                                                }
                                            }

                                            function handleDateSelection(type) {
                                                if (type === 'specific') {
                                                    
                                                    document.querySelector('input[name="startDate"]').value = '';
                                                    document.querySelector('input[name="endDate"]').value = '';
                                                } else if (type === 'range') {
                                                    
                                                    document.querySelector('input[name="specificDate"]').value = '';
                                                }
                                            }

                                            window.addEventListener('DOMContentLoaded', function () {
                                                
                                                const elements = document.querySelectorAll('button, .btn');
                                                elements.forEach(element => {
                                                    if (!element.classList.contains('lottery-btn')) {
                                                        element.classList.add('lottery-btn');
                                                    }
                                                });
                                            });

                                            
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
                                </body>

                                </html>

