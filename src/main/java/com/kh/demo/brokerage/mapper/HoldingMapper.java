package com.kh.demo.brokerage.mapper;

import com.kh.demo.brokerage.dto.HoldingDto;
import com.kh.demo.brokerage.dto.MyStockHoldingDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HoldingMapper {

    // 계좌의 특정 종목 보유내역 단건 조회 (없으면 미보유 상태 -> null)
    HoldingDto selectHolding(@Param("accountId") Long accountId, @Param("stockCode") String stockCode);

    // 계좌의 전체 보유내역
    List<HoldingDto> selectHoldingsByAccount(Long accountId);

    // 로그인 회원의 모든 계좌를 종목별로 합산한 내 주식 화면용 보유내역
    List<MyStockHoldingDto> selectPortfolioHoldings(String memberId);

    // 신규 보유 등록 (해당 계좌로 해당 종목을 처음 매수했을 때)
    int insertHolding(HoldingDto holdingDto);

    // 기존 보유내역 갱신 (추가매수로 수량/평단가가 바뀌거나, 매도로 수량이 줄었을 때)
    int updateHolding(HoldingDto holdingDto);

    // 전량매도로 보유수량이 0이 되었을 때 보유내역 자체를 삭제
    int deleteHolding(Long holdingId);
}
