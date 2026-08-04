package com.kh.demo.brokerage.dto;

import lombok.*;

import java.time.LocalDateTime;

/*
*   CashTransactionDto : cash_transaction 테이블과 1:1로 대응되는 클래스
*
*   계좌 입출금 원장. balanceAfter는 처리 후 잔고 스냅샷(명세서 조회시 매번 잔고를 역산하지 않도록 미리 저장).
* */

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CashTransactionDto {

    private Long cashTransactionId;    // 입출금내역 번호(PK)
    private Long accountId;            // 가상 계좌 번호
    private String transactionType;    // 입금/출금 구분("DEPOSIT" / "WITHDRAWAL")
    private Long amount;               // 입출금액
    private Long balanceAfter;         // 처리 후 잔고 스냅샷
    private String memo;               // 메모(예: 초기 입금, 생활비 출금)
    private LocalDateTime transactionAt; // 입출금일시
}
