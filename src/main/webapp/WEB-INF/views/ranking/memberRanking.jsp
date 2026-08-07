
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<%-- 공통 상단 헤더(네비게이션 등) 삽입 --%>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<%-- 랭킹 화면 전용 스타일 --%>
<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/ranking.css?v=6">

<section class="ranking-page" aria-labelledby="ranking-title">

    <div class="ranking-heading">
        <h1 id="ranking-title">투자 랭킹</h1>
        <p>회원들의 투자 수익률 순위를 확인해 보세요.</p>
    </div>

    <div class="ranking-card">

        <c:choose>

            <%-- 랭킹 데이터(rankingList)가 하나라도 있을 때 --%>
            <c:when test="${not empty rankingList}">

                <ol class="ranking-list">

                    <%-- 랭킹 목록을 한 명씩 순회하며 카드 하나씩 출력 --%>
                    <c:forEach items="${rankingList}"
                               var="ranking"
                               varStatus="status">

                        <%-- 1~3위는 금·은·동 테두리 색을 다르게 주기 위한 클래스 계산 --%>
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

                            <%-- 클릭되는 회원 기본 정보 영역: 공개/비공개 모두 클릭 가능하게 항상 렌더링 --%>
                            <button type="button"
                                    class="ranking-row"
                                    data-accordion-button
                                    aria-expanded="false"
                                    aria-controls="ranking-detail-${status.index}">

                                <%-- 숫자 순위 표시 --%>
                                <span class="ranking-position"
                                      aria-label="${ranking.rankPosition}위">
                                    <span class="ranking-number" aria-hidden="true">
                                        ${ranking.rankPosition}
                                    </span>
                                </span>

                                <%-- 프로필 이미지: 등록된 이미지가 없으면 기본 이미지로 대체 --%>
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

                                <%-- 닉네임, 아이디, 보유 수량 --%>
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
                              </span>
</span>


                                <%-- 수익률/평가손익 요약 (카드 접혀있을 때도 보이는 부분) --%>
                                <span class="ranking-profit ${ranking.returnRate lt 0 ? 'is-negative' : 'is-positive'}">
                                    <strong>
                                        <fmt:formatNumber value="${ranking.returnRate}" pattern="#,##0.00"/>%
                                    </strong>
                                    <span>
                                        <fmt:formatNumber value="${ranking.profit}" pattern="#,##0"/>원
                                    </span>
                                </span>

                                <%-- 아코디언 화살표: 공개 여부와 무관하게 모든 회원에게 항상 표시 --%>
                                <span class="ranking-chevron" aria-hidden="true"></span>

                            </button>

                            <%-- 클릭 시 펼쳐지는 상세 정보 영역: 안쪽 내용만 공개 여부에 따라 달라짐 --%>
                            <div id="ranking-detail-${status.index}"
                                 class="ranking-details"
                                 data-accordion-content
                                 aria-hidden="true">

                                <div class="ranking-details-inner">

                                    <div class="ranking-details-content">

                                        <c:choose>

                                            <%-- tradeHistoryPublicYn이 'Y'(공개)인 회원: 실제 투자 수치 표시 --%>
                                            <c:when test="${ranking.tradeHistoryPublicYn eq 'Y'}">

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
                                                    <p>현재 랭킹 집계 기준의 투자 내역입니다.</p>
                                                </div>

                                                <div class="ranking-details-stats">

                                                    <div class="ranking-detail-stat">
                                                        <span>수익률</span>
                                                        <strong class="${ranking.returnRate lt 0 ? 'is-negative' : 'is-positive'}">
                                                            <fmt:formatNumber value="${ranking.returnRate}" pattern="#,##0.00"/>%
                                                        </strong>
                                                    </div>

                                                    <div class="ranking-detail-stat">
                                                        <span>평가손익</span>
                                                        <strong class="${ranking.profit lt 0 ? 'is-negative' : 'is-positive'}">
                                                            <fmt:formatNumber value="${ranking.profit}" pattern="#,##0"/>원
                                                        </strong>
                                                    </div>



                                                </div>

                                            </c:when>

                                            <%-- tradeHistoryPublicYn이 'N'(비공개)인 회원: 수치 대신 안내 문구만 표시 --%>
                                            <c:otherwise>

                                                <div class="ranking-details-intro">
                                                    <strong>비공개로 설정한 회원입니다</strong>
                                                    <p>이 회원은 투자 정보를 공개하지 않았습니다.</p>
                                                </div>

                                            </c:otherwise>

                                        </c:choose>

                                    </div>

                                </div>

                            </div>

                        </li>

                    </c:forEach>

                </ol>

            </c:when>

            <%-- 랭킹 데이터가 하나도 없을 때 보여주는 빈 화면 --%>
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

<%-- 카드 클릭 시 아코디언을 열고 닫는 동작 --%>
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
                    otherItem.querySelector("[data-accordion-button]");

                const otherContent =
                    otherItem.querySelector("[data-accordion-content]");

                otherItem.classList.remove("is-open");

                if (otherButton) {
                    otherButton.setAttribute("aria-expanded", "false");
                }

                if (otherContent) {
                    otherContent.setAttribute("aria-hidden", "true");
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
            button.setAttribute("aria-expanded", "true");
            content.setAttribute("aria-hidden", "false");
        });
    });
});
</script>