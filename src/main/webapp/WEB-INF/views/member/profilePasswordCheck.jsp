<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%-- 공통 헤더를 현재 페이지에 포함 --%>
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<c:url var="passwordResetUrl" value="/member/password-reset" />

<h2 class="page-title">비밀번호 확인</h2>

<c:if test="${not empty error}">
    <p class="alert alert-error password-reset-alert">${error}</p>
</c:if>

<form class="form form-flex"
      action="${pageContext.request.contextPath}/member/mypage/password-check"
      method="post">
    <div class="form-row">
        <label for="current-password">현재 비밀번호</label>
        <input id="current-password" name="currentPassword"
               type="password" maxlength="100"
               required autofocus autocomplete="current-password">
    </div>

    <div class="form-row">
        <button class="btn btn-primary" type="submit">확인</button>
    </div>

    <div class="login-member-links">
        <a href="${passwordResetUrl}">비밀번호 찾기</a>
    </div>
</form>

<%-- 공통 푸터를 현재 페이지에 포함 --%>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
