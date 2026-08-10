<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:url var="myInfoCssUrl" value="/css/my-stocks.css">
    <c:param name="v" value="2" />
</c:url>
<c:set var="pageCssUrl" value="${myInfoCssUrl}" scope="request" />
<c:url var="myInfoUrl" value="/member/stocks" />
<c:url var="communityUrl" value="/community" />

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<section class="my-posts-page" aria-labelledby="bookmarked-posts-title">
    <header class="my-posts-header">
        <h1 id="bookmarked-posts-title">북마크한 글</h1>
        <span><c:out value="${totalCount}"/>개</span>
    </header>

    <c:choose>
        <c:when test="${not empty boardList}">
            <div class="my-posts-list">
                <c:forEach var="board" items="${boardList}">
                    <a class="my-post-card"
                       href="${communityUrl}/${board.boardId}">
                        <div>
                            <span class="my-post-category">
                                <c:out value="${allowedCategories[board.category]}"/>
                            </span>
                            <c:if test="${not empty board.title}">
                                <h2><c:out value="${board.title}"/></h2>
                            </c:if>
                            <p class="my-post-preview">
                                <c:out value="${board.content}"/>
                            </p>
                        </div>
                        <div class="my-post-meta">
                            <span><c:out value="${board.createAtStr}"/></span>
                            <span>조회 <c:out value="${board.count}"/></span>
                            <span>좋아요 <c:out value="${board.likeCount}"/></span>
                            <span>댓글 <c:out value="${board.commentCount}"/></span>
                        </div>
                    </a>
                </c:forEach>
            </div>
        </c:when>
        <c:otherwise>
            <div class="my-posts-empty">
                <p>아직 북마크한 게시글이 없습니다.</p>
                <a class="my-posts-back" href="${communityUrl}">
                    커뮤니티 둘러보기
                </a>
            </div>
        </c:otherwise>
    </c:choose>

    <a class="my-posts-back" href="${myInfoUrl}">내 정보로 돌아가기</a>
</section>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
