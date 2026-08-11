<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%-- 커뮤니티 기본 URL --%>
<c:url var="communityUrl" value="/community" />

<%-- 공통 헤더 --%>
<jsp:include page="/WEB-INF/views/common/header.jsp" />


<%-- 기존 금·은·동 랭킹 CSS --%>
<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/ranking.css?v=7">

<%-- 커뮤니티 전용 CSS --%>
<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/board.css?v=29">


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


    <%-- [게시판도구-1] 검색과 글쓰기를 같은 묶음에 두어 목록 조작 기능의 위치를 통일한다. --%>
    <div class="board-toolbar-actions">
        <%-- 게시글 검색 --%>
        <form class="search-form"
              action="${communityUrl}"
              method="get">

        <%-- 선택한 카테고리를 유지한 상태에서 검색한다. --%>
        <c:if test="${not empty category}">
            <input type="hidden"
                   name="category"
                   value="${category}">
        </c:if>

        <div class="search-box">

            <input type="text"
                   name="keyword"
                   value="${keyword}"
                   placeholder="제목/내용 검색">

            <button type="submit"
                    class="search-icon-btn"
                    aria-label="검색">

                <svg width="15"
                     height="15"
                     viewBox="0 0 24 24"
                     fill="none"
                     xmlns="http://www.w3.org/2000/svg">

                    <circle cx="11"
                            cy="11"
                            r="7"
                            stroke="currentColor"
                            stroke-width="2" />

                    <line x1="21"
                          y1="21"
                          x2="16.65"
                          y2="16.65"
                          stroke="currentColor"
                          stroke-width="2"
                          stroke-linecap="round" />

                </svg>
            </button>

        </div>
        </form>

        <%-- 로그인 여부에 따라 글쓰기 버튼 문구를 변경한다. --%>
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


<%-- =========================================================
     게시글 또는 검색 결과가 없을 때 표시하는 안내
     ========================================================= --%>
<c:if test="${empty boardList}">
    <p class="form-tip">

        <c:choose>
            <c:when test="${not empty keyword}">
                검색 결과가 없습니다.
            </c:when>

            <c:otherwise>
                등록된 게시글이 없습니다.
            </c:otherwise>
        </c:choose>

    </p>
</c:if>


<%-- =========================================================
     게시글 목록
     실제 카드 HTML은 boardCards.jsp에서 생성한다.
     ========================================================= --%>
<div class="board-list" id="board-list">
    <jsp:include page="/WEB-INF/views/community/boardCards.jsp" />
</div>


<%-- =========================================================
     무한 스크롤 감지 요소
     화면에 나타나면 board.js가 다음 페이지를 요청한다.
     ========================================================= --%>
<div id="feed-sentinel"
     data-next-page="2"
     data-category="${category}"
     data-keyword="${keyword}">
</div>


<%-- 게시판 전용 JavaScript --%>
<script src="${pageContext.request.contextPath}/js/board.js?v=3"></script>

<%-- 공통 푸터 --%>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
