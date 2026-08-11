package com.kh.demo.brokerage.partner.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/*
*   PartnerAccountDto : 제휴 증권사 API가 "계좌 실명조회/개설" 응답으로 내려준다고 가정한 응답 모양.
*
*   우리 내부 PK(accountId)는 파트너사가 알 수 없는 값이라 담지 않는다. 호출한 쪽이
*   partnerAccountNo로 내부 accountId를 다시 되짚어(resolveAccountId) 사용한다.
* */

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PartnerAccountDto {

    private String partnerAccountNo;   // 파트너사 계좌번호
    private String ownerName;          // 파트너사에 등록된 예금주명
    private Long balance;              // 계좌 잔고(예수금)
    private BigDecimal returnRate;     // 계좌 수익률
    private Long profitAmount;         // 계좌 수익금
    private Long holdingStockQuantity; // 계좌의 총 보유 주식 수량
    private LocalDateTime openedAt;    // 계좌 개설일(파트너사 기준)
}
