<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:url var="dashboardCssUrl" value="/css/dashboard.css">
    <c:param name="v" value="2" />
</c:url>
<c:set var="pageCssUrl" value="${dashboardCssUrl}" scope="request" />
<c:url var="defaultProfileUrl" value="/images/common_member.png" />
<c:url var="dashboardJsUrl" value="/js/dashboard.js" />

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

    <%-- 총 자산 요약 카드: 증권사가 여러 곳이어도 여기 하나로 합산해서 보여주는 게 핵심 --%>
    <dl class="dashboard-summary dashboard-summary-total">
        <div class="dashboard-summary-row">
            <dt>총 자산</dt>
            <dd><fmt:formatNumber value="${totalAsset}" pattern="#,##0"/>원</dd>
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
                data-rate="${totalReturnRate}">
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
            <dd><fmt:formatNumber value="${stockSummary.currentBalance}" pattern="#,##0"/>원</dd>
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
                            <div class="dashboard-goal-ring" style="--progress: ${goalProgress[goal.goalId]};">
                                <span class="dashboard-goal-ring-value"><c:out value="${goalProgress[goal.goalId]}"/>%</span>
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
                    <button type="button" id="stock-holdings-chart-open" class="dashboard-chart-open-btn">차트로 보기</button>
                </c:if>
            </div>
            <c:if test="${not empty stockSummary.holdings}">
                <select id="stock-holdings-sort" class="dashboard-holdings-sort" aria-label="보유 주식 정렬">
                    <option value="default">기본순</option>
                    <option value="returnRate">수익률순</option>
                    <option value="quantity">수량순</option>
                    <option value="name">가나다순</option>
                    <option value="price">현재가순</option>
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
                            <tr data-name="<c:out value='${holding.stockName}'/>"
                                data-quantity="${holding.quantity}"
                                data-price="${holding.currentPrice}"
                                data-value="${holding.currentValue}"
                                data-return-rate="${holding.returnRate}">
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
                <dialog id="stock-portfolio-dialog" class="dashboard-portfolio-dialog">
                    <div class="dashboard-portfolio-dialog-header">
                        <h3>보유 주식 구성</h3>
                        <button type="button" id="stock-portfolio-dialog-close"
                                class="dashboard-portfolio-dialog-close" aria-label="닫기">&times;</button>
                    </div>
                    <div class="dashboard-portfolio-chart">
                        <div class="dashboard-portfolio-donut" id="stock-portfolio-donut"></div>
                        <ul class="dashboard-portfolio-legend" id="stock-portfolio-legend"></ul>
                    </div>
                </dialog>
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
                <ul class="dashboard-timeline-list" id="dashboard-timeline-list">
                    <c:forEach var="event" items="${timeline}">
                        <li>
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
