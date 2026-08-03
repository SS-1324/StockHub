package com.kh.demo.hub.dto;

import java.util.List;

// 화면에 표시할 종목 랭킹 한 줄 (changeRate는 퍼센트 값, 예: 1.23 = +1.23%)
// brokers: 이 종목을 매수/매도할 수 있는 증권사 바로가기 목록
public record StockRankingDto(int rank, String code, String name, double price, double changeRate,
                               List<BrokerLinkDto> brokers) {
}
