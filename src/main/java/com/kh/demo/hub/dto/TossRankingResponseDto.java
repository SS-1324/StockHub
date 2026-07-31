package com.kh.demo.hub.dto;

import java.util.List;

public record TossRankingResponseDto(Result result) {

    public record Result(String rankedAt, List<Item> rankings) {
    }

    public record Item(int rank, String symbol, String currency, Price price,
                        String tradingVolume, String tradingAmount) {
    }

    public record Price(String lastPrice, String basePrice, String changeRate) {
    }
}
