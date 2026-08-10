package com.kh.demo.hub.controller;

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
        // 차트는 TradingView 위젯이 자체 시세로 그리므로 서버에서 별도로 조회할 데이터가 없음
        model.addAttribute("resolvedCode", stockService.resolveCode(code));
        model.addAttribute("resolvedPeriod", stockService.resolvePeriod(period));
        return "hub/chart";
    }
}
