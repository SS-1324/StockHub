package com.kh.demo.brokerage.dto;

import lombok.*;

import java.time.LocalDateTime;

/*
*   StockPriceHistoryDto : stock_price_history 테이블과 1:1로 대응되는 클래스
*
*   종목별 일자별(데모 생성기 기준으로는 주 단위) 시세 이력. 계좌마다 따로가 아니라
*   종목 하나에 시세 흐름 하나만 존재한다 - 실제 시장가가 계좌별로 다를 수 없는 것과 같다.
* */

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class StockPriceHistoryDto {

    private Long historyId;            // 이력 번호(PK)
    private String stockCode;          // 종목 코드
    private Long price;                // 그 시점 가격
    private LocalDateTime recordedAt;  // 기록 시점
}
