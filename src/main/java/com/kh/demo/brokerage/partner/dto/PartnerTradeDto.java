package com.kh.demo.brokerage.partner.dto;

import lombok.*;

import java.time.LocalDateTime;

/*
*   PartnerTradeDto : 제휴 증권사 API가 "매매 체결"(조회 또는 주문 실행 응답)로 내려준다고 가정한 응답 모양.
*
*   PartnerProductTransactionDto/PartnerCashTransactionDto와 동일하게, 파트너사 자체 식별자
*   (partnerTradeId)와 partnerAccountNo 기준으로 필드를 구성했다.
* */

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PartnerTradeDto {

    private String partnerTradeId;   // 파트너사 거래 식별자 (예: "TRD-123")
    private String partnerAccountNo; // 파트너사 계좌번호
    private String stockCode;        // 종목 코드
    private String stockName;        // 조회 편의를 위한 조인 컬럼
    private String tradeType;        // 매수/매도 구분("BUY" / "SELL")
    private Long quantity;           // 거래수량
    private Integer price;           // 체결단가
    private Integer fee;             // 체결시점 수수료(스냅샷)
    private LocalDateTime tradeAt;   // 체결일시
}
