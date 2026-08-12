package com.kh.demo.brokerage.service;

import com.kh.demo.brokerage.dto.AccountDto;
import com.kh.demo.brokerage.dto.MyStockHoldingAccountDto;
import com.kh.demo.brokerage.dto.MyStockHoldingDto;
import com.kh.demo.brokerage.dto.MyStockSummaryDto;
import com.kh.demo.brokerage.mapper.AccountMapper;
import com.kh.demo.brokerage.mapper.HoldingMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MyStockServiceImpl implements MyStockService {

    private final AccountMapper accountMapper;
    private final HoldingMapper holdingMapper;

    public MyStockServiceImpl(AccountMapper accountMapper,
                              HoldingMapper holdingMapper) {
        this.accountMapper = accountMapper;
        this.holdingMapper = holdingMapper;
    }

    @Override
    public MyStockSummaryDto getMyStockSummary(String memberId) {
        List<AccountDto> accounts = accountMapper.selectAccountsByMember(memberId);
        List<MyStockHoldingDto> holdings = holdingMapper.selectPortfolioHoldings(memberId);
        attachAccountBreakdown(holdings, memberId);

        // account.holding_stock_quantity/profit_amount/return_rate는 데모 데이터 생성기만 갱신하는
        // 저장값이라 실거래·실시간 시세 변동을 못 따라간다. holdings는 매번 stock.current_price를
        // 라이브로 조인해서 조회하므로, 요약값은 전부 이 holdings 기준으로 다시 계산한다.
        long totalStockQuantity = holdings.stream()
                .map(MyStockHoldingDto::getQuantity)
                .filter(value -> value != null)
                .mapToLong(Long::longValue)
                .sum();

        long totalProfitAmount = holdings.stream()
                .map(MyStockHoldingDto::getProfitAmount)
                .filter(value -> value != null)
                .mapToLong(Long::longValue)
                .sum();

        long currentBalance = accounts.stream()
                .map(AccountDto::getBalance)
                .filter(value -> value != null)
                .mapToLong(Long::longValue)
                .sum();

        long totalPurchaseAmount = holdings.stream()
                .map(MyStockHoldingDto::getPurchaseAmount)
                .filter(value -> value != null)
                .mapToLong(Long::longValue)
                .sum();

        BigDecimal returnRate = totalPurchaseAmount <= 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(totalProfitAmount * 100)
                        .divide(BigDecimal.valueOf(totalPurchaseAmount), 2, RoundingMode.HALF_UP);

        MyStockSummaryDto summary = new MyStockSummaryDto();
        summary.setHoldings(holdings);
        summary.setTotalStockQuantity(totalStockQuantity);
        summary.setReturnRate(returnRate);
        summary.setProfitAmount(totalProfitAmount);
        summary.setCurrentBalance(currentBalance);
        summary.setTotalPurchaseAmount(totalPurchaseAmount);
        return summary;
    }

    // 증권사별 세부 내역을 항상 붙인다 - 대시보드의 증권사 필터가 이 값으로 화면을 다시 그린다
    // (종목이 한 증권사에만 있어도, "그 증권사만 볼 때 이 종목이 보여야 하는지"를 판단하려면 필요하다)
    private void attachAccountBreakdown(List<MyStockHoldingDto> holdings, String memberId) {
        if (holdings.isEmpty()) {
            return;
        }
        Map<String, List<MyStockHoldingAccountDto>> byStock = holdingMapper.selectPortfolioHoldingsByAccount(memberId)
                .stream()
                .collect(Collectors.groupingBy(MyStockHoldingAccountDto::getStockCode));
        for (MyStockHoldingDto h : holdings) {
            List<MyStockHoldingAccountDto> rows = byStock.get(h.getStockCode());
            if (rows != null) {
                h.setAccountBreakdown(rows);
            }
        }
    }
}
