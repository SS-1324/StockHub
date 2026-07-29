package com.kh.demo.brokerage.dto;

import lombok.*;

import java.time.LocalDateTime;

/*
*   TradeDto : trade 테이블과 1:1로 대응되는 클래스
*
*   "내 거래 정보" 기능, 랭킹보드의 거래 히스토리 기능 등
*   다른 팀원들의 기능이 그대로 소비하게 될 핵심 데이터.
* */

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TradeDto {

    private Long tradeId;          // 거래내역 번호(PK)
    private Long accountId;        // 가상 계좌 번호
    private String stockCode;      // 종목 코드
    private String stockName;      // 조회 편의를 위한 조인 컬럼
    private String tradeType;      // 매수/매도 구분("BUY" / "SELL")
    private Long quantity;         // 거래수량
    private Integer price;         // 체결단가
    private Integer fee;           // 체결시점 수수료(스냅샷)
    private LocalDateTime tradeAt; // 체결일시
}
