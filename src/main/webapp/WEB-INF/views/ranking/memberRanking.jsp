<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/ranking.css?v=9">

<section class="ranking-page" aria-labelledby="ranking-title">
    <div class="ranking-heading">
        <h1 id="ranking-title">투자 랭킹</h1>
        <p>수익률과 수익금 기준의 투자 순위를 한눈에 확인해 보세요.</p>
    </div>

    <%-- CSS Grid로 수익률 보드와 수익금 보드를 좌우에 배치한다. --%>
    <div class="ranking-grid">
        <section class="ranking-board" aria-labelledby="return-rate-ranking-title">
            <div class="ranking-board-heading">
                <div>
                    <h2 id="return-rate-ranking-title">수익률 순</h2>
                    <p>수익률이 높은 회원 순서입니다.</p>
                </div>
                <span class="ranking-board-badge">%</span>
            </div>

            <%--
                공통 조각에 이번에 출력할 목록과 표시 방식을 전달한다.
                jsp:include는 별도 JSP를 실행하므로 request 범위를 사용한다.
                rankingKey는 두 보드의 상세 영역 id가 서로 겹치지 않게 한다.
                profitPrimary=false이면 수익률을 큰 숫자로 먼저 표시한다.
            --%>
            <c:set var="currentRankingList" value="${returnRateRankingList}" scope="request" />
            <c:set var="rankingKey" value="return-rate" scope="request" />
            <c:set var="profitPrimary" value="${false}" scope="request" />
            <jsp:include page="/WEB-INF/views/ranking/rankingBoardFragment.jsp" />
        </section>

        <section class="ranking-board" aria-labelledby="profit-ranking-title">
            <div class="ranking-board-heading">
                <div>
                    <h2 id="profit-ranking-title">수익금 순</h2>
                    <p>평가손익이 높은 회원 순서입니다.</p>
                </div>
                <span class="ranking-board-badge">₩</span>
            </div>

            <%--
                같은 공통 조각을 다시 사용하되 목록과 표시 기준만 바꾼다.
                profitPrimary=true이면 수익금을 큰 숫자로 먼저 표시한다.
            --%>
            <c:set var="currentRankingList" value="${profitRankingList}" scope="request" />
            <c:set var="rankingKey" value="profit" scope="request" />
            <c:set var="profitPrimary" value="${true}" scope="request" />
            <jsp:include page="/WEB-INF/views/ranking/rankingBoardFragment.jsp" />
        </section>
    </div>
</section>

<script>
document.addEventListener("DOMContentLoaded", function () {
    // 두 보드의 모든 회원 행에 동일한 펼치기/접기 기능을 연결한다.
    const accordionItems = document.querySelectorAll(".ranking-item");

    accordionItems.forEach(function (item) {
        const button = item.querySelector("[data-accordion-button]");
        const content = item.querySelector("[data-accordion-content]");

        if (!button || !content) return;

        button.addEventListener("click", function () {
            const wasOpen = item.classList.contains("is-open");

            // 다른 행을 먼저 닫아 한 번에 하나의 상세 정보만 열리게 한다.
            accordionItems.forEach(function (otherItem) {
                const otherButton = otherItem.querySelector("[data-accordion-button]");
                const otherContent = otherItem.querySelector("[data-accordion-content]");

                otherItem.classList.remove("is-open");
                if (otherButton) otherButton.setAttribute("aria-expanded", "false");
                if (otherContent) otherContent.setAttribute("aria-hidden", "true");
            });

            if (wasOpen) return;

            item.classList.add("is-open");
            button.setAttribute("aria-expanded", "true");
            content.setAttribute("aria-hidden", "false");
        });
    });
});
</script>
