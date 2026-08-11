package com.kh.demo.brokerage.service;

import com.kh.demo.brokerage.dto.AccountDto;
import com.kh.demo.brokerage.dto.MyStockHoldingDto;
import com.kh.demo.brokerage.dto.MyStockSummaryDto;
import com.kh.demo.brokerage.mapper.AccountMapper;
import com.kh.demo.brokerage.mapper.HoldingMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

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
}
