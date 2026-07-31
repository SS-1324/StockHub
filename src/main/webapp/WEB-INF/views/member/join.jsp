<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%-- 공통 헤더를 현재 페이지에 포함 --%>
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<c:url var="defaultProfileUrl" value="/images/common_member.png" />

<h2 class="page-title">회원가입</h2>

<%-- 회원가입 실패 메시지를 표시 --%>
<c:if test="${not empty error}">
    <p class="alert alert-error">${error}</p>
</c:if>

<%-- 회원 정보와 이미지를 서버로 전송 --%>
<form id="join-form" class="form form-flex"
      action="${pageContext.request.contextPath}/member/join"
      data-context-path="${pageContext.request.contextPath}"
      method="post" enctype="multipart/form-data">

    <%-- 프로필 이미지 선택과 미리보기 영역 --%>
    <div class="form-row form-row-center">
        <div class="profile-preview-wrap">
            <div id="profile-preview-placeholder"
                 class="profile-preview profile-preview-placeholder"
                 style="display:none;">사진없음</div>
            <img id="profile-preview" class="profile-preview"
                 src="${defaultProfileUrl}"
                 alt="기본 프로필 이미지">
        </div>

        <label class="file-label">
            프로필 이미지 선택
            <input id="profile-image" name="profileImage"
                   type="file"
                   accept=".jpg,.jpeg,.png,.webp,image/jpeg,image/png,image/webp">
        </label>
        <p class="form-tip">JPG, PNG, WEBP 파일만 선택할 수 있습니다. GIF 파일은 업로드할 수 없습니다.</p>
    </div>

    <%-- 아이디 입력과 중복확인 영역 --%>
    <div class="form-row">
        <label for="member-id">아이디</label>
        <div class="input-with-button">
            <input id="member-id" name="memberId"
                   type="text" minlength="6" maxlength="50"
                   pattern="[A-Za-z0-9]{6,50}" required autocomplete="off">
            <button id="check-id-btn" class="btn btn-outline"
                    type="button">중복확인</button>
        </div>
        <p id="check-id-result" class="form-tip"></p>
        <p class="form-tip">영문·숫자로 6자 이상 입력해주세요. 한글과 특수문자는 사용할 수 없습니다.</p>
    </div>

    <%-- 비밀번호 입력 영역 --%>
    <div class="form-row">
        <label for="member-pwd">비밀번호</label>
        <input id="member-pwd" name="memberPwd"
               type="password" minlength="10" maxlength="100"
               required autocomplete="new-password">
        <p id="password-rule-result" class="form-tip"></p>
        <p class="form-tip">
            한글 없이 대문자·소문자·숫자·특수문자를 각각 1자 이상 포함하여 10자 이상 입력해주세요.
        </p>
    </div>

    <%-- 비밀번호 일치 확인 영역 --%>
    <div class="form-row">
        <label for="member-pwd-confirm">비밀번호 확인</label>
        <input id="member-pwd-confirm" name="memberPwdConfirm"
               type="password" minlength="10" maxlength="100"
               required autocomplete="new-password">
        <p id="check-pwd-result" class="form-tip"></p>
    </div>

    <%-- 회원 이름 입력 영역 --%>
    <div class="form-row">
        <label for="member-name">이름</label>
        <input id="member-name" name="memberName"
               type="text" maxlength="50"
               pattern="\S{1,50}" required autocomplete="off">
        <p class="form-tip">띄어쓰기 없이 입력해주세요.</p>
    </div>

    <%-- 닉네임 입력과 중복확인 영역 --%>
    <div class="form-row">
        <label for="nickname">닉네임</label>
        <div class="input-with-button">
            <input id="nickname" name="nickname"
                   type="text" minlength="2" maxlength="10"
                   pattern="[가-힣A-Za-z0-9]{2,10}" required autocomplete="off">
            <button id="check-nickname-btn" class="btn btn-outline"
                    type="button">중복확인</button>
        </div>
        <p id="check-nickname-result" class="form-tip"></p>
        <p class="form-tip">한글·영문·숫자로 2자 이상 10자 이하로 입력해주세요. 특수문자는 사용할 수 없습니다.</p>
    </div>

    <%-- 이메일 입력 영역 --%>
    <div class="form-row">
        <label for="email-local">이메일</label>

        <%-- 화면에서 나눈 이메일 값을 전송 직전에 하나로 합쳐 담음 --%>
        <input id="email" name="email" type="hidden">

        <div class="email-auth-row">
            <div class="email-input-group">
                <input id="email-local"
                       type="text" minlength="1" maxlength="50"
                       pattern="(?=.*[a-z])[a-z0-9]{1,50}"
                       placeholder="이메일 아이디"
                       required autocomplete="off">
                <span class="email-at" aria-hidden="true">@</span>
                <input id="email-domain"
                       type="text" maxlength="50"
                       pattern="[A-Za-z]+(\.com|\.co\.kr|\.net)"
                       placeholder="직접 입력"
                       required autocomplete="off">
                <select id="email-domain-select" class="email-domain-select"
                        aria-label="이메일 도메인 선택">
                    <option value="">직접 입력</option>
                    <option value="naver.com">naver.com</option>
                    <option value="gmail.com">gmail.com</option>
                    <option value="kakao.com">kakao.com</option>
                    <option value="daum.net">daum.net</option>
                    <option value="nate.com">nate.com</option>
                </select>
            </div>
            <button id="send-email-code-btn" class="btn btn-outline"
                    type="button">인증</button>
        </div>

        <p id="email-result" class="form-tip"></p>

        <%-- 개발용 인증 코드 생성 후에만 입력 영역을 표시 --%>
        <div id="email-code-area" class="email-code-area" hidden>
            <div class="input-with-button">
                <input id="email-code" type="text"
                       inputmode="numeric" pattern="[0-9]{6}" maxlength="6"
                       placeholder="인증코드 6자리" autocomplete="one-time-code">
                <button id="verify-email-code-btn" class="btn btn-outline"
                        type="button">확인</button>
            </div>
            <p id="email-code-result" class="form-tip"></p>
        </div>
    </div>

    <div class="form-row">
        <button class="btn btn-primary" type="submit">가입하기</button>
    </div>
</form>

<%-- 회원가입 화면의 검사와 미리보기 기능을 불러옴 --%>
<script src="${pageContext.request.contextPath}/js/member.js"></script>

<%-- 공통 푸터를 현재 페이지에 포함 --%>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
