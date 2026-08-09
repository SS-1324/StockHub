package com.kh.demo.ranking.service;

import com.kh.demo.ranking.dto.RankingDto;

import java.util.List;

public interface RankingService {

    // 메인 화면 등 기존 호출은 수익률순 유지
    default List<RankingDto> getRankingBoard() {
        return getRankingBoard(false, false);
    }

    // 기존 /ranking/data 호출도 수익률순 유지
    default List<RankingDto> getRankingBoard(
            boolean includePrivateDetails
    ) {
        return getRankingBoard(includePrivateDetails, false);
    }

    /*
     * sortByProfit = false : 수익률순
     * sortByProfit = true  : 수익금순
     */
    List<RankingDto> getRankingBoard(
            boolean includePrivateDetails,
            boolean sortByProfit
    );
}