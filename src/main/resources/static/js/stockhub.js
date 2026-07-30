// hub/chart.jsp에서 lightweight-charts로 캔들차트를 그리는 스크립트
// LightweightCharts 전역 객체는 chart.jsp에 <script>로 불러온 CDN 라이브러리가 만들어줌



document.addEventListener('DOMContentLoaded', function () {
    const container = document.getElementById('stockhub-chart');
    if (!container) return;

    const { chart, candleSeries } = initChart(container);
    loadInitialData(candleSeries);
});

//candelChart를 그려주기 위한 LightweightCharts.createChart
function initChart(container) {
    const chart = LightweightCharts.createChart(container, {
        width: container.clientWidth,
        height: 500,
        layout: { background: { color: '#131722' }, textColor: '#d1d4dc' },
        grid: { vertLines: { color: '#242732' }, horzLines: { color: '#242732' } },
        timeScale: { borderColor: '#485c7b' },
    });
    const candleSeries = chart.addCandlestickSeries({
        upColor: '#26a69a', downColor: '#ef5350', borderVisible: false,
        wickUpColor: '#26a69a', wickDownColor: '#ef5350',
    });
    return { chart, candleSeries };
}

// 최초 전체 데이터 로드
// await를 사용하기 위해 async 사용함.

async function loadInitialData(candleSeries) {
    const res = await fetch('/api/stock/005930/chart'); // 우리 서버 API
    const data = await res.json();
    candleSeries.setData(data); // 캔들차트 데이터를 넘겨받으면 캔들차트를 그림
}


