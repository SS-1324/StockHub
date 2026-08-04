package com.kh.demo.brokerage.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/*
*   ProductTransactionDto : product_transaction 테이블과 1:1로 대응되는 클래스
*
*   trade(주식 매수/매도)와 동일한 역할이지만 금융상품(펀드/채권/ELS)용 - 가입/환매 원장.
* */

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProductTransactionDto {

    private Long transactionId;        // 상품거래내역 번호(PK)
    private Long accountId;            // 가상 계좌 번호
    private Long productId;            // 금융상품 번호
    private String productName;        // 조회 편의를 위한 조인 컬럼
    private String transactionType;    // 가입/환매 구분("SUBSCRIBE" / "REDEEM")
    private BigDecimal quantity;       // 거래 좌수
    private BigDecimal nav;            // 거래 시점 기준가
    private Long amount;               // 거래금액
    private LocalDateTime transactionAt; // 거래일시
}
