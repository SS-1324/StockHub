package com.kh.demo.hub.service;

import com.kh.demo.brokerage.dto.StockDto;
import com.kh.demo.brokerage.mapper.StockMapper;
import com.kh.demo.hub.dto.StockVoteResultDto;
import com.kh.demo.hub.dto.VoteCountsDto;
import com.kh.demo.hub.mapper.StockVoteMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Set;

// 종목 투표("살까?팔까?") 입력값 검사와 집계 조립을 처리
@Service
public class StockVoteServiceImpl implements StockVoteService {

    private static final Set<String> VALID_VOTE_TYPES = Set.of("UP", "DOWN");

    private final StockVoteMapper stockVoteMapper;
    private final StockMapper stockMapper;

    public StockVoteServiceImpl(StockVoteMapper stockVoteMapper, StockMapper stockMapper) {
        this.stockVoteMapper = stockVoteMapper;
        this.stockMapper = stockMapper;
    }

    @Override
    public StockVoteResultDto getResult(String stockCode, String memberId) {
        StockDto stock = stockMapper.selectStockByCode(stockCode);
        if (stock == null) {
            throw new NoSuchElementException("종목을 찾을 수 없습니다: " + stockCode);
        }
        String myVote = memberId == null ? null : stockVoteMapper.selectMyVote(stockCode, memberId);
        return buildResult(stock, myVote);
    }

    @Override
    @Transactional
    public StockVoteResultDto vote(String stockCode, String memberId, String voteType) {
        if (voteType == null || !VALID_VOTE_TYPES.contains(voteType)) {
            throw new IllegalArgumentException("올바르지 않은 투표값입니다.");
        }
        try {
            stockVoteMapper.upsertVote(stockCode, memberId, voteType);
        } catch (DataIntegrityViolationException e) {
            // stock 테이블에 없는 종목 코드로 투표를 시도한 경우(FK 위반)
            throw new NoSuchElementException("투표를 지원하지 않는 종목입니다: " + stockCode);
        }
        StockDto stock = stockMapper.selectStockByCode(stockCode);
        if (stock == null) {
            throw new NoSuchElementException("종목을 찾을 수 없습니다: " + stockCode);
        }
        // 방금 저장한 값을 알고 있으므로 myVote를 다시 조회하지 않고 그대로 사용
        return buildResult(stock, voteType);
    }

    @Override
    @Transactional
    public StockVoteResultDto cancelVote(String stockCode, String memberId) {
        stockVoteMapper.deleteVote(stockCode, memberId);
        StockDto stock = stockMapper.selectStockByCode(stockCode);
        if (stock == null) {
            throw new NoSuchElementException("종목을 찾을 수 없습니다: " + stockCode);
        }
        // 방금 지웠으므로 myVote를 다시 조회하지 않고 null로 확정
        return buildResult(stock, null);
    }

    private StockVoteResultDto buildResult(StockDto stock, String myVote) {
        VoteCountsDto counts = stockVoteMapper.selectVoteCounts(stock.getStockCode());
        long up = counts.getUpCount();
        long down = counts.getDownCount();
        long total = up + down;

        // 참여자가 없으면 그래프가 절반씩 중립으로 보이도록 50/50 기본값을 씀
        int upPercent = total == 0 ? 50 : Math.round(up * 100f / total);
        int downPercent = 100 - upPercent;

        return new StockVoteResultDto(
                stock.getStockCode(),
                stock.getStockName(),
                stock.getCurrentPrice(),
                up,
                down,
                total,
                upPercent,
                downPercent,
                myVote
        );
    }
}
