<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/ranking.css">

<%-- 피그마 시안의 중앙 720px 랭킹보드 영역 --%>
<section class="ranking-page" aria-labelledby="ranking-title">
    <header class="ranking-heading">
        <h1 id="ranking-title">랭킹보드</h1>
        <p>검증된 거래 히스토리 기반 수익률 랭킹입니다.</p>
    </header>

    <%-- 서버 조회 기간과 연결된 필터. 기본 선택은 월간 --%>
    <nav class="ranking-periods" aria-label="랭킹 조회 기간">
        <a class="${selectedPeriod eq 'daily' ? 'is-active' : ''}"
           href="${pageContext.request.contextPath}/ranking?period=daily"
           aria-current="${selectedPeriod eq 'daily' ? 'page' : 'false'}">
            일간
        </a>

        <a class="${selectedPeriod eq 'weekly' ? 'is-active' : ''}"
           href="${pageContext.request.contextPath}/ranking?period=weekly"
           aria-current="${selectedPeriod eq 'weekly' ? 'page' : 'false'}">
            주간
        </a>

        <a class="${selectedPeriod eq 'monthly' ? 'is-active' : ''}"
           href="${pageContext.request.contextPath}/ranking?period=monthly"
           aria-current="${selectedPeriod eq 'monthly' ? 'page' : 'false'}">
            월간
        </a>

        <a class="${selectedPeriod eq 'yearly' ? 'is-active' : ''}"
           href="${pageContext.request.contextPath}/ranking?period=yearly"
           aria-current="${selectedPeriod eq 'yearly' ? 'page' : 'false'}">
            연간
        </a>
    </nav>

    <div class="ranking-card">
        <c:choose>

            <%-- 랭킹 데이터가 하나도 없을 때 --%>
            <c:when test="${empty rankingList}">
                <div class="ranking-empty">
                    <span aria-hidden="true">📊</span>
                    <strong>아직 집계된 랭킹이 없습니다.</strong>
                    <p>거래 데이터가 쌓이면 이곳에 수익률 순위가 표시됩니다.</p>
                </div>
            </c:when>

            <%-- 랭킹 데이터가 있을 때 --%>
            <c:otherwise>
                <ol class="ranking-list">

                    <c:forEach var="ranking" items="${rankingList}">
                        <li class="ranking-row">

                            <%-- 순위 및 메달 --%>
                            <div class="ranking-position"
                                 aria-label="${ranking.rankPosition}위">

                                <c:choose>
                                    <c:when test="${ranking.rankPosition le 3}">
                                        <span class="ranking-medal
                                                     ranking-medal-${ranking.rankPosition}"
                                              aria-hidden="true">
                                            <i></i>
                                            <b>${ranking.rankPosition}</b>
                                        </span>
                                    </c:when>

                                    <c:otherwise>
                                        <span class="ranking-number"
                                              aria-hidden="true">
                                            ${ranking.rankPosition}
                                        </span>
                                    </c:otherwise>
                                </c:choose>
                            </div>

                            <%-- 프로필 이미지 --%>
                            <c:choose>
                                <c:when test="${not empty ranking.profile}">
                                    <img class="ranking-avatar"
                                         src="${pageContext.request.contextPath}${ranking.profile}"
                                         onerror="this.onerror=null;
                                                  this.src='${pageContext.request.contextPath}/images/default-profile.svg';"
                                         alt="랭커 프로필">
                                </c:when>

                                <c:otherwise>
                                    <img class="ranking-avatar"
                                         src="${pageContext.request.contextPath}/images/default-profile.svg"
                                         alt="기본 프로필">
                                </c:otherwise>
                            </c:choose>

                            <%-- 회원 닉네임, 아이디, 거래 횟수 --%>
                            <div class="ranking-member">
                                <strong>
                                    <c:out value="${ranking.nickname}"/>
                                </strong>

                                <p>
                                    @<c:out value="${ranking.memberId}"/>
                                    <span aria-hidden="true">·</span>
                                    <fmt:formatNumber
                                            value="${ranking.tradeCount}"/>회 거래
                                </p>
                            </div>

                            <%-- 수익률과 수익금 --%>
                            <div class="ranking-profit
                                        ${ranking.returnRate ge 0
                                        ? 'is-positive'
                                        : 'is-negative'}">

                                <strong>${ranking.returnRate ge 0 ? '+' : ''}<fmt:formatNumber
                                        value="${ranking.returnRate}"
                                        minFractionDigits="1"
                                        maxFractionDigits="2"/>%</strong>

                                <p>${ranking.profit ge 0 ? '+' : ''}<fmt:formatNumber
                                        value="${ranking.profit}"/>원</p>
                            </div>

                        </li>
                    </c:forEach>

                </ol>
            </c:otherwise>

        </c:choose>
    </div>
</section>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />