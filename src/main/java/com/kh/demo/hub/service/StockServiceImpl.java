package com.kh.demo.hub.service;

import com.kh.demo.hub.dto.CandleDto;
import com.kh.demo.hub.dto.TossCandleResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Comparator;
import java.util.List;

@Service
public class StockServiceImpl implements StockService {

    private final RestClient restClient;

    public StockServiceImpl(@Value("${toss.api.base-url}") String baseUrl,
                            TossAuthService tossAuthService) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestInterceptor((request, body, execution) -> {
                    request.getHeaders().setBearerAuth(tossAuthService.getAccessToken());
                    return execution.execute(request, body);
                })
                .build();
    }

    @Override
    public List<CandleDto> getCandles(String code) {
        TossCandleResponseDto response = restClient.get()
                .uri("/api/v1/candles?symbol={code}&interval=1d", code)
                .retrieve()
                .body(TossCandleResponseDto.class);

        return response.result().candles().stream()
                .map(c -> new CandleDto(
                        c.timestamp().substring(0, 10),
                        Double.parseDouble(c.openPrice()),
                        Double.parseDouble(c.highPrice()),
                        Double.parseDouble(c.lowPrice()),
                        Double.parseDouble(c.closePrice())))
                .sorted(Comparator.comparing(CandleDto::time))
                .toList();
    }
}