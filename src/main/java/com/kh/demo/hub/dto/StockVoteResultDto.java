package com.kh.demo.hub.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 종목 투표 카드("살까?팔까?")에 필요한 정보를 한 번에 담아 화면으로 전달
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockVoteResultDto {

    private String stockCode; // 종목 코드
    private String stockName; // 종목 이름
    private Integer currentPrice; // 현재가
    private long upCount; // 상승 의견 수
    private long downCount; // 하락 의견 수
    private long totalCount; // 전체 참여자 수
    private int upPercent; // 상승 의견 비율(%), 참여자가 없으면 50
    private int downPercent; // 하락 의견 비율(%), 참여자가 없으면 50
    private String myVote; // 로그인 회원의 현재 의견("UP"/"DOWN"), 비로그인/미투표면 null
}
