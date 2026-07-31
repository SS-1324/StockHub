package com.kh.demo.hub.dto;

import java.util.List;

public record TossCandleResponseDTO(Result result) {

    public record Result(List<TossCandleDto> candles) {
    }

    public record TossCandleDto(String timestamp, String openPrice, String highPrice,
                              String lowPrice, String closePrice) {
    }
}
