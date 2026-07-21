package com.kh.demo.brokerage.service;

import com.kh.demo.brokerage.dto.TradeDto;
import com.kh.demo.brokerage.dto.TradeRequestDto;

import java.util.List;

public interface TradeService {

    /*
    *  매수/매도를 "실제로 일어난 셈 치고" 체결시키는 핵심 로직.
    *  - 잔고/보유수량 검증
    *  - 계좌 잔고, 보유내역(holding) 갱신
    *  - 거래이력(trade) 기록
    *  까지 한번에 처리하고, 방금 체결된 거래내역을 반환한다.
    * */
    TradeDto executeTrade(String memberId, Long accountId, TradeRequestDto request);

    // 특정 계좌의 거래이력 (계좌 소유자 검증 포함)
    List<TradeDto> getTradesByAccount(String memberId, Long accountId);

    // 로그인한 회원의 "모든" 계좌를 통틀어 거래이력 조회 -> "내 거래 정보" 기능용
    List<TradeDto> getMyTrades(String memberId);
}
