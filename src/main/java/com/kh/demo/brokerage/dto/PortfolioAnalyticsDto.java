package com.kh.demo.brokerage.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

/*
*   PortfolioAnalyticsDto : 대시보드 "포트폴리오 분석" 섹션에 필요한 통계를 한데 모은 것.
*   전부 이미 있는 데이터(자산 스냅샷·매매 손익 재현·보유 종목)를 조합해서 계산할 뿐,
*   새로 저장하는 값은 없다.
* */
@Getter
@AllArgsConstructor
public class PortfolioAnalyticsDto {

    private List<AssetSnapshotDto> assetTrend;     // 자산 성장 추이(주 단위)

    private int closedTradeCount;                  // 청산(매도) 완료된 주식 매매 건수
    private BigDecimal winRate;                     // 그중 이익으로 마감한 비율(%)
    private long avgHoldingDays;                    // 평균 보유 기간(일)
    private RealizedProfitDto bestTrade;            // 손익금 기준 최고의 매매 (없으면 null)
    private RealizedProfitDto worstTrade;           // 손익금 기준 최악의 매매 (없으면 null)

    private String topHoldingName;                  // 비중이 가장 큰 보유 종목명
    private BigDecimal concentrationRate;            // 그 종목이 주식 평가금액에서 차지하는 비중(%)

    private long domesticStockValue;                 // 국내 주식 평가금액 합계
    private long foreignStockValue;                  // 해외 주식 평가금액 합계
}
