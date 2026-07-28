<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%-- 공통 헤더를 현재 페이지에 포함 --%>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<%-- 회원 탈퇴 완료 메시지를 표시 --%>
<c:if test="${not empty withdrawSuccess}">
    <p class="alert alert-success home-alert">회원 탈퇴가 완료되었습니다.</p>
</c:if>

<%-- 메인 화면의 안내와 회원 버튼 영역 --%>
<section class="hero">
    <h1 class="hero-title">StockHub</h1>
    <%-- 로그인 여부에 따라 안내 문구와 버튼을 변경 --%>
    <c:choose>
        <c:when test="${not empty sessionScope.loginMember}">
            <p class="hero-desc">
                    ${sessionScope.loginMember.nickname}님, 로그인되었습니다.
            </p>
            <a class="btn btn-outline"
               href="${pageContext.request.contextPath}/member/logout">로그아웃</a>
        </c:when>
        <c:otherwise>
            <p class="hero-desc">회원가입 또는 로그인으로 시작하세요.</p>
            <div class="hero-actions">
                <a class="btn btn-primary"
                   href="${pageContext.request.contextPath}/member/login">로그인</a>
                <a class="btn btn-outline"
                   href="${pageContext.request.contextPath}/member/join">회원가입</a>
            </div>
        </c:otherwise>
    </c:choose>
</section>

<%-- 공통 푸터를 현재 페이지에 포함 --%>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />