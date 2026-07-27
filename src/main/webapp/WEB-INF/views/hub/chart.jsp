<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>StockHub - 함께 나누는 주식 이야기</title>

    <!-- Google Fonts & FontAwesome Icons -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Pretendard:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">

    <!-- lightweight-charts 라이브러리 (CDN) -->
    <script src="https://unpkg.com/lightweight-charts@4.1.3/dist/lightweight-charts.standalone.production.js"></script>

    <!-- Custom CSS -->
    <link rel="stylesheet" href="<c:url value='/css/stockhub.css'/>">
</head>
<body>

<!-- 1. 상단 메인 네비게이션 바 -->
<header class="navbar">
    <div class="nav-left">
        <a href="/" class="logo">
            <i class="fa-solid fa-chart-line logo-icon"></i>
            <span>StockHub</span>
        </a>
        <nav class="nav-menu">
            <a href="#" class="nav-item active">홈</a>
            <a href="#" class="nav-item">커뮤니티</a>
            <a href="#" class="nav-item">거래 허브</a>
            <a href="#" class="nav-item">랭킹</a>
            <a href="#" class="nav-item">용어사전</a>
        </nav>
    </div>

    <div class="nav-right">
        <button class="theme-toggle" aria-label="테마 변경">
            <i class="fa-regular fa-sun"></i>
        </button>
        <button class="login-btn">
            <i class="fa-solid fa-lock"></i>
            <span>로그인이 필요합니다</span>
        </button>
    </div>
</header>

<!-- 2. 주식 종목 실시간 티커 띠 (Stock Ticker Bar) -->
<!-- 4. lightweight-charts 차트 영역 -->
<div class="chart-wrapper">
    <div id="stockhub-chart"></div>
</div>
<section class="ticker-bar">
    <div class="ticker-wrapper">
        <div class="ticker-item">DOW <span>891.23</span> <span class="down">-0.24%</span></div>
        <div class="ticker-item">삼성전자 <span>82,400</span> <span class="up">+1.23%</span></div>
        <div class="ticker-item">NAVER <span>198,500</span> <span class="up">+2.15%</span></div>
        <div class="ticker-item">카카오 <span>44,650</span> <span class="down">-0.78%</span></div>
        <div class="ticker-item">현대차 <span>234,000</span> <span class="up">+0.43%</span></div>
        <div class="ticker-item">LG에너지 <span>412,500</span> <span class="down">-1.52%</span></div>
        <div class="ticker-item">SK하이닉스 <span>186,300</span> <span class="up">+3.21%</span></div>
        <div class="ticker-item">POSCO홀딩스 <span>541,000</span> <span class="down">-0.37%</span></div>
        <div class="ticker-item">KOSPI <span>2,892.14</span> <span class="up">+0.69%</span></div>
        <div class="ticker-item">KOSDAQ <span>891.23</span> <span class="down">-0.24%</span></div>
    </div>
</section>

<!-- 3. 메인 히어로 섹션 -->
<main class="hero-container">
    <!-- 상단 뱃지 -->
    <div class="status-badge">
        <span class="pulse-dot"></span>
        실시간 시세 연동 중
    </div>

    <!-- 히어로 헤드라인 -->
    <h1 class="hero-title">
        주식, 혼자보면 숫지만<br>
        <span class="highlight">같이보면 이야기니까.</span>
    </h1>

    <!-- 서브 텍스트 -->
    <p class="hero-subtitle">
        여러 증권사 시세들을 비교하고, 커뮤니티에서 팁을 나누고, 검증된 랭커의 거래 내역을 확인하세요.
    </p>


</main>

<!-- Custom JS -->
<script src="<c:url value='/js/stockhub.js'/>"></script>
</body>
</html>