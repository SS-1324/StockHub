<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%-- 커뮤니티 기본 URL --%>
<c:url var="communityUrl" value="/community" />
<c:url var="tradeHubUrl" value="/trade-hub" />

<%-- 공통 헤더 --%>
<jsp:include page="/WEB-INF/views/common/header.jsp" />


<%-- 기존 금·은·동 랭킹 CSS --%>
<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/ranking.css?v=7">

<%-- 커뮤니티 전용 CSS --%>
<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/board.css?v=36">


<%-- =========================================================
     게시판 제목
     ========================================================= --%>
<div class="board-list-header">

    <div class="board-heading-copy">
        <h2 class="page-title">커뮤니티</h2>

        <p class="board-page-description">
            투자 이야기를 편하게 나눠보세요.
        </p>
    </div>

    <%-- [커뮤니티상단정렬-1] 제목 행 오른쪽에는 글쓰기 버튼만 두어 검색창보다 한 줄 위에 배치한다. --%>
    <div class="board-toolbar-actions">
        <c:choose>
            <c:when test="${empty loginMemberId}">
                <a class="btn-write" href="${communityUrl}/write">
                    <span class="btn-write-icon" aria-hidden="true">+</span>
                    로그인하고 글쓰기
                </a>
            </c:when>
            <c:otherwise>
                <a class="btn-write" href="${communityUrl}/write">
                    <span class="btn-write-icon" aria-hidden="true">+</span>
                    글쓰기
                </a>
            </c:otherwise>
        </c:choose>
    </div>

</div>


<%-- =========================================================
     카테고리 필터와 검색창
     ========================================================= --%>
<div class="board-toolbar">

    <%-- 카테고리 필터 --%>
    <div class="category-filter">

        <a class="category-tab
                  ${empty category ? 'category-tab-active' : ''}"
           href="${communityUrl}">
            전체
        </a>

        <c:forEach var="entry" items="${allowedCategories}">
            <a class="category-tab
                      ${category == entry.key
                        ? 'category-tab-active'
                        : ''}"
               href="${communityUrl}?category=${entry.key}">

                <c:out value="${entry.value}" />
            </a>
        </c:forEach>

    </div>

    <%-- [커뮤니티검색위치-4] 용어사전처럼 두 번째 행 오른쪽에 320px 검색창을 독립 배치한다. --%>
    <form class="search-form board-search-form" action="${communityUrl}" method="get">
        <%-- 선택한 카테고리를 유지한 상태에서 검색한다. --%>
        <c:if test="${not empty category}">
            <input type="hidden" name="category" value="${category}">
        </c:if>

        <div class="search-box">
            <input type="text" name="keyword" value="${keyword}" placeholder="제목/내용 검색">
            <button type="submit" class="search-icon-btn" aria-label="검색">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none"
                     xmlns="http://www.w3.org/2000/svg">
                    <circle cx="11" cy="11" r="7" stroke="currentColor" stroke-width="2" />
                    <line x1="21" y1="21" x2="16.65" y2="16.65"
                          stroke="currentColor" stroke-width="2" stroke-linecap="round" />
                </svg>
            </button>
        </div>
    </form>
</div>


<%-- =========================================================
     검색 결과 안내
     ========================================================= --%>
<c:if test="${not empty keyword}">
    <div class="search-result-info">

        '<strong><c:out value="${keyword}" /></strong>'
        검색 결과 총
        <strong><c:out value="${totalCount}" /></strong>건

        <a class="search-reset"
           href="${communityUrl}${not empty category
                   ? '?category='.concat(category)
                   : ''}">
            전체보기
        </a>

    </div>
</c:if>


<%-- [커뮤니티사이드바-1]
     본문은 다시 한 줄 피드로 표시하고, 남는 오른쪽 폭은 독립적인 정보 위젯으로 사용한다. --%>
