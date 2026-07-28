package com.kh.demo.ranking.service;

import com.kh.demo.ranking.dto.RankingDto;
import java.util.List;


public interface RankingService {
    List<RankingDto> getRankingBoard();
}