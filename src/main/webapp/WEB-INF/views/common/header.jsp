<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<%-- 모든 페이지에서 공통으로 사용하는 주소 --%>
<c:url var="homeUrl" value="/" />
<c:url var="tradeHubUrl" value="/trade-hub" />
<c:url var="rankingUrl" value="/ranking" />
<c:url var="dictionaryUrl" value="/dictionary" />
<c:url var="loginUrl" value="/member/login" />
<c:url var="myInfoUrl" value="/member/stocks" />
<c:url var="dashboardUrl" value="/member/dashboard" />
<c:url var="adminUrl" value="/admin" />
<c:url var="logoutUrl" value="/member/logout" />
<c:url var="logoUrl" value="/images/StockHub_logo_blue.png" />
<c:url var="defaultProfileUrl" value="/images/common_member.png" />
<c:url var="commonCssUrl" value="/css/common.css" />
<c:url var="headerJsUrl" value="/js/header.js" />
<c:url var="memberProfileApiUrl" value="/member/profile/" />

<c:set var="forwardRequestUri"
       value="${requestScope['jakarta.servlet.forward.request_uri']}" />
<c:set var="requestUri"
       value="${not empty forwardRequestUri
                ? forwardRequestUri
                : pageContext.request.requestURI}" />

<%-- 커뮤니티용 주소 --%>
<c:url var="communityUrl" value="/community" scope="request" />
<c:url var="communityFreeUrl" value="/community?category=free" />
<c:url var="communityTipUrl" value="/community?category=tip" />
<c:url var="communityProfitUrl" value="/community?category=profit" />
<c:url var="communityReviewUrl" value="/community?category=review" />

<%--용어사전용 주소--%>
<c:url var="dictionaryTradingUrl" value="/dictionary/category/trading" />
<c:url var="dictionaryRiskUrl" value="/dictionary/category/risk-management" />
<c:url var="dictionaryPositionUrl" value="/dictionary/category/position" />
<c:url var="dictionaryMarketUrl" value="/dictionary/category/market" />
<c:url var="dictionaryFundamentalUrl" value="/dictionary/category/fundamental" />
<c:url var="dictionaryChartUrl" value="/dictionary/category/chart" />

<c:url var="hubBaseUrl" value="/hub/" />
<c:set var="homeMenuActive" value="${requestUri eq homeUrl}" />
<c:set var="communityMenuActive" value="${fn:startsWith(requestUri, communityUrl)}" />
<c:set var="tradeHubMenuActive"
       value="${requestUri eq tradeHubUrl or fn:startsWith(requestUri, hubBaseUrl)}" />
<c:set var="rankingMenuActive" value="${fn:startsWith(requestUri, rankingUrl)}" />
<c:set var="dictionaryMenuActive" value="${fn:startsWith(requestUri, dictionaryUrl)}" />

<!DOCTYPE html>
<html lang="ko">
<head>
    <!-- FontAwesome 아이콘 불러오기 CDN -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
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
    <title>StockHub</title>

    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@100..900&display=swap"
          rel="stylesheet">

    <link rel="stylesheet" href="${commonCssUrl}?v=38">
    <c:if test="${not empty requestScope.pageCssUrl}">
        <link rel="stylesheet" href="${requestScope.pageCssUrl}">
    </c:if>
    <script src="${headerJsUrl}?v=23" defer></script>
