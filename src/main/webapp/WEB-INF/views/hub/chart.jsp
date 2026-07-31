<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%-- 공통 헤더를 현재 페이지에 포함 --%>
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<!DOCTYPE html>
<html lang="ko">
<head>

    <!-- lightweight-charts 라이브러리 (CDN) -->
    <script src="https://unpkg.com/lightweight-charts@4.1.3/dist/lightweight-charts.standalone.production.js"></script>

    <!-- Custom CSS -->
    <link rel="stylesheet" href="<c:url value='/css/stockhub.css'/>">
</head>
<body>
<H3>주식 거래 허브</H3>
<a>증권사별 시세와 수수료를 비교하고 매매 페이지로 연결합니다.</a>


<!-- 2. 종목 전환 버튼 (클릭하면 그 종목 코드로 차트가 다시 그려짐) -->
<!-- 4. lightweight-charts 차트 영역 -->
<section class="ticker-bar">
    <div class="ticker-wrapper">
        <div class="ticker-item" data-code="005930">삼성전자</div>
        <div class="ticker-item" data-code="000660">SK하이닉스</div>
        <div class="ticker-item" data-code="AAPL">AAPL</div>
        <div class="ticker-item" data-code="TSLA">TSLA</div>
    </div>
</section>
<div class="chart-wrapper">
    <div id="stockhub-chart"></div>
</div>


    <!-- 서브 텍스트 -->
    <p class="hero-subtitle">
        여러 증권사 시세들을 비교하고, 커뮤니티에서 팁을 나누고, 검증된 랭커의 거래 내역을 확인하세요.
    </p>


</main>

<!-- Custom JS -->
<script>
    // 서버가 결정한 종목 코드 (URL에 code가 없으면 기본 종목)
    const resolvedCode = '<c:out value="${resolvedCode}"/>';
    // 서버가 페이지 렌더링 시점에 미리 조회해둔 캔들 데이터 (없으면 null)
    const initialCandles = ${empty initialCandlesJson ? 'null' : initialCandlesJson};
</script>
<script src="<c:url value='/js/stockhub.js'/>"></script>
</body>
</html>
<%-- 공통 푸터를 현재 페이지에 포함 --%>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />