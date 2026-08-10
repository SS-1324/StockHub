package com.kh.demo.brokerage.service;

import com.kh.demo.brokerage.dto.*;
import com.kh.demo.brokerage.mapper.AccountMapper;
import com.kh.demo.brokerage.mapper.BrokerageMapper;
import com.kh.demo.brokerage.mapper.HoldingMapper;
import com.kh.demo.brokerage.mapper.StockMapper;
import com.kh.demo.brokerage.mapper.TradeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class TradeServiceImpl implements TradeService {

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private BrokerageMapper brokerageMapper;

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private HoldingMapper holdingMapper;

    @Autowired
    private TradeMapper tradeMapper;

    @Override
    @Transactional
    public TradeDto executeTrade(String memberId, Long accountId, TradeRequestDto request) {
        // 1. 계좌 소유자 검증
        AccountDto account = accountMapper.selectAccountById(accountId);
        if (account == null) {
            throw new IllegalStateException("존재하지 않는 계좌입니다.");
        }
        if (!memberId.equals(account.getMemberId())) {
            throw new IllegalStateException("본인 계좌로만 거래할 수 있습니다.");
        }

        // 2. 요청값 검증
        String tradeType = request.getTradeType() == null ? "" : request.getTradeType().toUpperCase();
        if (!tradeType.equals("BUY") && !tradeType.equals("SELL")) {
            throw new IllegalArgumentException("tradeType은 BUY 또는 SELL 이어야 합니다.");
        }
        Long quantity = request.getQuantity();
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
        }

        // 3. 상품/증권사 정보 조회 (현재가, 수수료율)
        StockDto stock = stockMapper.selectStockByCode(request.getStockCode());
        if (stock == null) {
            throw new IllegalStateException("존재하지 않는 종목입니다.");
        }
        BrokerageDto brokerage = brokerageMapper.selectBrokerageById(account.getBrokerageId());

        int price = stock.getCurrentPrice();
        BigDecimal totalAmount = BigDecimal.valueOf(price).multiply(BigDecimal.valueOf(quantity));
        int fee = totalAmount.multiply(brokerage.getFeeRate())
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();

        HoldingDto holding = holdingMapper.selectHolding(accountId, stock.getStockCode());

        if (tradeType.equals("BUY")) {
            long totalCost = totalAmount.longValue() + fee;
            if (account.getBalance() < totalCost) {
                throw new IllegalStateException("잔고가 부족합니다.");
            }

            // 잔고 차감
            accountMapper.updateBalance(accountId, account.getBalance() - totalCost);

            // 보유내역 갱신(신규 매수면 insert, 추가 매수면 수량/평단가 재계산 후 update)
            // 평단가(매입원가)에는 매수수수료도 포함시킨다 - 실제 증권사도 수수료를 원가에 얹어서 평가손익을 계산함
            if (holding == null) {
                HoldingDto newHolding = new HoldingDto();
                newHolding.setAccountId(accountId);
                newHolding.setStockCode(stock.getStockCode());
                newHolding.setQuantity(quantity);
                newHolding.setAvgPrice((int) (totalCost / quantity));
                holdingMapper.insertHolding(newHolding);
            } else {
                long newQuantity = holding.getQuantity() + quantity;
                // 가중평균 평단가 = (기존수량*기존평단가 + 이번 매수원가(체결금액+수수료)) / 총수량
                long newAvgPrice = (holding.getQuantity() * holding.getAvgPrice() + totalCost) / newQuantity;
                holding.setQuantity(newQuantity);
                holding.setAvgPrice((int) newAvgPrice);
                holdingMapper.updateHolding(holding);
            }
        } else { // 매도(SELL)인 경우
            if (holding == null || holding.getQuantity() < quantity) {
                throw new IllegalStateException("보유수량이 부족합니다.");
            }

            long proceeds = totalAmount.longValue() - fee;
            accountMapper.updateBalance(accountId, account.getBalance() + proceeds);

            long remaining = holding.getQuantity() - quantity;
            if (remaining == 0) {
                holdingMapper.deleteHolding(holding.getHoldingId());
            } else {
                // 매도시 평단가는 변하지 않는다 (남은 수량만 줄어듦)
                holding.setQuantity(remaining);
                holdingMapper.updateHolding(holding);
            }
        }

        // 4. 거래이력 기록
        TradeDto trade = new TradeDto();
        trade.setAccountId(accountId);
        trade.setStockCode(stock.getStockCode());
        trade.setTradeType(tradeType);
        trade.setQuantity(quantity);
        trade.setPrice(price);
        trade.setFee(fee);
        tradeMapper.insertTrade(trade);

        trade.setStockName(stock.getStockName());
        return trade;
    }

    @Override
    public List<TradeDto> getTradesByAccount(String memberId, Long accountId) {
        AccountDto account = accountMapper.selectAccountById(accountId);
        if (account == null) {
            throw new IllegalStateException("존재하지 않는 계좌입니다.");
        }
        if (!memberId.equals(account.getMemberId())) {
            throw new IllegalStateException("본인 계좌만 조회할 수 있습니다.");
        }
        return tradeMapper.selectTradesByAccount(accountId);
    }

    @Override
    public List<TradeDto> getMyTrades(String memberId) {
        return tradeMapper.selectTradesByMember(memberId);
    }
}
