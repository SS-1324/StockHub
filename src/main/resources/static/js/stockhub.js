// hub/chart.jsp에서 lightweight-charts로 캔들차트를 그리는 스크립트
// LightweightCharts 전역 객체는 chart.jsp에 <script>로 불러온 CDN 라이브러리가 만들어줌


// URL 쿼리 파라미터(?code=005930)로 표시할 종목 코드를 받되,
// 없으면 서버가 정해준 기본 종목(resolvedCode)을 사용
const code = new URLSearchParams(window.location.search).get('code')
    || (typeof resolvedCode !== 'undefined' ? resolvedCode : null);

document.addEventListener('DOMContentLoaded', function () {
    const container = document.getElementById('stockhub-chart');

    // 종목 전환 버튼: 클릭하면 그 종목 코드로 이동, 현재 보고 있는 종목은 강조 표시
    document.querySelectorAll('.ticker-item[data-code]').forEach((item) => {
        if (item.dataset.code === code) {
            item.classList.add('active');
        }
        item.addEventListener('click', () => {
            window.location.href = `/hub/chart?code=${item.dataset.code}`;
        });
    });

    if (!container) return;
    if (!code) {
        console.error('종목 코드가 없습니다. URL에 ?code=005930 형태로 접속하세요.');
        return;
    }


    const { chart, candleSeries } = initChart(container);

    // 서버에서 페이지 렌더링 시점에 미리 내려준 데이터가 있으면 즉시 그려서
    // 첫 fetch를 기다리는 동안 빈 차트가 보이는 지연을 없앰
    if (typeof initialCandles !== 'undefined' && initialCandles) {
        candleSeries.setData(initialCandles);
    }

    let intervalId = null;

    function startPolling() {
        if (intervalId !== null) return; // 이미 돌고 있으면 중복 실행 방지
        loadInitialData(candleSeries); // 다시 보일 때 바로 한 번 갱신
        intervalId = setInterval(() => {
            loadInitialData(candleSeries);
        }, 3000);
    }

    function stopPolling() {
        if (intervalId !== null) {
            clearInterval(intervalId);
            intervalId = null;
        }
    }

    document.addEventListener('visibilitychange', () => {
        if (document.hidden) {
            stopPolling();
        } else {
            startPolling();
        }
    });

    startPolling();
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
    const res = await fetch(`/api/stock/${code}/chart`); // 우리 서버 API
    const data = await res.json();
    candleSeries.setData(data); // 캔들차트 데이터를 넘겨받으면 캔들차트를 그림
}

