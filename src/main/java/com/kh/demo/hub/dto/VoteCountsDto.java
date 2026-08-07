package com.kh.demo.hub.dto;

import lombok.Getter;
import lombok.Setter;

// stock_vote 집계 쿼리 결과 전용(마퍼 내부용) - StockVoteResultDto 조립에만 쓰임
@Getter
@Setter
public class VoteCountsDto {

    private long upCount;
    private long downCount;
}
