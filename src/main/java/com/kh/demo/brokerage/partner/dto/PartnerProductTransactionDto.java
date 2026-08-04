package com.kh.demo.brokerage.partner.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/*
*   PartnerProductTransactionDto : 제휴 증권사 API가 "상품 가입/환매 이력"으로 내려준다고 가정한 응답 모양.
*
*   partnerTransactionId는 파트너사 쪽 원장 번호 - 우리 PK와 별개로, 실제 제휴였다면
*   대사(reconciliation)할 때 이 값으로 우리 기록과 매칭했을 식별자.
* */

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PartnerProductTransactionDto {

    private String partnerTransactionId; // 파트너사측 거래 식별자
    private String partnerAccountNo;     // 파트너사 계좌번호
    private Long productId;
    private String productName;
    private String transactionType;      // "SUBSCRIBE" / "REDEEM"
    private BigDecimal quantity;
    private BigDecimal nav;
    private Long amount;
    private LocalDateTime settledAt;     // 파트너사측 체결(정산) 시각
}
