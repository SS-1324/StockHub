<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<%--
    실현수익률 보드와 실현수익금 보드가 함께 사용하는 공통 카드 조각이다.
    부모인 memberRanking.jsp에서 currentRankingList, rankingKey,
    profitPrimary 값을 request 범위로 설정한 뒤 이 파일을 불러온다.
--%>

<div class="ranking-card">
    <c:choose>
        <c:when test="${not empty currentRankingList}">
            <ol class="ranking-list">

                <c:forEach items="${currentRankingList}" var="ranking" varStatus="status">

                    <%-- 현재 보드의 정렬 결과 1~3위에 금·은·동 CSS 클래스를 부여한다. --%>
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

                        <%--
                            rankingKey를 넣어
                            왼쪽과 오른쪽 보드의 상세 영역 id 중복을 방지한다.
                        --%>
                        <button type="button"
                                class="ranking-row"
                                data-accordion-button
                                aria-expanded="false"
                                aria-controls="ranking-detail-${rankingKey}-${status.index}">


                            <span class="ranking-position"
                                  aria-label="${ranking.rankPosition}위">

                                <span class="ranking-number"
                                      aria-hidden="true">
                                    ${ranking.rankPosition}
                                </span>

                            </span>


                            <%--
                                프로필 이미지나 이름을 클릭하면
                                아코디언 대신 공통 프로필 모달을 연다.

                                data-profile-rank-type을 통해
                                이 프로필을 실현수익률 보드에서 눌렀는지,
                                실현수익금 보드에서 눌렀는지 구분한다.
                            --%>

                            <span class="ranking-avatar-frame"
                                  data-user-profile="${ranking.memberId}"
                                  data-profile-rank-type="${profitPrimary ? 'profit' : 'returnRate'}"
                                  role="button"
                                  tabindex="0">

                                <c:choose>

                                    <c:when test="${not empty ranking.profile}">
                                        <img class="ranking-avatar"
                                             src="${pageContext.request.contextPath}${ranking.profile}"
                                             onerror="this.onerror=null; this.src='${pageContext.request.contextPath}/images/common_member.png';"
                                             alt="${ranking.nickname} 프로필">
                                    </c:when>

                                    <c:otherwise>
                                        <img class="ranking-avatar"
                                             src="${pageContext.request.contextPath}/images/common_member.png"
                                             alt="기본 프로필">
                                    </c:otherwise>

                                </c:choose>

                            </span>


                            <span class="ranking-member"
                                  data-user-profile="${ranking.memberId}"
                                  data-profile-rank-type="${profitPrimary ? 'profit' : 'returnRate'}"
                                  role="button"
                                  tabindex="0">

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


                            <%--
                                실현수익률 보드에서는 실현수익률을 크게 표시하고,
                                실현수익금 보드에서는 누적 실현손익을 크게 표시한다.

                                각 숫자는 자신의 값이
                                양수면 빨강,
                                음수면 파랑,
                                0이면 중립색으로 표시한다.
                            --%>


                            <%--
                                공개 설정값이 Y인 회원만
                                실현수익률과 누적 실현손익을 HTML에 출력한다.
                            --%>

                            <c:choose>

                                <c:when test="${ranking.tradeHistoryPublicYn eq 'Y'}">

                                    <c:choose>

                                        <%--
                                            실현수익금 보드
                                            누적 실현손익을 크게,
                                            실현수익률을 작게 표시
                                        --%>
                                        <c:when test="${profitPrimary}">

                                            <span class="ranking-profit">

                                                <strong class="${ranking.profit gt 0 ? 'is-positive' : (ranking.profit lt 0 ? 'is-negative' : 'is-neutral')}">

                                                    <fmt:formatNumber
                                                            value="${ranking.profit}"
                                                            pattern="#,##0"/>원

                                                </strong>


                                                <span class="${ranking.returnRate gt 0 ? 'is-positive' : (ranking.returnRate lt 0 ? 'is-negative' : 'is-neutral')}">

                                                    <fmt:formatNumber
                                                            value="${ranking.returnRate}"
                                                            pattern="#,##0.00"/>%

                                                </span>

                                            </span>

                                        </c:when>


                                        <%--
                                            실현수익률 보드
                                            실현수익률을 크게,
                                            누적 실현손익을 작게 표시
                                        --%>
                                        <c:otherwise>

                                            <span class="ranking-profit">

                                                <strong class="${ranking.returnRate gt 0 ? 'is-positive' : (ranking.returnRate lt 0 ? 'is-negative' : 'is-neutral')}">

                                                    <fmt:formatNumber
                                                            value="${ranking.returnRate}"
                                                            pattern="#,##0.00"/>%

                                                </strong>


                                                <span class="${ranking.profit gt 0 ? 'is-positive' : (ranking.profit lt 0 ? 'is-negative' : 'is-neutral')}">

                                                    <fmt:formatNumber
                                                            value="${ranking.profit}"
                                                            pattern="#,##0"/>원

                                                </span>

                                            </span>

                                        </c:otherwise>

                                    </c:choose>

                                </c:when>


                                <c:otherwise>

                                    <span class="ranking-profit is-private"
                                          aria-label="투자 정보 비공개">

                                        <strong>비공개</strong>

                                    </span>

                                </c:otherwise>

                            </c:choose>


                            <span class="ranking-chevron"
                                  aria-hidden="true">
                            </span>

                        </button>


                        <%--
                            회원 행을 클릭했을 때 아래로 펼쳐지는
                            아코디언 상세 영역
                        --%>

                        <div id="ranking-detail-${rankingKey}-${status.index}"
                             class="ranking-details"
                             data-accordion-content
                             aria-hidden="true">

                            <div class="ranking-details-inner">

                                <div class="ranking-details-content">


                                    <%--
                                        공개 설정 회원만
                                        실현수익률과 누적 실현손익 상세값을 보여준다.
                                    --%>

                                    <c:choose>

                                        <c:when test="${ranking.tradeHistoryPublicYn eq 'Y'}">

                                            <%--
                                                회원 이름은 위 행에 이미 표시되어 있으므로
                                                상세 영역에서는 핵심 투자 수치만 보여준다.
                                            --%>

                                            <div class="ranking-details-stats">


                                                <div class="ranking-detail-stat">

                                                    <span>실현수익률</span>

                                                    <strong class="${ranking.returnRate gt 0 ? 'is-positive' : (ranking.returnRate lt 0 ? 'is-negative' : 'is-neutral')}">

                                                        <fmt:formatNumber
                                                                value="${ranking.returnRate}"
                                                                pattern="#,##0.00"/>%

                                                    </strong>

                                                </div>


                                                <div class="ranking-detail-stat">

                                                    <span>누적 실현손익</span>

                                                    <strong class="${ranking.profit gt 0 ? 'is-positive' : (ranking.profit lt 0 ? 'is-negative' : 'is-neutral')}">

                                                        <fmt:formatNumber
                                                                value="${ranking.profit}"
                                                                pattern="#,##0"/>원

                                                    </strong>

                                                </div>


                                            </div>

                                        </c:when>


                                        <c:otherwise>

                                            <div class="ranking-details-intro">

                                                <strong>
                                                    비공개로 설정한 회원입니다
                                                </strong>

                                                <p>
                                                    이 회원은 투자 정보를 공개하지 않았습니다.
                                                </p>

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


        <c:otherwise>

            <div class="ranking-empty">

                <span aria-hidden="true">📊</span>

                <strong>
                    표시할 랭킹이 없습니다.
                </strong>

                <p>
                    매도 완료된 거래 데이터가 등록되면 랭킹이 표시됩니다.
                </p>

            </div>

        </c:otherwise>

    </c:choose>
</div>