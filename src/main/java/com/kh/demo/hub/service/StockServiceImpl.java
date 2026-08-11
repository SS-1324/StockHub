package com.kh.demo.hub.service;

import com.kh.demo.brokerage.dto.StockDto;
import com.kh.demo.brokerage.mapper.StockMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class StockServiceImpl implements StockService {

    private static final String DEFAULT_CODE = "AAPL";
    private static final String DEFAULT_PERIOD = "day";

    // 차트 헤더의 빠른 종목 전환 버튼에 노출할 대표 5개 종목.
    // tradingview-chart.js의 QUICK_SWITCH_CODES와 반드시 동일하게 유지해야 함
    private static final List<String> QUICK_SWITCH_CODES = List.of("AAPL", "MSFT", "NVDA", "TSLA", "GOOGL");

    private final StockMapper stockMapper;

    public StockServiceImpl(StockMapper stockMapper) {
        this.stockMapper = stockMapper;
    }

    @Override
    public String resolveCode(String code) {
        return (code != null && !code.isBlank()) ? code : DEFAULT_CODE;
    }

    @Override
    public String resolvePeriod(String period) {
        return (period != null && !period.isBlank()) ? period : DEFAULT_PERIOD;
    }

    @Override
    public StockDto findStock(String code) {
        return stockMapper.selectStockByCode(code);
    }

    @Override
    public List<StockDto> searchStocks(String keyword) {
        return stockMapper.searchStocks(keyword);
    }

    @Override
    public List<StockDto> resolveQuickSwitchStocks() {
        return QUICK_SWITCH_CODES.stream()
                .map(stockMapper::selectStockByCode)
                .filter(Objects::nonNull)
                .toList();
    }
}
