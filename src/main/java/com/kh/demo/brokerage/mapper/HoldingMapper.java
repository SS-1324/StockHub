package com.kh.demo.brokerage.mapper;

import com.kh.demo.brokerage.dto.HoldingDto;
import com.kh.demo.brokerage.dto.MyStockHoldingAccountDto;
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

    // 위와 같은 대상이지만 종목으로 합치지 않고 증권사(계좌)별로 한 줄씩 - 종목이 여러 증권사에
    // 나뉘어 있을 때 펼쳐 보여주는 용도(합산 결과에 덧붙여 쓴다)
    List<MyStockHoldingAccountDto> selectPortfolioHoldingsByAccount(String memberId);

    // 신규 보유 등록 (해당 계좌로 해당 종목을 처음 매수했을 때)
    int insertHolding(HoldingDto holdingDto);

    // 기존 보유내역 갱신 (추가매수로 수량/평단가가 바뀌거나, 매도로 수량이 줄었을 때)
    int updateHolding(HoldingDto holdingDto);

    // 전량매도로 보유수량이 0이 되었을 때 보유내역 자체를 삭제
    int deleteHolding(Long holdingId);

    // 계좌의 보유내역을 전부 삭제 (데모 데이터 생성기가 재생성 전 초기화할 때 사용)
    int deleteHoldingsByAccount(Long accountId);
}
