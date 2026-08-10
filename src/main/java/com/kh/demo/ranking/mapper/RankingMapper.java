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
}
