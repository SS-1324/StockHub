package com.kh.demo.hub.controller;

import com.kh.demo.brokerage.dto.StockDto;
import com.kh.demo.hub.service.StockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockControllerTest {

    @Mock
    private StockService stockService;

    private StockController stockController;

    @BeforeEach
    void setUp() {
        stockController = new StockController(stockService);
    }

    private StockDto stock(String code, String exchange) {
        StockDto stock = new StockDto();
        stock.setStockCode(code);
        stock.setExchange(exchange);
        return stock;
    }

    @Test
    void chartResolvesCodePeriodAndExchangeThroughService() {
        when(stockService.resolveCode("TSLA")).thenReturn("TSLA");
        when(stockService.resolvePeriod("week")).thenReturn("week");
        when(stockService.findStock("TSLA")).thenReturn(stock("TSLA", "NASDAQ"));
        ExtendedModelMap model = new ExtendedModelMap();

        String viewName = stockController.chart("TSLA", "week", model);

        assertEquals("hub/chart", viewName);
        assertEquals("TSLA", model.get("resolvedCode"));
        assertEquals("week", model.get("resolvedPeriod"));
        assertEquals("NASDAQ", model.get("resolvedExchange"));
    }

    @Test
    void chartFallsBackToEmptyExchangeWhenStockNotFound() {
        when(stockService.resolveCode("XXXX")).thenReturn("XXXX");
        when(stockService.findStock("XXXX")).thenReturn(null);
        ExtendedModelMap model = new ExtendedModelMap();

        stockController.chart("XXXX", null, model);

        assertEquals("", model.get("resolvedExchange"));
    }

    @Test
    void chartPassesQuickSwitchStocksToModel() {
        List<StockDto> quickSwitch = List.of(stock("AAPL", "NASDAQ"));
        when(stockService.resolveQuickSwitchStocks()).thenReturn(quickSwitch);
        ExtendedModelMap model = new ExtendedModelMap();

        stockController.chart(null, null, model);

        assertEquals(quickSwitch, model.get("quickSwitchStocks"));
    }

    @Test
    void tradeHubDelegatesToChartWithNoParams() {
        when(stockService.resolveCode(null)).thenReturn("AAPL");
        when(stockService.resolvePeriod(null)).thenReturn("day");
        ExtendedModelMap model = new ExtendedModelMap();

        String viewName = stockController.tradeHub(null, null, model);

        assertEquals("hub/chart", viewName);
        assertEquals("AAPL", model.get("resolvedCode"));
        assertEquals("day", model.get("resolvedPeriod"));
    }

    @Test
    void tradeHubForwardsCodeAndPeriodQueryParamsToChart() {
        // 회귀 방지: tradeHub()가 자기 쿼리파라미터를 무시하고 항상 chart(null, null, ...)을
        // 호출해서, 검색/빠른전환으로 바뀐 종목을 새로고침하면 항상 AAPL로 되돌아가던 버그가 있었음
        when(stockService.resolveCode("JPM")).thenReturn("JPM");
        when(stockService.resolvePeriod("week")).thenReturn("week");
        when(stockService.findStock("JPM")).thenReturn(stock("JPM", "NYSE"));
        ExtendedModelMap model = new ExtendedModelMap();

        stockController.tradeHub("JPM", "week", model);

        assertEquals("JPM", model.get("resolvedCode"));
        assertEquals("NYSE", model.get("resolvedExchange"));
        assertEquals("week", model.get("resolvedPeriod"));
    }
}