</head>
<body>
<%-- 로고, 페이지 이동 메뉴, 회원 메뉴를 표시하는 공통 헤더 --%>
<header class="site-header">
    <div class="site-header-inner">
        <a class="site-logo" href="${homeUrl}" aria-label="StockHub 홈으로 이동">
            <img src="${logoUrl}?v=2" alt="StockHub">
        </a>

        <nav id="main-navigation" class="main-navigation" aria-label="주요 메뉴">
            <a class="main-nav-link ${homeMenuActive ? 'is-active' : ''}"
               href="${homeUrl}">홈</a>

            <div class="main-nav-community">
                <a class="main-nav-link ${communityMenuActive ? 'is-active' : ''}"
                   href="${communityUrl}">커뮤니티</a>

                <div class="community-dropdown">
                    <a href="${communityUrl}">전체</a>
                    <a href="${communityFreeUrl}">자유</a>
                    <a href="${communityTipUrl}">팁 공유</a>
                    <a href="${communityProfitUrl}">수익인증</a>
                    <a href="${communityReviewUrl}">반성</a>
                </div>
            </div>

            <a class="main-nav-link ${tradeHubMenuActive ? 'is-active' : ''}"
               href="${tradeHubUrl}">종목 라운지</a>
            <a class="main-nav-link ${rankingMenuActive ? 'is-active' : ''}"
               href="${rankingUrl}">랭킹</a>
            <div class = "main-nav-dictionary">
                <a class="main-nav-link ${dictionaryMenuActive ? 'is-active' : ''}"
                href="${dictionaryUrl}">용어사전</a>
                <div class = "dictionary-dropdown">
                    <a href="${dictionaryTradingUrl}">매매와 투자 행동</a>
                    <a href="${dictionaryRiskUrl}">투자자·자금·손익 관리</a>
                    <a href="${dictionaryPositionUrl}">상품과 포지션</a>
                    <a href="${dictionaryMarketUrl}">시장·지수·주문·거래 제도</a>
                    <a href="${dictionaryFundamentalUrl}">종목 정보와 기업 분석</a>
                    <a href="${dictionaryChartUrl}">차트와 기술적 분석</a>
                </div>
            </div>
        </nav>

        <div class="header-actions">
            <button id="theme-toggle"
                    class="header-icon-button"
                    type="button"
                    aria-label="화면 색상 모드 변경"
                    title="화면 색상 모드 변경">
                <span class="theme-light-icon" aria-hidden="true">☀️</span>
                <span class="theme-dark-icon" aria-hidden="true">🌙</span>
            </button>

            <c:choose>
                <c:when test="${not empty sessionScope.loginMember}">
                    <c:set var="headerRankClass" value="" />
                    <c:choose>
                        <c:when test="${headerRankPosition eq 1}">
                            <c:set var="headerRankClass" value="rank-first" />
                        </c:when>
                        <c:when test="${headerRankPosition eq 2}">
                            <c:set var="headerRankClass" value="rank-second" />
                        </c:when>
                        <c:when test="${headerRankPosition eq 3}">
                            <c:set var="headerRankClass" value="rank-third" />
                        </c:when>
                    </c:choose>
                    <div class="header-profile-menu">
                        <button class="header-profile-toggle"
                                type="button"
                                aria-expanded="false"
                                aria-controls="header-profile-dropdown">
                            <span class="header-profile-rank-frame ${headerRankClass}">
                                <c:choose>
                                    <c:when test="${not empty sessionScope.loginMember.profile}">
                                        <img class="header-profile-image"
                                             src="${pageContext.request.contextPath}${sessionScope.loginMember.profile}"
                                             onerror="this.onerror=null; this.src='${defaultProfileUrl}';"
                                             alt="프로필 이미지">
                                    </c:when>
                                    <c:otherwise>
                                        <img class="header-profile-image"
                                             src="${defaultProfileUrl}"
                                             alt="기본 프로필 이미지">
                                    </c:otherwise>
                                </c:choose>
                            </span>

                            <span class="header-profile-nickname">
                                <c:out value="${sessionScope.loginMember.nickname}"/>님
                            </span>
                            <span class="header-profile-arrow" aria-hidden="true">▾</span>
                        </button>

                        <div id="header-profile-dropdown"
                             class="header-profile-dropdown"
                             hidden>
                            <c:choose>
                                <c:when test="${fn:toUpperCase(sessionScope.loginMember.memberRole) eq 'ADMIN'}">
                                    <a href="${adminUrl}">관리자 페이지</a>
                                </c:when>
                                <c:otherwise>
                                    <a href="${myInfoUrl}">내 정보</a>
                                    <a href="${dashboardUrl}">내 금융거래</a>
                                </c:otherwise>
                            </c:choose>
                            <a class="header-logout-link" href="${logoutUrl}">로그아웃</a>
                        </div>
                        <%-- 세션 타이머 (ADMIN이 아닌 로그인 유저에게 표시) --%>
                        <c:if test="${not empty sessionScope.loginMember and fn:toUpperCase(sessionScope.loginMember.memberRole) ne 'ADMIN'}">
                            <%-- 세션에 담긴 sessionExpiresAt을 사용하고, 혹시나 없는 예외 상황엔 현재시간+유지시간 계산 --%>
                            <c:set var="headerSessionExpiresAt"
                                   value="${not empty sessionScope.sessionExpiresAt ? sessionScope.sessionExpiresAt : (pageContext.session.lastAccessedTime + (pageContext.session.maxInactiveInterval * 1000))}" />

                            <div class="header-session-timer"
                                 data-session-expires-at="${headerSessionExpiresAt}">
                                <span id="header-session-remaining-time">--:--</span>
                                <button type="button" class="session-extend-btn" onclick="extendSession(event)" title="세션 30분 연장" aria-label="세션 30분 연장">
                                    <i class="fa-solid fa-arrows-rotate"></i>
                                </button>
                            </div>
                        </c:if>
                    </div>
                </c:when>
                <c:otherwise>
                    <a class="header-login-link header-login-required"
                       href="${loginUrl}">로그인이 필요합니다.</a>
                </c:otherwise>
            </c:choose>

            <button id="mobile-menu-toggle"
                    class="header-icon-button mobile-menu-toggle"
                    type="button"
                    aria-label="메뉴 열기"
                    aria-expanded="false"
                    aria-controls="main-navigation">
                <span aria-hidden="true">☰</span>
            </button>
        </div>
    </div>
