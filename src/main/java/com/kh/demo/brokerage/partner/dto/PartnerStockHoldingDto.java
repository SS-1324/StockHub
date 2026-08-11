package com.kh.demo.brokerage.partner.dto;

import lombok.*;

import java.time.LocalDateTime;

/*
*   PartnerStockHoldingDto : 제휴 증권사 API가 "계좌의 주식 보유내역"으로 내려준다고 가정한 응답 모양.
*
*   PartnerHoldingDto(상품 보유내역)와 대응되는 주식 버전. 우리 내부 PK(holdingId) 대신
*   partnerAccountNo 기준으로 필드를 구성했다.
* */

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PartnerStockHoldingDto {

    private String partnerAccountNo; // 파트너사 계좌번호
    private String stockCode;        // 종목 코드
    private String stockName;        // 조회 편의를 위한 조인 컬럼
    private Long quantity;           // 보유수량
    private Integer avgPrice;        // 평균매입단가
    private LocalDateTime asOf;      // 이 보유내역의 기준 시각(파트너사 스냅샷 시각)
}
