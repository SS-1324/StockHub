package com.kh.demo.brokerage.service;

import com.kh.demo.brokerage.dto.MyProductHoldingDto;
import com.kh.demo.brokerage.dto.MyProductSummaryDto;
import com.kh.demo.brokerage.mapper.ProductHoldingMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class MyProductServiceImpl implements MyProductService {

    @Autowired
    private ProductHoldingMapper productHoldingMapper;

    @Override
    public MyProductSummaryDto getMyProductSummary(String memberId) {
        List<MyProductHoldingDto> holdings = productHoldingMapper.selectPortfolioHoldings(memberId);

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
}
