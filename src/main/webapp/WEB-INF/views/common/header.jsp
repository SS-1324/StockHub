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
<%-- JSP 내부 forward 경로가 아니라 브라우저가 요청한 실제 주소를 사용 --%>
<c:set var="forwardRequestUri"
       value="${requestScope['jakarta.servlet.forward.request_uri']}" />
<c:set var="requestUri"
       value="${not empty forwardRequestUri
                ? forwardRequestUri
                : pageContext.request.requestURI}" />

<%-- 커뮤니티용 주소 --%>
<c:url var="communityUrl" value="/community" scope="request" />
<c:url var="communityFreeUrl" value="/community?category=free" />
<%-- DB에 저장된 기존 key는 유지하고 사용자에게 보이는 이름만 새 카테고리명으로 연결한다. --%>
<c:url var="communityDiscussionUrl" value="/community?category=tip" />
<c:url var="communityInfoUrl" value="/community?category=profit" />
<c:url var="communityReflectionUrl" value="/community?category=review" />

<%--용어사전용 주소--%>
<c:url var="dictionaryTradingUrl" value="/dictionary/category/trading" />
<c:url var="dictionaryRiskUrl" value="/dictionary/category/risk-management" />
<c:url var="dictionaryPositionUrl" value="/dictionary/category/position" />
<c:url var="dictionaryMarketUrl" value="/dictionary/category/market" />
<c:url var="dictionaryFundamentalUrl" value="/dictionary/category/fundamental" />
<c:url var="dictionaryChartUrl" value="/dictionary/category/chart" />

<%-- 하위 주소에서도 현재 선택한 주요 메뉴가 유지되도록 주소 앞부분을 비교 --%>
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

    <link rel="stylesheet" href="${commonCssUrl}?v=37">
    <%-- 현재 화면에서 요청한 전용 CSS를 head 안에서 불러옴 --%>
    <c:if test="${not empty requestScope.pageCssUrl}">
        <link rel="stylesheet" href="${requestScope.pageCssUrl}">
    </c:if>
    <script src="${headerJsUrl}?v=22" defer></script>
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
            <a class="main-nav-link ${homeMenuActive ? 'is-active' : ''}"
               href="${homeUrl}">홈</a>

            <div class="main-nav-community">
                <a class="main-nav-link ${communityMenuActive ? 'is-active' : ''}"
                   href="${communityUrl}">커뮤니티</a>

                <div class="community-dropdown">
                    <a href="${communityUrl}">전체</a>
                    <a href="${communityFreeUrl}">자유</a>
                    <a href="${communityDiscussionUrl}">종목토론</a>
                    <a href="${communityInfoUrl}">정보공유</a>
                    <a href="${communityReflectionUrl}">반성일지</a>
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

            <%-- 로그인 여부에 따라 로그인 버튼 또는 회원 메뉴를 표시 --%>
            <c:choose>
                <c:when test="${not empty sessionScope.loginMember}">
                    <%-- 공통 모델의 수익률 순위를 기존 랭킹 CSS와 같은 클래스 이름으로 변환 --%>
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
                            <%-- 랭커는 프로필 이미지 바깥에 금·은·동 원형 프레임을 표시 --%>
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
                                <%-- 관리자는 관리자 페이지 외 회원 전용 메뉴를 사용하지 않음 --%>
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

<%-- [프로필모달-1] 커뮤니티와 랭킹이 함께 쓰며, 실제 데이터는 클릭할 때 API로 채운다. --%>
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
                    <%-- [프로필랭커프레임-1] JS가 순위에 따라 금·은·동 클래스를 이 프레임에 붙인다. --%>
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
                        <%-- [팔로우토글-1] 본인이 아닌 로그인 회원의 프로필에서만 JS가 버튼을 표시한다. --%>
                        <button type="button"
                                class="member-profile-follow-toggle"
                                data-profile-follow-toggle
                                aria-pressed="false"
                                hidden>팔로우</button>
                    </div>
                </header>

                <%-- [프로필공개정보-4]
                     작성글·팔로워·팔로잉은 항상 표시하고, 투자정보 두 행만 공개 설정에 따라 제어한다. --%>
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

<%-- 각 JSP의 본문이 들어가는 영역 --%>
<main class="container">
