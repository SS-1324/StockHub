<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<%-- 기존 금·은·동 랭킹 CSS --%>
<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/ranking.css?v=7">

<%--
    커뮤니티 전용 CSS.
    v 값은 CSS 내용을 바꾼 뒤 브라우저가 예전 파일을 재사용하지 않고 새로 받게 하는 캐시 갱신 번호다.
--%>
<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/board.css?v=18">

<c:if test="${not empty error}">
    <p class="alert alert-error">${error}</p>
</c:if>

<%--
    비로그인 방문자는 loginMemberId도 null이라, 예전엔 탈퇴 회원(member.memberId가 null)의 글에서
    "null == null"이 참이 되어 아무나 수정/삭제 버튼을 보는 버그가 있었음.
    본인 소유이거나(loginMemberId가 비어있지 않고 일치), ADMIN 권한이 있는 경우에만 노출.
--%>
<c:set var="isAdmin" value="${not empty sessionScope.loginMember and fn:toUpperCase(sessionScope.loginMember.memberRole) eq 'ADMIN'}" />
<c:if test="${(not empty loginMemberId and loginMemberId == board.memberId) or isAdmin}">
    <div class="board-actions">
        <a class="btn btn-outline" href="${communityUrl}/edit/${board.boardId}">수정</a>
        <form action="${communityUrl}/delete/${board.boardId}" method="post"
              onsubmit="return confirm('게시글을 삭제하시겠습니까?');">
            <button type="submit" class="btn btn-danger">삭제</button>
        </form>
    </div>
</c:if>

<%--
    게시글 본문과 댓글은 서로 별개의 글이 아니라 하나의 대화 흐름이다.
    두 영역을 하나의 바깥 카드로 묶어 테두리·둥근 모서리·그림자를 공유한다.
--%>
<div class="board-thread-card">

<div class="board-detail-card">

<%-- 제목은 선택 입력: 있으면 제목 -> 이미지 -> 내용, 없으면 이미지 -> 내용 순서로만 노출 --%>
<c:if test="${not empty board.title}">
    <h2 class="board-title"><c:out value="${board.title}" /></h2>
</c:if>
<%-- 상세 게시글 작성자의 수익률 순위 클래스 --%>
<c:set var="boardRankClass" value="" />

<c:choose>
    <c:when test="${board.rankPosition eq 1}">
        <c:set var="boardRankClass"
               value="rank-first" />
    </c:when>

    <c:when test="${board.rankPosition eq 2}">
        <c:set var="boardRankClass"
               value="rank-second" />
    </c:when>

    <c:when test="${board.rankPosition eq 3}">
        <c:set var="boardRankClass"
               value="rank-third" />
    </c:when>
</c:choose>
<div class="board-header-meta ${boardRankClass}">

    <c:choose>

        <%-- 수익률 1~3위 작성자 --%>
        <c:when test="${not empty boardRankClass}">
            <span class="ranking-avatar-frame
                         board-detail-rank-frame
                         ${boardRankClass}">

                <img class="board-header-avatar"
                     src="${board.profile}"
                     alt="${board.nickname}">
            </span>
        </c:when>

        <%-- 일반 작성자 --%>
        <c:otherwise>
            <img class="board-header-avatar"
                 src="${board.profile}"
                 alt="${board.nickname}">
        </c:otherwise>

    </c:choose>

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
    <%--
        하트와 책갈피는 글자 기호가 아니라 SVG를 사용한다.
        SVG는 화면 크기가 달라져도 선명하고, CSS의 currentColor를 받아
        active 상태에서 윤곽선과 내부 색을 함께 변경할 수 있다.
    --%>
    <button type="button" id="like-btn" class="btn-icon btn-like ${board.liked ? 'active' : ''}"
            data-board-id="${board.boardId}" data-active="${board.liked}"
            aria-pressed="${board.liked}" aria-label="이 게시글 좋아요" title="좋아요">
        <svg class="reaction-icon" viewBox="0 0 24 24" aria-hidden="true">
            <path d="M20.8 4.6a5.5 5.5 0 0 0-7.8 0L12 5.7l-1.1-1.1a5.5 5.5 0 0 0-7.8 7.8l1.1 1.1L12 21.2l7.8-7.7 1.1-1.1a5.5 5.5 0 0 0-.1-7.8Z"/>
        </svg>
        <span id="like-count" class="reaction-count">${board.likeCount}</span>
    </button>
    <button type="button" id="bookmark-btn" class="btn-icon btn-bookmark ${board.bookmarked ? 'active' : ''}"
            data-board-id="${board.boardId}" data-active="${board.bookmarked}"
            aria-pressed="${board.bookmarked}" aria-label="이 게시글 북마크" title="북마크">
        <svg class="reaction-icon" viewBox="0 0 24 24" aria-hidden="true">
            <path d="M6 3.5h12v17l-6-4-6 4v-17Z"/>
        </svg>
    </button>
    <span class="meta-count">조회 ${board.count}</span>
    <span class="meta-count">댓글 ${fn:length(comments)}</span>
    <span class="category-badge">${allowedCategories[board.category]}</span>
