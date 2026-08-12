<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:url var="dashboardCssUrl" value="/css/dashboard.css">
    <c:param name="v" value="6" />
</c:url>
<c:set var="pageCssUrl" value="${dashboardCssUrl}" scope="request" />
<c:url var="defaultProfileUrl" value="/images/common_member.png" />
<c:url var="dashboardJsUrl" value="/js/dashboard.js">
    <c:param name="v" value="5" />
</c:url>

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<%-- [전체프로필프레임-3] 대시보드도 공통 프로필 프레임 판정값을 그대로 사용한다. --%>
<c:set var="dashboardRankClass" value="" />
<c:choose>
    <c:when test="${headerRankPosition eq 1}"><c:set var="dashboardRankClass" value="rank-first" /></c:when>
    <c:when test="${headerRankPosition eq 2}"><c:set var="dashboardRankClass" value="rank-second" /></c:when>
    <c:when test="${headerRankPosition eq 3}"><c:set var="dashboardRankClass" value="rank-third" /></c:when>
</c:choose>

<section class="dashboard-page" aria-labelledby="dashboard-title">
    <header class="dashboard-profile">
        <span class="account-profile-rank-frame dashboard-profile-rank-frame ${dashboardRankClass}">
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
        </span>

        <div class="dashboard-profile-info">
            <p class="dashboard-eyebrow">MY DASHBOARD</p>
            <h1 id="dashboard-title"><c:out value="${member.nickname}"/></h1>
        </div>

        <button type="button" class="btn btn-outline dashboard-link-account-toggle"
                aria-expanded="${(not empty linkSuccess or not empty linkError) ? 'true' : 'false'}"
                aria-controls="link-account-panel">계좌 연동하기</button>

        <div id="link-account-panel" class="dashboard-link-account-panel"
             <c:if test="${empty linkSuccess and empty linkError}">hidden</c:if>>
            <c:if test="${not empty linkError}">
                <p class="dashboard-goal-error"><c:out value="${linkError}"/></p>
            </c:if>
            <c:if test="${not empty linkSuccess}">
                <p class="dashboard-link-success">계좌가 연동되었습니다.</p>
            </c:if>
            <c:if test="${not empty myAccounts}">
                <p class="dashboard-link-subheading">연동된 계좌</p>
                <ul class="dashboard-linked-accounts">
                    <c:forEach var="acc" items="${myAccounts}">
                        <li><c:out value="${acc.brokerageName}"/> (<c:out value="${acc.accountNo}"/>)</li>
                    </c:forEach>
                </ul>
            </c:if>

            <p class="dashboard-link-subheading">새로 추가하기</p>
            <p class="dashboard-link-desc">증권사와 계좌번호, 예금주명을 입력하면 그 증권사가 이미 갖고 있던 거래 이력을 그대로 불러옵니다.</p>
            <form class="dashboard-inline-form"
                  action="${pageContext.request.contextPath}/member/dashboard/link-account" method="post">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                <select name="brokerageId" required>
                    <option value="">증권사 선택</option>
                    <c:forEach var="b" items="${brokerages}">
                        <option value="${b.brokerageId}"><c:out value="${b.brokerageName}"/></option>
                    </c:forEach>
                </select>
                <input type="text" name="accountNo" placeholder="계좌번호" maxlength="50" required>
                <input type="text" name="ownerName" placeholder="예금주명" maxlength="50" required>
                <button type="submit" class="btn btn-primary">연동하기</button>
            </form>
        </div>
    </header>

    <%-- 여러 증권사를 연동했을 때만 나오는 "전체/증권사별 보기" 필터. 하나 바꾸면 아래 화면
         전체(총자산~최근활동)가 그 기준으로 다시 그려지는 하나의 값이라, 통 하나로만 둔다 --%>
    <c:if test="${fn:length(myBrokerageNames) > 1}">
        <script id="dashboard-brokerage-data" type="application/json">${brokerageFilterDataJson}</script>
        <div class="dashboard-brokerage-filter-bar">
            <select id="dashboard-brokerage-filter" class="dashboard-brokerage-filter" aria-label="증권사 필터">
                <option value="__ALL__">전체</option>
                <c:forEach var="name" items="${myBrokerageNames}">
                    <option value="<c:out value='${name}'/>"><c:out value="${name}"/></option>
                </c:forEach>
            </select>
        </div>
    </c:if>

    <%-- 총 자산 요약 카드: 증권사가 여러 곳이어도 여기 하나로 합산해서 보여주는 게 핵심 --%>
    <dl class="dashboard-summary dashboard-summary-total">
        <div class="dashboard-summary-row">
            <dt>총 자산</dt>
            <dd id="dashboard-total-asset" data-all="${totalAsset}"><fmt:formatNumber value="${totalAsset}" pattern="#,##0"/>원</dd>
        </div>
        <div class="dashboard-summary-row">
            <dt class="dashboard-profit-label">
                총 손익
                <span class="dashboard-period-tabs" role="tablist" aria-label="손익 조회 기간">
                    <button type="button" class="dashboard-period-tab" data-period="week">1주</button>
                    <button type="button" class="dashboard-period-tab" data-period="month">1달</button>
                    <button type="button" class="dashboard-period-tab" data-period="year">1년</button>
                    <button type="button" class="dashboard-period-tab is-active" data-period="all">전체</button>
                </span>
            </dt>
            <dd id="dashboard-period-profit"
                class="${totalProfit gt 0 ? 'value-positive' : (totalProfit lt 0 ? 'value-negative' : '')}"
                data-week="${periodProfit.week}" data-month="${periodProfit.month}"
                data-year="${periodProfit.year}" data-all="${periodProfit.all}"
                data-week-rate="${periodProfit.weekRate}" data-month-rate="${periodProfit.monthRate}"
                data-year-rate="${periodProfit.yearRate}" data-all-rate="${periodProfit.allRate}">
                <span id="dashboard-period-profit-amount">
                    <c:if test="${totalProfit gt 0}">+</c:if><fmt:formatNumber value="${totalProfit}" pattern="#,##0"/>원
                </span>
                <span id="dashboard-period-profit-rate" class="dashboard-period-profit-rate">
                    (<c:if test="${totalReturnRate gt 0}">+</c:if><fmt:formatNumber value="${totalReturnRate}" pattern="#,##0.00"/>%)
                </span>
            </dd>
        </div>
        <div class="dashboard-summary-row">
            <dt>현금 잔고</dt>
            <dd id="dashboard-cash-balance" data-all="${stockSummary.currentBalance}"><fmt:formatNumber value="${stockSummary.currentBalance}" pattern="#,##0"/>원</dd>
        </div>
        <div class="dashboard-summary-footer">
            <a class="dashboard-section-link"
               href="${pageContext.request.contextPath}/member/dashboard/history">전체 매매 손익 보기 →</a>
        </div>
    </dl>

    <%-- 목표 도달률 --%>
    <section class="dashboard-goal" aria-labelledby="goal-title">
        <div class="dashboard-section-heading">
            <h2 id="goal-title">목표 도달률</h2>
            <c:if test="${hasGoalHistory}">
                <a class="dashboard-section-link"
                   href="${pageContext.request.contextPath}/member/dashboard/goals/history">목표 히스토리 →</a>
            </c:if>
        </div>

        <c:if test="${not empty goalError}">
            <p class="dashboard-goal-error"><c:out value="${goalError}"/></p>
        </c:if>

        <c:choose>
            <c:when test="${not empty activeGoals}">
                <div class="dashboard-goal-list">
                    <c:forEach var="goal" items="${activeGoals}">
                        <div class="dashboard-goal-card">
                            <div class="dashboard-goal-ring ${goalSuccess[goal.goalId] ? 'is-success' : ''}"
                                 style="--progress: ${goalProgress[goal.goalId]};">
                                <span class="dashboard-goal-ring-value">
                                    <c:choose>
                                        <c:when test="${goalSuccess[goal.goalId]}">&#10003;</c:when>
                                        <c:otherwise><c:out value="${goalProgress[goal.goalId]}"/>%</c:otherwise>
                                    </c:choose>
                                </span>
                            </div>
                            <div class="dashboard-goal-info">
                                <p class="dashboard-goal-target">
                                    <c:choose>
                                        <c:when test="${goal.goalType == 'RETURN_RATE'}">
                                            수익률 <fmt:formatNumber value="${goal.targetValue}" pattern="#,##0.##"/>%
                                        </c:when>
                                        <c:otherwise>
                                            수익금 <fmt:formatNumber value="${goal.targetValue}" pattern="#,##0"/>원
                                        </c:otherwise>
                                    </c:choose>
                                </p>
                                <p class="dashboard-goal-name"><c:out value="${goal.title}"/></p>
                                <c:if test="${not empty goal.targetDate}">
                                    <p class="dashboard-goal-deadline">
                                        <c:out value="${goal.targetDateText}"/>까지
                                    </p>
                                </c:if>
                                <form class="dashboard-goal-cancel-form"
                                      action="${pageContext.request.contextPath}/member/dashboard/goal/cancel" method="post">
                                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                    <input type="hidden" name="goalId" value="${goal.goalId}">
                                    <button type="submit" class="dashboard-goal-cancel-btn">취소</button>
                                </form>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </c:when>
            <c:otherwise>
                <p class="dashboard-empty-inline">아직 설정한 목표가 없습니다. 아래에서 새 목표를 만들어보세요.</p>
            </c:otherwise>
        </c:choose>

        <button type="button" class="btn btn-outline dashboard-goal-form-toggle"
                aria-expanded="false" aria-controls="goal-form-panel">목표 설정하기</button>

        <form id="goal-form-panel" class="dashboard-inline-form dashboard-goal-form" hidden
              action="${pageContext.request.contextPath}/member/dashboard/goal" method="post">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
            <select name="goalType">
                <option value="RETURN_RATE">수익률(%)</option>
                <option value="PROFIT_AMOUNT">수익금(원)</option>
            </select>
            <input type="number" name="targetValue" placeholder="목표치" min="0.01" step="0.01" required>
            <input type="text" name="title" placeholder="목표 이름 (예: 이번 달 +5%)" maxlength="100" required>
            <label class="dashboard-goal-date-label">
                기한
                <input type="date" name="targetDate">
            </label>
            <button type="submit" class="btn btn-primary">목표 설정</button>
        </form>
    </section>

    <%-- 보유 주식 --%>
    <section class="dashboard-holdings" aria-labelledby="stock-holdings-title">
        <div class="dashboard-section-heading">
            <div class="dashboard-holdings-title-group">
                <h2 id="stock-holdings-title">보유 주식</h2>
                <c:if test="${not empty stockSummary.holdings}">
                    <button type="button" id="stock-holdings-count-toggle" class="dashboard-count-toggle"
                            data-mode="quantity"
                            data-quantity-text="<fmt:formatNumber value='${stockSummary.totalStockQuantity}' pattern='#,##0'/>주"
                            data-stock-text="<c:out value='${fn:length(stockSummary.holdings)}'/>종목">
                        <fmt:formatNumber value="${stockSummary.totalStockQuantity}" pattern="#,##0"/>주
                    </button>
                </c:if>
            </div>
            <c:if test="${not empty stockSummary.holdings}">
                <select id="stock-holdings-sort" class="dashboard-holdings-sort" aria-label="보유 주식 정렬">
                    <option value="default">기본순</option>
                    <option value="returnRate">수익률순</option>
                    <option value="quantity">수량순</option>
                    <option value="name">가나다순</option>
                    <option value="price">현재가순</option>
                    <option value="value">평가금액순</option>
                </select>
            </c:if>
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
                        <tbody id="stock-holdings-tbody">
                        <c:forEach var="holding" items="${stockSummary.holdings}">
                            <%-- 증권사 필터가 이 값들을 읽어서, 특정 증권사를 고르면 그 증권사 몫으로 셀 내용을
                                 바꿔치고, 그 증권사에 없는 종목은 행 자체를 숨긴다(브로커리지 이름은 우리
                                 brokerage 테이블에서 나오는 값이라 JSON 이스케이프 없이 그대로 써도 안전하다) --%>
                            <c:set var="accountsJson">[<c:forEach var="acc" items="${holding.accountBreakdown}" varStatus="st">{"brokerage":"${acc.brokerageName}","quantity":${acc.quantity},"avgPrice":${acc.avgPrice},"currentValue":${acc.currentValue},"returnRate":${acc.returnRate}}<c:if test="${!st.last}">,</c:if></c:forEach>]</c:set>
                            <tr data-name="<c:out value='${holding.stockName}'/>"
                                data-quantity="${holding.quantity}"
                                data-avg-price="${holding.avgPrice}"
                                data-price="${holding.currentPrice}"
                                data-value="${holding.currentValue}"
                                data-return-rate="${holding.returnRate}"
                                data-accounts="${fn:escapeXml(accountsJson)}">
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
            <div class="dashboard-holdings-title-group">
                <h2 id="product-holdings-title">보유 상품</h2>
                <span id="product-holdings-count"><c:out value="${fn:length(productSummary.holdings)}"/>개</span>
            </div>
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
                        <tbody id="product-holdings-tbody">
                        <c:forEach var="holding" items="${productSummary.holdings}">
                            <c:set var="accountsJson">[<c:forEach var="acc" items="${holding.accountBreakdown}" varStatus="st">{"brokerage":"${acc.brokerageName}","quantity":${acc.quantity},"avgNav":${acc.avgNav},"currentValue":${acc.currentValue},"returnRate":${acc.returnRate}}<c:if test="${!st.last}">,</c:if></c:forEach>]</c:set>
                            <tr data-quantity="${holding.quantity}"
                                data-avg-nav="${holding.avgNav}"
                                data-current-nav="${holding.currentNav}"
                                data-value="${holding.currentValue}"
                                data-return-rate="${holding.returnRate}"
                                data-accounts="${fn:escapeXml(accountsJson)}">
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

    <%-- 포트폴리오 분석 (보유 구성 + 자산 성장 추이 + 매매 통계) --%>
    <section class="dashboard-analytics" aria-labelledby="analytics-title">
        <div class="dashboard-section-heading">
            <h2 id="analytics-title">포트폴리오 분석</h2>
            <c:if test="${not empty stockSummary.holdings}">
                <button type="button" id="dashboard-analytics-download" class="dashboard-analytics-download-btn">PNG로 저장</button>
            </c:if>
        </div>

        <c:choose>
            <c:when test="${not empty stockSummary.holdings}">
                <div class="dashboard-analytics-grid" id="dashboard-analytics-capture">
                    <div class="dashboard-analytics-card">
                        <h3>보유 종목 구성</h3>
                        <div class="dashboard-analytics-donut-wrap">
                            <canvas id="portfolio-donut-canvas" width="220" height="220"></canvas>
                            <ul class="dashboard-portfolio-legend" id="stock-portfolio-legend"></ul>
                        </div>
                    </div>

                    <div class="dashboard-analytics-card">
                        <h3>자산 성장 추이</h3>
                        <canvas id="asset-trend-canvas"
                                width="520" height="220"
                                data-trend="<c:forEach var="p" items="${portfolioAnalytics.assetTrend}" varStatus="st"><c:out value='${p.snapshotDate}'/>:<c:out value='${p.totalAsset}'/><c:if test="${!st.last}">,</c:if></c:forEach>"></canvas>
                    </div>

                    <div class="dashboard-analytics-stats">
                        <div class="dashboard-analytics-stat">
                            <p class="dashboard-analytics-stat-label">매매 승률</p>
                            <c:choose>
                                <c:when test="${portfolioAnalytics.closedTradeCount > 0}">
                                    <p class="dashboard-analytics-stat-value"><fmt:formatNumber value="${portfolioAnalytics.winRate}" pattern="#,##0.0"/>%</p>
                                    <p class="dashboard-analytics-stat-sub"><c:out value="${portfolioAnalytics.closedTradeCount}"/>건 중</p>
                                </c:when>
                                <c:otherwise><p class="dashboard-analytics-stat-value">-</p></c:otherwise>
                            </c:choose>
                        </div>
                        <div class="dashboard-analytics-stat">
                            <p class="dashboard-analytics-stat-label">평균 보유기간</p>
                            <c:choose>
                                <c:when test="${portfolioAnalytics.closedTradeCount > 0}">
                                    <p class="dashboard-analytics-stat-value"><c:out value="${portfolioAnalytics.avgHoldingDays}"/>일</p>
                                </c:when>
                                <c:otherwise><p class="dashboard-analytics-stat-value">-</p></c:otherwise>
                            </c:choose>
                        </div>
                        <div class="dashboard-analytics-stat">
                            <p class="dashboard-analytics-stat-label">최고의 매매</p>
                            <c:choose>
                                <c:when test="${not empty portfolioAnalytics.bestTrade}">
                                    <p class="dashboard-analytics-stat-value value-positive">
                                        <c:if test="${portfolioAnalytics.bestTrade.profitAmount gt 0}">+</c:if><fmt:formatNumber value="${portfolioAnalytics.bestTrade.profitAmount}" pattern="#,##0"/>원
                                    </p>
                                    <p class="dashboard-analytics-stat-sub"><c:out value="${portfolioAnalytics.bestTrade.itemName}"/></p>
                                </c:when>
                                <c:otherwise><p class="dashboard-analytics-stat-value">-</p></c:otherwise>
                            </c:choose>
                        </div>
                        <div class="dashboard-analytics-stat">
                            <p class="dashboard-analytics-stat-label">최악의 매매</p>
                            <c:choose>
                                <c:when test="${not empty portfolioAnalytics.worstTrade}">
                                    <p class="dashboard-analytics-stat-value ${portfolioAnalytics.worstTrade.profitAmount lt 0 ? 'value-negative' : 'value-positive'}">
                                        <c:if test="${portfolioAnalytics.worstTrade.profitAmount gt 0}">+</c:if><fmt:formatNumber value="${portfolioAnalytics.worstTrade.profitAmount}" pattern="#,##0"/>원
                                    </p>
                                    <p class="dashboard-analytics-stat-sub"><c:out value="${portfolioAnalytics.worstTrade.itemName}"/></p>
                                </c:when>
                                <c:otherwise><p class="dashboard-analytics-stat-value">-</p></c:otherwise>
                            </c:choose>
                        </div>
                        <div class="dashboard-analytics-stat">
                            <p class="dashboard-analytics-stat-label">집중도</p>
                            <p class="dashboard-analytics-stat-value"><fmt:formatNumber value="${portfolioAnalytics.concentrationRate}" pattern="#,##0.0"/>%</p>
                            <p class="dashboard-analytics-stat-sub"><c:out value="${portfolioAnalytics.topHoldingName}"/></p>
                        </div>
                        <div class="dashboard-analytics-stat">
                            <p class="dashboard-analytics-stat-label">국내 · 해외 비중</p>
                            <c:set var="regionTotal" value="${portfolioAnalytics.domesticStockValue + portfolioAnalytics.foreignStockValue}" />
                            <c:choose>
                                <c:when test="${regionTotal > 0}">
                                    <c:set var="domesticPct" value="${portfolioAnalytics.domesticStockValue * 100.0 / regionTotal}" />
                                    <div class="dashboard-region-bar">
                                        <div class="dashboard-region-bar-domestic" style="width: ${domesticPct}%;"></div>
                                    </div>
                                    <p class="dashboard-analytics-stat-sub">
                                        국내 <fmt:formatNumber value="${domesticPct}" pattern="#,##0.0"/>% · 해외 <fmt:formatNumber value="${100 - domesticPct}" pattern="#,##0.0"/>%
                                    </p>
                                </c:when>
                                <c:otherwise><p class="dashboard-analytics-stat-value">-</p></c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <div class="dashboard-empty">
                    <p>보유 중인 주식이 생기면 포트폴리오 분석을 볼 수 있습니다.</p>
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
                <ul class="dashboard-timeline-list" id="dashboard-timeline-list">
                    <c:forEach var="event" items="${timeline}">
                        <li data-brokerage="<c:out value='${event.brokerageName}'/>">
                            <span class="dashboard-timeline-badge dashboard-timeline-badge-${event.category}"><c:out value="${event.badge}"/></span>
                            <span class="dashboard-timeline-desc"><c:out value="${event.description}"/></span>
                            <span class="dashboard-timeline-amount"><fmt:formatNumber value="${event.amount}" pattern="#,##0"/>원</span>
                            <span class="dashboard-timeline-date"><c:out value="${event.occurredAtText}"/></span>
                        </li>
                    </c:forEach>
                </ul>
                <div class="dashboard-timeline-pagination" id="dashboard-timeline-pagination"></div>
            </c:when>
            <c:otherwise>
                <div class="dashboard-empty">
                    <p>최근 활동 내역이 없습니다.</p>
                </div>
            </c:otherwise>
        </c:choose>
    </section>
</section>

<script src="${dashboardJsUrl}" defer></script>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
