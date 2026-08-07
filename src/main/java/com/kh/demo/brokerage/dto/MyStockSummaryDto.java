package com.kh.demo.brokerage.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

// 로그인 회원의 전체 계좌와 보유 종목을 합산한 내 주식 요약
@Getter
@Setter
public class MyStockSummaryDto {

    private List<MyStockHoldingDto> holdings;
    private Long totalStockQuantity;
    private BigDecimal returnRate;
    private Long profitAmount;
    private Long currentBalance;
    private Long totalPurchaseAmount; // 대시보드에서 상품 매입금액과 합산해 전체 수익률을 계산할 때 사용
}
