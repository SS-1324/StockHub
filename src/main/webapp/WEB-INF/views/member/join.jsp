<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:url var="homeUrl" value="/" />
<c:url var="loginUrl" value="/member/login" />
<c:url var="logoUrl" value="/images/StockHub_logo_blue.png" />
<c:url var="authImageUrl" value="/images/auth-image.png" />
<c:url var="defaultProfileUrl" value="/images/common_member.png" />
<c:url var="commonCssUrl" value="/css/common.css" />
<c:url var="authCssUrl" value="/css/auth.css" />

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>회원가입 | StockHub</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@100..900&display=swap"
          rel="stylesheet">
    <link rel="stylesheet" href="${commonCssUrl}?v=16">
    <link rel="stylesheet" href="${authCssUrl}?v=4">
</head>
<body class="auth-page auth-page-join">
<main class="auth-shell">
    <%-- 세 인증 화면에서 공통으로 사용하는 브랜드 이미지 영역 --%>
    <section class="auth-visual" aria-hidden="true"
             style="--auth-image: url('${authImageUrl}');">
        <img src="${authImageUrl}" alt="">
    </section>

    <section class="auth-content">
        <div class="auth-content-scroll">
            <div class="auth-content-card">
                <header class="auth-heading">
                    <h1>회원가입</h1>
                    <p>투자 정보와 경험을 함께 나눌 계정을 만들어보세요.</p>
                </header>

                <c:if test="${not empty error}">
                    <p class="alert alert-error auth-alert"><c:out value="${error}" /></p>
                </c:if>

                <%-- 회원 정보와 기존 개발용 이메일 인증 결과를 서버로 전송 --%>
                <form id="join-form" class="form auth-join-form"
                      action="${pageContext.request.contextPath}/member/join"
                      data-context-path="${pageContext.request.contextPath}"
                      method="post" enctype="multipart/form-data">

                    <div class="form-row form-row-center auth-field-profile">
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
                        <p class="form-tip">3MB 이하 JPG, PNG, WEBP 파일을 선택할 수 있습니다.</p>
                    </div>

                    <div class="form-row auth-field-id">
                        <label for="member-id">아이디</label>
                        <div class="input-with-button">
                            <input id="member-id" name="memberId"
                                   type="text" minlength="6" maxlength="50"
                                   pattern="[A-Za-z0-9]{6,50}" required autocomplete="off"
                                   placeholder="영문·숫자 6자 이상">
                            <button id="check-id-btn" class="btn btn-outline"
                                    type="button">중복확인</button>
                        </div>
                        <p id="check-id-result" class="form-tip"></p>
                        <p class="form-tip">한글과 특수문자는 사용할 수 없습니다.</p>
                    </div>

                    <div class="form-row auth-field-nickname">
                        <label for="nickname">닉네임</label>
                        <div class="input-with-button">
                            <input id="nickname" name="nickname"
                                   type="text" minlength="2" maxlength="10"
                                   pattern="[가-힣A-Za-z0-9]{2,10}" required autocomplete="off"
                                   placeholder="2자 이상">
                            <button id="check-nickname-btn" class="btn btn-outline"
                                    type="button">중복확인</button>
                        </div>
                        <p id="check-nickname-result" class="form-tip"></p>
                        <p class="form-tip">한글·영문·숫자로 2~10자까지 입력할 수 있습니다.</p>
                    </div>

                    <div class="form-row auth-field-name">
                        <label for="member-name">이름</label>
                        <input id="member-name" name="memberName"
                               type="text" maxlength="50"
                               pattern="\S{1,50}" required autocomplete="off"
                               placeholder="띄어쓰기 없이 입력">
                        <p class="form-tip">띄어쓰기 없이 입력해주세요.</p>
                    </div>

                    <div class="form-row auth-field-password">
                        <label for="member-pwd">비밀번호</label>
                        <input id="member-pwd" name="memberPwd"
                               type="password" minlength="10" maxlength="100"
                               required autocomplete="new-password"
                               placeholder="10자 이상">
                        <p id="password-rule-result" class="form-tip"></p>
                        <p class="form-tip">대·소문자, 숫자, 특수문자를 각각 포함해주세요.</p>
                    </div>

                    <div class="form-row auth-field-password-confirm">
                        <label for="member-pwd-confirm">비밀번호 확인</label>
                        <input id="member-pwd-confirm" name="memberPwdConfirm"
                               type="password" minlength="10" maxlength="100"
                               required autocomplete="new-password"
                               placeholder="비밀번호 다시 입력">
                        <p id="check-pwd-result" class="form-tip"></p>
                    </div>

                    <div class="form-row auth-field-email">
                        <label for="email-local">이메일</label>
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
                        <p class="form-tip">개발용 인증코드는 인증 버튼을 누르면 화면에 표시됩니다.</p>

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

                    <div class="form-row auth-field-submit">
                        <button class="btn btn-primary auth-primary-button" type="submit">회원가입 완료</button>
                    </div>
                </form>

                <p class="auth-switch">이미 계정이 있나요? <a href="${loginUrl}">로그인</a></p>
                <a class="auth-home-logo" href="${homeUrl}" aria-label="StockHub 홈으로 이동">
                    <img src="${logoUrl}" alt="StockHub">
                </a>
            </div>
        </div>
    </section>
</main>

<script src="${pageContext.request.contextPath}/js/member.js?v=4"></script>
</body>
</html>
