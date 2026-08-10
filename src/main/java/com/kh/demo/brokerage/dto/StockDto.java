package com.kh.demo.brokerage.dto;

import lombok.*;

import java.time.LocalDateTime;

/*
*   StockDto : stock 테이블과 1:1로 대응되는 클래스
*
*   stockCode를 PK로 사용(자연키). 실제 증권사들이 '해외 잡주'가 아닌 이상 대부분
*   같은 주식을 취급하는 것처럼, 이 사이트의 주식도 특정 증권사에 종속되지 않는 범용 자산이다.
*   (증권사마다 갈리는 상품은 FinancialProductDto(펀드/채권/ELS) 쪽에서 다룸)
* */

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class StockDto {

    private String stockCode;       // 종목 코드(PK, 예: NVDA)
    private String stockName;       // 종목 이름
    private Integer currentPrice;   // 현재가
    private String description;     // 기업정보 및 설명
    private LocalDateTime createAt; // 상장(등록)일시
    private String exchange;        // 거래소 코드(NASDAQ/NYSE 등). 국내 종목은 null — 검색/트레이딩뷰 심볼 조합 대상 아님
}
