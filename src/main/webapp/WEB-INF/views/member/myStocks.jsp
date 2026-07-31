<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:url var="myStocksCssUrl" value="/css/my-stocks.css" />
<c:set var="pageCssUrl" value="${myStocksCssUrl}" scope="request" />
<c:url var="defaultProfileUrl" value="/images/common_member.png" />

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<section class="my-stocks-page" aria-labelledby="my-stocks-title">
    <header class="my-stocks-profile">
        <c:choose>
            <c:when test="${not empty member.profile}">
                <img class="my-stocks-profile-image"
                     src="${pageContext.request.contextPath}${member.profile}"
                     onerror="this.onerror=null; this.src='${defaultProfileUrl}';"
                     alt="프로필 이미지">
            </c:when>
            <c:otherwise>
                <img class="my-stocks-profile-image"
                     src="${defaultProfileUrl}"
                     alt="기본 프로필 이미지">
            </c:otherwise>
        </c:choose>

        <div>
            <p class="my-stocks-eyebrow">MY STOCK</p>
            <h1 id="my-stocks-title"><c:out value="${member.nickname}"/></h1>
        </div>
    </header>

    <section class="my-stocks-holdings" aria-labelledby="holdings-title">
        <div class="my-stocks-section-heading">
            <h2 id="holdings-title">보유 주식</h2>
            <span><c:out value="${fn:length(stockSummary.holdings)}"/>종목</span>
        </div>

        <c:choose>
            <c:when test="${not empty stockSummary.holdings}">
                <div class="my-stocks-table-wrap">
                    <table class="my-stocks-table">
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
                                    <span class="my-stocks-code"><c:out value="${holding.stockCode}"/></span>
                                </td>
                                <td data-label="수량">
                                    <fmt:formatNumber value="${holding.quantity}" pattern="#,##0"/>주
                                </td>
                                <td data-label="평균 매입가">
                                    <fmt:formatNumber value="${holding.avgPrice}" pattern="#,##0"/>원
                                </td>
                                <td data-label="현재가">
                                    <fmt:formatNumber value="${holding.currentPrice}" pattern="#,##0"/>원
                                </td>
                                <td data-label="평가금액">
                                    <fmt:formatNumber value="${holding.currentValue}" pattern="#,##0"/>원
                                </td>
                                <td data-label="수익률"
                                    class="${holding.profitAmount gt 0 ? 'value-positive' : (holding.profitAmount lt 0 ? 'value-negative' : '')}">
                                    <c:if test="${holding.returnRate gt 0}">+</c:if><fmt:formatNumber
                                        value="${holding.returnRate}" pattern="#,##0.00"/>%
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:when>
            <c:otherwise>
                <div class="my-stocks-empty">
                    <p>아직 보유 중인 주식이 없습니다.</p>
                    <a href="${pageContext.request.contextPath}/trade-hub">거래 허브에서 주식 살펴보기</a>
                </div>
            </c:otherwise>
        </c:choose>
    </section>

    <dl class="my-stocks-summary">
        <div class="my-stocks-summary-row">
            <dt>총 보유 주식</dt>
            <dd><fmt:formatNumber value="${stockSummary.totalStockQuantity}" pattern="#,##0"/>주</dd>
        </div>
        <div class="my-stocks-summary-row">
            <dt>수익률</dt>
            <dd class="${stockSummary.profitAmount gt 0 ? 'value-positive' : (stockSummary.profitAmount lt 0 ? 'value-negative' : '')}">
                <c:if test="${stockSummary.returnRate gt 0}">+</c:if><fmt:formatNumber
                    value="${stockSummary.returnRate}" pattern="#,##0.00"/>%
            </dd>
        </div>
        <div class="my-stocks-summary-row">
            <dt>수익금</dt>
            <dd class="${stockSummary.profitAmount gt 0 ? 'value-positive' : (stockSummary.profitAmount lt 0 ? 'value-negative' : '')}">
                <c:if test="${stockSummary.profitAmount gt 0}">+</c:if><fmt:formatNumber
                    value="${stockSummary.profitAmount}" pattern="#,##0"/>원
            </dd>
        </div>
        <div class="my-stocks-summary-row">
            <dt>현재 잔고</dt>
            <dd><fmt:formatNumber value="${stockSummary.currentBalance}" pattern="#,##0"/>원</dd>
        </div>
    </dl>
</section>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
