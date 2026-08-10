package com.kh.demo.hub.controller;

import com.kh.demo.hub.service.StockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;

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

    @Test
    void chartResolvesCodeAndPeriodThroughService() {
        when(stockService.resolveCode("TSLA")).thenReturn("TSLA");
        when(stockService.resolvePeriod("week")).thenReturn("week");
        ExtendedModelMap model = new ExtendedModelMap();

        String viewName = stockController.chart("TSLA", "week", model);

        assertEquals("hub/chart", viewName);
        assertEquals("TSLA", model.get("resolvedCode"));
        assertEquals("week", model.get("resolvedPeriod"));
    }

    @Test
    void tradeHubDelegatesToChartWithNoParams() {
        when(stockService.resolveCode(null)).thenReturn("AAPL");
        when(stockService.resolvePeriod(null)).thenReturn("day");
        ExtendedModelMap model = new ExtendedModelMap();

        String viewName = stockController.tradeHub(model);

        assertEquals("hub/chart", viewName);
        assertEquals("AAPL", model.get("resolvedCode"));
        assertEquals("day", model.get("resolvedPeriod"));
    }
}
