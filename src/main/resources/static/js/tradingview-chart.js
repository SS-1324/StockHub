// hub/chart.jsp에서 TradingView Advanced Chart 위젯을 마운트/갱신하는 컴포넌트.
//
// 지금은 차트 표시 전용이지만, 나중에 매매 엔진(주문 패널 등)이 추가될 것을 고려해
// 위젯 내부 구현(TradingView.widget 생성자, 심볼 문자열 형식 등)을 이 파일 안에만
// 가둬두고, 바깥(stockhub.js)에는 mount/setSymbol/getCurrentCode 같은 좁은 인터페이스만
// 노출한다. 매매 엔진이 "지금 보고 있는 종목 코드"가 필요하면 getCurrentCode()만 호출하면 됨.

// 캔들 주기(day/week/month/minute) -> TradingView interval 코드
const TV_INTERVAL_BY_PERIOD = {
    minute: '1',
    day: 'D',
    week: 'W',
    month: 'M',
};

// 트레이딩뷰 무료 공개 위젯이 실제로 표시 가능한 심볼만 화이트리스트로 관리.
// 국내(KRX) 종목은 위젯 라이선스상 데이터 표시가 막혀 있어 "This symbol is only
// available on TradingView" 안내와 함께 임의의 다른 심볼로 대체돼버리므로 지원 대상에서 제외함.
// (지원 여부가 바뀌면 이 목록만 갱신하면 됨)
const SUPPORTED_TRADINGVIEW_SYMBOLS = {
    AAPL: 'NASDAQ:AAPL',
    TSLA: 'NASDAQ:TSLA',
    MSFT: 'NASDAQ:MSFT',
    GOOGL: 'NASDAQ:GOOGL',
    AMZN: 'NASDAQ:AMZN',
    NVDA: 'NASDAQ:NVDA',
    META: 'NASDAQ:META',
    NFLX: 'NASDAQ:NFLX',
};

const DEFAULT_SUPPORTED_CODE = 'AAPL';

// 화이트리스트에 없는 코드(예: 국내 종목 코드)가 들어오면 기본 지원 종목으로 대체
function resolveTradingViewSymbol(code) {
    return SUPPORTED_TRADINGVIEW_SYMBOLS[code] || SUPPORTED_TRADINGVIEW_SYMBOLS[DEFAULT_SUPPORTED_CODE];
}

function isSupportedCode(code) {
    return Object.prototype.hasOwnProperty.call(SUPPORTED_TRADINGVIEW_SYMBOLS, code);
}

function resolveTradingViewInterval(period) {
    return TV_INTERVAL_BY_PERIOD[period] || 'D';
}

// 사이트 전체 테마(header.js가 <html data-theme="dark|light">로 관리)를 그대로 읽어옴
function readSiteTheme() {
    return document.documentElement.dataset.theme === 'light' ? 'light' : 'dark';
}

class TradingViewChart {
    constructor(containerId) {
        this.containerId = containerId;
        this.widget = null;
        this.code = null;
        this.period = null;
    }

    // 위젯을 처음 생성. 이후에는 remount 없이 setSymbol()만 호출하면 됨
    mount(code, period) {
        this.code = code;
        this.period = period;

        this.widget = new TradingView.widget({
            symbol: resolveTradingViewSymbol(code),
            interval: resolveTradingViewInterval(period),
            container_id: this.containerId,
            autosize: true,
            theme: readSiteTheme(),
            locale: 'ko',
            timezone: 'Asia/Seoul',
            style: '1',
            withdateranges: true,
            hide_side_toolbar: false,
            // 매매 엔진이 붙기 전까지는 위젯 안 검색창으로 임의 종목을 바꾸지 못하게 막아둠
            allow_symbol_change: false,
        });
    }

    // 종목 코드/주기가 바뀌었을 때 위젯을 다시 만들지 않고 심볼/해상도만 갱신
    setSymbol(code, period) {
        this.code = code;
        this.period = period;

        if (!this.widget) {
            this.mount(code, period);
            return;
        }

        this.widget.onChartReady(() => {
            const activeChart = this.widget.activeChart();
            activeChart.setSymbol(resolveTradingViewSymbol(code), () => {
                activeChart.setResolution(resolveTradingViewInterval(period), () => {});
            });
        });
    }

    // TradingView 위젯 공개 API는 생성 이후 테마를 즉시 바꾸는 기능을 제공하지 않으므로,
    // 테마가 바뀔 때만 예외적으로 기존 위젯을 제거하고 같은 종목/주기로 다시 마운트함
    // (remove() 없이 새로 생성하면 같은 컨테이너에 iframe이 중첩됨)
    applyCurrentTheme() {
        if (!this.widget || !this.code) return;
        this.widget.remove();
        this.mount(this.code, this.period);
    }

    getCurrentCode() {
        return this.code;
    }

    getCurrentPeriod() {
        return this.period;
    }
}

// stockhub.js 및 (향후) 매매 엔진 스크립트에서 사용할 전역 진입점
window.StockHubTradingViewChart = TradingViewChart;
