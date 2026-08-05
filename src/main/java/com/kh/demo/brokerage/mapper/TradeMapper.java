package com.kh.demo.brokerage.mapper;

import com.kh.demo.brokerage.dto.TradeDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TradeMapper {

    // 거래(체결) 이력 등록 (체결시각은 DB가 now()로 채움 - 실시간 매매용)
    int insertTrade(TradeDto tradeDto);

    // 특정 계좌의 거래이력
    List<TradeDto> selectTradesByAccount(Long accountId);

    // 특정 회원의 "모든" 계좌를 통틀어 거래이력 조회
    // -> "내 거래 정보", 랭킹보드의 "거래 히스토리" 기능이 그대로 이 메서드를 소비하면 됨
    List<TradeDto> selectTradesByMember(String memberId);

    // 과거 시각을 직접 지정해 거래이력을 등록 (데모 데이터 생성기 전용 - tradeDto.tradeAt을 그대로 씀)
    int insertHistoricalTrade(TradeDto tradeDto);

    // 계좌의 거래이력을 전부 삭제 (데모 데이터 생성기가 재생성 전 초기화할 때 사용)
    int deleteTradesByAccount(Long accountId);
}
