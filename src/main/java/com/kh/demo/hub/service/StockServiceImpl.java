package com.kh.demo.hub.service;

import com.kh.demo.hub.dto.CandleDto;
import com.kh.demo.hub.dto.TossCandleResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Comparator;
import java.util.List;

@Service
public class StockServiceImpl implements StockService {

    private final RestClient restClient;

    public StockServiceImpl(@Value("${toss.api.base-url}") String baseUrl,
                             @Value("${toss.api.token}") String token) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + token)
                .build();
    }

    @Override
    public List<CandleDto> getCandles(String code) {
        TossCandleResponseDTO response = restClient.get()
                .uri("/api/v1/candles?symbol={code}&interval=1d", code)
                .retrieve()
                .body(TossCandleResponseDTO.class);

        return response.result().candles().stream()
                .map(c -> new CandleDto(
                        c.timestamp().substring(0, 10), // "2026-07-30T00:00..." -> "2026-07-30"
                        Double.parseDouble(c.openPrice()),
                        Double.parseDouble(c.highPrice()),
                        Double.parseDouble(c.lowPrice()),
                        Double.parseDouble(c.closePrice())))
                .sorted(Comparator.comparing(CandleDto::time)) // 토스는 최신순이라 오름차순 재정렬
                .toList();
    }
}
