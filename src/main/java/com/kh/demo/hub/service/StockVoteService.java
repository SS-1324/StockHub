package com.kh.demo.hub.service;

import com.kh.demo.hub.dto.StockVoteResultDto;

public interface StockVoteService {

    // 투표 현황 조회. memberId가 null이면(비로그인) myVote는 항상 null로 내려감
    StockVoteResultDto getResult(String stockCode, String memberId);

    // 상승/하락 의견 투표(이미 투표했다면 의견 변경) 후 최신 현황을 반환
    StockVoteResultDto vote(String stockCode, String memberId, String voteType);
}
