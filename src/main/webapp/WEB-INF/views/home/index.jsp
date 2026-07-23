<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/WEB-INF/views/common/header.jsp" />
<section class="hero">
    <h1 class="hero-title">StockHub</h1>
    <c:choose>
        <c:when test="${not empty sessionScope.loginMember}">
            <p class="hero-desc">
                ${sessionScope.loginMember.nickname}님, 로그인되었습니다.
            </p>
            <a class="btn btn-outline" href="/member/logout">로그아웃</a>
        </c:when>
        <c:otherwise>
            <p class="hero-desc">회원가입 또는 로그인으로 시작하세요.</p>
            <div class="hero-actions">
                <a class="btn btn-primary" href="/member/login">로그인</a>
                <a class="btn btn-outline" href="/member/join">회원가입</a>
            </div>
        </c:otherwise>
    </c:choose>
</section>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
