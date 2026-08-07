package com.kh.demo.ranking.mapper;

import com.kh.demo.ranking.dto.RankingDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RankingMapper {

    // 전체 누적 기준 랭킹을 상위 5명까지 조회
    List<RankingDto> selectRankingBoard(
            @Param("includePrivateDetails") boolean includePrivateDetails
    );
}
