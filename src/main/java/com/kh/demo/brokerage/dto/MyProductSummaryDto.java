package com.kh.demo.brokerage.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

// 로그인 회원의 전체 계좌를 합산한 내 상품(펀드/채권/ELS) 요약 (MyStockSummaryDto의 상품 버전)
@Getter
@Setter
public class MyProductSummaryDto {

    private List<MyProductHoldingDto> holdings;
    private Long totalPurchaseAmount;
    private Long totalCurrentValue;
    private Long profitAmount;
    private BigDecimal returnRate;
}
