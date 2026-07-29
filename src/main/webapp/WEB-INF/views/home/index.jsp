<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%-- 홈 전용 CSS를 공통 헤더의 head 안에서 불러오도록 전달 --%>
<c:url var="homeCssUrl" value="/css/home.css" />
<c:set var="pageCssUrl" value="${homeCssUrl}" scope="request" />

<%-- 공통 헤더를 현재 페이지에 포함 --%>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<%-- 회원 탈퇴 완료 메시지를 표시 --%>
<c:if test="${not empty withdrawSuccess}">
    <p class="alert alert-success home-alert">회원 탈퇴가 완료되었습니다.</p>
</c:if>

<%-- 문의 등록 성공 메시지를 표시 --%>
<c:if test="${not empty inquirySuccess}">
    <p class="alert alert-success home-alert">문의가 관리자에게 전달되었습니다.</p>
</c:if>

<%-- 메인 문구와 최신 게시글 영역 --%>
<section class="home-main">
    <h1 class="home-headline">
        <span class="home-headline-main">주식, 혼자보면 숫자지만</span>
        <span class="home-headline-accent">같이보면 이야기니까.</span>
    </h1>

    <%-- 전체 게시판에서 가장 최근에 작성된 글 3개를 표시 --%>
    <div class="home-latest-list">
        <c:choose>
            <c:when test="${not empty latestBoards}">
                <c:forEach var="board" items="${latestBoards}">
                    <a class="home-latest-card"
                       href="${pageContext.request.contextPath}/community/board/${board.boardId}">
                        <div class="home-latest-category">
                            <c:out value="${board.category}"/>
                        </div>

                        <h2 class="home-latest-title">
                            <c:choose>
                                <c:when test="${not empty board.title}">
                                    <c:out value="${board.title}"/>
                                </c:when>
                                <c:otherwise>제목 없는 게시글</c:otherwise>
                            </c:choose>
                        </h2>

                        <p class="home-latest-content">
                            <c:out value="${board.content}"/>
                        </p>

                        <div class="home-latest-meta">
                            <span><c:out value="${board.nickname}"/></span>
                            <span><c:out value="${board.createAtStr}"/></span>
                        </div>
                    </a>
                </c:forEach>
            </c:when>
            <c:otherwise>
                <p class="home-empty-message">아직 등록된 게시글이 없습니다.</p>
            </c:otherwise>
        </c:choose>
    </div>
</section>

<%-- 공통 푸터를 현재 페이지에 포함 --%>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
