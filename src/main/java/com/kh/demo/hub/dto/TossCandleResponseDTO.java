package com.kh.demo.hub.dto;

import java.util.List;

public record TossCandleResponseDTO(Result result) {

    public record Result(List<TossCandle> candles) {
    }

    public record TossCandle(String timestamp, String openPrice, String highPrice,
                              String lowPrice, String closePrice) {
    }
}
