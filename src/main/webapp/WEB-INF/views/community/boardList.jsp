<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:url var="communityUrl" value="/community" scope="request" />

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/board.css">

<div class="board-page">
    <div class="board-list-top">
        <div class="board-list-heading">
            <h2 class="page-title">커뮤니티 게시판</h2>
            <p class="board-list-description">투자 이야기를 편하게 나눠보세요.</p>
        </div>

        <c:choose>
            <c:when test="${empty loginMemberId}">
                <a class="board-write-btn" href="${communityUrl}/write">로그인하고 글쓰기</a>
            </c:when>
            <c:otherwise>
                <a class="board-write-btn" href="${communityUrl}/write">글쓰기</a>
            </c:otherwise>
        </c:choose>
    </div>

    <div class="board-list-toolbar">
        <nav class="category-filter" aria-label="게시글 카테고리">
            <a class="category-tab ${empty category ? 'category-tab-active' : ''}"
               href="${communityUrl}">전체</a>

            <c:forEach var="entry" items="${allowedCategories}">
                <a class="category-tab ${category == entry.key ? 'category-tab-active' : ''}"
                   href="${communityUrl}?category=${entry.key}">${entry.value}</a>
            </c:forEach>
        </nav>

        <form class="search-form" action="${communityUrl}" method="get" role="search">
            <c:if test="${not empty category}">
                <input type="hidden" name="category" value="${category}">
            </c:if>

            <div class="search-box">
                <input type="text"
                       name="keyword"
                       value="${keyword}"
                       placeholder="제목/내용 검색"
                       aria-label="게시글 제목 및 내용 검색">

                <button type="submit" class="search-icon-btn" aria-label="검색">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none"
                         xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
                        <circle cx="11" cy="11" r="7" stroke="currentColor" stroke-width="2" />
                        <line x1="21" y1="21" x2="16.65" y2="16.65"
                              stroke="currentColor" stroke-width="2" stroke-linecap="round" />
                    </svg>
                </button>
            </div>
        </form>
    </div>

    <c:if test="${not empty keyword}">
        <div class="search-result-info">
            <span>'<strong><c:out value="${keyword}" /></strong>' 검색결과 총 <strong>${totalCount}</strong>건</span>
            <a href="${communityUrl}${not empty category ? '?category='.concat(category) : ''}"
               class="search-reset">전체보기</a>
        </div>
    </c:if>

    <c:if test="${empty boardList}">
        <div class="board-empty-state">
            <c:choose>
                <c:when test="${not empty keyword}">
                    <strong>검색 결과가 없어요.</strong>
                    <span>다른 검색어로 다시 찾아보세요.</span>
                </c:when>
                <c:otherwise>
                    <strong>아직 등록된 게시글이 없어요.</strong>
                    <span>첫 번째 이야기를 남겨보세요.</span>
                </c:otherwise>
            </c:choose>
        </div>
    </c:if>

    <div class="board-list" id="board-list">
        <jsp:include page="/WEB-INF/views/community/boardCards.jsp" />
    </div>

    <%-- 화면에 보이면 board.js가 다음 페이지를 불러와 목록 끝에 이어 붙인다. --%>
    <div id="feed-sentinel"
         data-next-page="2"
         data-category="${category}"
         data-keyword="${keyword}"></div>
</div>

<script src="${pageContext.request.contextPath}/js/board.js"></script>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
