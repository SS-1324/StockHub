package com.kh.demo.brokerage.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

/*
*   PeriodProfitDto : 대시보드 요약의 기간별 손익.
*
*   week/month/year = (지금 총자산 - 그 시점 총자산 스냅샷) - 그 기간 순입금액 (실제 증권사와 동일한 방식)
*   all             = 지금 총자산 - 총 투자원금
*   totalPrincipal  = 총 투자원금(처음부터 지금까지 순입금액) - allRate(=all/totalPrincipal*100) 계산에 사용
*
*   weekRate/monthRate/yearRate = 그 기간 손익을 "그 시점 총자산"(baseline) 대비 %로 나타낸 것.
*   allRate은 "전체"이므로 시작 자산이 없어 baseline 대신 총투자원금을 분모로 쓴다 - 실제 증권사와 동일.
* */
@Getter
@AllArgsConstructor
public class PeriodProfitDto {
    private long week;
    private long month;
    private long year;
    private long all;
    private long totalPrincipal;
    private BigDecimal weekRate;
    private BigDecimal monthRate;
    private BigDecimal yearRate;
    private BigDecimal allRate;
}
