package com.kh.demo.hub.service;

import com.kh.demo.hub.dto.BrokerLinkDto;
import com.kh.demo.hub.dto.CandleDto;
import com.kh.demo.hub.dto.StockRankingDto;
import com.kh.demo.hub.dto.TossCandleResponseDto;
import com.kh.demo.hub.dto.TossRankingResponseDto;
import com.kh.demo.hub.dto.TossStockInfoResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class StockServiceImpl implements StockService {

    // 화면에서 매수/매도 버튼으로 연결할 실제 증권사 목록 (feeRate는 참고용 예시 수치)
    private static final List<BrokerLinkDto> BROKERS = List.of(
            new BrokerLinkDto("키움증권", 0.015, "https://www.kiwoom.com"),
            new BrokerLinkDto("NH투자증권", 0.018, "https://www.nhqv.com"),
            new BrokerLinkDto("KB증권", 0.025, "https://www.kbsec.com"),
            new BrokerLinkDto("삼성증권", 0.020, "https://www.samsungpop.com"),
            new BrokerLinkDto("미래에셋증권", 0.014, "https://securities.miraeasset.com"),
            new BrokerLinkDto("토스증권", 0.000, "https://tossinvest.com"),
            new BrokerLinkDto("한국투자증권", 0.015, "https://www.truefriend.com")
    );

    private static final int BROKERS_PER_STOCK = 3;

    // 종목 코드마다 항상 같은 조합이 보이도록 코드값을 시드로 고정 셔플
    private static List<BrokerLinkDto> pickBrokers(String code) {
        List<BrokerLinkDto> shuffled = new ArrayList<>(BROKERS);
        Collections.shuffle(shuffled, new Random(code.hashCode()));
        return shuffled.subList(0, BROKERS_PER_STOCK);
    }

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

        List<TossRankingResponseDto.Item> rankings = response.result().rankings();
        Map<String, String> namesBySymbol = getStockNames(rankings.stream()
                .map(TossRankingResponseDto.Item::symbol)
                .toList());

        return rankings.stream()
                .map(item -> new StockRankingDto(
                        item.rank(),
                        item.symbol(),
                        namesBySymbol.getOrDefault(item.symbol(), item.symbol()),
                        Double.parseDouble(item.price().lastPrice()),
                        Double.parseDouble(item.price().changeRate()) * 100,
                        pickBrokers(item.symbol())))
                .toList();
    }

    // 종목 코드로 실제 종목명을 조회 (랭킹 API 응답에는 이름이 없어서 별도 조회 필요)
    private Map<String, String> getStockNames(List<String> symbols) {
        TossStockInfoResponseDto response = restClient.get()
                .uri("/api/v1/stocks?symbols={symbols}", String.join(",", symbols))
                .retrieve()
                .body(TossStockInfoResponseDto.class);

        return response.result().stream()
                .collect(Collectors.toMap(TossStockInfoResponseDto.Item::symbol,
                        TossStockInfoResponseDto.Item::name));
    }
}