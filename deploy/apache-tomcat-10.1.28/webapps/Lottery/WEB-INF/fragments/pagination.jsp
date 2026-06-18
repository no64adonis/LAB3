<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
        <%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
            <%@ page import="java.util.*" %>

                <style>
                    #jumpPageInput {
                        width: 60px !important;
                        height: 40px !important;
                        padding: 0 10px !important;
                        border-radius: 8px !important;
                        background-color: #2a2c42 !important;
                        border: 1px solid #3a3f5a !important;
                        color: #ffffff !important;
                        text-align: center !important;
                        font-weight: 500 !important;
                        font-size: 14px !important;
                        transition: all 0.3s ease !important;
                        -webkit-appearance: none !important;
                        -moz-appearance: textfield !important;
                        appearance: none !important;
                        margin: 0 !important;
                        outline: none !important;
                        box-sizing: border-box !important;
                    }

                    #jumpPageInput::-webkit-outer-spin-button,
                    #jumpPageInput::-webkit-inner-spin-button {
                        -webkit-appearance: none !important;
                        margin: 0 !important;
                    }

                    #jumpPageInput:focus {
                        border-color: #ffc107 !important;
                        box-shadow: 0 0 10px rgba(255, 193, 7, 0.3) !important;
                        background-color: #1a1a2e !important;
                    }

                    .pagination-jump .jump-btn {
                        min-width: 40px;
                        height: 40px !important;
                        border-radius: 8px !important;
                        font-size: 14px !important;
                        padding: 0 12px !important;
                    }

                    
                    @media (max-width: 768px) {
                        .pagination .lottery-btn {
                            min-width: 28px !important;
                            height: 30px !important;
                            padding: 0 5px !important;
                            font-size: 0.75em !important;
                        }
                        
                        .pagination .lottery-btn .pagination-text {
                            display: none !important;
                        }
                        #jumpPageInput {
                            width: 35px !important;
                            height: 30px !important;
                            font-size: 11px !important;
                            padding: 0 4px !important;
                        }
                        .pagination-jump .jump-btn {
                            min-width: 28px !important;
                            height: 30px !important;
                            font-size: 11px !important;
                            padding: 0 5px !important;
                        }
                        .pagination-jump {
                            margin-left: 2px !important;
                        }
                        .pagination {
                            gap: 2px !important;
                            flex-wrap: nowrap !important;
                        }
                        .pagination-ellipsis {
                            padding: 0 1px !important;
                            font-size: 0.8em !important;
                        }
                    }
                    @media (max-width: 480px) {
                        .pagination .lottery-btn {
                            min-width: 24px !important;
                            height: 26px !important;
                            padding: 0 3px !important;
                            font-size: 0.7em !important;
                        }
                        .pagination-jump {
                            display: none !important;
                        }
                        .pagination {
                            gap: 1px !important;
                        }
                    }
                </style>

                <c:set var="cp"
                    value="${not empty param.page ? param.page : (not empty currentPage ? currentPage : 1)}" />
                <c:set var="tp"
                    value="${not empty param.totalPages ? param.totalPages : (not empty totalPages ? totalPages : 0)}" />

                <%  Integer cp=1; try { Object cpObj=pageContext.getAttribute("cp"); if
                    (cpObj instanceof String) cp=Integer.parseInt((String)cpObj); else if (cpObj instanceof Integer)
                    cp=(Integer)cpObj; } catch (Exception e) { cp=1; } Integer tp=0; try { Object
                    tpObj=pageContext.getAttribute("tp"); if (tpObj instanceof String)
                    tp=Integer.parseInt((String)tpObj); else if (tpObj instanceof Integer) tp=(Integer)tpObj; } catch
                    (Exception e) { tp=0; } if (tp> 1) {
                    TreeSet<Integer> pages = new TreeSet<>();

                            
                            for (int i = 1; i <= 3 && i <=tp; i++) { pages.add(i); }  for (int i=cp - 1; i
                                <=cp + 1; i++) { if (i>= 1 && i <= tp) { pages.add(i); } }  for (int i=tp -
                                    2; i <=tp; i++) { if (i>= 1) { pages.add(i); }
                                    }

                                    request.setAttribute("pageArray", pages);
                                    }
                                    %>

                                    <c:if test="${tp > 1}">
                                        <div class="pagination-container">
                                            <div class="pagination">
                                                <a href="${baseUrl}${fn:contains(baseUrl, '?') ? '&' : '?'}page=1"
                                                    class="lottery-btn ${cp == 1 ? 'disabled' : ''}" title="First Page">
                                                    <i class="fas fa-angle-double-left"></i> <span class="pagination-text">First</span>
                                                </a>

                                                <c:if test="${cp > 1}">
                                                    <a href="${baseUrl}${fn:contains(baseUrl, '?') ? '&' : '?'}page=${cp - 1}"
                                                        class="lottery-btn">
                                                        <i class="fas fa-angle-left"></i>
                                                    </a>
                                                </c:if>

                                                <c:set var="prevPage" value="0" />
                                                <c:forEach var="pageNum" items="${pageArray}">
                                                    <c:if test="${prevPage > 0 && pageNum - prevPage > 1}">
                                                        <span class="pagination-ellipsis">...</span>
                                                    </c:if>

                                                    <a href="${baseUrl}${fn:contains(baseUrl, '?') ? '&' : '?'}page=${pageNum}"
                                                        class="lottery-btn ${pageNum == cp ? 'active' : ''}">
                                                        ${pageNum}
                                                    </a>
                                                    <c:set var="prevPage" value="${pageNum}" />
                                                </c:forEach>

                                                <c:if test="${cp < tp}">
                                                    <a href="${baseUrl}${fn:contains(baseUrl, '?') ? '&' : '?'}page=${cp + 1}"
                                                        class="lottery-btn">
                                                        <i class="fas fa-angle-right"></i>
                                                    </a>
                                                </c:if>

                                                <a href="${baseUrl}${fn:contains(baseUrl, '?') ? '&' : '?'}page=${tp}"
                                                    class="lottery-btn ${cp == tp ? 'disabled' : ''}" title="Last Page">
                                                    <span class="pagination-text">Last</span> <i class="fas fa-angle-double-right"></i>
                                                </a>

                                                <div class="pagination-jump">
                                                    <input type="number" id="jumpPageInput" class="jump-input" min="1"
                                                        max="${tp}" placeholder="Page"
                                                        onkeypress="if(event.key === 'Enter') jumpToPage()">
                                                    <button type="button" class="lottery-btn jump-btn"
                                                        onclick="jumpToPage()" title="Go to page">
                                                        Go
                                                    </button>
                                                </div>
                                            </div>

                                            <script>
                                                function jumpToPage() {
                                                    const input = document.getElementById('jumpPageInput');
                                                    const page = parseInt(input.value);
                                                    const totalPages = parseInt('${tp}');

                                                    if (isNaN(page) || page < 1 || page > totalPages) {
                                                        alert('Please enter a valid page number between 1 and ' + totalPages);
                                                        return;
                                                    }

                                                    const baseUrl = '${baseUrl}';
                                                    const separator = baseUrl.indexOf('?') !== -1 ? '&' : '?';

                                                    
                                                    localStorage.setItem('scrollPosition', window.scrollY);

                                                    window.location.href = baseUrl + separator + 'page=' + page;
                                                }

                                                
                                                document.addEventListener('click', function (event) {
                                                    const link = event.target.closest('.pagination a');
                                                    if (link) {
                                                        localStorage.setItem('scrollPosition', window.scrollY);
                                                    }
                                                });
                                            </script>
                                        </div>
                                    </c:if>