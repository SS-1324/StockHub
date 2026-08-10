<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:url var="myInfoCssUrl" value="/css/my-stocks.css">
    <c:param name="v" value="2" />
</c:url>
<c:set var="pageCssUrl" value="${myInfoCssUrl}" scope="request" />
<c:url var="myInfoUrl" value="/member/stocks" />
<c:url var="communityUrl" value="/community" />

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<%-- 본인 메뉴에서는 기존 문구를, 프로필 숫자로 들어온 경우에는 해당 회원 닉네임을 표시한다. --%>
<c:set var="postsPageTitle" value="내가 쓴 글" />
<c:if test="${not empty profileOwner and not ownProfile}">
    <c:set var="postsPageTitle" value="${profileOwner.nickname}님의 작성글" />
</c:if>

<section class="my-posts-page" aria-labelledby="my-posts-title">
    <header class="my-posts-header">
        <h1 id="my-posts-title"><c:out value="${postsPageTitle}"/></h1>
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
                <p>아직 작성한 게시글이 없습니다.</p>
                <a class="my-posts-back" href="${communityUrl}">
                    커뮤니티 둘러보기
                </a>
            </div>
        </c:otherwise>
    </c:choose>

    <c:choose>
        <c:when test="${not empty profileOwner and not ownProfile}">
            <a class="my-posts-back" href="${communityUrl}">커뮤니티로 돌아가기</a>
        </c:when>
        <c:otherwise>
            <a class="my-posts-back" href="${myInfoUrl}">내 정보로 돌아가기</a>
        </c:otherwise>
    </c:choose>
</section>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
