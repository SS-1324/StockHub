package com.kh.demo.hub.controller;

import tools.jackson.databind.ObjectMapper;
import com.kh.demo.hub.dto.CandleDto;
import com.kh.demo.hub.service.StockService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class StockController {

    // 종목 코드가 없을 때 기본으로 보여줄 종목 (삼성전자)
    private static final String DEFAULT_CODE = "005930";

    private final StockService stockService;
    private final ObjectMapper objectMapper;

    public StockController(StockService stockService, ObjectMapper objectMapper) {
        this.stockService = stockService;
        this.objectMapper = objectMapper;
    }

    // 거래 허브 메인 (헤더 메뉴에서 연결되는 경로)
    @GetMapping("/trade-hub")
    public String tradeHub(Model model) {
        return chart(null, model);
    }

    // jhk측에서 작업 중 쓰던 경로도 그대로 유지 (같은 화면)
    @GetMapping("/hub/chart")
    public String chart(@RequestParam(required = false) String code, Model model) {
        // code가 없으면 기본 종목으로 대체
        String resolvedCode = (code != null && !code.isBlank()) ? code : DEFAULT_CODE;

        // 페이지 렌더링 시점에 캔들 데이터를 미리 조회해 뷰에 내려줌
        // -> 클라이언트가 첫 fetch를 기다리는 동안 빈 차트가 보이는 지연을 없앰
        List<CandleDto> candles = stockService.getCandles(resolvedCode);
        model.addAttribute("initialCandlesJson", objectMapper.writeValueAsString(candles));
        model.addAttribute("resolvedCode", resolvedCode);

        // 차트 아래에 카드로 보여줄 등락률 TOP 5
        model.addAttribute("topGainers", stockService.getTopGainers(5));
        return "hub/chart";
    }
}