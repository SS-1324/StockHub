package com.kh.demo.hub.controller;

import com.kh.demo.hub.dto.CandleDto;
import com.kh.demo.hub.service.StockService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/api/stock")
@RestController
public class StockApiController {

    private final StockService stockService;

    public StockApiController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping("/{code}/chart")
    public List<CandleDto> getChart(@PathVariable String code) {
        return stockService.getCandles(code); // 실제 로직은 Service가 담당
    }
}
