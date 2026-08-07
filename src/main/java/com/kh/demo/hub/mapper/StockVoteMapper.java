package com.kh.demo.hub.mapper;

import com.kh.demo.hub.dto.VoteCountsDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

// 종목별 상승/하락 의견 투표(stock_vote) SQL을 StockVoteMapper.xml과 연결
@Mapper
public interface StockVoteMapper {

    // 특정 종목의 상승/하락 집계
    VoteCountsDto selectVoteCounts(@Param("stockCode") String stockCode);

    // 로그인 회원이 해당 종목에 남긴 현재 의견 (없으면 null)
    String selectMyVote(@Param("stockCode") String stockCode, @Param("memberId") String memberId);

    // 투표 등록, 이미 투표했다면 의견만 갱신 (stock_code가 stock 테이블에 없으면 FK 위반 예외 발생)
    int upsertVote(@Param("stockCode") String stockCode,
                    @Param("memberId") String memberId,
                    @Param("voteType") String voteType);
}
