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

    <%-- 작성자의 현재 수익률 순위에 따라 기존 랭킹 CSS 클래스 선택 --%>
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

    <%-- 사진 유무별로 본문 노출 줄 수와 미리보기 영역을 일정하게 제어한다. --%>
    <a class="board-card ${not empty board.imageList ? 'has-image' : 'has-no-image'}"
       href="${communityUrl}/${board.boardId}">

        <%-- [프로필연결-1] 카드 이동과 구분하여 작성자 영역을 누르면 공통 프로필 모달을 연다. --%>
        <div class="board-card-header ${boardRankClass}"
             data-user-profile="${board.memberId}"
             role="button"
             tabindex="0"
             aria-label="작성자 프로필 보기">

            <c:choose>

                <%-- 수익률 1~3위는 기존 랭킹 프로필 프레임 사용 --%>
                <c:when test="${not empty boardRankClass}">
                    <span class="ranking-avatar-frame
                                 board-card-rank-frame
                                 ${boardRankClass}">

                        <img class="board-card-avatar"
                             src="${board.profile}"
                             alt="${board.nickname}">
                    </span>
                </c:when>

                <%-- 일반 회원은 기존 프로필 유지 --%>
                <c:otherwise>
                    <img class="board-card-avatar"
                         src="${board.profile}"
                         alt="${board.nickname}">
                </c:otherwise>

            </c:choose>

            <div class="board-card-header-text">
                <span class="board-card-nickname">
                    ${board.nickname}
                </span>

                <span class="board-card-date">
                    ${board.createAtStr}
                </span>
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

        <%-- 카테고리·조회수는 왼쪽, 사용자 액션은 오른쪽에 한 줄로 정렬한다. --%>
        <div class="board-card-footer">
            <div class="board-card-meta">
                <span class="category-badge">${allowedCategories[board.category]}</span>
                <span>조회 ${board.count}</span>
            </div>

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
        </div>
    </a>
</c:forEach>
