package com.kh.demo.ranking.mapper;

import com.kh.demo.ranking.dto.RankingDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RankingMapper {

    List<RankingDto> selectRankingBoard(
            @Param("includePrivateDetails")
            boolean includePrivateDetails,

            @Param("sortByProfit")
            boolean sortByProfit
    );

    // 헤더 프로필용: 공개 회원 수익률 랭킹에서 해당 회원이 1~3위일 때만 순위를 반환한다.
    Integer selectHeaderRankPosition(@Param("memberId") String memberId);

    /*
     * [프로필순위-4] 회원 팝업에서 사용할 기준별 상위 3위 순위를 조회한다.
     * sortByProfit=false면 수익률, true면 수익금을 기준으로 계산한다.
     */
    Integer selectProfileRankPosition(
            @Param("memberId") String memberId,
            @Param("sortByProfit") boolean sortByProfit
    );

    // 공개 회원 한 명의 계좌 수익률·수익금 합계만 조회한다.
    RankingDto selectProfileInvestmentSummary(@Param("memberId") String memberId);
}
