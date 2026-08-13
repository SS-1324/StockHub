<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:url var="dashboardCssUrl" value="/css/dashboard.css">
    <c:param name="v" value="6" />
</c:url>
<c:set var="pageCssUrl" value="${dashboardCssUrl}" scope="request" />

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<section class="dashboard-page" aria-labelledby="trade-history-title">
    <div class="dashboard-section-heading dashboard-page-heading">
        <h1 id="trade-history-title">매매 손익 내역</h1>
        <a class="dashboard-section-link" href="${pageContext.request.contextPath}/member/dashboard">← 대시보드로</a>
    </div>

    <section class="dashboard-holdings">
        <c:if test="${not empty realizedProfits or not empty stockSummary.holdings}">
            <div class="dashboard-history-summary">
                <div class="dashboard-analytics-stat">
                    <p class="dashboard-analytics-stat-label">총 매도금액</p>
                    <p class="dashboard-analytics-stat-value"><fmt:formatNumber value="${totalSellAmount}" pattern="#,##0"/>원</p>
                </div>
                <div class="dashboard-analytics-stat">
                    <p class="dashboard-analytics-stat-label">총 매수금액</p>
                    <p class="dashboard-analytics-stat-value"><fmt:formatNumber value="${totalBuyAmount}" pattern="#,##0"/>원</p>
                </div>
                <div class="dashboard-analytics-stat">
                    <p class="dashboard-analytics-stat-label">총 실현손익</p>
                    <p class="dashboard-analytics-stat-value ${totalProfitAmount gt 0 ? 'value-positive' : (totalProfitAmount lt 0 ? 'value-negative' : '')}">
                        <c:if test="${totalProfitAmount gt 0}">+</c:if><fmt:formatNumber value="${totalProfitAmount}" pattern="#,##0"/>원
                    </p>
                    <p class="dashboard-analytics-stat-sub">
                        <c:if test="${totalProfitRate gt 0}">+</c:if><fmt:formatNumber value="${totalProfitRate}" pattern="#,##0.00"/>%
                    </p>
                </div>
                <div class="dashboard-analytics-stat">
                    <p class="dashboard-analytics-stat-label">보유 주식 평가금액</p>
                    <p class="dashboard-analytics-stat-value"><fmt:formatNumber value="${currentHoldingsValue}" pattern="#,##0"/>원</p>
                    <p class="dashboard-analytics-stat-sub">
                        평균 <c:if test="${stockSummary.returnRate gt 0}">+</c:if><fmt:formatNumber value="${stockSummary.returnRate}" pattern="#,##0.00"/>%
                    </p>
                </div>
            </div>
        </c:if>

        <c:choose>
            <c:when test="${not empty realizedProfits}">
                <div class="dashboard-table-wrap">
                    <table class="dashboard-table">
                        <thead>
                        <tr>
                            <th scope="col">종목/상품</th>
                            <th scope="col">매수일</th>
                            <th scope="col">매수가</th>
                            <th scope="col">매도일</th>
                            <th scope="col">매도가</th>
                            <th scope="col">수량</th>
                            <th scope="col">보유일수</th>
                            <th scope="col">손익</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="item" items="${realizedProfits}">
                            <tr>
                                <td data-label="종목/상품">
                                    <strong><c:out value="${item.itemName}"/></strong>
                                    <span class="dashboard-code"><c:out value="${item.itemType == 'STOCK' ? '주식' : '상품'}"/></span>
                                </td>
                                <td data-label="매수일"><c:out value="${item.buyAtText}"/></td>
                                <td data-label="매수가"><fmt:formatNumber value="${item.buyPrice}" pattern="#,##0.##"/>원</td>
                                <td data-label="매도일"><c:out value="${item.sellAtText}"/></td>
                                <td data-label="매도가"><fmt:formatNumber value="${item.sellPrice}" pattern="#,##0.##"/>원</td>
                                <td data-label="수량"><fmt:formatNumber value="${item.quantity}" pattern="#,##0.####"/></td>
                                <td data-label="보유일수"><c:out value="${item.holdingDays}"/>일</td>
                                <td data-label="손익"
                                    class="${item.profitAmount gt 0 ? 'value-positive' : (item.profitAmount lt 0 ? 'value-negative' : '')}">
                                    <c:if test="${item.profitAmount gt 0}">+</c:if><fmt:formatNumber value="${item.profitAmount}" pattern="#,##0"/>원
                                    (<c:if test="${item.returnRate gt 0}">+</c:if><fmt:formatNumber value="${item.returnRate}" pattern="#,##0.00"/>%)
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:when>
            <c:otherwise>
                <div class="dashboard-empty">
                    <p>아직 매도·환매한 이력이 없습니다.</p>
                </div>
            </c:otherwise>
        </c:choose>
    </section>
</section>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
