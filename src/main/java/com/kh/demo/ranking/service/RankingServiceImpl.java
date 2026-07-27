package com.kh.demo.ranking.service;

import com.kh.demo.ranking.mapper.RankingMapper;
import com.kh.demo.ranking.dto.RankingDto;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class RankingServiceImpl implements RankingService {

    private final RankingMapper rankingMapper;

    public RankingServiceImpl(RankingMapper rankingMapper) {
        this.rankingMapper = rankingMapper;
    }

    @Override
    public List<RankingDto> getRankingBoard() {
        return rankingMapper.selectRankingBoard();
    }
}