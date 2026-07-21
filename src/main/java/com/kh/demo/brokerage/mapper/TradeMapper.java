package com.kh.demo.brokerage.mapper;

import com.kh.demo.brokerage.dto.TradeDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TradeMapper {

    // 거래(체결) 이력 등록
    int insertTrade(TradeDto tradeDto);

    // 특정 계좌의 거래이력
    List<TradeDto> selectTradesByAccount(Long accountId);

    // 특정 회원의 "모든" 계좌를 통틀어 거래이력 조회
    // -> "내 거래 정보", 랭킹보드의 "거래 히스토리" 기능이 그대로 이 메서드를 소비하면 됨
    List<TradeDto> selectTradesByMember(String memberId);
}
