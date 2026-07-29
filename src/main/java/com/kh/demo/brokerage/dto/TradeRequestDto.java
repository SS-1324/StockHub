package com.kh.demo.brokerage.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/*
*   TradeRequestDto : 매수/매도 API 요청 바디로 들어오는 값
*
*   POST /api/accounts/{accountId}/trades 요청시 JSON body로 전달됨.
*   예) { "stockCode": "005930", "tradeType": "BUY", "quantity": 10 }
* */

@ToString
@Getter
@Setter
public class TradeRequestDto {
    private String stockCode; // 매매할 종목 코드
    private String tradeType; // 매수/매도 구분("BUY" / "SELL")
    private Long quantity;    // 거래수량
}
