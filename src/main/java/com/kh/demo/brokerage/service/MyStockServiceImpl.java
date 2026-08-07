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

        // account 테이블에 저장된 계좌별 총 보유 수량을 합산
        long totalStockQuantity = accounts.stream()
                .map(AccountDto::getHoldingStockQuantity)
                .filter(value -> value != null)
                .mapToLong(Long::longValue)
                .sum();

        // account 테이블에 저장된 계좌별 수익금을 합산
        long totalProfitAmount = accounts.stream()
                .map(AccountDto::getProfitAmount)
                .filter(value -> value != null)
                .mapToLong(Long::longValue)
                .sum();

        long currentBalance = accounts.stream()
                .map(AccountDto::getBalance)
                .filter(value -> value != null)
                .mapToLong(Long::longValue)
                .sum();

        // 연결된 대표 계좌의 DB 수익률을 사용
        BigDecimal returnRate = accounts.stream()
                .map(AccountDto::getReturnRate)
                .filter(value -> value != null)
                .findFirst()
                .orElse(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);

        long totalPurchaseAmount = holdings.stream()
                .map(MyStockHoldingDto::getPurchaseAmount)
                .filter(value -> value != null)
                .mapToLong(Long::longValue)
                .sum();

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
