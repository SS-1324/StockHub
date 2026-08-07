<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:url var="dashboardCssUrl" value="/css/dashboard.css" />
<c:set var="pageCssUrl" value="${dashboardCssUrl}" scope="request" />
<c:url var="defaultProfileUrl" value="/images/common_member.png" />

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<section class="dashboard-page" aria-labelledby="dashboard-title">
    <header class="dashboard-profile">
        <c:choose>
            <c:when test="${not empty member.profile}">
                <img class="dashboard-profile-image"
                     src="${pageContext.request.contextPath}${member.profile}"
                     onerror="this.onerror=null; this.src='${defaultProfileUrl}';"
                     alt="프로필 이미지">
            </c:when>
            <c:otherwise>
                <img class="dashboard-profile-image"
                     src="${defaultProfileUrl}"
                     alt="기본 프로필 이미지">
            </c:otherwise>
        </c:choose>

        <div>
            <p class="dashboard-eyebrow">MY DASHBOARD</p>
            <h1 id="dashboard-title"><c:out value="${member.nickname}"/></h1>
        </div>
    </header>

    <%-- 총 자산 요약 카드: 증권사가 여러 곳이어도 여기 하나로 합산해서 보여주는 게 핵심 --%>
    <dl class="dashboard-summary dashboard-summary-total">
        <div class="dashboard-summary-row">
            <dt>총 자산</dt>
            <dd><fmt:formatNumber value="${totalAsset}" pattern="#,##0"/>원</dd>
        </div>
        <div class="dashboard-summary-row">
            <dt>총 손익</dt>
            <dd class="${totalProfit gt 0 ? 'value-positive' : (totalProfit lt 0 ? 'value-negative' : '')}">
                <c:if test="${totalProfit gt 0}">+</c:if><fmt:formatNumber value="${totalProfit}" pattern="#,##0"/>원
            </dd>
        </div>
        <div class="dashboard-summary-row">
            <dt>현금 잔고</dt>
            <dd><fmt:formatNumber value="${stockSummary.currentBalance}" pattern="#,##0"/>원</dd>
        </div>
    </dl>

    <%-- 목표 도달률 --%>
    <section class="dashboard-goal" aria-labelledby="goal-title">
        <div class="dashboard-section-heading">
            <h2 id="goal-title">목표 도달률</h2>
        </div>

        <c:if test="${not empty goalError}">
            <p class="dashboard-goal-error"><c:out value="${goalError}"/></p>
        </c:if>

        <c:choose>
            <c:when test="${not empty goal}">
                <div class="dashboard-goal-body">
                    <div class="dashboard-goal-ring" style="--progress: ${goalProgress};">
                        <span class="dashboard-goal-ring-value"><c:out value="${goalProgress}"/>%</span>
                    </div>
                    <div class="dashboard-goal-info">
                        <p class="dashboard-goal-name"><c:out value="${goal.title}"/></p>
                        <p class="dashboard-goal-target">
                            목표:
                            <c:choose>
                                <c:when test="${goal.goalType == 'RETURN_RATE'}">
                                    수익률 <fmt:formatNumber value="${goal.targetValue}" pattern="#,##0.##"/>%
                                </c:when>
                                <c:otherwise>
                                    수익금 <fmt:formatNumber value="${goal.targetValue}" pattern="#,##0"/>원
                                </c:otherwise>
                            </c:choose>
                        </p>
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <p class="dashboard-empty-inline">아직 설정한 목표가 없습니다. 아래에서 새 목표를 만들어보세요.</p>
            </c:otherwise>
        </c:choose>

        <form class="dashboard-goal-form"
              action="${pageContext.request.contextPath}/member/dashboard/goal" method="post">
            <input type="text" name="title" placeholder="목표 이름 (예: 이번 달 +5%)" maxlength="100" required>
            <select name="goalType">
                <option value="RETURN_RATE">수익률(%)</option>
                <option value="PROFIT_AMOUNT">수익금(원)</option>
            </select>
            <input type="number" name="targetValue" placeholder="목표치" min="0.01" step="0.01" required>
            <button type="submit" class="btn btn-primary">목표 설정</button>
        </form>
    </section>

    <%-- 보유 주식 --%>
    <section class="dashboard-holdings" aria-labelledby="stock-holdings-title">
        <div class="dashboard-section-heading">
            <h2 id="stock-holdings-title">보유 주식</h2>
            <span><c:out value="${fn:length(stockSummary.holdings)}"/>종목</span>
        </div>

        <c:choose>
            <c:when test="${not empty stockSummary.holdings}">
                <div class="dashboard-table-wrap">
                    <table class="dashboard-table">
                        <thead>
                        <tr>
                            <th scope="col">종목</th>
                            <th scope="col">수량</th>
                            <th scope="col">평균 매입가</th>
                            <th scope="col">현재가</th>
                            <th scope="col">평가금액</th>
                            <th scope="col">수익률</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="holding" items="${stockSummary.holdings}">
                            <tr>
                                <td data-label="종목">
                                    <strong><c:out value="${holding.stockName}"/></strong>
                                    <span class="dashboard-code"><c:out value="${holding.stockCode}"/></span>
                                </td>
                                <td data-label="수량"><fmt:formatNumber value="${holding.quantity}" pattern="#,##0"/>주</td>
                                <td data-label="평균 매입가"><fmt:formatNumber value="${holding.avgPrice}" pattern="#,##0"/>원</td>
                                <td data-label="현재가"><fmt:formatNumber value="${holding.currentPrice}" pattern="#,##0"/>원</td>
                                <td data-label="평가금액"><fmt:formatNumber value="${holding.currentValue}" pattern="#,##0"/>원</td>
                                <td data-label="수익률"
                                    class="${holding.profitAmount gt 0 ? 'value-positive' : (holding.profitAmount lt 0 ? 'value-negative' : '')}">
                                    <c:if test="${holding.returnRate gt 0}">+</c:if><fmt:formatNumber value="${holding.returnRate}" pattern="#,##0.00"/>%
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:when>
            <c:otherwise>
                <div class="dashboard-empty">
                    <p>아직 보유 중인 주식이 없습니다.</p>
                    <a href="${pageContext.request.contextPath}/trade-hub">거래 허브에서 주식 살펴보기</a>
                </div>
            </c:otherwise>
        </c:choose>
    </section>

    <%-- 보유 상품(펀드/채권/ELS) --%>
    <section class="dashboard-holdings" aria-labelledby="product-holdings-title">
        <div class="dashboard-section-heading">
            <h2 id="product-holdings-title">보유 상품</h2>
            <span><c:out value="${fn:length(productSummary.holdings)}"/>개</span>
        </div>

        <c:choose>
            <c:when test="${not empty productSummary.holdings}">
                <div class="dashboard-table-wrap">
                    <table class="dashboard-table">
                        <thead>
                        <tr>
                            <th scope="col">상품</th>
                            <th scope="col">좌수</th>
                            <th scope="col">평균 매입가</th>
                            <th scope="col">현재 기준가</th>
                            <th scope="col">평가금액</th>
                            <th scope="col">수익률</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="holding" items="${productSummary.holdings}">
                            <tr>
                                <td data-label="상품">
                                    <strong><c:out value="${holding.productName}"/></strong>
                                    <span class="dashboard-code"><c:out value="${holding.productType}"/></span>
                                </td>
                                <td data-label="좌수"><fmt:formatNumber value="${holding.quantity}" pattern="#,##0.####"/>좌</td>
                                <td data-label="평균 매입가"><fmt:formatNumber value="${holding.avgNav}" pattern="#,##0.00"/>원</td>
                                <td data-label="현재 기준가"><fmt:formatNumber value="${holding.currentNav}" pattern="#,##0.00"/>원</td>
                                <td data-label="평가금액"><fmt:formatNumber value="${holding.currentValue}" pattern="#,##0"/>원</td>
                                <td data-label="수익률"
                                    class="${holding.profitAmount gt 0 ? 'value-positive' : (holding.profitAmount lt 0 ? 'value-negative' : '')}">
                                    <c:if test="${holding.returnRate gt 0}">+</c:if><fmt:formatNumber value="${holding.returnRate}" pattern="#,##0.00"/>%
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:when>
            <c:otherwise>
                <div class="dashboard-empty">
                    <p>아직 보유 중인 상품이 없습니다.</p>
                </div>
            </c:otherwise>
        </c:choose>
    </section>

    <%-- 최근 활동(매매/가입환매/입출금 통합 타임라인) --%>
    <section class="dashboard-timeline" aria-labelledby="timeline-title">
        <div class="dashboard-section-heading">
            <h2 id="timeline-title">최근 활동</h2>
        </div>

        <c:choose>
            <c:when test="${not empty timeline}">
                <ul class="dashboard-timeline-list">
                    <c:forEach var="event" items="${timeline}">
                        <li>
                            <span class="dashboard-timeline-badge dashboard-timeline-badge-${event.category}"><c:out value="${event.badge}"/></span>
                            <span class="dashboard-timeline-desc"><c:out value="${event.description}"/></span>
                            <span class="dashboard-timeline-amount"><fmt:formatNumber value="${event.amount}" pattern="#,##0"/>원</span>
                            <span class="dashboard-timeline-date"><c:out value="${event.occurredAtText}"/></span>
                        </li>
                    </c:forEach>
                </ul>
            </c:when>
            <c:otherwise>
                <div class="dashboard-empty">
                    <p>최근 활동 내역이 없습니다.</p>
                </div>
            </c:otherwise>
        </c:choose>
    </section>
</section>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
