<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<%--
    게시글 카드 목록 조각(fragment). boardList.jsp가 최초 화면에 include하는 용도로도,
    /community/feed가 무한스크롤 다음 페이지로 이 조각만 그대로 응답하는 용도로도 재사용된다.
    header.jsp를 거치지 않고 이 파일 단독으로 렌더링될 수도 있으므로(무한스크롤 응답),
    communityUrl을 header.jsp에만 기대지 않고 여기서도 직접 채워둔다(같은 값이라 중복 설정해도 안전).
--%>
<c:url var="communityUrl" value="/community" scope="request" />

<c:forEach var="board" items="${boardList}">
    <a class="board-card" href="${communityUrl}/${board.boardId}">
        <div class="board-card-header">
            <img class="board-card-avatar" src="${board.profile}" alt="${board.nickname}">
            <div class="board-card-header-text">
                <span class="board-card-nickname">${board.nickname}</span>
                <span class="board-card-date">${board.createAtStr}</span>
            </div>
        </div>

        <c:if test="${not empty board.title}">
            <div class="board-card-title"><c:out value="${board.title}" /></div>
        </c:if>

        <div class="board-card-snippet"><c:out value="${board.content}" /></div>

        <%-- 인스타그램식(글 위에 이미지)이 아니라 본문 아래에 이미지를 두는 커뮤니티식 배치.
             이미지가 1장뿐이면 필름스트립이 아니라 카드 폭을 꽉 채워 더 크게 보여준다(짤방 위주라 크게 보는 게 낫다는 판단). --%>
        <c:if test="${not empty board.imageList}">
            <c:choose>
                <c:when test="${fn:length(board.imageList) == 1}">
                    <div class="board-card-image-single">
                        <img src="${board.imageList[0].imgPath}" alt="${board.imageList[0].originalName}" loading="lazy" draggable="false">
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="board-card-image-strip">
                        <c:forEach var="image" items="${board.imageList}">
                            <img src="${image.imgPath}" alt="${image.originalName}" loading="lazy" draggable="false">
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
        </c:if>

        <div class="board-card-actions">
            <button type="button" class="board-card-action-btn board-card-like-btn ${board.liked ? 'active' : ''}"
                    data-board-id="${board.boardId}">
                좋아요 <span class="like-count">${board.likeCount}</span>
            </button>
            <button type="button" class="board-card-action-btn board-card-bookmark-btn ${board.bookmarked ? 'active' : ''}"
                    data-board-id="${board.boardId}">
                북마크
            </button>
            <span class="board-card-action-btn board-card-comment-link"
                  data-href="${communityUrl}/${board.boardId}#comment-section">
                댓글 ${board.commentCount}
            </span>
        </div>

        <div class="board-card-meta">
            <span class="category-badge">${allowedCategories[board.category]}</span>
            <span>조회 ${board.count}</span>
        </div>
    </a>
</c:forEach>
