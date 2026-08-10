package com.kh.demo.hub.service;

import org.springframework.stereotype.Service;

@Service
public class StockServiceImpl implements StockService {

    // 종목 코드가 없을 때 기본으로 보여줄 종목
    // TradingView 무료 위젯이 국내(KRX) 종목 데이터는 표시하지 못해서(라이선스 제한),
    // 위젯이 실제로 지원하는 해외 종목(AAPL)을 기본값으로 씀.
    // 위젯 쪽 지원 심볼 목록은 tradingview-chart.js의 SUPPORTED_TRADINGVIEW_SYMBOLS 참고.
    private static final String DEFAULT_CODE = "AAPL";
    // 캔들 주기가 없을 때 기본으로 보여줄 주기 (TradingView 위젯의 초기 interval)
    private static final String DEFAULT_PERIOD = "day";

    @Override
    public String resolveCode(String code) {
        return (code != null && !code.isBlank()) ? code : DEFAULT_CODE;
    }

    @Override
    public String resolvePeriod(String period) {
        return (period != null && !period.isBlank()) ? period : DEFAULT_PERIOD;
    }
}
