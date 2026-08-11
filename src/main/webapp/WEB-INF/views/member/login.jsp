<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:url var="homeUrl" value="/" />
<c:url var="joinUrl" value="/member/join" />
<c:url var="passwordResetUrl" value="/member/password-reset" />
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
    <title>로그인 | StockHub</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@100..900&display=swap"
          rel="stylesheet">
    <link rel="stylesheet" href="${commonCssUrl}?v=30">
    <link rel="stylesheet" href="${authCssUrl}?v=11">
    <script src="${headerJsUrl}?v=17" defer></script>
</head>
<body class="auth-page auth-page-login">
<main class="auth-shell">
    <button id="theme-toggle" class="header-icon-button auth-theme-toggle"
            type="button" aria-label="화면 색상 모드 변경" title="화면 색상 모드 변경">
        <span class="theme-light-icon" aria-hidden="true">☀️</span>
        <span class="theme-dark-icon" aria-hidden="true">🌙</span>
    </button>

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
                    <h1>로그인</h1>
                    <p>함께 나누는 투자 이야기, 다시 시작해볼까요?</p>
                </header>

                <%-- 로그인 실패 메시지를 표시 --%>
                <c:if test="${not empty error}">
                    <p class="alert alert-error login-alert"><c:out value="${error}" /></p>
                </c:if>

                <%-- 회원가입과 비밀번호 변경 성공 메시지를 표시 --%>
                <c:if test="${not empty joinSuccess}">
                    <p class="alert alert-success login-alert">회원가입이 완료되었습니다. 로그인해주세요.</p>
                </c:if>
                <c:if test="${not empty passwordResetSuccess}">
                    <p class="alert alert-success login-alert">비밀번호가 변경되었습니다. 새 비밀번호로 로그인해주세요.</p>
                </c:if>

                <%-- 아이디와 비밀번호를 로그인 주소로 전송 --%>
                <form class="form login-form auth-login-form"
                      action="${pageContext.request.contextPath}/member/login"
                      method="post">
                    <input type="hidden" name="redirectURL" value="${param.redirectURL}">

                    <div class="login-field">
                        <label class="sr-only" for="member-id">아이디</label>
                        <input id="member-id" name="memberId" type="text"
                               placeholder="아이디"
                               required autofocus autocomplete="username">
                    </div>

                    <div class="login-field">
                        <label class="sr-only" for="member-pwd">비밀번호</label>
                        <input id="member-pwd" name="memberPwd" type="password"
                               placeholder="비밀번호"
                               required autocomplete="current-password">
                    </div>

                    <button class="btn login-submit" type="submit">로그인</button>
                </form>

                <div class="login-member-links">
                    <a href="${joinUrl}">회원가입</a>
                    <span aria-hidden="true">/</span>
                    <a href="${passwordResetUrl}">비밀번호 찾기</a>
                </div>

                <a class="auth-home-logo" href="${homeUrl}" aria-label="StockHub 홈으로 이동">
                    <img src="${logoUrl}" alt="StockHub">
                </a>
            </div>
        </div>
    </section>
</main>
</body>
</html>
