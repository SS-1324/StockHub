package com.kh.demo.brokerage.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/*
*   PeriodProfitDto : 대시보드 요약의 기간별 손익.
*
*   week/month/year = (지금 총자산 - 그 시점 총자산 스냅샷) - 그 기간 순입금액 (실제 증권사와 동일한 방식)
*   all             = 지금 총자산 - 총 투자원금
*   totalPrincipal  = 총 투자원금(처음부터 지금까지 순입금액) - 총수익률(=all/totalPrincipal*100) 계산에 사용
* */
@Getter
@AllArgsConstructor
public class PeriodProfitDto {
    private long week;
    private long month;
    private long year;
    private long all;
    private long totalPrincipal;
}
