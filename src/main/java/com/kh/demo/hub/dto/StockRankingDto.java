package com.kh.demo.hub.dto;

// 화면에 표시할 종목 랭킹 한 줄 (changeRate는 퍼센트 값, 예: 1.23 = +1.23%)
public record StockRankingDto(int rank, String code, String name, double price, double changeRate) {
}
