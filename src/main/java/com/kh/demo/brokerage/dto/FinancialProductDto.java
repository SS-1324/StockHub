package com.kh.demo.brokerage.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/*
*   FinancialProductDto : financial_product 테이블과 1:1로 대응되는 클래스
*
*   stock(주식)과 달리 증권사마다 갈리는 전용 상품(펀드/채권/ELS). F-BNK-01-01 "가상 증권사 상품 모아보기"용.
* */

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class FinancialProductDto {

    private Long productId;          // 금융상품 번호(PK)
    private Long brokerageId;        // 판매 증권사(FK)
    private String brokerageName;    // 조회 편의를 위한 조인 컬럼 (증권사별 비교 화면에서 바로 사용)
    private String productType;      // 상품유형(FUND/BOND/ELS)
    private String productName;      // 상품명
    private String description;      // 상품 설명
    private BigDecimal nav;          // 기준가 또는 평가금액
    private LocalDate maturityDate;  // 만기일 (없을 수 있음)
    private LocalDateTime launchDate; // 판매 개시일시
}
