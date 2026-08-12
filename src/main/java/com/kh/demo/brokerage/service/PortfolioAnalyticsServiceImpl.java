package com.kh.demo.brokerage.service;

import com.kh.demo.brokerage.dto.*;
import com.kh.demo.brokerage.mapper.AssetSnapshotMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

@Service
public class PortfolioAnalyticsServiceImpl implements PortfolioAnalyticsService {

    @Autowired
    private AssetSnapshotMapper assetSnapshotMapper;

    @Autowired
    private RealizedProfitService realizedProfitService;

    @Override
    public PortfolioAnalyticsDto getMyPortfolioAnalytics(String memberId, MyStockSummaryDto stockSummary) {
        List<AssetSnapshotDto> assetTrend = assetSnapshotMapper.selectAssetTrendByMember(memberId);

        // 청산이 끝난 주식 매매만 - 승률/보유기간/최고·최악은 결과가 확정된 거래에만 의미가 있다
        List<RealizedProfitDto> closedStockTrades = realizedProfitService.getMyRealizedProfits(memberId).stream()
                .filter(r -> "STOCK".equals(r.getItemType()))
                .toList();

        int closedTradeCount = closedStockTrades.size();
        BigDecimal winRate = BigDecimal.ZERO;
        long avgHoldingDays = 0;
        RealizedProfitDto bestTrade = null;
        RealizedProfitDto worstTrade = null;

        if (closedTradeCount > 0) {
            long winCount = closedStockTrades.stream().filter(r -> r.getProfitAmount() > 0).count();
            winRate = BigDecimal.valueOf(winCount * 100).divide(BigDecimal.valueOf(closedTradeCount), 1, RoundingMode.HALF_UP);
            avgHoldingDays = Math.round(closedStockTrades.stream().mapToLong(RealizedProfitDto::getHoldingDays).average().orElse(0));
            bestTrade = closedStockTrades.stream().max(Comparator.comparingLong(RealizedProfitDto::getProfitAmount)).orElse(null);
            worstTrade = closedStockTrades.stream().min(Comparator.comparingLong(RealizedProfitDto::getProfitAmount)).orElse(null);
        }

        List<MyStockHoldingDto> holdings = stockSummary.getHoldings();
        String topHoldingName = null;
        BigDecimal concentrationRate = BigDecimal.ZERO;
        long domesticStockValue = 0;
        long foreignStockValue = 0;

        if (holdings != null && !holdings.isEmpty()) {
            long totalStockValue = holdings.stream().mapToLong(h -> h.getCurrentValue() == null ? 0L : h.getCurrentValue()).sum();
            MyStockHoldingDto top = holdings.stream()
                    .max(Comparator.comparingLong(h -> h.getCurrentValue() == null ? 0L : h.getCurrentValue()))
                    .orElse(null);
            if (top != null) {
                topHoldingName = top.getStockName();
                long topValue = top.getCurrentValue() == null ? 0L : top.getCurrentValue();
                concentrationRate = totalStockValue <= 0
                        ? BigDecimal.ZERO
                        : BigDecimal.valueOf(topValue * 100).divide(BigDecimal.valueOf(totalStockValue), 1, RoundingMode.HALF_UP);
            }
            for (MyStockHoldingDto h : holdings) {
                long value = h.getCurrentValue() == null ? 0L : h.getCurrentValue();
                if (h.getExchange() == null) {
                    domesticStockValue += value;
                } else {
                    foreignStockValue += value;
                }
            }
        }

        return new PortfolioAnalyticsDto(assetTrend, closedTradeCount, winRate, avgHoldingDays, bestTrade, worstTrade,
                topHoldingName, concentrationRate, domesticStockValue, foreignStockValue);
    }
}