</div>

<%--
    이전글·다음글은 같은 카테고리 안에서만 이동한다.
    이동할 글이 없는 방향도 숨기지 않고 비활성 안내를 보여줘서
    사용자가 기능 누락이 아니라 목록의 처음/끝이라는 사실을 알 수 있게 한다.
--%>
<div class="board-adjacent-nav" aria-label="이전글 및 다음글">
    <c:choose>
        <c:when test="${not empty board.prevBoardId}">
            <a class="board-adjacent-link" href="${communityUrl}/${board.prevBoardId}">
                <span class="board-adjacent-label">이전글</span>
                <span class="board-adjacent-title"><c:out value="${board.prevTitle}" /></span>
            </a>
        </c:when>
        <c:otherwise>
            <span class="board-adjacent-link is-disabled" aria-disabled="true">
                <span class="board-adjacent-label">이전글</span>
                <span class="board-adjacent-title">이전글이 없습니다</span>
            </span>
        </c:otherwise>
    </c:choose>

    <c:choose>
        <c:when test="${not empty board.nextBoardId}">
            <a class="board-adjacent-link" href="${communityUrl}/${board.nextBoardId}">
                <span class="board-adjacent-label">다음글</span>
                <span class="board-adjacent-title"><c:out value="${board.nextTitle}" /></span>
            </a>
        </c:when>
        <c:otherwise>
            <span class="board-adjacent-link is-disabled" aria-disabled="true">
                <span class="board-adjacent-label">다음글</span>
                <span class="board-adjacent-title">다음글이 없습니다</span>
            </span>
        </c:otherwise>
    </c:choose>
</div>

</div>

<div class="comment-section" id="comment-section">
    <h3 class="comment-section-title">댓글 ${fn:length(comments)}</h3>

    <c:choose>
        <c:when test="${not empty loginMemberId}">
            <%--
                댓글 입력은 자주 쓰는 핵심 기능이므로 접기/펼치기 버튼 없이 항상 보여준다.
                textarea를 별도 래퍼로 감싼 이유는 board.js가 만드는 글자 수(0/1500)를
                입력창 오른쪽 아래에 안정적으로 배치하기 위해서다.
            --%>
            <form id="comment-form" class="comment-form" data-board-id="${board.boardId}">
                <div class="comment-input-wrap">
                    <textarea id="comment-input" aria-label="댓글 내용"
                              placeholder="댓글을 입력하세요" maxlength="1500" required></textarea>
                </div>
                <button type="submit" class="btn btn-primary comment-submit-btn">등록</button>
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
            <div id="comment-${c.commentId}"
                 class="comment-item ${not empty c.parentCommentId ? 'comment-item-reply' : ''}"
                 data-comment-id="${c.commentId}">
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
                <c:if test="${isAdmin}">
                    <form class="comment-edit-form hidden"
                          data-comment-id="${c.commentId}"
                          data-board-id="${board.boardId}">
                        <textarea maxlength="1500" required><c:out value="${c.content}"/></textarea>
                        <div class="comment-edit-actions">
                            <button type="submit" class="btn btn-primary">수정 완료</button>
                            <button type="button" class="btn btn-outline comment-edit-cancel">취소</button>
                        </div>
                    </form>
                </c:if>
                <div class="comment-meta">
                    <span>${c.createAtStr}</span>
                    <button type="button" class="comment-like-btn ${c.liked ? 'active' : ''}"
                            data-comment-id="${c.commentId}" data-active="${c.liked}">
                        좋아요 <span class="like-count">${c.likeCount}</span>
                    </button>
                    <c:if test="${empty c.parentCommentId && not empty loginMemberId}">
                        <button type="button" class="comment-reply-btn" data-comment-id="${c.commentId}">답글</button>
                    </c:if>
                    <c:if test="${isAdmin}">
                        <button type="button" class="comment-edit-btn" data-comment-id="${c.commentId}">수정</button>
                    </c:if>
                    <c:if test="${(not empty loginMemberId and c.memberId == loginMemberId) or isAdmin}">
                        <button type="button" class="comment-delete-btn" data-comment-id="${c.commentId}">삭제</button>
                    </c:if>
                </div>
                <c:if test="${empty c.parentCommentId && not empty loginMemberId}">
                    <%--
                        대댓글도 일반 댓글과 같은 comment-input-wrap과 comment-submit-btn을 사용한다.
                        같은 클래스를 재사용하면 두 입력창의 높이·테두리·글자 수 위치가 함께 유지되어
                        나중에 디자인을 바꿀 때 CSS 한 곳만 수정하면 된다.
                    --%>
                    <form class="reply-form hidden" data-parent-id="${c.commentId}" data-board-id="${board.boardId}">
                        <div class="comment-input-wrap">
                            <textarea aria-label="답글 내용" placeholder="답글을 입력하세요"
                                      maxlength="1500" required></textarea>
                        </div>
                        <button type="submit" class="btn btn-primary comment-submit-btn">등록</button>
                    </form>
                </c:if>
            </div>
        </c:forEach>
    </div>
</div>

</div>

<script src="/js/board.js"></script>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
