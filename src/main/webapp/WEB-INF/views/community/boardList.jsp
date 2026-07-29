<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/board.css">

<h2 class="page-title">커뮤니티 게시판</h2>
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
