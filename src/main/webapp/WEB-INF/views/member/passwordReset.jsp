<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%-- 공통 헤더를 현재 페이지에 포함 --%>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<h2 class="page-title">비밀번호 찾기</h2>

<%-- 비밀번호 찾기 처리 실패 메시지를 표시 --%>
<c:if test="${not empty error}">
    <p class="alert alert-error password-reset-alert">${error}</p>
</c:if>

<%-- 이메일 인증 후 새 비밀번호를 서버로 전송 --%>
<form id="password-reset-form" class="form form-flex"
      action="${pageContext.request.contextPath}/member/password-reset"
      data-context-path="${pageContext.request.contextPath}"
      method="post">

    <%-- 회원가입과 동일한 형식으로 이메일을 입력 --%>
    <div class="form-row">
        <label for="reset-email-local">이메일</label>

        <div class="email-auth-row">
            <div class="email-input-group">
                <input id="reset-email-local"
                       type="text" minlength="1" maxlength="50"
                       pattern="(?=.*[a-z])[a-z0-9]{1,50}"
                       placeholder="이메일 아이디"
                       required autocomplete="off">
                <span class="email-at" aria-hidden="true">@</span>
                <input id="reset-email-domain"
                       type="text" maxlength="50"
                       pattern="[A-Za-z]+(\.com|\.co\.kr|\.net)"
                       placeholder="직접 입력"
                       required autocomplete="off">
                <select id="reset-email-domain-select" class="email-domain-select"
                        aria-label="이메일 도메인 선택">
                    <option value="">직접 입력</option>
                    <option value="naver.com">naver.com</option>
                    <option value="gmail.com">gmail.com</option>
                    <option value="kakao.com">kakao.com</option>
                    <option value="daum.net">daum.net</option>
                    <option value="nate.com">nate.com</option>
                </select>
            </div>
            <button id="send-reset-code-btn" class="btn btn-outline"
                    type="button">인증</button>
        </div>

        <p id="reset-email-result" class="form-tip"></p>

        <%-- 인증 코드가 생성된 뒤에만 표시 --%>
        <div id="reset-code-area" class="email-code-area" hidden>
            <div class="input-with-button">
                <input id="reset-email-code" type="text"
                       inputmode="numeric" pattern="[0-9]{6}" maxlength="6"
                       placeholder="인증코드 6자리" autocomplete="one-time-code">
                <button id="verify-reset-code-btn" class="btn btn-outline"
                        type="button">확인</button>
            </div>
            <p id="reset-code-result" class="form-tip"></p>
        </div>
    </div>

    <%-- 인증 성공 후 서버가 발급한 일회성 토큰을 전송 --%>
    <input id="reset-token" name="resetToken" type="hidden">

    <%-- 이메일 인증이 완료된 뒤에만 새 비밀번호 입력 영역을 표시 --%>
    <div id="reset-password-area" class="password-reset-fields" hidden>
        <div class="form-row">
            <label for="new-password">새 비밀번호</label>
            <input id="new-password" name="newPassword"
                   type="password" minlength="10" maxlength="100"
                   required autocomplete="new-password">
            <p id="reset-password-rule-result" class="form-tip"></p>
            <p class="form-tip">
                한글 없이 대문자·소문자·숫자·특수문자를 각각 1자 이상 포함하여 10자 이상 입력해주세요.
            </p>
        </div>

        <div class="form-row">
            <label for="new-password-confirm">새 비밀번호 확인</label>
            <input id="new-password-confirm" name="newPasswordConfirm"
                   type="password" minlength="10" maxlength="100"
                   required autocomplete="new-password">
            <p id="reset-password-confirm-result" class="form-tip"></p>
        </div>

        <div class="form-row">
            <button class="btn btn-primary" type="submit">비밀번호 변경</button>
        </div>
    </div>
</form>

<%-- 이메일 인증과 비밀번호 입력 검사를 불러옴 --%>
<script src="${pageContext.request.contextPath}/js/password-reset.js?v=1"></script>

<%-- 공통 푸터를 현재 페이지에 포함 --%>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
