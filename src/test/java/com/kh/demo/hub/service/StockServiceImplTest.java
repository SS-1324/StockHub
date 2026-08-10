package com.kh.demo.hub.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StockServiceImplTest {

    private final StockService stockService = new StockServiceImpl();

    @Test
    void resolveCodeKeepsGivenCode() {
        assertEquals("TSLA", stockService.resolveCode("TSLA"));
    }

    @Test
    void resolveCodeFallsBackToDefaultWhenMissing() {
        assertEquals("AAPL", stockService.resolveCode(null));
        assertEquals("AAPL", stockService.resolveCode(""));
        assertEquals("AAPL", stockService.resolveCode("   "));
    }

    @Test
    void resolvePeriodKeepsGivenPeriod() {
        assertEquals("week", stockService.resolvePeriod("week"));
    }

    @Test
    void resolvePeriodFallsBackToDefaultWhenMissing() {
        assertEquals("day", stockService.resolvePeriod(null));
        assertEquals("day", stockService.resolvePeriod(""));
        assertEquals("day", stockService.resolvePeriod("   "));
    }
}
