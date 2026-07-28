<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/board.css">

<c:if test="${not empty error}">
    <p class="alert alert-error">${error}</p>
</c:if>

<c:if test="${loginMemberId == board.memberId}">
    <div class="board-actions">
        <a class="btn btn-outline" href="/community/board/edit/${board.boardId}">수정</a>
        <form action="/community/board/delete/${board.boardId}" method="post"
              onsubmit="return confirm('게시글을 삭제하시겠습니까?');">
            <button type="submit" class="btn btn-danger">삭제</button>
        </form>
    </div>
</c:if>

<%-- 제목은 선택 입력: 있으면 제목 -> 이미지 -> 내용, 없으면 이미지 -> 내용 순서로만 노출 --%>
<c:if test="${not empty board.title}">
    <h2 class="board-title">${board.title}</h2>
</c:if>

<div class="board-header-meta">
    <span class="category-badge">${board.category}</span>
    <span>${board.nickname}</span>
    <span>${board.createAtStr}</span>
</div>

<c:if test="${not empty images}">
    <div class="board-images">
        <c:forEach var="image" items="${images}">
            <img class="board-image" src="${image.imgPath}" alt="${image.originalName}">
        </c:forEach>
    </div>
</c:if>

<div class="board-content">${board.highlightedContent}</div>

<div class="board-meta-bottom">
    <button type="button" id="like-btn" class="btn-icon ${board.liked ? 'active' : ''}"
            data-board-id="${board.boardId}" data-active="${board.liked}">
        좋아요 <span id="like-count">${board.likeCount}</span>
    </button>
    <button type="button" id="bookmark-btn" class="btn-icon ${board.bookmarked ? 'active' : ''}"
            data-board-id="${board.boardId}" data-active="${board.bookmarked}">
        북마크
    </button>
    <span class="meta-count">조회 ${board.count}</span>
    <span class="meta-count">댓글 ${fn:length(comments)}</span>
</div>

<div class="comment-section">
    <h3 class="comment-section-title">댓글 ${fn:length(comments)}</h3>

    <c:choose>
        <c:when test="${not empty loginMemberId}">
            <form id="comment-form" class="comment-form" data-board-id="${board.boardId}">
                <textarea id="comment-input" placeholder="댓글을 입력하세요" required></textarea>
                <button type="submit" class="btn btn-primary">등록</button>
            </form>
        </c:when>
        <c:otherwise>
            <p class="form-tip"><a href="/member/login">로그인</a> 후 댓글을 작성할 수 있습니다.</p>
        </c:otherwise>
    </c:choose>

    <div id="comment-list" class="comment-list" data-board-id="${board.boardId}">
        <c:forEach var="c" items="${comments}">
            <%--
                답글(parentCommentId 있음)에는 들여쓰기를 추가하지 않고 @멘션만 표시한다.
                댓글 목록은 이미 부모 댓글 바로 뒤에 그 답글이 오도록 정렬되어 내려온다.
            --%>
            <div class="comment-item" data-comment-id="${c.commentId}">
                <%-- 닉네임 클릭으로 프로필 열람하는 기능은 아직 미구현 - data-member-id만 심어두고 링크는 걸지 않음 --%>
                <div class="comment-author" data-member-id="${c.memberId}">
                    <c:choose>
                        <c:when test="${not empty c.profile}">
                            <img class="comment-avatar" src="${c.profile}" alt="${c.nickname}">
                        </c:when>
                        <c:otherwise>
                            <span class="comment-avatar-placeholder">${fn:substring(c.nickname, 0, 1)}</span>
                        </c:otherwise>
                    </c:choose>
                    <span class="comment-nickname">${c.nickname}</span>
                </div>
                <div class="comment-body">
                    <c:if test="${not empty c.parentNickname}">
                        <span class="comment-mention">@${c.parentNickname}</span>
                    </c:if>
                    ${c.highlightedContent}
                </div>
                <div class="comment-meta">
                    <span>${c.createAtStr}</span>
                    <button type="button" class="comment-like-btn ${c.liked ? 'active' : ''}"
                            data-comment-id="${c.commentId}" data-active="${c.liked}">
                        좋아요 <span class="like-count">${c.likeCount}</span>
                    </button>
                    <c:if test="${empty c.parentCommentId && not empty loginMemberId}">
                        <button type="button" class="comment-reply-btn" data-comment-id="${c.commentId}">답글</button>
                    </c:if>
                    <c:if test="${c.memberId == loginMemberId}">
                        <button type="button" class="comment-delete-btn" data-comment-id="${c.commentId}">삭제</button>
                    </c:if>
                </div>
                <c:if test="${empty c.parentCommentId && not empty loginMemberId}">
                    <form class="reply-form hidden" data-parent-id="${c.commentId}" data-board-id="${board.boardId}">
                        <textarea placeholder="답글을 입력하세요" required></textarea>
                        <button type="submit" class="btn btn-outline">답글 등록</button>
                    </form>
                </c:if>
            </div>
        </c:forEach>
    </div>
</div>

<script src="/js/board.js"></script>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
