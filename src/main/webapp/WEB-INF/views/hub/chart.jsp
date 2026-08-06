<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%-- 공통 헤더를 현재 페이지에 포함 --%>
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<!DOCTYPE html>
<html lang="ko">
<head>

    <!-- Custom CSS -->
    <link rel="stylesheet" href="<c:url value='/css/stockhub.css'/>">

    <script type="module" src="https://widgets.tradingview-widget.com/w/kr/tv-ticker-tape.js"></script>

    <!-- 종목 채팅용 WebSocket(STOMP) 클라이언트 -->
    <script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/@stomp/stompjs@7/bundles/stomp.umd.min.js"></script>
</head>
<body>

<%-- 거래 허브 페이지 전용 실시간 시세 티커바 (TradingView Ticker Tape 위젯) --%>
<tv-ticker-tape class="hub-ticker-tape" symbols="NASDAQ:AAPL,NASDAQ:TSLA,NASDAQ:MSFT,NASDAQ:AMZN,NASDAQ:META,NASDAQ:SPCX,NASDAQ:NVDA,NASDAQ:AMD,NASDAQ:GOOGL,NASDAQ:INTC,NASDAQ:NFLX,NASDAQ:MSTR" hide-chart item-size="compact"></tv-ticker-tape>

<H2>주식 거래 허브</H2>
<p>관심 종목의 실시간 차트를 확인하세요.</p>

<div class="chart-wrapper">
    <div class="chart-header">
        <div class="chart-header-left">
            <span id="chart-symbol-name" class="chart-symbol-name"></span>
        </div>
        <div class="chart-header-right">
            <!-- 빠른 종목 전환 버튼 (JS가 채워 넣음) -->
            <div id="chart-symbol-buttons" class="chart-symbol-buttons"></div>

            <button type="button" id="chat-toggle-btn" class="chart-icon-btn" aria-label="채팅 열기" title="채팅">
                <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"/>
                </svg>
            </button>

            <button type="button" id="fullscreen-toggle-btn" class="chart-icon-btn" aria-label="전체화면" title="전체화면">
                <svg class="icon-expand" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M8 3H5a2 2 0 0 0-2 2v3"/>
                    <path d="M21 8V5a2 2 0 0 0-2-2h-3"/>
                    <path d="M3 16v3a2 2 0 0 0 2 2h3"/>
                    <path d="M16 21h3a2 2 0 0 0 2-2v-3"/>
                </svg>
                <svg class="icon-collapse" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M9 3v3a2 2 0 0 1-2 2H4"/>
                    <path d="M15 3v3a2 2 0 0 0 2 2h3"/>
                    <path d="M9 21v-3a2 2 0 0 0-2-2H4"/>
                    <path d="M15 21v-3a2 2 0 0 1 2-2h3"/>
                </svg>
            </button>
        </div>
    </div>
    <div class="chart-body">
        <!-- TradingView 위젯이 마운트되는 컨테이너. 채팅이 열리면 flex가 이 영역만 줄여줌 -->
        <div id="tv-chart-container"></div>

        <!-- 종목 채팅 패널: 채팅 아이콘을 누르면 차트 가로폭이 줄면서 이 자리에 나타남 (UI 전용, 실제 메시지 저장/전송 기능은 아직 없음) -->
        <aside id="chat-panel" class="chat-panel" aria-hidden="true">
            <div class="chat-panel-header">
                <span>종목 채팅</span>
                <button type="button" id="chat-panel-close-btn" class="chat-panel-close-btn" aria-label="채팅 닫기">&times;</button>
            </div>
            <div id="chat-messages" class="chat-panel-body">
                <p id="chat-panel-placeholder" class="chat-panel-placeholder">채팅 연결 중...</p>
            </div>
            <div class="chat-panel-footer">
                <input type="text" id="chat-input" class="chat-panel-input" placeholder="메시지 입력" maxlength="200" disabled>
                <button type="button" id="chat-send-btn" class="chat-panel-send-btn" disabled>전송</button>
            </div>
        </aside>
    </div>
</div>

</main>

<!-- Custom JS -->
<script>
    // 서버가 결정한 종목 코드 (URL에 code가 없으면 기본 종목)
    const resolvedCode = '<c:out value="${resolvedCode}"/>';
    // 서버가 결정한 캔들 주기: minute/day/week/month (URL에 period가 없으면 day)
    const resolvedPeriod = '<c:out value="${resolvedPeriod}"/>';
    // 채팅 입력창 활성화 여부 판단용 (비로그인 사용자는 채팅 전송 불가)
    const isLoggedIn = <c:out value="${not empty sessionScope.loginMember}"/>;
</script>
<script src="<c:url value='/js/tradingview-chart.js'/>"></script>
<script src="<c:url value='/js/stockhub.js'/>"></script>
</body>
</html>
<%-- 공통 푸터를 현재 페이지에 포함 --%>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
