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
                        <li class="ranking-row"
                            data-nickname="${ranking.nickname}"
                            data-member-id="${ranking.memberId}"
                            data-trade-count="${ranking.tradeCount}"
                            data-return-rate="${ranking.returnRate}"
                            data-profile="${not empty ranking.profile ? ranking.profile : '/images/default-profile.svg'}"
                            onclick="openProfileModal(this)">

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
                          <c:set var="medalClass" value="${ranking.rankPosition le 3 ? ' ranking-avatar-'.concat(ranking.rankPosition) : ''}"/>
                          <c:choose>
                              <c:when test="${not empty ranking.profile}">
                                  <img class="ranking-avatar${medalClass}"
                                       src="${pageContext.request.contextPath}${ranking.profile}"
                                       onerror="this.onerror=null;
                                                this.src='${pageContext.request.contextPath}/images/default-profile.svg';"
                                       alt="랭커 프로필">
                              </c:when>

                              <c:otherwise>
                                  <img class="ranking-avatar${medalClass}"
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

<%-- 프로필 팝오버: 클릭한 랭킹 행 바로 아래에 붙어서 뜨는 작은 카드.
     모달과 다르게 배경을 어둡게 하지 않고, 화면 전체를 덮지도 않음 --%>
<div id="profile-popover" class="profile-popover" hidden>
    <button class="profile-popover-close" onclick="closeProfileModal()" aria-label="닫기">×</button>
    <img id="modal-avatar" class="profile-popover-avatar" src="" alt="프로필">
    <h2 id="modal-nickname"></h2>
    <p id="modal-memberid" class="profile-popover-id"></p>
    <div class="profile-popover-stats">
        <div>
            <span class="stat-label">수익률</span>
            <span id="modal-return-rate" class="stat-value"></span>
        </div>
        <div>
            <span class="stat-label">거래횟수</span>
            <span id="modal-trade-count" class="stat-value"></span>
        </div>
    </div>
</div>

<script>
// 랭킹 행을 클릭했을 때 실행됨. el = 클릭된 <li class="ranking-row"> 요소
function openProfileModal(el) {
    // 클릭된 행의 data-* 속성에서 정보를 읽어와 팝오버 내용 채우기
    document.getElementById('modal-nickname').textContent = el.dataset.nickname;
    document.getElementById('modal-memberid').textContent = '@' + el.dataset.memberId;
    document.getElementById('modal-avatar').src =
        '${pageContext.request.contextPath}' + el.dataset.profile;

    const rate = parseFloat(el.dataset.returnRate);
    document.getElementById('modal-return-rate').textContent =
        (rate >= 0 ? '+' : '') + rate.toFixed(1) + '%';

    document.getElementById('modal-trade-count').textContent =
        el.dataset.tradeCount + '회';

    const popover = document.getElementById('profile-popover');

    // 클릭한 랭킹 행의 화면상 위치를 계산해서, 그 바로 아래에 팝오버를 붙임
    const rect = el.getBoundingClientRect();
    popover.style.top = (rect.bottom + window.scrollY + 8) + 'px';
    popover.style.left = (rect.left + window.scrollX) + 'px';

    popover.hidden = false;
}

// X 버튼을 누르면 팝오버 숨김
function closeProfileModal() {
    document.getElementById('profile-popover').hidden = true;
}

// 팝오버가 열려있는 상태에서, 팝오버 자신과 랭킹 행이 아닌 다른 곳을 클릭하면 자동으로 닫힘
document.addEventListener('click', function (e) {
    const popover = document.getElementById('profile-popover');
    if (popover.hidden) return;

    const clickedRow = e.target.closest('.ranking-row');
    const clickedInsidePopover = e.target.closest('.profile-popover');

    if (!clickedInsidePopover && !clickedRow) {
        closeProfileModal();
    }
});
</script>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />