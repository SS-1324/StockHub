package com.kh.demo.ranking.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RankingDto {
    private String memberId;
    private String nickname;
    private Long profit;
    private String tier;
}