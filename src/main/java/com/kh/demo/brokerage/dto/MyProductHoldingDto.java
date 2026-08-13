package com.kh.demo.brokerage.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

// 로그인 회원의 모든 계좌를 상품별로 합산한 내 상품 보유 현황 (대시보드용, MyStockHoldingDto의 상품 버전)
@Getter
@Setter
public class MyProductHoldingDto {

    private Long productId;
    private String productName;
    private String productType;
    private BigDecimal quantity;
    private BigDecimal avgNav;
    private BigDecimal currentNav;
    private Long purchaseAmount;
    private Long currentValue;
    private Long profitAmount;
    private BigDecimal returnRate;

    // 이 상품을 보유한 증권사가 2곳 이상일 때만 채워진다
    private List<MyProductHoldingAccountDto> accountBreakdown;
}
