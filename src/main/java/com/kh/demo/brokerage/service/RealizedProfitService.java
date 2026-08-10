package com.kh.demo.brokerage.service;

import com.kh.demo.brokerage.dto.PeriodProfitDto;
import com.kh.demo.brokerage.dto.RealizedProfitDto;

import java.util.List;

public interface RealizedProfitService {

    // "언제 사서 얼마에 팔아 얼마 벌었나" 매매 손익 내역 (최신순), 주식+상품 통합
    List<RealizedProfitDto> getMyRealizedProfits(String memberId);

    // 기간별(1주/1달/1년/전체) 손익.
    // week/month/year = (지금 총자산 - 그 시점 총자산 스냅샷) - 그 기간 순입금액  (실제 증권사와 동일한 방식)
    // all = 지금 총자산 - 총 투자원금(전체 기간 순입금액)
    PeriodProfitDto getMyPeriodProfit(String memberId, long currentTotalAsset);
}