<div class="board-community-layout">
    <section class="board-feed-column" aria-label="커뮤니티 게시글 목록">
        <%-- 게시글 또는 검색 결과가 없을 때 표시하는 안내 --%>
        <c:if test="${empty boardList}">
            <p class="form-tip">
                <c:choose>
                    <c:when test="${not empty keyword}">검색 결과가 없습니다.</c:when>
                    <c:otherwise>등록된 게시글이 없습니다.</c:otherwise>
                </c:choose>
            </p>
        </c:if>

        <%-- 실제 카드 HTML은 boardCards.jsp에서 생성하며 무한스크롤 추가 카드도 이 열에 붙는다. --%>
        <div class="board-list" id="board-list">
            <jsp:include page="/WEB-INF/views/community/boardCards.jsp" />
        </div>

        <%-- 화면에 나타나면 board.js가 다음 페이지를 요청한다. --%>
        <div id="feed-sentinel"
             data-next-page="2"
             data-category="${category}"
             data-keyword="${keyword}">
        </div>
    </section>

    <aside class="board-sidebar" aria-label="커뮤니티 추천 정보">
        <%-- [커뮤니티사이드바-2] 좋아요·댓글·조회수를 합산한 참여도 순위 --%>
        <section class="board-widget" aria-labelledby="hot-board-title">
            <div class="board-widget-heading">
                <h3 id="hot-board-title">HOT 글</h3>
                <span class="board-widget-label is-hot">HOT</span>
            </div>
            <ol class="board-widget-list">
                <c:forEach var="hotBoard" items="${hotBoards}" varStatus="status">
                    <li>
                        <a href="${communityUrl}/${hotBoard.boardId}">
                            <span class="board-widget-rank">${status.count}</span>
                            <span class="board-widget-copy">
                                <strong><c:choose>
                                    <c:when test="${not empty hotBoard.title}"><c:out value="${hotBoard.title}" /></c:when>
                                    <c:otherwise>제목 없는 게시글</c:otherwise>
                                </c:choose></strong>
                                <small>좋아요 ${hotBoard.likeCount} · 댓글 ${hotBoard.commentCount}</small>
                            </span>
                        </a>
                    </li>
                </c:forEach>
            </ol>
        </section>

        <%-- [커뮤니티사이드바-3] 누적 조회수 기준 인기글 --%>
        <section class="board-widget" aria-labelledby="popular-board-title">
            <div class="board-widget-heading">
                <h3 id="popular-board-title">인기글</h3>
                <span class="board-widget-label">조회수 순</span>
            </div>
            <ol class="board-widget-list">
                <c:forEach var="popularBoard" items="${popularBoards}" varStatus="status">
                    <li>
                        <a href="${communityUrl}/${popularBoard.boardId}">
                            <span class="board-widget-rank">${status.count}</span>
                            <span class="board-widget-copy">
                                <strong><c:choose>
                                    <c:when test="${not empty popularBoard.title}"><c:out value="${popularBoard.title}" /></c:when>
                                    <c:otherwise>제목 없는 게시글</c:otherwise>
                                </c:choose></strong>
                                <small>조회 ${popularBoard.count}</small>
                            </span>
                        </a>
                    </li>
                </c:forEach>
            </ol>
        </section>

        <%-- [커뮤니티사이드바-4] 실제 광고 연동 전 사용하는 교체 가능한 내부 프로모션 영역 --%>
        <a class="board-ad-widget" href="${tradeHubUrl}" aria-label="주식 거래 허브 바로가기">
            <span class="board-ad-label">AD</span>
            <strong>차트를 보며<br>투자 이야기를 확인하세요</strong>
            <small>실시간 차트와 관심 종목을 한곳에서</small>
            <span class="board-ad-action">거래 허브 보기 →</span>
        </a>
    </aside>
</div>


<%-- 게시판 전용 JavaScript --%>
<script src="${pageContext.request.contextPath}/js/board.js?v=5"></script>

<%-- 공통 푸터 --%>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
