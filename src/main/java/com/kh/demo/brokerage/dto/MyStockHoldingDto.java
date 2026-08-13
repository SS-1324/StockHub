package com.kh.demo.brokerage.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

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

    // 이 종목을 보유한 증권사가 2곳 이상일 때만 채워진다(1곳뿐이면 굳이 펼쳐볼 이유가 없어 null로 둔다)
    private List<MyStockHoldingAccountDto> accountBreakdown;
}
