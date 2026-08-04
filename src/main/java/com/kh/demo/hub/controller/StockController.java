package com.kh.demo.hub.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class StockController {

    // 종목 코드가 없을 때 기본으로 보여줄 종목
    // TradingView 무료 위젯이 국내(KRX) 종목 데이터는 표시하지 못해서(라이선스 제한),
    // 위젯이 실제로 지원하는 해외 종목(AAPL)을 기본값으로 씀.
    // 위젯 쪽 지원 심볼 목록은 tradingview-chart.js의 SUPPORTED_TRADINGVIEW_SYMBOLS 참고.
    private static final String DEFAULT_CODE = "AAPL";
    // 캔들 주기가 없을 때 기본으로 보여줄 주기 (TradingView 위젯의 초기 interval)
    private static final String DEFAULT_PERIOD = "day";

    // 거래 허브 메인 (헤더 메뉴에서 연결되는 경로)
    @GetMapping("/trade-hub")
    public String tradeHub(Model model) {
        return chart(null, null, model);
    }

    // jhk측에서 작업 중 쓰던 경로도 그대로 유지 (같은 화면)

    @GetMapping("/hub/chart")
    public String chart(@RequestParam(required = false) String code,
                         @RequestParam(required = false) String period,
                         Model model) {
        // code/period가 없으면 기본값으로 대체
        String resolvedCode = (code != null && !code.isBlank()) ? code : DEFAULT_CODE;
        String resolvedPeriod = (period != null && !period.isBlank()) ? period : DEFAULT_PERIOD;

        // 차트는 TradingView 위젯이 자체 시세로 그리므로 서버에서 별도로 조회할 데이터가 없음
        model.addAttribute("resolvedCode", resolvedCode);
        model.addAttribute("resolvedPeriod", resolvedPeriod);
        return "hub/chart";
    }
}
