package com.kh.demo.hub.service;

import com.kh.demo.hub.dto.CandleDto;
import com.kh.demo.hub.dto.StockRankingDto;
import com.kh.demo.hub.dto.TossCandleResponseDto;
import com.kh.demo.hub.dto.TossRankingResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class StockServiceImpl implements StockService {

    // 토스 랭킹 응답에는 종목명이 없어서 화면 표시용으로 알고 있는 종목만 이름을 붙여줌
    private static final Map<String, String> KNOWN_NAMES = Map.of(
            "005930", "삼성전자",
            "000660", "SK하이닉스",
            "035420", "NAVER",
            "035720", "카카오",
            "005380", "현대차",
            "373220", "LG에너지솔루션",
            "005490", "POSCO홀딩스"
    );

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

    @Override
    public List<StockRankingDto> getTopGainers(int count) {
        TossRankingResponseDto response = restClient.get()
                .uri("/api/v1/rankings?type=TOP_GAINERS&marketCountry=KR&duration=1d&count={count}", count)
                .retrieve()
                .body(TossRankingResponseDto.class);

        return response.result().rankings().stream()
                .map(item -> new StockRankingDto(
                        item.rank(),
                        item.symbol(),
                        KNOWN_NAMES.getOrDefault(item.symbol(), item.symbol()),
                        Double.parseDouble(item.price().lastPrice()),
                        Double.parseDouble(item.price().changeRate()) * 100))
                .toList();
    }
}