package com.kh.demo.hub.service;

import com.kh.demo.brokerage.dto.StockDto;
import com.kh.demo.brokerage.mapper.StockMapper;
import com.kh.demo.hub.dto.StockVoteResultDto;
import com.kh.demo.hub.dto.VoteCountsDto;
import com.kh.demo.hub.mapper.StockVoteMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Set;

// 종목 투표("살까?팔까?") 입력값 검사와 집계 조립을 처리
@Service
public class StockVoteServiceImpl implements StockVoteService {

    private static final Logger log = LoggerFactory.getLogger(StockVoteServiceImpl.class);
    private static final Set<String> VALID_VOTE_TYPES = Set.of("UP", "DOWN");
    private static final int RESET_HOUR = 10;

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

    // 매일 오전 10시, 전 종목 투표를 초기화
    @Scheduled(cron = "0 0 10 * * *")
    public void resetDailyVotes() {
        runReset();
    }

    // 서버가 오전 10시에 꺼져있다 나중에 켜진 경우, 그날의 초기화가 통째로 스킵되는 걸 막기 위해
    // 기동 시점에도 같은 로직을 한 번 태워서 밀린 초기화를 바로 따라잡음
    @EventListener(ApplicationReadyEvent.class)
    public void resetVotesOnStartup() {
        runReset();
    }

    private void runReset() {
        int deleted = stockVoteMapper.deleteVotesBefore(lastResetCutoff());
        if (deleted > 0) {
            log.info("[투표 초기화] 전 종목 투표 {}건 삭제", deleted);
        }
    }

    // "가장 최근에 이미 지난 오전 10시" 시각을 계산.
    // 지금이 오늘 10시 이전이면 아직 오늘자 리셋 시각이 안 지났으므로 어제 10시 기준,
    // 10시 이후면 오늘 10시 기준 — 이 시각 이전 투표는 전부 정리 대상
    private LocalDateTime lastResetCutoff() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayReset = LocalDate.now().atTime(RESET_HOUR, 0);
        return now.isBefore(todayReset) ? todayReset.minusDays(1) : todayReset;
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
