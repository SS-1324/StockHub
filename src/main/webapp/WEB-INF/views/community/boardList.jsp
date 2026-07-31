<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/board.css">

<h2 class="page-title">커뮤니티 게시판</h2>
<c:choose>
    <c:when test="${empty loginMemberId}">
        <a class="btn-pill" href="${communityUrl}/write">로그인하고 글쓰기</a>
    </c:when>
    <c:otherwise>
        <a class="btn-pill" href="${communityUrl}/write">글쓰기</a>
    </c:otherwise>
</c:choose>

<div class="category-filter">
    <a class="category-tab ${empty category ? 'category-tab-active' : ''}" href="${communityUrl}">전체</a>
    <c:forEach var="entry" items="${allowedCategories}">
        <a class="category-tab ${category == entry.key ? 'category-tab-active' : ''}"
           href="${communityUrl}?category=${entry.key}">${entry.value}</a>
    </c:forEach>
</div>

<c:if test="${empty boardList}">
    <p class="form-tip">등록된 게시글이 없습니다.</p>
</c:if>

<div class="board-list" id="board-list">
    <jsp:include page="/WEB-INF/views/community/boardCards.jsp" />
</div>

<%-- 이 요소가 화면에 보이면 board.js가 다음 페이지를 불러와 위 목록 끝에 이어붙인다(무한스크롤) --%>
<div id="feed-sentinel" data-next-page="2" data-category="${category}"></div>

<script src="/js/board.js"></script>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
