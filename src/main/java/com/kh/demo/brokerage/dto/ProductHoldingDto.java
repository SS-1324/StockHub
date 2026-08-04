package com.kh.demo.brokerage.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/*
*   ProductHoldingDto : product_holding 테이블과 1:1로 대응되는 클래스
*
*   holding(주식)과 동일한 역할이지만 금융상품(펀드/채권/ELS)용 - 계좌별 "현재" 보유 스냅샷.
*   좌수(quantity)/기준가(avgNav)는 소수 단위라 stock 쪽과 달리 BigDecimal을 쓴다.
* */

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProductHoldingDto {

    private Long productHoldingId;     // 상품보유 번호(PK)
    private Long accountId;            // 가상 계좌 번호
    private Long productId;            // 금융상품 번호
    private String productName;        // 조회 편의를 위한 조인 컬럼
    private String productType;        // 조회 편의를 위한 조인 컬럼(FUND/BOND/ELS)
    private BigDecimal quantity;       // 보유 좌수
    private BigDecimal avgNav;         // 평균 매입 기준가
    private Long purchaseAmount;       // 누적 매입원금
    private LocalDateTime updateAt;    // 최종 갱신일시
}
