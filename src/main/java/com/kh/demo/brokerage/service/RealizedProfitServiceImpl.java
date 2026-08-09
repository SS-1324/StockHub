package com.kh.demo.brokerage.service;

import com.kh.demo.brokerage.dto.PeriodProfitDto;
import com.kh.demo.brokerage.dto.ProductTransactionDto;
import com.kh.demo.brokerage.dto.RealizedProfitDto;
import com.kh.demo.brokerage.dto.TradeDto;
import com.kh.demo.brokerage.mapper.AssetSnapshotMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RealizedProfitServiceImpl implements RealizedProfitService {

    @Autowired
    private TradeService tradeService;

    @Autowired
    private ProductTransactionService productTransactionService;

    @Autowired
    private CashTransactionService cashTransactionService;

    @Autowired
    private AssetSnapshotMapper assetSnapshotMapper;

    @Override
    public List<RealizedProfitDto> getMyRealizedProfits(String memberId) {
        List<RealizedProfitDto> result = new ArrayList<>();
        result.addAll(replayStockTrades(tradeService.getMyTrades(memberId)));
        result.addAll(replayProductTransactions(productTransactionService.getMyTransactions(memberId)));
        result.sort(Comparator.comparing(RealizedProfitDto::getSellAt).reversed());
        return result;
    }

    // 실제 증권사(예: 토스증권)와 동일한 방식: 기간손익 = (지금 총자산 - 그때 총자산) - 그 기간 순입금액
    // 이렇게 하면 실현손익뿐 아니라 계속 보유 중인 종목의 그 기간 중 가격 변동분까지 정확히 반영된다.
    @Override
    public PeriodProfitDto getMyPeriodProfit(String memberId, long currentTotalAsset) {
        LocalDate today = LocalDate.now();
        long week = computePeriodProfit(memberId, currentTotalAsset, today.minusWeeks(1));
        long month = computePeriodProfit(memberId, currentTotalAsset, today.minusMonths(1));
        long year = computePeriodProfit(memberId, currentTotalAsset, today.minusYears(1));

        // 총손익(전체) = 지금 총자산 - 총 투자원금(처음부터 지금까지 순입금액)
        long totalPrincipal = cashTransactionService.getMyNetDeposits(memberId, null);
        long all = currentTotalAsset - totalPrincipal;

        return new PeriodProfitDto(week, month, year, all, totalPrincipal);
    }

    private long computePeriodProfit(String memberId, long currentTotalAsset, LocalDate periodStart) {
        long baselineAsset = assetSnapshotMapper.selectTotalAssetAsOf(memberId, periodStart);
        // 기간 시작일이 아니라 baselineAsset이 실제로 반영된 스냅샷 날짜를 기준으로 순입금액을 잡아야
        // 스냅샷에 이미 포함된 입출금이 여기서 또 한 번 빠지는 이중 차감을 막을 수 있다
        long netDepositsInPeriod = cashTransactionService.getMyNetDepositsSinceBaseline(memberId, periodStart);
        return (currentTotalAsset - baselineAsset) - netDepositsInPeriod;
    }

    // ==================== 주식 (가중평균 단가 재현) ====================

    private List<RealizedProfitDto> replayStockTrades(List<TradeDto> trades) {
        Map<String, List<TradeDto>> grouped = trades.stream()
                .collect(Collectors.groupingBy(t -> t.getAccountId() + ":" + t.getStockCode()));

        List<RealizedProfitDto> result = new ArrayList<>();
        for (List<TradeDto> group : grouped.values()) {
            group.sort(Comparator.comparing(TradeDto::getTradeAt));

            long quantity = 0;
            long avgPrice = 0;
            LocalDateTime lastBuyAt = null;

            for (TradeDto t : group) {
                if ("BUY".equals(t.getTradeType())) {
                    long buyQty = t.getQuantity();
                    // 매수원가 = 체결금액 + 매수수수료 (TradeServiceImpl의 평단가 계산과 동일한 방식으로 재현)
                    long buyCost = (long) t.getPrice() * buyQty + t.getFee();
                    avgPrice = quantity == 0
                            ? buyCost / buyQty
                            : (quantity * avgPrice + buyCost) / (quantity + buyQty);
                    quantity += buyQty;
                    lastBuyAt = t.getTradeAt();
                } else {
                    long sellQty = t.getQuantity();
                    if (quantity <= 0) {
                        continue; // 데이터 정합성 방어(보유 없이 매도 이력은 없어야 정상)
                    }
                    // 실현손익 = (매도가 - 매입원가평단가) * 수량 - 매도수수료
                    long profit = ((long) t.getPrice() - avgPrice) * sellQty - t.getFee();
                    BigDecimal returnRate = avgPrice <= 0
                            ? BigDecimal.ZERO
                            : BigDecimal.valueOf(profit * 100).divide(BigDecimal.valueOf(avgPrice * sellQty), 2, RoundingMode.HALF_UP);
                    long holdingDays = lastBuyAt == null ? 0 : ChronoUnit.DAYS.between(lastBuyAt, t.getTradeAt());

                    result.add(new RealizedProfitDto(
                            "STOCK", t.getStockName(), lastBuyAt, t.getTradeAt(),
                            BigDecimal.valueOf(avgPrice), BigDecimal.valueOf(t.getPrice()), BigDecimal.valueOf(sellQty),
                            profit, returnRate, holdingDays));

                    quantity -= sellQty;
                }
            }
        }
        return result;
    }

    // ==================== 상품 (가중평균 기준가 재현) ====================

    private List<RealizedProfitDto> replayProductTransactions(List<ProductTransactionDto> txs) {
        Map<String, List<ProductTransactionDto>> grouped = txs.stream()
                .collect(Collectors.groupingBy(t -> t.getAccountId() + ":" + t.getProductId()));

        List<RealizedProfitDto> result = new ArrayList<>();
        for (List<ProductTransactionDto> group : grouped.values()) {
            group.sort(Comparator.comparing(ProductTransactionDto::getTransactionAt));

            BigDecimal quantity = BigDecimal.ZERO;
            BigDecimal avgNav = BigDecimal.ZERO;
            LocalDateTime lastSubscribeAt = null;

            for (ProductTransactionDto t : group) {
                if ("SUBSCRIBE".equals(t.getTransactionType())) {
                    BigDecimal buyQty = t.getQuantity();
                    avgNav = quantity.signum() == 0
                            ? t.getNav()
                            : quantity.multiply(avgNav).add(buyQty.multiply(t.getNav()))
                                    .divide(quantity.add(buyQty), 2, RoundingMode.HALF_UP);
                    quantity = quantity.add(buyQty);
                    lastSubscribeAt = t.getTransactionAt();
                } else {
                    BigDecimal redeemQty = t.getQuantity();
                    if (quantity.signum() <= 0) {
                        continue;
                    }
                    long profit = t.getNav().subtract(avgNav).multiply(redeemQty).setScale(0, RoundingMode.HALF_UP).longValueExact();
                    BigDecimal baseAmount = avgNav.multiply(redeemQty);
                    BigDecimal returnRate = baseAmount.signum() <= 0
                            ? BigDecimal.ZERO
                            : BigDecimal.valueOf(profit).multiply(BigDecimal.valueOf(100))
                                    .divide(baseAmount, 2, RoundingMode.HALF_UP);
                    long holdingDays = lastSubscribeAt == null ? 0 : ChronoUnit.DAYS.between(lastSubscribeAt, t.getTransactionAt());

                    result.add(new RealizedProfitDto(
                            "PRODUCT", t.getProductName(), lastSubscribeAt, t.getTransactionAt(),
                            avgNav, t.getNav(), redeemQty,
                            profit, returnRate, holdingDays));

                    quantity = quantity.subtract(redeemQty);
                }
            }
        }
        return result;
    }
}
