package com.kh.demo.brokerage.service;

import com.kh.demo.brokerage.dto.MyProductHoldingAccountDto;
import com.kh.demo.brokerage.dto.MyProductHoldingDto;
import com.kh.demo.brokerage.dto.MyProductSummaryDto;
import com.kh.demo.brokerage.mapper.ProductHoldingMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MyProductServiceImpl implements MyProductService {

    @Autowired
    private ProductHoldingMapper productHoldingMapper;

    @Override
    public MyProductSummaryDto getMyProductSummary(String memberId) {
        List<MyProductHoldingDto> holdings = productHoldingMapper.selectPortfolioHoldings(memberId);
        attachAccountBreakdown(holdings, memberId);

        long totalPurchaseAmount = holdings.stream()
                .mapToLong(h -> h.getPurchaseAmount() == null ? 0L : h.getPurchaseAmount())
                .sum();
        long totalCurrentValue = holdings.stream()
                .mapToLong(h -> h.getCurrentValue() == null ? 0L : h.getCurrentValue())
                .sum();
        long profitAmount = totalCurrentValue - totalPurchaseAmount;
        BigDecimal returnRate = totalPurchaseAmount <= 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(profitAmount).multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(totalPurchaseAmount), 2, RoundingMode.HALF_UP);

        MyProductSummaryDto summary = new MyProductSummaryDto();
        summary.setHoldings(holdings);
        summary.setTotalPurchaseAmount(totalPurchaseAmount);
        summary.setTotalCurrentValue(totalCurrentValue);
        summary.setProfitAmount(profitAmount);
        summary.setReturnRate(returnRate);
        return summary;
    }

    // 증권사별 세부 내역을 항상 붙인다 - 대시보드의 증권사 필터가 이 값으로 화면을 다시 그린다
    private void attachAccountBreakdown(List<MyProductHoldingDto> holdings, String memberId) {
        if (holdings.isEmpty()) {
            return;
        }
        Map<Long, List<MyProductHoldingAccountDto>> byProduct = productHoldingMapper.selectPortfolioHoldingsByAccount(memberId)
                .stream()
                .collect(Collectors.groupingBy(MyProductHoldingAccountDto::getProductId));
        for (MyProductHoldingDto h : holdings) {
            List<MyProductHoldingAccountDto> rows = byProduct.get(h.getProductId());
            if (rows != null) {
                h.setAccountBreakdown(rows);
            }
        }
    }
}
