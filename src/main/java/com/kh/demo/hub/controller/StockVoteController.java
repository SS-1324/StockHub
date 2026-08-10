package com.kh.demo.hub.controller;

import com.kh.demo.common.dto.ApiResponse;
import com.kh.demo.common.util.SessionUtil;
import com.kh.demo.hub.dto.StockVoteResultDto;
import com.kh.demo.hub.dto.VoteRequestDto;
import com.kh.demo.hub.service.StockVoteService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.NoSuchElementException;

// 거래 허브 차트 페이지의 종목별 "살까?팔까?" 실시간 투표 API.
// 조회(GET)는 비로그인도 가능(myVote만 null), 투표(POST)는 로그인이 필요함.
@RestController
@RequestMapping("/api/hub/vote")
public class StockVoteController {

    private final StockVoteService stockVoteService;

    public StockVoteController(StockVoteService stockVoteService) {
        this.stockVoteService = stockVoteService;
    }

    @GetMapping("/{stockCode}")
    public ResponseEntity<ApiResponse<StockVoteResultDto>> result(@PathVariable String stockCode,
                                                                    HttpSession session) {
        try {
            String memberId = SessionUtil.currentMemberId(session);
            return ResponseEntity.ok(ApiResponse.success(stockVoteService.getResult(stockCode, memberId)));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail(e.getMessage()));
        }
    }

    @PostMapping("/{stockCode}")
    public ResponseEntity<ApiResponse<StockVoteResultDto>> vote(@PathVariable String stockCode,
                                                                  @RequestBody VoteRequestDto request,
                                                                  HttpSession session) {
        String memberId = SessionUtil.currentMemberId(session);
        if (memberId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.fail("로그인이 필요합니다."));
        }
        try {
            StockVoteResultDto result = stockVoteService.vote(stockCode, memberId, request.getVoteType());
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.fail(e.getMessage()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail(e.getMessage()));
        }
    }

    @DeleteMapping("/{stockCode}")
    public ResponseEntity<ApiResponse<StockVoteResultDto>> cancel(@PathVariable String stockCode,
                                                                     HttpSession session) {
        String memberId = SessionUtil.currentMemberId(session);
        if (memberId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.fail("로그인이 필요합니다."));
        }
        try {
            StockVoteResultDto result = stockVoteService.cancelVote(stockCode, memberId);
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail(e.getMessage()));
        }
    }
}
