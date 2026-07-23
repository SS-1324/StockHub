<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<h2 class="page-title">로그인</h2>

<c:if test="${not empty error}">
    <p class="alert alert-error">${error}</p>
</c:if>

<c:if test="${not empty joinSuccess}">
    <p class="alert alert-success">회원가입이 완료되었습니다. 로그인해주세요.</p>
</c:if>

<form class="form form-flex" action="/member/login" method="post">
    <input type="hidden" name="redirectURL" value="${param.redirectURL}">

    <div class="form-row">
        <label for="member-id">아이디</label>
        <input id="member-id" name="memberId" type="text" required autofocus>
    </div>

    <div class="form-row">
        <label for="member-pwd">비밀번호</label>
        <input id="member-pwd" name="memberPwd" type="password" required>
    </div>

    <div class="form-row form-row-actions">
        <button class="btn btn-primary" type="submit">로그인</button>
        <a class="btn btn-outline" href="/member/join">회원가입</a>
    </div>
</form>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
