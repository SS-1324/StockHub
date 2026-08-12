package com.kh.demo.brokerage.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/*
*   MyProductHoldingAccountDto : 같은 상품이 여러 증권사에 나뉘어 있을 때, 증권사(계좌) 하나의 몫.
*   MyProductHoldingDto(상품 기준 합산)를 펼쳤을 때 보여줄 세부 줄이다.
* */
@Getter
@Setter
public class MyProductHoldingAccountDto {

    private Long productId;
    private Long accountId;
    private String brokerageName;
    private BigDecimal quantity;
    private BigDecimal avgNav;
    private BigDecimal currentNav;
    private Long currentValue;
    private Long profitAmount;
    private BigDecimal returnRate;
}
