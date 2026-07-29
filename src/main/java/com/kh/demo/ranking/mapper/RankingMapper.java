package com.kh.demo.ranking.mapper;

import com.kh.demo.ranking.dto.RankingDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RankingMapper {

    // 선택한 기간에 해당하는 가장 최근 랭킹을 상위 5명까지 조회
    List<RankingDto> selectRankingBoard(@Param("period") String period);
}