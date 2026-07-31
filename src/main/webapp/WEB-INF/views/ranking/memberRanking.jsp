<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/ranking.css?v=6">

<section class="ranking-page" aria-labelledby="ranking-title">

    <div class="ranking-heading">
        <h1 id="ranking-title">투자 랭킹</h1>
        <p>회원들의 투자 수익률 순위를 확인해 보세요.</p>
    </div>

    <div class="ranking-card">

        <c:choose>

            <%-- 랭킹 데이터가 있을 때 --%>
            <c:when test="${not empty rankingList}">

                <ol class="ranking-list">

                    <c:forEach items="${rankingList}"
                               var="ranking"
                               varStatus="status">

                        <%-- 1~3위 프로필 테두리용 클래스 --%>
                        <c:set var="rankClass" value="" />

                        <c:choose>
                            <c:when test="${ranking.rankPosition eq 1}">
                                <c:set var="rankClass" value="rank-first" />
                            </c:when>

                            <c:when test="${ranking.rankPosition eq 2}">
                                <c:set var="rankClass" value="rank-second" />
                            </c:when>

                            <c:when test="${ranking.rankPosition eq 3}">
                                <c:set var="rankClass" value="rank-third" />
                            </c:when>
                        </c:choose>

                        <li class="ranking-item ${rankClass}">

                            <%-- 클릭되는 회원 기본 정보 영역 --%>
                            <button type="button"
                                    class="ranking-row"
                                    data-accordion-button
                                    aria-expanded="false"
                                    aria-controls="ranking-detail-${status.index}">

                                <%-- 숫자 순위 --%>
                                <span class="ranking-position"
                                      aria-label="${ranking.rankPosition}위">

                                    <span class="ranking-number"
                                          aria-hidden="true">
                                        ${ranking.rankPosition}
                                    </span>
                                </span>

                                <%-- 프로필 이미지와 금·은·동 테두리 --%>
                                <span class="ranking-avatar-frame">

                                    <c:choose>

                                        <c:when test="${not empty ranking.profile}">
                                            <img class="ranking-avatar"
                                                 src="${pageContext.request.contextPath}${ranking.profile}"
                                                 onerror="this.onerror=null; this.src='${pageContext.request.contextPath}/images/common_member.png';"
                                                 alt="랭커 프로필">
                                        </c:when>

                                        <c:otherwise>
                                            <img class="ranking-avatar"
                                                 src="${pageContext.request.contextPath}/images/common_member.png"
                                                 alt="기본 프로필">
                                        </c:otherwise>

                                    </c:choose>

                                </span>

                                <%-- 회원 정보 --%>
                                <span class="ranking-member">

                                    <strong>
                                        <c:choose>
                                            <c:when test="${not empty ranking.nickname}">
                                                <c:out value="${ranking.nickname}" />
                                            </c:when>

                                            <c:otherwise>
                                                이름 없는 투자자
                                            </c:otherwise>
                                        </c:choose>
                                    </strong>

                                    <span class="ranking-member-description">

                                        <span class="ranking-member-id">
                                            @<c:out value="${ranking.memberId}" />
                                        </span>

                                        <span aria-hidden="true">·</span>

                                        보유 수량
                                        <fmt:formatNumber
                                                value="${ranking.holdingQuantity}"
                                                pattern="#,##0"/>주

                                    </span>

                                </span>

                                <%-- 수익 정보 --%>
                                <span class="ranking-profit
                                    ${ranking.returnRate lt 0
                                        ? 'is-negative'
                                        : 'is-positive'}">

                                    <strong>
                                        <fmt:formatNumber
                                                value="${ranking.returnRate}"
                                                pattern="#,##0.00"/>%
                                    </strong>

                                    <span>
                                        <fmt:formatNumber
                                                value="${ranking.profit}"
                                                pattern="#,##0"/>원
                                    </span>

                                </span>

                                <%-- 아코디언 화살표 --%>
                                <span class="ranking-chevron"
                                      aria-hidden="true">
                                </span>

                            </button>

                            <%-- 클릭했을 때 열리는 회원 상세 정보 --%>
                            <div id="ranking-detail-${status.index}"
                                 class="ranking-details"
                                 data-accordion-content
                                 aria-hidden="true">

                                <div class="ranking-details-inner">

                                    <div class="ranking-details-content">

                                        <div class="ranking-details-intro">

                                            <strong>
                                                <c:choose>
                                                    <c:when test="${not empty ranking.nickname}">
                                                        <c:out value="${ranking.nickname}" />님의 투자 정보
                                                    </c:when>

                                                    <c:otherwise>
                                                        회원 투자 정보
                                                    </c:otherwise>
                                                </c:choose>
                                            </strong>

                                            <p>
                                                현재 랭킹 집계 기준의 투자 내역입니다.
                                            </p>

                                        </div>

                                        <div class="ranking-details-stats">

                                            <div class="ranking-detail-stat">
                                                <span>수익률</span>

                                                <strong class="${ranking.returnRate lt 0
                                                    ? 'is-negative'
                                                    : 'is-positive'}">

                                                    <fmt:formatNumber
                                                            value="${ranking.returnRate}"
                                                            pattern="#,##0.00"/>%

                                                </strong>
                                            </div>

                                            <div class="ranking-detail-stat">
                                                <span>평가손익</span>

                                                <strong class="${ranking.profit lt 0
                                                    ? 'is-negative'
                                                    : 'is-positive'}">

                                                    <fmt:formatNumber
                                                            value="${ranking.profit}"
                                                            pattern="#,##0"/>원

                                                </strong>
                                            </div>

                                            <div class="ranking-detail-stat">
                                                <span>보유 수량</span>

                                                <strong>
                                                    <fmt:formatNumber
                                                            value="${ranking.holdingQuantity}"
                                                            pattern="#,##0"/>주
                                                </strong>
                                            </div>

                                        </div>

                                    </div>

                                </div>

                            </div>

                        </li>

                    </c:forEach>

                </ol>

            </c:when>

            <%-- 랭킹 데이터가 없을 때 --%>
            <c:otherwise>

                <div class="ranking-empty">
                    <span aria-hidden="true">📊</span>
                    <strong>표시할 랭킹이 없습니다.</strong>
                    <p>투자 데이터가 등록되면 랭킹이 표시됩니다.</p>
                </div>

            </c:otherwise>

        </c:choose>

    </div>

