package com.kh.demo.ranking.service;

import com.kh.demo.ranking.dto.RankingDto;

import java.util.List;

public interface RankingService {

    // 전체 누적 기준 랭킹 목록을 조회
    List<RankingDto> getRankingBoard();
}