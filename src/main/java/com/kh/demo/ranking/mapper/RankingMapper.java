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
}