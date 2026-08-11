package com.kh.demo.hub.service;

import com.kh.demo.brokerage.dto.StockDto;
import com.kh.demo.brokerage.mapper.StockMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockServiceImplTest {

    @Mock
    private StockMapper stockMapper;

    private StockService stockService;

    @BeforeEach
    void setUp() {
        stockService = new StockServiceImpl(stockMapper);
    }

    private StockDto stock(String code, String name, String exchange) {
        StockDto stock = new StockDto();
        stock.setStockCode(code);
        stock.setStockName(name);
        stock.setExchange(exchange);
        return stock;
    }

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

    @Test
    void findStockDelegatesToMapper() {
        StockDto apple = stock("AAPL", "Apple", "NASDAQ");
        when(stockMapper.selectStockByCode("AAPL")).thenReturn(apple);

        assertSame(apple, stockService.findStock("AAPL"));
    }

    @Test
    void findStockReturnsNullWhenNotFound() {
        when(stockMapper.selectStockByCode("XXXX")).thenReturn(null);

        assertNull(stockService.findStock("XXXX"));
    }

    @Test
    void searchStocksDelegatesToMapper() {
        List<StockDto> results = List.of(stock("JPM", "JPMorgan Chase", "NYSE"));
        when(stockMapper.searchStocks("JP")).thenReturn(results);

        assertSame(results, stockService.searchStocks("JP"));
    }

    @Test
    void resolveQuickSwitchStocksLooksUpAllFiveCodesAndSkipsMissing() {
        when(stockMapper.selectStockByCode("AAPL")).thenReturn(stock("AAPL", "Apple", "NASDAQ"));
        when(stockMapper.selectStockByCode("MSFT")).thenReturn(stock("MSFT", "Microsoft", "NASDAQ"));
        when(stockMapper.selectStockByCode("NVDA")).thenReturn(stock("NVDA", "NVIDIA", "NASDAQ"));
        when(stockMapper.selectStockByCode("TSLA")).thenReturn(null);
        when(stockMapper.selectStockByCode("GOOGL")).thenReturn(stock("GOOGL", "Alphabet", "NASDAQ"));

        List<StockDto> result = stockService.resolveQuickSwitchStocks();

        assertEquals(4, result.size());
    }
}
