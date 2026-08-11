package com.kh.demo.hub.controller;

import com.kh.demo.brokerage.dto.StockDto;
import com.kh.demo.common.dto.ApiResponse;
import com.kh.demo.hub.service.StockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockSearchControllerTest {

    @Mock
    private StockService stockService;

    private StockSearchController stockSearchController;

    @BeforeEach
    void setUp() {
        stockSearchController = new StockSearchController(stockService);
    }

    @Test
    void searchReturnsEmptyListWithoutQueryingWhenKeywordBlank() {
        ApiResponse<List<StockDto>> response = stockSearchController.search("   ");

        assertTrue(response.getData().isEmpty());
        verify(stockService, never()).searchStocks(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void searchDelegatesTrimmedKeywordToService() {
        List<StockDto> results = List.of(new StockDto());
        when(stockService.searchStocks("AAPL")).thenReturn(results);

        ApiResponse<List<StockDto>> response = stockSearchController.search("  AAPL  ");

        assertSame(results, response.getData());
    }
}
