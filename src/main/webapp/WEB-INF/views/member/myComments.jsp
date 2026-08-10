<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:url var="myInfoCssUrl" value="/css/my-stocks.css">
    <c:param name="v" value="3" />
</c:url>
<c:set var="pageCssUrl" value="${myInfoCssUrl}" scope="request" />
<c:url var="myInfoUrl" value="/member/stocks" />
<c:url var="communityUrl" value="/community" />

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<section class="my-posts-page" aria-labelledby="my-comments-title">
    <header class="my-posts-header">
        <h1 id="my-comments-title">내가 쓴 댓글</h1>
        <span><c:out value="${totalCount}"/>개</span>
    </header>

    <c:choose>
        <c:when test="${not empty commentList}">
            <div class="my-posts-list">
                <c:forEach var="comment" items="${commentList}">
                    <a class="my-post-card"
                       href="${communityUrl}/${comment.boardId}#comment-${comment.commentId}">
                        <div>
                            <span class="my-post-category">
                                ${empty comment.parentCommentId ? '댓글' : '답글'}
                            </span>
                            <h2>
                                <c:choose>
                                    <c:when test="${not empty comment.boardTitle}">
                                        <c:out value="${comment.boardTitle}"/>
                                    </c:when>
                                    <c:otherwise>게시글 #<c:out value="${comment.boardId}"/></c:otherwise>
                                </c:choose>
                            </h2>
                            <p class="my-post-preview">
                                <c:out value="${comment.content}"/>
                            </p>
                        </div>
                        <div class="my-post-meta">
                            <span><c:out value="${comment.createAtStr}"/></span>
                            <span>좋아요 <c:out value="${comment.likeCount}"/></span>
                            <span>게시글 보기 →</span>
                        </div>
                    </a>
                </c:forEach>
            </div>
        </c:when>
        <c:otherwise>
            <div class="my-posts-empty">
                <p>아직 작성한 댓글이 없습니다.</p>
                <a class="my-posts-back" href="${communityUrl}">
                    커뮤니티 둘러보기
                </a>
            </div>
        </c:otherwise>
    </c:choose>

    <a class="my-posts-back" href="${myInfoUrl}">내 정보로 돌아가기</a>
</section>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
