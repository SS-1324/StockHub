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

    // 공통 헤더의 금·은·동 프로필 프레임에 사용할 현재 수익률 순위(1~3위, 그 외 null)
    Integer getHeaderRankPosition(String memberId);

    /* [프로필순위-3] 팝업을 연 랭킹보드의 기준에 맞는 1~3위만 반환한다. */
    Integer getProfileRankPosition(String memberId, boolean sortByProfit);

    /* [프로필공개정보-1] 공개 프로필 팝업에 표시할 회원 한 명의 수익률·수익금 합계. */
    RankingDto getProfileInvestmentSummary(String memberId);
}
