package com.kh.demo.brokerage.service;

import com.kh.demo.brokerage.dto.MyStockSummaryDto;
import com.kh.demo.brokerage.dto.PortfolioAnalyticsDto;

public interface PortfolioAnalyticsService {

    // 자산 성장 추이 + 매매 승률/평균 보유기간/최고·최악의 매매 + 집중도 + 국내·해외 비중
    PortfolioAnalyticsDto getMyPortfolioAnalytics(String memberId, MyStockSummaryDto stockSummary);
}
