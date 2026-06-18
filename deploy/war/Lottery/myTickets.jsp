<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
        <%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
            <%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
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
    <title>Fortuna Lotto - My Tickets</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/variables.css?v=<%= System.currentTimeMillis() %>">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/lottery-style.css?v=<%= System.currentTimeMillis() %>">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/userShared.css?v=<%= System.currentTimeMillis() %>">
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@400;600;700&family=Poppins:wght@400;500;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css?v=<%= System.currentTimeMillis() %>">
    <script src="${pageContext.request.contextPath}/assets/js/custom-select.js?v=<%= System.currentTimeMillis() %>"></script>
</head>

<body class="dashboard-page">
    <div class="dashboard-container">
        <jsp:include page="/WEB-INF/fragments/userHeader.jsp">
            <jsp:param name="pageTitle" value="My Tickets"/>
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

                                                    
                                                    <div class="card">
                                                        <h3>My Purchased Tickets</h3>

                                                        <c:if test="${not empty tickets}">
                                                            <table class="ticket-table">
                                                                <thead>
                                                                    <tr>
                                                                        <th>Numbers</th>
                                                                        <th>Company</th>
                                                                        <th>Creation Date</th>
                                                                        <th>Price</th>
                                                                    </tr>
                                                                </thead>
                                                                <tbody>
                                                                    <c:forEach var="ticket" items="${tickets}">
                                                                        <tr>
                                                                            <td data-label="Numbers" class="ticket-numbers-cell">
                                                                                ${ticket.numbers}</td>
                                                                            <td data-label="Company">${ticket.company}</td>
                                                                            <td data-label="Creation Date">${ticket.creationDate}</td>
                                                                            <td data-label="Price">$${ticket.price}</td>
                                                                        </tr>
                                                                    </c:forEach>
                                                                </tbody>
                                                            </table>

                                                            <% StringBuilder baseUrl=new StringBuilder("myTickets?");
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
                                                        </c:if>

                                                        <c:if test="${empty tickets}">
                                                            <p>You haven't purchased any tickets yet.</p>
                                                            <p><a href="ticketPurchase"
                                                                    class="btn lottery-btn btn-emerald">Purchase
                                                                    Tickets</a></p>
                                                        </c:if>
                                                    </div>

                                                    
                                                    <div class="card" style="margin-top: 30px;">
                                                        <h3>Purchase History</h3>

                                                        <c:choose>
                                                            <c:when test="${not empty purchaseHistory}">
                                                                <p style="color: #a0a0b0; margin-bottom: 15px;">${totalHistoryCount} total record(s)</p>
                                                                <table class="ticket-table">
                                                                    <thead>
                                                                        <tr>
                                                                            <th>Time</th>
                                                                            <th>Ticket Number</th>
                                                                            <th>Company</th>
                                                                        </tr>
                                                                    </thead>
                                                                    <tbody>
                                                                        <c:forEach var="history" items="${purchaseHistory}">
                                                                            <tr>
                                                                                <td data-label="Time">${history.claimDate}</td>
                                                                                <td data-label="Ticket Number" class="ticket-numbers-cell">${history.numbers}</td>
                                                                                <td data-label="Company">${history.company}</td>
                                                                            </tr>
                                                                        </c:forEach>
                                                                    </tbody>
                                                                </table>

                                                                
                                                                <c:if test="${historyTotalPages > 1}">
                                                                    <%
                                                                        
                                                                        int hcp = 1;
                                                                        try { hcp = (Integer) request.getAttribute("historyCurrentPage"); } catch (Exception e) {}
                                                                        int htp = 0;
                                                                        try { htp = (Integer) request.getAttribute("historyTotalPages"); } catch (Exception e) {}
                                                                        if (htp > 1) {
                                                                            java.util.TreeSet<Integer> histPages = new java.util.TreeSet<>();
                                                                            for (int i = 1; i <= 3 && i <= htp; i++) histPages.add(i);
                                                                            for (int i = hcp - 1; i <= hcp + 1; i++) { if (i >= 1 && i <= htp) histPages.add(i); }
                                                                            for (int i = htp - 2; i <= htp; i++) { if (i >= 1) histPages.add(i); }
                                                                            request.setAttribute("histPageArray", histPages);
                                                                        }
                                                                    %>
                                                                    <c:set var="histBaseUrl" value="myTickets" />
                                                                    <div class="pagination-container">
                                                                        <div class="pagination">
                                                                            <a href="${histBaseUrl}?historyPage=1"
                                                                                class="lottery-btn ${historyCurrentPage == 1 ? 'disabled' : ''}" title="First Page">
                                                                                <i class="fas fa-angle-double-left"></i> <span class="pagination-text">First</span>
                                                                            </a>

                                                                            <c:if test="${historyCurrentPage > 1}">
                                                                                <a href="${histBaseUrl}?historyPage=${historyCurrentPage - 1}"
                                                                                    class="lottery-btn">
                                                                                    <i class="fas fa-angle-left"></i>
                                                                                </a>
                                                                            </c:if>

                                                                            <c:set var="prevHistPage" value="0" />
                                                                            <c:forEach var="pageNum" items="${histPageArray}">
                                                                                <c:if test="${prevHistPage > 0 && pageNum - prevHistPage > 1}">
                                                                                    <span class="pagination-ellipsis">...</span>
                                                                                </c:if>
                                                                                <a href="${histBaseUrl}?historyPage=${pageNum}"
                                                                                    class="lottery-btn ${pageNum == historyCurrentPage ? 'active' : ''}">
                                                                                    ${pageNum}
                                                                                </a>
                                                                                <c:set var="prevHistPage" value="${pageNum}" />
                                                                            </c:forEach>

                                                                            <c:if test="${historyCurrentPage < historyTotalPages}">
                                                                                <a href="${histBaseUrl}?historyPage=${historyCurrentPage + 1}"
                                                                                    class="lottery-btn">
                                                                                    <i class="fas fa-angle-right"></i>
                                                                                </a>
                                                                            </c:if>

                                                                            <a href="${histBaseUrl}?historyPage=${historyTotalPages}"
                                                                                class="lottery-btn ${historyCurrentPage == historyTotalPages ? 'disabled' : ''}" title="Last Page">
                                                                                <span class="pagination-text">Last</span> <i class="fas fa-angle-double-right"></i>
                                                                            </a>

                                                                            <div class="pagination-jump">
                                                                                <input type="number" id="jumpHistoryPageInput" class="jump-input" min="1"
                                                                                    max="${historyTotalPages}" placeholder="Page"
                                                                                    onkeypress="if(event.key === 'Enter') jumpToHistoryPage()"
                                                                                    style="width:60px;height:40px;padding:0 10px;border-radius:8px;background-color:#2a2c42;border:1px solid #3a3f5a;color:#fff;text-align:center;font-weight:500;font-size:14px;">
                                                                                <button type="button" class="lottery-btn jump-btn"
                                                                                    onclick="jumpToHistoryPage()" title="Go to page">
                                                                                    Go
                                                                                </button>
                                                                            </div>
                                                                        </div>

                                                                        <script>
                                                                            function jumpToHistoryPage() {
                                                                                const input = document.getElementById('jumpHistoryPageInput');
                                                                                const page = parseInt(input.value);
                                                                                const totalPages = parseInt('${historyTotalPages}');
                                                                                if (isNaN(page) || page < 1 || page > totalPages) {
                                                                                    alert('Please enter a valid page number between 1 and ' + totalPages);
                                                                                    return;
                                                                                }
                                                                                window.location.href = 'myTickets?historyPage=' + page;
                                                                            }
                                                                        </script>
                                                                    </div>
                                                                </c:if>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <p>No purchase history found.</p>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </div>

                                                    
                                                    <div class="card" style="margin-top: 30px;">
                                                        <h3>Financial Tracking</h3>
                                                        <form method="get" action="myTickets"
                                                            style="display: flex; gap: 10px; align-items: center; flex-wrap: wrap; margin-bottom: 20px;">
                                                            <label style="font-weight: 600;">Time Period:</label>
                                                            <button type="submit" name="timePeriod" value="week"
                                                                class="btn lottery-btn ${selectedTimePeriod == 'week' ? 'btn-sapphire' : ''}">Past Week</button>
                                                            <button type="submit" name="timePeriod" value="month"
                                                                class="btn lottery-btn ${selectedTimePeriod == 'month' ? 'btn-sapphire' : ''}">Past Month</button>
                                                            <button type="submit" name="timePeriod" value="quarter"
                                                                class="btn lottery-btn ${selectedTimePeriod == 'quarter' ? 'btn-sapphire' : ''}">Past Quarter</button>
                                                            <button type="submit" name="timePeriod" value="year"
                                                                class="btn lottery-btn ${selectedTimePeriod == 'year' ? 'btn-sapphire' : ''}">Past Year</button>
                                                        </form>

                                                        <c:if test="${not empty timeSummary}">
                                                            <div style="max-width: 520px;">
                                                                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px;">
                                                                    <div>Total Spent</div>
                                                                    <div style="text-align: right;">
                                                                        <fmt:formatNumber value="${timeSummary.totalSpent}" type="currency" />
                                                                    </div>
                                                                    <div>Total Winnings</div>
                                                                    <div style="text-align: right;">
                                                                        <fmt:formatNumber value="${timeSummary.totalWinnings}" type="currency" />
                                                                    </div>
                                                                    <div>Net Result</div>
                                                                    <div style="text-align: right;">
                                                                        <fmt:formatNumber value="${timeSummary.netResult}" type="currency" />
                                                                    </div>
                                                                </div>
                                                            </div>
                                                        </c:if>

                                                        <c:if test="${empty timeSummary}">
                                                            <p style="color: #a0a0b0;">Select a time period above to view your financial summary.</p>
                                                        </c:if>
                                                    </div>

                                                    
                                                    <div class="card" style="margin-top: 30px;">
                                                        <h3>Past Rounds</h3>
                                                        <c:choose>
                                                            <c:when test="${not empty pastRounds}">
                                                                <form method="get" action="myTickets"
                                                                    style="display: flex; gap: 12px; align-items: center; flex-wrap: wrap;">
                                                                    <label for="roundId"
                                                                        style="font-weight: 600;">Select Round</label>
                                                                    <div class="custom-select-container" style="min-width: 220px;">
                                                                        <input type="hidden" id="roundId" name="roundId" value="${empty selectedRoundId ? '' : selectedRoundId}">
                                                                        <div class="custom-select-header" onclick="toggleCustomSelect(this, event)">
                                                                            <span class="custom-select-text">Choose a past round</span>
                                                                            <i class="fas fa-chevron-down dropdown-icon"></i>
                                                                        </div>
                                                                        <ul class="custom-select-list">
                                                                            <li class="custom-select-item" data-value="" onclick="selectCustomItem(this, event)">Choose a past round</li>
                                                                            <c:forEach var="round" items="${pastRounds}">
                                                                                <li class="custom-select-item" data-value="${round.roundID}" onclick="selectCustomItem(this, event)">
                                                                                    ${round.startDate} - ${round.endDate} (Winning Numbers: ${round.winningNumbers})
                                                                                </li>
                                                                            </c:forEach>
                                                                        </ul>
                                                                    </div>
                                                                    <button type="submit"
                                                                        class="btn lottery-btn btn-emerald">View</button>
                                                                </form>

                                                                <c:if test="${not empty selectedRoundId}">
                                                                    <div style="margin-top: 20px;">
                                                                        <c:if test="${not empty historyTickets}">
                                                                            <table class="ticket-table">
                                                                                <thead>
                                                                                    <tr>
                                                                                        <th>Ticket ID</th>
                                                                                        <th>Numbers</th>
                                                                                        <th>Match Count</th>
                                                                                        <th>Purchase Price</th>
                                                                                        <th>Winnings</th>
                                                                                        <th>Claim Date</th>
                                                                                    </tr>
                                                                                </thead>
                                                                                <tbody>
                                                                                    <c:forEach var="history"
                                                                                        items="${historyTickets}">
                                                                                        <tr
                                                                                            class="${history.winnings > 0 ? 'winner' : ''}">
                                                                                            <td data-label="Ticket ID">${history.ticketID}
                                                                                            </td>
                                                                                            <td data-label="Numbers"
                                                                                                class="ticket-numbers-cell">
                                                                                                ${history.numbers}
                                                                                            </td>
                                                                                            <td data-label="Match Count">${history.matchCount}
                                                                                            </td>
                                                                                            <td data-label="Purchase Price">
                                                                                                <fmt:formatNumber
                                                                                                    value="${history.purchasePrice}"
                                                                                                    type="currency" />
                                                                                            </td>
                                                                                            <td data-label="Winnings">
                                                                                                <fmt:formatNumber
                                                                                                    value="${history.winnings}"
                                                                                                    type="currency" />
                                                                                            </td>
                                                                                            <td data-label="Claim Date">${history.claimDate}
                                                                                            </td>
                                                                                        </tr>
                                                                                    </c:forEach>
                                                                                </tbody>
                                                                            </table>
                                                                        </c:if>

                                                                        <c:if test="${empty historyTickets}">
                                                                            <p>No ticket history found for this
                                                                                round.</p>
                                                                        </c:if>
                                                                    </div>

                                                                        <c:if test="${not empty historySummary}">
                                                                            <div class="card"
                                                                                style="margin-top: 20px; max-width: 520px;">
                                                                                <h4 style="margin-top: 0;">Round Summary
                                                                                </h4>

                                                                                <c:if test="${not empty selectedRound and not empty selectedRound.winningNumbers}">
                                                                                    <div style="margin-bottom: 20px; text-align: center; background: #1a1a2e; padding: 15px; border-radius: 8px;">
                                                                                        <div style="color: #a0a0b0; font-size: 0.9em; margin-bottom: 5px; text-transform: uppercase;">Winning Numbers</div>
                                                                                        <div class="ticket-numbers-cell" style="font-size: 1.2em; display: inline-block;">
                                                                                            ${selectedRound.winningNumbers}
                                                                                        </div>
                                                                                    </div>
                                                                                </c:if>

                                                                                <div
                                                                                    style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px;">
                                                                                <div>Total Spent</div>
                                                                                <div style="text-align: right;">
                                                                                    <fmt:formatNumber
                                                                                        value="${historySummary.totalSpent}"
                                                                                        type="currency" />
                                                                                </div>
                                                                                <div>Total Winnings</div>
                                                                                <div style="text-align: right;">
                                                                                    <fmt:formatNumber
                                                                                        value="${historySummary.totalWinnings}"
                                                                                        type="currency" />
                                                                                </div>
                                                                                <div>Net Result</div>
                                                                                <div style="text-align: right;">
                                                                                    <fmt:formatNumber
                                                                                        value="${historySummary.netResult}"
                                                                                        type="currency" />
                                                                                </div>
                                                                            </div>
                                                                        </div>
                                                                    </c:if>
                                                                </c:if>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <p>No completed rounds yet.</p>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </div>

                                                </div>

                                            </div>
                                        </div>
                                    </div>
                                    <script>
                                        
                                        function renderLotteryBalls() {
                                            const cells = document.querySelectorAll('.ticket-numbers-cell');
                                            cells.forEach(cell => {
                                                const content = cell.textContent.trim();
                                                if (!content) return; 

                                                const numbers = content.split(/[\s,]+/).filter(n => n.trim() !== '');
                                                if (numbers.length === 0) return; 

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
