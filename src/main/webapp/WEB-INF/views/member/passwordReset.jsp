<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:url var="homeUrl" value="/" />
<c:url var="loginUrl" value="/member/login" />
<c:url var="logoUrl" value="/images/StockHub_logo_blue.png" />
<c:url var="authImageUrl" value="/images/auth-image.png" />
<c:url var="authImageDarkUrl" value="/images/auth-image-dark.png" />
<c:url var="commonCssUrl" value="/css/common.css" />
<c:url var="authCssUrl" value="/css/auth.css" />
<c:url var="headerJsUrl" value="/js/header.js" />

<!DOCTYPE html>
<html lang="ko">
<head>
    <script>
        (() => {
            try {
                const savedTheme = localStorage.getItem("stockhub-theme");
                document.documentElement.dataset.theme =
                    savedTheme === "dark" ? "dark" : "light";
            } catch {
                document.documentElement.dataset.theme = "light";
            }
        })();
    </script>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>비밀번호 찾기 | StockHub</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@100..900&display=swap"
          rel="stylesheet">
    <link rel="stylesheet" href="${commonCssUrl}?v=30">
    <link rel="stylesheet" href="${authCssUrl}?v=9">
    <script src="${headerJsUrl}?v=17" defer></script>
</head>
<body class="auth-page auth-page-reset">
<button id="theme-toggle" class="header-icon-button auth-theme-toggle"
        type="button" aria-label="화면 색상 모드 변경" title="화면 색상 모드 변경">
    <span class="theme-light-icon" aria-hidden="true">☀️</span>
    <span class="theme-dark-icon" aria-hidden="true">🌙</span>
</button>
<main class="auth-shell">
    <%-- 세 인증 화면에서 공통으로 사용하는 브랜드 이미지 영역 --%>
    <section class="auth-visual">
        <img class="auth-visual-image auth-visual-image-light"
             src="${authImageUrl}?v=4" alt="">
        <img class="auth-visual-image auth-visual-image-dark"
             src="${authImageDarkUrl}?v=1" alt="">
        <p class="auth-ai-note">이 이미지는 ai로 제작한 이미지입니다.</p>
    </section>

    <section class="auth-content">
        <div class="auth-content-scroll auth-content-centered">
            <div class="auth-content-card">
                <header class="auth-heading">
                    <h1>비밀번호 찾기</h1>
                    <p>가입한 이메일을 인증하고 새 비밀번호를 설정해주세요.</p>
                </header>

                <c:if test="${not empty error}">
                    <p class="alert alert-error password-reset-alert"><c:out value="${error}" /></p>
                </c:if>

                <%-- 기존 이메일 인증 후 새 비밀번호를 서버로 전송 --%>
                <form id="password-reset-form" class="form form-flex auth-reset-form"
                      action="${pageContext.request.contextPath}/member/password-reset"
                      data-context-path="${pageContext.request.contextPath}"
                      method="post">

                    <div class="form-row">
                        <label for="reset-email-local">이메일</label>

                        <div class="reset-email-controls">
                            <div class="email-input-group">
                                <input id="reset-email-local"
                                       type="text" minlength="1" maxlength="64"
                                       pattern="(?=.*[a-z])[a-z0-9]+([._%+-][a-z0-9]+)*"
                                       placeholder="이메일 아이디"
                                       required autocomplete="off">
                                <span class="email-at" aria-hidden="true">@</span>
                                <input id="reset-email-domain"
                                       type="text" maxlength="50"
                                       pattern="[a-z0-9]+(-[a-z0-9]+)*(\.[a-z0-9]+(-[a-z0-9]+)*)*(\.com|\.net|\.co\.kr)"
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
                            <button id="send-reset-code-btn" class="btn btn-outline reset-email-send-button"
                                    type="button">인증번호 받기</button>
                        </div>

                        <p id="reset-email-result" class="form-tip"></p>

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

                    <input id="reset-token" name="resetToken" type="hidden">

                    <div id="reset-password-area" class="password-reset-fields" hidden>
                        <div class="form-row">
                            <label for="new-password">새 비밀번호</label>
                            <input id="new-password" name="newPassword"
                                   type="password" minlength="10" maxlength="100"
                                   required autocomplete="new-password"
                                   placeholder="10자 이상">
                            <p id="reset-password-rule-result" class="form-tip"></p>
                            <p class="form-tip">대·소문자, 숫자, 특수문자를 각각 포함해주세요.</p>
                        </div>

                        <div class="form-row">
                            <label for="new-password-confirm">새 비밀번호 확인</label>
                            <input id="new-password-confirm" name="newPasswordConfirm"
                                   type="password" minlength="10" maxlength="100"
                                   required autocomplete="new-password"
                                   placeholder="비밀번호 다시 입력">
                            <p id="reset-password-confirm-result" class="form-tip"></p>
                        </div>

                        <div class="form-row">
                            <button class="btn btn-primary auth-primary-button" type="submit">비밀번호 변경</button>
                        </div>
                    </div>
                </form>

                <p class="auth-switch">비밀번호가 기억났나요? <a href="${loginUrl}">로그인</a></p>
                <a class="auth-home-logo" href="${homeUrl}" aria-label="StockHub 홈으로 이동">
                    <img src="${logoUrl}" alt="StockHub">
                </a>
            </div>
        </div>
    </section>
</main>

<script src="${pageContext.request.contextPath}/js/password-reset.js?v=2"></script>
</body>
</html>
