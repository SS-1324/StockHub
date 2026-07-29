<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<%-- 모든 페이지에서 공통으로 사용하는 주소 --%>
<c:url var="homeUrl" value="/" />
<c:url var="communityUrl" value="/community/board" />
<c:url var="tradeHubUrl" value="/trade-hub" />
<c:url var="rankingUrl" value="/ranking" />
<c:url var="dictionaryUrl" value="/dictionary" />
<c:url var="loginUrl" value="/member/login" />
<c:url var="joinUrl" value="/member/join" />
<c:url var="mypageUrl" value="/member/mypage" />
<c:url var="adminUrl" value="/admin" />
<c:url var="logoutUrl" value="/member/logout" />
<c:url var="logoUrl" value="/images/StockHub_logo_blue.png" />
<c:url var="defaultProfileUrl" value="/images/common_member.png" />
<c:url var="commonCssUrl" value="/css/common.css" />
<c:url var="headerJsUrl" value="/js/header.js" />
<c:set var="requestUri" value="${pageContext.request.requestURI}" />
<%--용어사전용 주소--%>
<c:url var="dictionaryTradingUrl" value="/dictionary/category/trading" />
<c:url var="dictionaryRiskUrl" value="/dictionary/category/risk-management" />
<c:url var="dictionaryPositionUrl" value="/dictionary/category/position" />
<c:url var="dictionaryMarketUrl" value="/dictionary/category/market" />
<c:url var="dictionaryFundamentalUrl" value="/dictionary/category/fundamental" />
<c:url var="dictionaryChartUrl" value="/dictionary/category/chart" />

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>StockHub</title>

    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@100..900&display=swap"
          rel="stylesheet">

    <link rel="stylesheet" href="${commonCssUrl}?v=8">
    <%-- 현재 화면에서 요청한 전용 CSS를 head 안에서 불러옴 --%>
    <c:if test="${not empty requestScope.pageCssUrl}">
        <link rel="stylesheet" href="${requestScope.pageCssUrl}">
    </c:if>
    <script src="${headerJsUrl}?v=5" defer></script>
</head>
<body>
<%-- 로고, 페이지 이동 메뉴, 회원 메뉴를 표시하는 공통 헤더 --%>
<header class="site-header">
    <div class="site-header-inner">
        <a class="site-logo" href="${homeUrl}" aria-label="StockHub 홈으로 이동">
            <img src="${logoUrl}?v=2" alt="StockHub">
        </a>

        <%-- 데스크톱·모바일에서 함께 사용하는 주요 페이지 메뉴 --%>
        <nav id="main-navigation" class="main-navigation" aria-label="주요 메뉴">
            <a class="main-nav-link ${requestUri eq homeUrl ? 'is-active' : ''}"
               href="${homeUrl}">홈</a>

            <div class="main-nav-community">
                <a class="main-nav-link ${requestUri eq communityUrl ? 'is-active' : ''}"
                   href="${communityUrl}">커뮤니티</a>

                <div class="community-dropdown">
                    <a href="${communityUrl}">전체</a>
                    <a href="${communityUrl}?category=free">자유</a>
                    <a href="${communityUrl}?category=trade">살까?팔까?</a>
                    <a href="${communityUrl}?category=tip">팁 공유</a>
                    <a href="${communityUrl}?category=profit">수익인증</a>
                    <a href="${communityUrl}?category=review">반성</a>
                </div>
            </div>


            <a class="main-nav-link ${requestUri eq tradeHubUrl ? 'is-active' : ''}"
               href="${tradeHubUrl}">거래 허브</a>
            <a class="main-nav-link ${requestUri eq rankingUrl ? 'is-active' : ''}"
               href="${rankingUrl}">랭킹</a>
            <div class = "main-nav-dictionary">
                <a class="main-nav-link ${requestUri eq dictionaryUrl ? 'is-active' : ''}"
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

            <%-- 로그인 여부에 따라 로그인 버튼 또는 회원 메뉴를 표시 --%>
            <c:choose>
                <c:when test="${not empty sessionScope.loginMember}">
                    <div class="header-profile-menu">
                        <button class="header-profile-toggle"
                                type="button"
                                aria-expanded="false"
                                aria-controls="header-profile-dropdown">
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

                            <span class="header-profile-nickname">
                                    <c:out value="${sessionScope.loginMember.nickname}"/>님
                                </span>
                            <span class="header-profile-arrow" aria-hidden="true">▾</span>
                        </button>

                        <div id="header-profile-dropdown"
                             class="header-profile-dropdown"
                             hidden>
                            <a href="${mypageUrl}">프로필 수정</a>
                            <a href="${tradeHubUrl}">내 주식</a>
                            <%-- 로그인 회원의 문의 목록을 페이지 이동 없이 표시 --%>
                            <button type="button"
                                    data-modal-target="my-inquiries-modal"
                                    data-load-my-inquiries="true">내 문의</button>
                            <%-- ADMIN 권한 회원에게만 관리자 페이지 링크를 표시 --%>
                            <c:if test="${fn:toUpperCase(sessionScope.loginMember.memberRole) eq 'ADMIN'}">
                                <a href="${adminUrl}">관리자 페이지</a>
                            </c:if>
                            <a class="header-logout-link" href="${logoutUrl}">로그아웃</a>
                        </div>
                    </div>
                </c:when>
                <c:otherwise>
                    <a class="header-login-link" href="${loginUrl}">로그인</a>
                    <a class="header-join-link" href="${joinUrl}">회원가입</a>
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

<%-- 각 JSP의 본문이 들어가는 영역 --%>
<main class="container">