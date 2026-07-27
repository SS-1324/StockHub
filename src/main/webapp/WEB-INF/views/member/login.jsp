<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%-- 공통 헤더를 현재 페이지에 포함 --%>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<h2 class="page-title">로그인</h2>

<%-- 로그인 실패 메시지를 표시 --%>
<c:if test="${not empty error}">
    <p class="alert alert-error login-alert">${error}</p>
</c:if>

<%-- 회원가입 성공 메시지를 표시 --%>
<c:if test="${not empty joinSuccess}">
    <p class="alert alert-success login-alert">회원가입이 완료되었습니다. 로그인해주세요.</p>
</c:if>

<%-- 아이디와 비밀번호를 로그인 주소로 전송 --%>
<form class="form form-flex login-form"
      action="${pageContext.request.contextPath}/member/login"
      method="post">
    <%-- 로그인 후 돌아갈 주소를 숨겨서 전달 --%>
    <input type="hidden" name="redirectURL" value="${param.redirectURL}">

    <%-- 아이디 입력 영역 --%>
    <div class="form-row">
        <label for="member-id">아이디</label>
        <input id="member-id" name="memberId" type="text"
               required autofocus autocomplete="username">
    </div>

    <%-- 비밀번호 입력 영역 --%>
    <div class="form-row">
        <label for="member-pwd">비밀번호</label>
        <input id="member-pwd" name="memberPwd" type="password"
               required autocomplete="current-password">
    </div>

    <%-- 로그인과 회원가입 이동 버튼 --%>
    <div class="form-row form-row-actions">
        <button class="btn btn-primary" type="submit">로그인</button>
        <a class="btn btn-outline"
           href="${pageContext.request.contextPath}/member/join">회원가입</a>
    </div>
</form>

<%-- 공통 푸터를 현재 페이지에 포함 --%>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
