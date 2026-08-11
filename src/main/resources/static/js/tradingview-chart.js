// hub/chart.jsp에서 TradingView Advanced Chart 임베드 위젯을 마운트/갱신하는 컴포넌트.
//
// 지금은 차트 표시 전용이지만, 나중에 매매 엔진(주문 패널 등)이 추가될 것을 고려해
// 위젯 내부 구현(embed-widget-advanced-chart.js 마크업, 심볼 문자열 형식 등)을 이 파일 안에만
// 가둬두고, 바깥(stockhub.js)에는 mount/setSymbol/getCurrentCode 같은 좁은 인터페이스만
// 노출한다. 매매 엔진이 "지금 보고 있는 종목 코드"가 필요하면 getCurrentCode()만 호출하면 됨.

// 캔들 주기(day/week/month/minute) -> TradingView interval 코드
const TV_INTERVAL_BY_PERIOD = {
    minute: '1',
    day: 'D',
    week: 'W',
    month: 'M',
};

// 종목의 거래소 정보를 못 구했을 때(예: DB에 없는 코드로 ?code= 접속)만 쓰는 최후 기본값.
// 정상 흐름에서는 거래소 정보가 항상 서버(stock 테이블의 exchange 컬럼)나 검색 API 응답에서
// 내려오므로, 이 화이트리스트를 더 이상 프론트에서 관리하지 않아도 됨.
const DEFAULT_SUPPORTED_CODE = 'AAPL';
const DEFAULT_EXCHANGE = 'NASDAQ';

// 차트 헤더의 빠른 종목 전환 버튼에 노출할 대표 5개 종목
const QUICK_SWITCH_CODES = ['AAPL', 'MSFT', 'NVDA', 'TSLA', 'GOOGL'];

// exchange가 없으면(국내 종목이거나 존재하지 않는 코드) 기본 지원 종목으로 대체
function resolveTradingViewSymbol(code, exchange) {
    return exchange ? `${exchange}:${code}` : `${DEFAULT_EXCHANGE}:${DEFAULT_SUPPORTED_CODE}`;
}

function resolveTradingViewInterval(period) {
    return TV_INTERVAL_BY_PERIOD[period] || 'D';
}

// 사이트 전체 테마(header.js가 <html data-theme="dark|light">로 관리)를 그대로 읽어옴
function readSiteTheme() {
    return document.documentElement.dataset.theme === 'light' ? 'light' : 'dark';
}

// 위젯 배경/그리드 색상은 theme 파라미터만으로는 자동 전환되지 않아 테마별로 직접 지정
const TV_THEME_COLORS = {
    dark: { backgroundColor: '#0F0F0F', gridColor: 'rgba(242, 242, 242, 0.06)' },
    light: { backgroundColor: '#FFFFFF', gridColor: 'rgba(42, 46, 57, 0.06)' },
};

class TradingViewChart {
    constructor(containerId) {
        this.containerId = containerId;
        this.code = null;
        this.period = null;
        this.exchange = null;
    }

    // embed-widget-advanced-chart.js는 생성 이후 심볼/테마를 바꿀 수 있는 JS API를
    // 제공하지 않는 완결형 임베드 위젯이라, 값이 바뀔 때마다 컨테이너를 비우고
    // 위젯 마크업 전체를 다시 그린다.
    mount(code, period, exchange) {
        this.code = code;
        this.period = period;
        this.exchange = exchange;

        const container = document.getElementById(this.containerId);
        if (!container) return;
        container.innerHTML = '';

        const symbol = resolveTradingViewSymbol(code, exchange);
        const theme = readSiteTheme();
        const themeColors = TV_THEME_COLORS[theme];

        const widgetContainer = document.createElement('div');
        widgetContainer.className = 'tradingview-widget-container';
        widgetContainer.style.height = '100%';
        widgetContainer.style.width = '100%';

        const widgetDiv = document.createElement('div');
        widgetDiv.className = 'tradingview-widget-container__widget';
        widgetDiv.style.height = 'calc(100% - 32px)';
        widgetDiv.style.width = '100%';

        // code는 URL 쿼리(?code=)에서 그대로 들어올 수 있는 값이라, innerHTML로 조립하면
        // DOM 기반 XSS로 이어질 수 있음. innerHTML 대신 DOM API + textContent로만 채워 넣는다.
        const copyright = document.createElement('div');
        copyright.className = 'tradingview-widget-copyright';

        const copyrightLink = document.createElement('a');
        copyrightLink.href = `https://www.tradingview.com/symbols/${symbol.replace(':', '-')}/`;
        copyrightLink.rel = 'noopener nofollow';
        copyrightLink.target = '_blank';

        const copyrightLinkText = document.createElement('span');
        copyrightLinkText.className = 'blue-text';
        copyrightLinkText.textContent = `${code} stock chart`;
        copyrightLink.appendChild(copyrightLinkText);

        const copyrightTrademark = document.createElement('span');
        copyrightTrademark.className = 'trademark';
        copyrightTrademark.textContent = ' by TradingView';

        copyright.appendChild(copyrightLink);
        copyright.appendChild(copyrightTrademark);

        const script = document.createElement('script');
        script.type = 'text/javascript';
        script.src = 'https://s3.tradingview.com/external-embedding/embed-widget-advanced-chart.js';
        script.async = true;
        script.textContent = JSON.stringify({
            allow_symbol_change: true,
            calendar: false,
            details: true,
            hide_side_toolbar: false,
            hide_top_toolbar: false,
            hide_legend: false,
            hide_volume: false,
            hotlist: false,
            interval: resolveTradingViewInterval(period),
            locale: 'en',
            save_image: true,
            style: '1',
            symbol: symbol,
            theme: theme,
            timezone: 'Etc/UTC',
            backgroundColor: themeColors.backgroundColor,
            gridColor: themeColors.gridColor,
            watchlist: [
                'BITSTAMP:BTCUSD',
                'NASDAQ:AAPL',
                'NASDAQ:NVDA',
                'NASDAQ:TSLA',
                'BITSTAMP:ETHUSD',
                'NASDAQ:AMZN',
                'NASDAQ:GOOGL',
            ],
            withdateranges: false,
            compareSymbols: [],
            studies: [],
            autosize: true,
        });

        widgetContainer.appendChild(widgetDiv);
        widgetContainer.appendChild(copyright);
        widgetContainer.appendChild(script);
        container.appendChild(widgetContainer);
    }

    // 종목 코드/주기가 바뀌었을 때도 임베드 위젯은 부분 갱신 API가 없으므로 재마운트로 처리
    setSymbol(code, period, exchange) {
        this.mount(code, period, exchange);
    }

    // 테마가 바뀔 때도 마찬가지로 재마운트
    applyCurrentTheme() {
        if (!this.code) return;
        this.mount(this.code, this.period, this.exchange);
    }

    getCurrentCode() {
        return this.code;
    }

    getCurrentPeriod() {
        return this.period;
    }

    getCurrentExchange() {
        return this.exchange;
    }
}

// stockhub.js 및 (향후) 매매 엔진 스크립트에서 사용할 전역 진입점
TradingViewChart.QUICK_SWITCH_CODES = QUICK_SWITCH_CODES;
window.StockHubTradingViewChart = TradingViewChart;
