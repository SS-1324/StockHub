<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<%--
    최초 목록과 무한스크롤 응답에서 함께 사용하는 게시글 카드 조각.
    단독 렌더링될 수 있으므로 communityUrl을 이 파일에서도 선언한다.
--%>
<c:url var="communityUrl" value="/community" scope="request" />

<c:forEach var="board" items="${boardList}">
    <a class="board-card" href="${communityUrl}/${board.boardId}">
        <div class="board-card-header">
            <img class="board-card-avatar"
                 src="${board.profile}"
                 alt="${board.nickname}"
                 loading="lazy">

            <div class="board-card-header-text">
                <span class="board-card-nickname"><c:out value="${board.nickname}" /></span>
                <span class="board-card-date">${board.createAtStr}</span>
            </div>
        </div>

        <c:if test="${not empty board.title}">
            <div class="board-card-title"><c:out value="${board.title}" /></div>
        </c:if>

        <div class="board-card-snippet"><c:out value="${board.content}" /></div>

        <c:if test="${not empty board.imageList}">
            <c:choose>
                <c:when test="${fn:length(board.imageList) == 1}">
                    <div class="board-card-image-single">
                        <img src="${board.imageList[0].imgPath}"
                             alt="${board.imageList[0].originalName}"
                             loading="lazy"
                             draggable="false">
                    </div>
                </c:when>

                <c:otherwise>
                    <div class="board-card-image-strip">
                        <c:forEach var="image" items="${board.imageList}">
                            <img src="${image.imgPath}"
                                 alt="${image.originalName}"
                                 loading="lazy"
                                 draggable="false">
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
        </c:if>

        <div class="board-card-footer">
            <div class="board-card-meta">
                <span class="category-badge">${allowedCategories[board.category]}</span>
                <span class="board-card-view-count">조회 ${board.count}</span>
            </div>

            <div class="board-card-actions">
                <button type="button"
                        class="board-card-action-btn board-card-like-btn ${board.liked ? 'active' : ''}"
                        data-board-id="${board.boardId}"
                        aria-label="좋아요">
                    <svg class="board-card-action-icon" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                        <path d="M20.8 4.6a5.5 5.5 0 0 0-7.8 0L12 5.7l-1.1-1.1a5.5 5.5 0 0 0-7.8 7.8l1.1 1.1L12 21l7.8-7.5 1.1-1.1a5.5 5.5 0 0 0-.1-7.8Z"
                              stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" />
                    </svg>
                    <span>좋아요</span>
                    <span class="like-count">${board.likeCount}</span>
                </button>

                <button type="button"
                        class="board-card-action-btn board-card-bookmark-btn ${board.bookmarked ? 'active' : ''}"
                        data-board-id="${board.boardId}"
                        aria-label="북마크">
                    <svg class="board-card-action-icon" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                        <path d="M6 4.5A1.5 1.5 0 0 1 7.5 3h9A1.5 1.5 0 0 1 18 4.5V21l-6-4-6 4V4.5Z"
                              stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" />
                    </svg>
                    <span>북마크</span>
                </button>

                <span class="board-card-action-btn board-card-comment-link"
                      data-href="${communityUrl}/${board.boardId}#comment-section">
                    <svg class="board-card-action-icon" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                        <path d="M21 11.5a8.4 8.4 0 0 1-9 8.5 9.2 9.2 0 0 1-4-.9L3 21l1.7-4.5A8.5 8.5 0 1 1 21 11.5Z"
                              stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" />
                    </svg>
                    <span>댓글 ${board.commentCount}</span>
                </span>
            </div>
        </div>
    </a>
</c:forEach>