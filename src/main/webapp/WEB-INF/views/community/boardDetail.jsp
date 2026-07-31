<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/board.css">

<c:if test="${not empty error}">
    <p class="alert alert-error">${error}</p>
</c:if>

<%--
    비로그인 방문자는 loginMemberId도 null이라, 예전엔 탈퇴 회원(member.memberId가 null)의 글에서
    "null == null"이 참이 되어 아무나 수정/삭제 버튼을 보는 버그가 있었음.
    본인 소유이거나(loginMemberId가 비어있지 않고 일치), 관리자가 주인 없는 글을 처리하는 경우에만 노출.
--%>
<c:set var="isAdmin" value="${not empty sessionScope.loginMember and fn:toUpperCase(sessionScope.loginMember.memberRole) eq 'ADMIN'}" />
<c:if test="${(not empty loginMemberId and loginMemberId == board.memberId) or (isAdmin and empty board.memberId)}">
    <div class="board-actions">
        <a class="btn btn-outline" href="${communityUrl}/edit/${board.boardId}">수정</a>
        <form action="${communityUrl}/delete/${board.boardId}" method="post"
              onsubmit="return confirm('게시글을 삭제하시겠습니까?');">
            <button type="submit" class="btn btn-danger">삭제</button>
        </form>
    </div>
</c:if>

<%-- 제목은 선택 입력: 있으면 제목 -> 이미지 -> 내용, 없으면 이미지 -> 내용 순서로만 노출 --%>
<c:if test="${not empty board.title}">
    <h2 class="board-title"><c:out value="${board.title}" /></h2>
</c:if>

<div class="board-header-meta">
    <img class="board-header-avatar" src="${board.profile}" alt="${board.nickname}">
    <span>${board.nickname}</span>
    <span>${board.createAtStr}</span>
</div>

<div class="board-content">${board.highlightedContent}</div>

<%-- 인스타그램식(글 위에 이미지)이 아니라 커뮤니티 성격에 맞게 본문 아래에 이미지 배치.
     이미지가 1장뿐이면 고정 높이 필름스트립 대신 폭을 꽉 채워 크게 보여준다. --%>
<c:if test="${not empty images}">
    <c:choose>
        <c:when test="${fn:length(images) == 1}">
            <div class="board-image-single">
                <img src="${images[0].imgPath}" alt="${images[0].originalName}" draggable="false">
            </div>
        </c:when>
        <c:otherwise>
            <div class="board-image-strip">
                <c:forEach var="image" items="${images}">
                    <img class="board-image" src="${image.imgPath}" alt="${image.originalName}" draggable="false">
                </c:forEach>
            </div>
        </c:otherwise>
    </c:choose>
</c:if>

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
    <span class="category-badge">${allowedCategories[board.category]}</span>
</div>

<div class="comment-section" id="comment-section">
    <h3 class="comment-section-title">댓글 ${fn:length(comments)}</h3>

    <c:choose>
        <c:when test="${not empty loginMemberId}">
            <button type="button" id="comment-form-toggle" class="comment-form-toggle">댓글 달기</button>
            <form id="comment-form" class="comment-form hidden" data-board-id="${board.boardId}">
                <textarea id="comment-input" placeholder="댓글을 입력하세요" maxlength="1500" required></textarea>
                <button type="submit" class="btn btn-primary">등록</button>
            </form>
        </c:when>
        <c:otherwise>
            <p class="form-tip"><a href="/member/login">로그인</a> 후 댓글을 작성할 수 있습니다.</p>
        </c:otherwise>
    </c:choose>

    <div id="comment-list" class="comment-list" data-board-id="${board.boardId}">
        <c:forEach var="c" items="${comments}">
            <%-- 답글(parentCommentId 있음)은 comment-item-reply 클래스로 우측으로 들여써서 부모 댓글에 속한다는 걸 시각적으로 드러낸다.
                 댓글 목록은 이미 부모 댓글 바로 뒤에 그 답글이 오도록 정렬되어 내려온다. --%>
            <div class="comment-item ${not empty c.parentCommentId ? 'comment-item-reply' : ''}" data-comment-id="${c.commentId}">
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
                <%-- white-space: pre-wrap이 걸려있어서, 아래 태그 사이에 줄바꿈/들여쓰기를 남기면
                     그 공백까지 그대로 화면에 찍힌다(댓글 앞에 빈 칸이 생기던 원인). 한 줄로 붙여서 방지. --%>
                <div class="comment-body"><c:if test="${not empty c.parentNickname}"><span class="comment-mention">@${c.parentNickname}</span></c:if>${c.highlightedContent}</div>
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
                        <textarea placeholder="답글을 입력하세요" maxlength="1500" required></textarea>
                        <button type="submit" class="btn btn-outline">답글 등록</button>
                    </form>
                </c:if>
            </div>
        </c:forEach>
    </div>
</div>

<script src="/js/board.js"></script>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
