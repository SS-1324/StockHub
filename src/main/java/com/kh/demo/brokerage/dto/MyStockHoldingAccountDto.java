package com.kh.demo.brokerage.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/*
*   MyStockHoldingAccountDto : 같은 종목이 여러 증권사에 나뉘어 있을 때, 증권사(계좌) 하나의 몫.
*   MyStockHoldingDto(종목 기준 합산)를 펼쳤을 때 보여줄 세부 줄이다.
* */
@Getter
@Setter
public class MyStockHoldingAccountDto {

    private String stockCode;
    private Long accountId;
    private String brokerageName;
    private Long quantity;
    private BigDecimal avgPrice;
    private Long currentPrice;
    private Long currentValue;
    private Long profitAmount;
    private BigDecimal returnRate;
}
