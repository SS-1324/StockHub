package com.kh.demo.brokerage.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

// 내 주식 화면에 표시할 종목별 통합 보유 현황
@Getter
@Setter
public class MyStockHoldingDto {

    private String stockCode;
    private String stockName;
    private String exchange;   // 거래소 코드(NASDAQ/NYSE 등). 국내 종목은 null
    private Long quantity;
    private BigDecimal avgPrice;
    private Long currentPrice;
    private Long purchaseAmount;
    private Long currentValue;
    private Long profitAmount;
    private BigDecimal returnRate;
}