</header>

<div id="member-profile-modal"
     class="member-profile-modal-overlay"
     data-profile-api="${memberProfileApiUrl}"
     data-context-path="${pageContext.request.contextPath}"
     data-default-profile="${defaultProfileUrl}"
     hidden>
    <section class="member-profile-modal"
             role="dialog"
             aria-modal="true"
             aria-labelledby="member-profile-heading">
        <button type="button"
                class="member-profile-modal-close"
                data-profile-modal-close
                aria-label="프로필 창 닫기">×</button>

        <p class="member-profile-loading" data-profile-loading>프로필을 불러오는 중입니다.</p>
        <p class="member-profile-error" data-profile-error hidden></p>

        <div class="member-profile-content" data-profile-content hidden>
            <h2 id="member-profile-heading"
                class="member-profile-heading"
                data-profile-heading>회원 정보</h2>

            <div class="member-profile-card">
                <header class="member-profile-summary">
                    <span class="member-profile-avatar-frame" data-profile-avatar-frame>
                        <img class="member-profile-avatar"
                             data-profile-avatar
                             src="${defaultProfileUrl}"
                             alt="프로필 이미지">
                    </span>
                    <div class="member-profile-identity">
                        <h3 data-profile-nickname></h3>
                        <p data-profile-member-id></p>
                    </div>
                    <div class="member-profile-actions">
                        <span class="member-profile-badge" data-profile-badge>USER</span>
                        <button type="button"
                                class="member-profile-follow-toggle"
                                data-profile-follow-toggle
                                aria-pressed="false"
                                hidden>팔로우</button>
                    </div>
                </header>

                <nav class="member-profile-stats" data-profile-public-stats
                     aria-label="회원 활동 및 투자 정보" hidden>
                    <a class="member-profile-stat member-profile-posts-link"
                       data-profile-posts-link>
                        <span>작성글</span><strong data-profile-post-count>0</strong>
                    </a>
                    <div class="member-profile-stat">
                        <span>팔로워</span><strong data-profile-follower-count>0</strong>
                    </div>
                    <div class="member-profile-stat">
                        <span>팔로잉</span><strong data-profile-following-count>0</strong>
                    </div>
                    <div class="member-profile-stat is-investment" data-profile-investment-stat>
                        <span>수익률</span><strong data-profile-return-rate>0%</strong>
                    </div>
                    <div class="member-profile-stat is-investment" data-profile-investment-stat>
                        <span>수익금</span><strong data-profile-profit>0원</strong>
                    </div>
                </nav>
            </div>
        </div>
    </section>
</div>

<main class="container">