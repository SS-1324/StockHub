package com.kh.demo.hub.controller;

import com.kh.demo.brokerage.dto.StockDto;
import com.kh.demo.hub.service.StockService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    // 거래 허브 메인 (헤더 메뉴에서 연결되는 경로).
    // code/period를 안 받고 무조건 chart(null, null, ...)을 호출하던 버그가 있었음 - 그러면
    // 빠른전환/검색으로 바뀐 종목이 history.replaceState로 이 경로의 ?code=에 반영돼도
    // 새로고침하거나 링크를 공유하면 항상 기본 종목(AAPL)으로 되돌아가버림
    @GetMapping("/trade-hub")
    public String tradeHub(@RequestParam(required = false) String code,
                            @RequestParam(required = false) String period,
                            Model model) {
        return chart(code, period, model);
    }

    // jhk측에서 작업 중 쓰던 경로도 그대로 유지 (같은 화면)

    @GetMapping("/hub/chart")
    public String chart(@RequestParam(required = false) String code,
                         @RequestParam(required = false) String period,
                         Model model) {
        // 차트는 TradingView 위젯이 자체 시세로 그리므로 서버에서 별도로 조회할 데이터가 없음
        String resolvedCode = stockService.resolveCode(code);
        StockDto resolvedStock = stockService.findStock(resolvedCode);

        model.addAttribute("resolvedCode", resolvedCode);
        // 트레이딩뷰 심볼(EXCHANGE:CODE) 조립용 - 종목을 못 찾으면 빈 문자열로 내려보내고
        // 프론트(tradingview-chart.js)가 기본 종목으로 안전하게 대체함
        model.addAttribute("resolvedExchange", resolvedStock != null ? resolvedStock.getExchange() : "");
        model.addAttribute("resolvedPeriod", stockService.resolvePeriod(period));
        // 빠른전환 버튼 5종목도 클릭마다 네트워크 왕복 없이 바로 트레이딩뷰 심볼을 조립할 수 있도록
        // 페이지 로드 시 거래소 정보까지 함께 내려줌
        model.addAttribute("quickSwitchStocks", stockService.resolveQuickSwitchStocks());
        return "hub/chart";
    }
}
