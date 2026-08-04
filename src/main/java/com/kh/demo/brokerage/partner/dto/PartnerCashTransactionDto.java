package com.kh.demo.brokerage.partner.dto;

import lombok.*;

import java.time.LocalDateTime;

/*
*   PartnerCashTransactionDto : 제휴 증권사 API가 "계좌 입출금 이력"으로 내려준다고 가정한 응답 모양.
* */

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PartnerCashTransactionDto {

    private String partnerTransactionId; // 파트너사측 거래 식별자
    private String partnerAccountNo;     // 파트너사 계좌번호
    private String transactionType;      // "DEPOSIT" / "WITHDRAWAL"
    private Long amount;
    private Long balanceAfter;
    private String memo;
    private LocalDateTime settledAt;
}
