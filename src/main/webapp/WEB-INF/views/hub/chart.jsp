<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%-- 공통 헤더를 현재 페이지에 포함 --%>
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<!DOCTYPE html>
<html lang="ko">
<head>

    <!-- TradingView Advanced Chart 위젯 (심볼/해상도를 JS API로 갱신할 수 있는 widget 생성자) -->
    <script src="https://s3.tradingview.com/tv.js"></script>

    <!-- Custom CSS -->
    <link rel="stylesheet" href="<c:url value='/css/stockhub.css'/>">
</head>
<body>

<H3>주식 거래 허브</H3>
<p>관심 종목의 실시간 차트를 확인하세요.</p>

<div class="chart-wrapper">
    <!-- TradingView 위젯이 마운트되는 컨테이너 -->
    <div id="tv-chart-container"></div>
</div>

</main>

<!-- Custom JS -->
<script>
    // 서버가 결정한 종목 코드 (URL에 code가 없으면 기본 종목)
    const resolvedCode = '<c:out value="${resolvedCode}"/>';
    // 서버가 결정한 캔들 주기: minute/day/week/month (URL에 period가 없으면 day)
    const resolvedPeriod = '<c:out value="${resolvedPeriod}"/>';
</script>
<script src="<c:url value='/js/tradingview-chart.js'/>"></script>
<script src="<c:url value='/js/stockhub.js'/>"></script>
</body>
</html>
<%-- 공통 푸터를 현재 페이지에 포함 --%>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
