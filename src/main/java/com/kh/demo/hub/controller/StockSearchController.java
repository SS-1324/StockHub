package com.kh.demo.hub.controller;

import com.kh.demo.brokerage.dto.StockDto;
import com.kh.demo.common.dto.ApiResponse;
import com.kh.demo.hub.service.StockService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// 거래 허브 차트 헤더의 종목 검색 자동완성 API. 국내(KRX) 종목은 검색 대상에서 제외됨(StockMapper.searchStocks 참고)
@RestController
@RequestMapping("/api/hub")
public class StockSearchController {

    private final StockService stockService;

    public StockSearchController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping("/stock-search")
    public ApiResponse<List<StockDto>> search(@RequestParam(defaultValue = "") String keyword) {
        String trimmed = keyword.trim();
        if (trimmed.isBlank()) {
            return ApiResponse.success(List.of());
        }
        return ApiResponse.success(stockService.searchStocks(trimmed));
    }
}
