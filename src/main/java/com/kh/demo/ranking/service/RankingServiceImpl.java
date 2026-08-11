package com.kh.demo.ranking.service;

import com.kh.demo.ranking.dto.RankingDto;
import com.kh.demo.ranking.mapper.RankingMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RankingServiceImpl implements RankingService {

    private final RankingMapper rankingMapper;

    public RankingServiceImpl(RankingMapper rankingMapper) {
        this.rankingMapper = rankingMapper;
    }

    @Override
    public List<RankingDto> getRankingBoard(
            boolean includePrivateDetails,
            boolean sortByProfit
    ) {
        return rankingMapper.selectRankingBoard(
                includePrivateDetails,
                sortByProfit
        );
    }

    @Override
    public Integer getHeaderRankPosition(String memberId) {
        return rankingMapper.selectHeaderRankPosition(memberId);
    }

    @Override
    public Integer getProfileRankPosition(String memberId, boolean sortByProfit) {
        /* [프로필순위-3] 컨트롤러가 정규화한 기준을 MyBatis 순위 조회에 전달한다. */
        return rankingMapper.selectProfileRankPosition(memberId, sortByProfit);
    }

    @Override
    public RankingDto getProfileInvestmentSummary(String memberId) {
        return rankingMapper.selectProfileInvestmentSummary(memberId);
    }
}
