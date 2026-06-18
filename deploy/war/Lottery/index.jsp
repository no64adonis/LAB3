<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
        <%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
            <%@ page import="com.lottery.model.LotteryTicket" %>
                <%@ page import="java.util.List" %>
                    <%@ page import="java.util.Enumeration" %>
                        <%@ page import="com.lottery.db.LotteryTicketDAO" %>

                            <!DOCTYPE html>
                            <html>

                            <head>
                                <meta charset="UTF-8">
                                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                                <title>Fortuna Lotto - Lottery Ticket Checker</title>
                                <link rel="stylesheet"
                                    href="${pageContext.request.contextPath}/assets/css/variables.css">
                                <link rel="stylesheet"
                                    href="${pageContext.request.contextPath}/assets/css/lottery-style.css?v=<%= System.currentTimeMillis() %>">
                                <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/userLottery.css">
                                <link
                                    href="https://fonts.googleapis.com/css2?family=Montserrat:wght@400;600;700&family=Poppins:wght@400;500;600&display=swap"
                                    rel="stylesheet">
                                <link rel="stylesheet"
                                    href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
                                <script src="${pageContext.request.contextPath}/assets/components/LotteryCompanySelector/companySelector.js?v=<%= System.currentTimeMillis() %>"></script>
                                <script src="https://html2canvas.hertzen.com/dist/html2canvas.min.js"></script>

                                
                                <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/components/DatePicker/flatpickr.min.css">
                                <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/components/DatePicker/datepicker-theme.css">
                                <script src="${pageContext.request.contextPath}/assets/components/DatePicker/flatpickr.min.js"></script>
                                <script src="${pageContext.request.contextPath}/assets/components/DatePicker/datepicker-init.js"></script>
                                <style>
                                    @media (min-width: 769px) and (max-width: 1024px) {
                                        
                                        .search-form > div[style*="min-width: 300px"] {
                                            min-width: 0 !important;
                                        }
                                        
                                        .search-form > .left-column {
                                            flex: 0 0 55% !important;
                                        }
                                        
                                        .search-form > div:nth-child(2) {
                                            flex: 0 0 40% !important;
                                        }
                                        
                                        .search-form {
                                            flex-direction: row !important;
                                            flex-wrap: wrap !important;
                                        }
                                        
                                        .dropdown-container,
                                        .dropdown-header {
                                            min-width: 100% !important;
                                        }
                                        .selected-count {
                                            font-size: 0.8em !important;
                                        }
                                        
                                        .search-form input[type="number"] {
                                            width: 100% !important;
                                            min-width: 120px !important;
                                        }
                                        .search-box {
                                            padding: 15px !important;
                                        }
                                    }
                                </style>
                            </head>

                            <body>
                                <div class="full-width-header">
                                    <div class="header">
                                        <h1>Lottery Ticket Checker</h1>
                                        <div class="nav-links">
                                            <a href="login">Login</a>
                                            <a href="register">Register</a>
                                        </div>
                                    </div>
                                </div>

                                <div class="container" style="margin-top: 0; padding-top: 0; min-height: calc(100vh - 80px);">
                                    <c:if test="${not empty sessionScope.message}">
                                        <div class="message info">${sessionScope.message}</div>
                                        <% session.removeAttribute("message"); %>
                                    </c:if>
                                    <div class="welcome-text">
                                        <h2>Welcome to the Lottery Ticket Checker</h2>
                                        <p>Search for published lottery tickets using various criteria.</p>
                                    </div>

                                    <div class="search-box">
                                        <h3>Search Published Lottery Tickets</h3>
                                        <form class="search-form" method="get" action="homepage">
                                            <input type="hidden" name="fromHistory" value="">
                                            <input type="hidden" name="searchId" value="">
                                            
                                            <div class="left-column"
                                                style="flex: 1; min-width: 300px; padding: 10px; display: flex; flex-direction: column; gap: 15px;">
                                                
                                                <div>
                                                    <div id="company-selector-container"></div>
                                                    <input type="hidden" name="company" id="selected-companies-input"
                                                        value="${param.company}">
                                                </div>
                                                
                                                <div
                                                    style="display: grid; grid-template-columns: repeat(2, 1fr); gap: 10px; width: 100%;">
                                                    <input type="number" name="num1" placeholder="1st number" min="1"
                                                        max="99" value="${param.num1}"
                                                        style="padding: 12px; border: 1px solid #3a3f5a; border-radius: 8px; background-color: #1a1a2e; color: #ffffff; text-align: center; font-weight: 600;">
                                                    <input type="number" name="num2" placeholder="2nd number" min="1"
                                                        max="99" value="${param.num2}"
                                                        style="padding: 12px; border: 1px solid #3a3f5a; border-radius: 8px; background-color: #1a1a2e; color: #ffffff; text-align: center; font-weight: 600;">
                                                    <input type="number" name="num3" placeholder="3rd number" min="1"
                                                        max="99" value="${param.num3}"
                                                        style="padding: 12px; border: 1px solid #3a3f5a; border-radius: 8px; background-color: #1a1a2e; color: #ffffff; text-align: center; font-weight: 600;">
                                                    <input type="number" name="num4" placeholder="4th number" min="1"
                                                        max="99" value="${param.num4}"
                                                        style="padding: 12px; border: 1px solid #3a3f5a; border-radius: 8px; background-color: #1a1a2e; color: #ffffff; text-align: center; font-weight: 600;">
                                                    <input type="number" name="num5" placeholder="5th number" min="1"
                                                        max="99" value="${param.num5}"
                                                        style="padding: 12px; border: 1px solid #3a3f5a; border-radius: 8px; background-color: #1a1a2e; color: #ffffff; text-align: center; font-weight: 600;">
                                                    <input type="number" name="num6" placeholder="6th number" min="1"
                                                        max="99" value="${param.num6}"
                                                        style="padding: 12px; border: 1px solid #3a3f5a; border-radius: 8px; background-color: #1a1a2e; color: #ffffff; text-align: center; font-weight: 600;">
                                                </div>
                                            </div>

                                            
                                            <div
                                                style="flex: 1; min-width: 300px; padding: 10px; display: flex; flex-direction: column; align-items: center; position: relative;">
                                                <div
                                                    style="display: grid; grid-template-columns: 1fr; gap: 10px; margin-top: 15px; width: 100%;">
                                                    <label style="text-align: center; color: #ffffff;">From:</label>
                                                    <input type="text" name="startDate" class="date-picker"
                                                        value="${param.startDate}" placeholder="YYYY-MM-DD"
                                                        style="padding: 12px; border: 1px solid #3a3f5a; border-radius: 8px; background-color: #1a1a2e; color: #ffffff; text-align: center;"
                                                        onchange="handleDateSelection('range')">
                                                    <label style="text-align: center; color: #ffffff;">To:</label>
                                                    <input type="text" name="endDate" class="date-picker"
                                                        value="${param.endDate}" placeholder="YYYY-MM-DD"
                                                        style="padding: 12px; border: 1px solid #3a3f5a; border-radius: 8px; background-color: #1a1a2e; color: #ffffff; text-align: center;"
                                                        onchange="handleDateSelection('range')">
                                                    <label style="text-align: center; color: #ffffff;">On:</label>
                                                    <input type="text" name="specificDate" class="date-picker"
                                                        value="${param.specificDate}" placeholder="YYYY-MM-DD"
                                                        style="padding: 12px; border: 1px solid #3a3f5a; border-radius: 8px; background-color: #1a1a2e; color: #ffffff; text-align: center;"
                                                        onchange="handleDateSelection('specific')">
                                                </div>
                                            </div>

                                            
                                            <div class="form-buttons-row"
                                                style="flex: 0 0 100%; display: flex; justify-content: center; padding: 10px 0;">
                                                <button type="submit"
                                                    class="btn lottery-btn btn-sapphire">Search</button>
                                                <button type="button" class="btn lottery-btn btn-ruby"
                                                    onclick="resetForm()">Reset</button>
                                            </div>
                                        </form>
                                    </div>

                                    <c:if test="${not empty tickets}">
                                        <div class="search-box">
                                            <div
                                                style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
                                                <h3>Published Lottery Tickets (${totalTickets} found)</h3>
                                            </div>

                                            <table class="ticket-table">
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
                                                            <td class="ticket-numbers-cell">${ticket.numbers}</td>
                                                            <td>${ticket.company}</td>
                                                            <td>${ticket.creationDate}</td>
                                                        </tr>
                                                    </c:forEach>
                                                </tbody>
                                            </table>

                                            <% StringBuilder baseUrl=new StringBuilder("homepage?"); Enumeration<String>
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

                                                <c:set var="baseUrl" value="${computedBaseUrl}" scope="request" />
                                                <jsp:include page="/WEB-INF/fragments/pagination.jsp" />
                                        </div>
                                    </c:if>

                                    <c:if test="${empty tickets && not empty param.company}">
                                        <p>No published tickets found matching your search criteria.</p>
                                    </c:if>

                                    <c:if test="${empty param.company && empty tickets}">
                                        <p>Use the search form above to find published lottery tickets.</p>
                                    </c:if>

                                    
                                    <c:if test="${not empty searchHistory}">
                                        <div class="search-box" style="margin-top: 40px;">
                                            <div
                                                style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
                                                <h3>Previous Searches</h3>
                                                <form method="get" action="homepage" style="margin: 0;">
                                                    <input type="hidden" name="action" value="clearHistory">
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
                                                            <small style="color: #a0a0b0;">${history.searchDate}</small>
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
                                </div>

                                <script>

                                    
                                    function handleFormSubmit(event) {
                                        event.preventDefault();

                                        
                                        const form = event.target;

                                        
                                        let selectedCompanies = [];
                                        if (window.companySelector) {
                                            selectedCompanies = window.companySelector.getSelectedCompanies();
                                        }

                                        
                                        const url = new URL(form.action, window.location.origin + window.location.pathname.replace(/\/[^\/]*$/, '/'));
                                        const params = new URLSearchParams();

                                        
                                        if (selectedCompanies.length > 0) {
                                            params.set('company', selectedCompanies.join(','));
                                        }

                                        
                                        const inputs = form.querySelectorAll('input[name]');
                                        inputs.forEach(input => {
                                            if (input.name !== 'company' && input.value) {
                                                params.set(input.name, input.value);
                                            }
                                        });

                                        
                                        url.search = params.toString();

                                        
                                        localStorage.setItem('scrollPosition', window.scrollY);

                                        
                                        window.location.href = url.toString();
                                    }

                                    
                                    document.addEventListener('DOMContentLoaded', function () {
                                        const form = document.querySelector('.search-form');
                                        if (form) {
                                            form.addEventListener('submit', handleFormSubmit);
                                        }

                                        
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

                                    function resetForm() {
                                        
                                        const inputs = document.querySelectorAll('.search-form input[type="number"], .search-form input[type="date"]');
                                        inputs.forEach(input => {
                                            input.value = '';
                                        });

                                        
                                        if (window.companySelector) {
                                            window.companySelector.clearAllSelections();
                                        }

                                        
                                        const fromHistory = document.querySelector('input[name="fromHistory"]');
                                        const searchId = document.querySelector('input[name="searchId"]');
                                        if (fromHistory) fromHistory.value = '';
                                        if (searchId) searchId.value = '';
                                    }

                                    function handleDateSelection(type) {
                                        if (type === 'specific') {
                                            
                                            const startDate = document.querySelector('input[name="startDate"]');
                                            const endDate = document.querySelector('input[name="endDate"]');
                                            if (startDate) startDate.value = '';
                                            if (endDate) endDate.value = '';
                                        } else if (type === 'range') {
                                            
                                            const specificDate = document.querySelector('input[name="specificDate"]');
                                            if (specificDate) specificDate.value = '';
                                        }
                                    }

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

                                        if (window.companySelector && params.company) {
                                            window.companySelector.setSelectedCompanies(params.company.split(','));
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

                                        if (window.companySelector) {
                                            const selected = window.companySelector.getSelectedCompanies();
                                            const hiddenInput = document.getElementById('selected-companies-input');
                                            if (hiddenInput) {
                                                hiddenInput.value = selected.join(',');
                                            }
                                        }

                                        localStorage.setItem('scrollPosition', window.scrollY);
                                        document.querySelector('.search-form').submit();
                                    }

                                    
                                    window.addEventListener('DOMContentLoaded', function () {
                                        
                                        window.allCompanies = [
                                            <c:forEach var="comp" items="${allCompanies}" varStatus="loop">
                                                "${comp}"<c:if test="${!loop.last}">,</c:if>
                                            </c:forEach>
                                        ];

                                        
                                        if (typeof CompanySelector !== 'undefined') {
                                            const companySelector = new CompanySelector('company-selector-container', window.allCompanies);

                                            
                                            window.companySelector = companySelector;

                                            
                                            const urlParams = new URLSearchParams(window.location.search);
                                            const companyParam = urlParams.get('company');
                                            if (companyParam) {
                                                const companies = companyParam.split(',').map(c => c.trim());
                                                companySelector.setSelectedCompanies(companies);
                                            }

                                            
                                            const hiddenInput = document.getElementById('selected-companies-input');
                                            if (hiddenInput) {
                                                
                                                const originalGetSelectedCompanies = companySelector.getSelectedCompanies;
                                                companySelector.getSelectedCompanies = function () {
                                                    const selected = originalGetSelectedCompanies.call(this);
                                                    hiddenInput.value = selected.join(',');
                                                    return selected;
                                                };

                                                
                                                const originalSetSelectedCompanies = companySelector.setSelectedCompanies;
                                                companySelector.setSelectedCompanies = function (companies) {
                                                    originalSetSelectedCompanies.call(this, companies);
                                                    hiddenInput.value = companies.join(',');
                                                };
                                            }
                                        }
                                    });

                                </script>
                                <script>
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