</section>

<script>
document.addEventListener("DOMContentLoaded", function () {

    const accordionItems =
        document.querySelectorAll(".ranking-item");

    accordionItems.forEach(function (item) {

        const button =
            item.querySelector("[data-accordion-button]");

        const content =
            item.querySelector("[data-accordion-content]");

        if (!button || !content) {
            return;
        }

        button.addEventListener("click", function () {

            const wasOpen =
                item.classList.contains("is-open");

            /*
             * 다른 회원의 정보가 열려 있다면 먼저 닫는다.
             * 한 번에 한 명의 정보만 표시된다.
             */
            accordionItems.forEach(function (otherItem) {

                const otherButton =
                    otherItem.querySelector(
                        "[data-accordion-button]"
                    );

                const otherContent =
                    otherItem.querySelector(
                        "[data-accordion-content]"
                    );

                otherItem.classList.remove("is-open");

                if (otherButton) {
                    otherButton.setAttribute(
                        "aria-expanded",
                        "false"
                    );
                }

                if (otherContent) {
                    otherContent.setAttribute(
                        "aria-hidden",
                        "true"
                    );
                }
            });

            /*
             * 이미 열려 있던 회원을 다시 누른 경우에는
             * 닫힌 상태를 그대로 유지한다.
             */
            if (wasOpen) {
                return;
            }

            item.classList.add("is-open");

            button.setAttribute(
                "aria-expanded",
                "true"
            );

            content.setAttribute(
                "aria-hidden",
                "false"
            );
        });
    });
});
</script>