// hub/chart.jsp의 화면 동작(위젯 마운트, 테마 동기화)을 담당.
// 실제 차트 렌더링은 tradingview-chart.js의 StockHubTradingViewChart가 처리함.

// URL 쿼리 파라미터(?code=AAPL)로 표시할 종목 코드를 받되,
// 없으면 서버가 정해준 기본 종목(resolvedCode)을 사용
const code = new URLSearchParams(window.location.search).get('code')
    || (typeof resolvedCode !== 'undefined' ? resolvedCode : null);
// 트레이딩뷰 위젯이 지원하지 않는 코드(예: 국내 종목 코드)면 위젯이 내부적으로
// 기본 종목으로 대체해서 보여주므로, 왜 다른 종목이 보이는지 헷갈리지 않도록 콘솔에 남겨둠
if (code && typeof isSupportedCode === 'function' && !isSupportedCode(code)) {
    console.warn(`'${code}'는 트레이딩뷰 위젯이 지원하지 않는 종목이라 기본 종목으로 대체됩니다.`);
}
// 캔들 주기(minute/day/week/month)도 같은 방식으로 결정
const period = new URLSearchParams(window.location.search).get('period')
    || (typeof resolvedPeriod !== 'undefined' ? resolvedPeriod : 'day');

document.addEventListener('DOMContentLoaded', function () {
    const container = document.getElementById('tv-chart-container');
    if (!container) return;
    if (!code) {
        console.error('종목 코드가 없습니다. URL에 ?code=AAPL 형태로 접속하세요.');
        return;
    }

    const tvChart = new StockHubTradingViewChart('tv-chart-container');
    tvChart.mount(code, period);

    // 사이트 테마 토글(header.js가 <html data-theme>를 바꿈)에 맞춰 위젯도 재생성
    // TradingView 공개 위젯 API는 생성 후 테마를 즉시 바꾸는 기능이 없어 재마운트로 처리함
    new MutationObserver(() => tvChart.applyCurrentTheme())
        .observe(document.documentElement, { attributes: true, attributeFilter: ['data-theme'] });
});
