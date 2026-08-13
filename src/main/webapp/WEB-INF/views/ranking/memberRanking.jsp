<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/ranking.css?v=14">

<section class="ranking-page" aria-labelledby="ranking-title">

    <div class="ranking-heading">
        <h1 id="ranking-title">투자 랭킹</h1>
        <p>매도 완료된 거래의 실현손익을 기준으로 투자 순위를 확인해 보세요.</p>
    </div>

    <%--
        매도 완료된 거래의 실현손익을 기준으로 랭킹을 표시한다.
        현재 보유 중인 종목의 미실현 평가손익은 랭킹에 반영하지 않는다.

        실현수익률 순:
        누적 실현수익률이 높은 순서

        실현수익금 순:
        수수료를 반영한 누적 실현손익이 높은 순서

        두 랭킹 보드는 CSS Grid로 좌우에 배치한다.
    --%>

    <div class="ranking-grid">

        <!-- 실현수익률 랭킹 -->
        <section class="ranking-board"
                 aria-labelledby="return-rate-ranking-title">

            <div class="ranking-board-heading">

                <div>
                    <h2 id="return-rate-ranking-title">
                        실현수익률 순
                    </h2>

                    <p>
                        매도 완료 거래의 실현수익률이 높은 회원 순서입니다.
                    </p>
                </div>

                <span class="ranking-board-badge">%</span>

            </div>


            <%--
                공통 랭킹 조각에
                실현수익률 기준 목록을 전달한다.

                rankingKey는 두 보드의 상세 영역 id가
                서로 중복되지 않게 구분한다.

                profitPrimary=false이면
                실현수익률을 큰 숫자로 먼저 표시한다.
            --%>

            <c:set var="currentRankingList"
                   value="${returnRateRankingList}"
                   scope="request" />

            <c:set var="rankingKey"
                   value="return-rate"
                   scope="request" />

            <c:set var="profitPrimary"
                   value="${false}"
                   scope="request" />

            <jsp:include page="/WEB-INF/views/ranking/rankingBoardFragment.jsp" />

        </section>


        <!-- 실현수익금 랭킹 -->
        <section class="ranking-board"
                 aria-labelledby="profit-ranking-title">

            <div class="ranking-board-heading">

                <div>
                    <h2 id="profit-ranking-title">
                        실현수익금 순
                    </h2>

                    <p>
                        수수료를 반영한 누적 실현손익이 높은 회원 순서입니다.
                    </p>
                </div>

                <span class="ranking-board-badge">₩</span>

            </div>


            <%--
                공통 랭킹 조각에
                누적 실현손익 기준 목록을 전달한다.

                profitPrimary=true이면
                누적 실현손익을 큰 숫자로 먼저 표시한다.
            --%>

            <c:set var="currentRankingList"
                   value="${profitRankingList}"
                   scope="request" />

            <c:set var="rankingKey"
                   value="profit"
                   scope="request" />

            <c:set var="profitPrimary"
                   value="${true}"
                   scope="request" />

            <jsp:include page="/WEB-INF/views/ranking/rankingBoardFragment.jsp" />

        </section>

    </div>

</section>


<script>
document.addEventListener("DOMContentLoaded", function () {

    /*
     * [랭킹 아코디언]
     *
     * 두 랭킹 보드의 모든 회원 행에 같은 이벤트를 연결한다.
     * 한 번에 하나의 상세 정보만 열리도록 처리한다.
     *
     * CSS용 is-open과
     * 접근성용 aria-expanded / aria-hidden을 함께 변경한다.
     */

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
             * 현재 행을 열기 전에
             * 모든 랭킹 행을 먼저 닫는다.
             *
             * 따라서 왼쪽/오른쪽 보드를 통틀어
             * 한 번에 하나의 상세 영역만 열린다.
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
             * 이미 열려 있던 행을 다시 클릭했다면
             * 위에서 닫은 상태 그대로 유지한다.
             */

            if (wasOpen) {
                return;
            }


            /*
             * 클릭한 행만 다시 연다.
             */

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


<%--
    header.jsp가 시작한 공통 페이지 구조를 닫는다.

    랭킹 전용 푸터를 따로 만들지 않고
    다른 페이지와 동일한 공통 footer.jsp를 재사용한다.
--%>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />