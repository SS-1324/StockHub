<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/board.css">

<%--
    h2랑 form을 같은 부모(div.board-list-header) 안에 나란히 넣는다.
    그래야 CSS에서 이 둘을 flex로 한 줄에 배치할 수 있다.
    참고: 검색 기능용 Controller/Service/Mapper 코드가 이미 붙어있다는 전제.
    아직 안 붙였으면 그 백엔드 부분도 같이 해야 keyword가 실제로 동작함.
--%>

<div class="board-list-header">

    <h2 class="page-title">커뮤니티 게시판</h2>

<form class="search-form" action="/community/board" method="get">
    <c:if test="${not empty category}">
        <input type="hidden" name="category" value="${category}">
    </c:if>
    <div class="search-box">
        <input type="text" name="keyword" value="${keyword}" placeholder="제목/내용 검색">
        <button type="submit" class="search-icon-btn" aria-label="검색">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <circle cx="11" cy="11" r="7" stroke="currentColor" stroke-width="2"/>
                <line x1="21" y1="21" x2="16.65" y2="16.65" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
        </button>
    </div>
</form>

</div>
<c:choose>
    <c:when test="${empty loginMemberId}">
        <a class="btn-pill" href="/community/board/write">로그인하고 글쓰기</a>
    </c:when>
    <c:otherwise>
        <a class="btn-pill" href="/community/board/write">글쓰기</a>
    </c:otherwise>
</c:choose>

<div class="category-filter">
    <a class="category-tab ${empty category ? 'category-tab-active' : ''}" href="/community/board">전체</a>
    <c:forEach var="c" items="${allowedCategories}">
        <a class="category-tab ${category == c ? 'category-tab-active' : ''}"
           href="/community/board?category=${c}">${c}</a>
    </c:forEach>
</div>
<c:if test="${not empty keyword}">
    <div class="search-result-info">
        '<strong>${keyword}</strong>' 검색결과 총 <strong>${totalCount}</strong>건
        <a href="/community/board${not empty category ? '?category='.concat(category) : ''}" class="search-reset">전체보기</a>
    </div>
</c:if>

<c:if test="${empty boardList}">
    <p class="form-tip">등록된 게시글이 없습니다.</p>
</c:if>

<div class="board-list">
    <c:forEach var="board" items="${boardList}">
        <a class="board-card" href="/community/board/${board.boardId}">
            <c:if test="${not empty board.title}">
                <div class="board-card-title">${board.title}</div>
            </c:if>
            <div class="board-card-snippet">${board.content}</div>
            <div class="board-card-meta">
                <span class="category-badge">${board.category}</span>
                <span>${board.nickname}</span>
                <span>${board.createAtStr}</span>
                <span>조회 ${board.count}</span>
                <span>좋아요 ${board.likeCount}</span>
            </div>
        </a>
    </c:forEach>
</div>

<c:if test="${totalPages > 1}">
    <div class="pagination">
        <c:forEach begin="1" end="${totalPages}" var="p">
            <a class="btn ${p == page ? 'btn-primary' : 'btn-outline'}"
               href="/community/board?page=${p}${not empty category ? '&category='.concat(category) : ''}">${p}</a>
        </c:forEach>
    </div>
</c:if>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
