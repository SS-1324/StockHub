package com.kh.demo.hub.dto;

import java.util.List;

// GET /api/v1/stocks?symbols=... 응답 (종목 코드로 실제 종목명을 조회)
public record TossStockInfoResponseDto(List<Item> result) {

    public record Item(String symbol, String name) {
    }
}
