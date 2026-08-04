<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%-- 공통 헤더를 현재 페이지에 포함 --%>
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<c:url var="homeUrl" value="/" />
<c:url var="joinUrl" value="/member/join" />
<c:url var="passwordResetUrl" value="/member/password-reset" />
<c:url var="logoUrl" value="/images/StockHub_logo_blue.png" />

<section class="login-panel">
    <h2 class="page-title login-title">로그인</h2>

    <%-- 로그인 실패 메시지를 표시 --%>
    <c:if test="${not empty error}">
        <p class="alert alert-error login-alert">${error}</p>
    </c:if>

    <%-- 회원가입과 비밀번호 변경 성공 메시지를 표시 --%>
    <c:if test="${not empty joinSuccess}">
        <p class="alert alert-success login-alert">회원가입이 완료되었습니다. 로그인해주세요.</p>
    </c:if>
    <c:if test="${not empty passwordResetSuccess}">
        <p class="alert alert-success login-alert">비밀번호가 변경되었습니다. 새 비밀번호로 로그인해주세요.</p>
    </c:if>

    <%-- 아이디와 비밀번호를 로그인 주소로 전송 --%>
    <form class="form login-form"
          action="${pageContext.request.contextPath}/member/login"
          method="post">
        <%-- 로그인 후 돌아갈 주소를 숨겨서 전달 --%>
        <input type="hidden" name="redirectURL" value="${param.redirectURL}">

        <%-- 아이디 입력 영역 --%>
        <div class="login-field">
            <label class="sr-only" for="member-id">아이디</label>
            <input id="member-id" name="memberId" type="text"
                   placeholder="아이디"
                   required autofocus autocomplete="username">
        </div>

        <%-- 비밀번호 입력 영역 --%>
        <div class="login-field">
            <label class="sr-only" for="member-pwd">비밀번호</label>
            <input id="member-pwd" name="memberPwd" type="password"
                   placeholder="비밀번호"
                   required autocomplete="current-password">
        </div>

        <button class="btn login-submit" type="submit">로그인</button>
    </form>

    <%-- 회원가입과 비밀번호 찾기 화면으로 이동 --%>
    <div class="login-member-links">
        <a href="${joinUrl}">회원가입</a>
        <span aria-hidden="true">/</span>
        <a href="${passwordResetUrl}">비밀번호 찾기</a>
    </div>

    <%-- 하단 로고를 누르면 메인 화면으로 이동 --%>
    <a class="login-home-logo" href="${homeUrl}" aria-label="StockHub 홈으로 이동">
        <img src="${logoUrl}" alt="StockHub">
    </a>
</section>

<%-- 공통 푸터를 현재 페이지에 포함 --%>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
