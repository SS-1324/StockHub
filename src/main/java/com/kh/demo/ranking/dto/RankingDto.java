package com.kh.demo.ranking.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

// 랭킹보드 한 줄에 표시할 회원·거래·수익 정보
@Getter
@Setter
public class RankingDto {

    // 화면에 표시되는 순위
    private Integer rankPosition;

    // 회원 아이디
    private String memberId;

    // 회원 닉네임
    private String nickname;

    // 프로필 이미지 경로
    private String profile;

    // 거래내역 공개 여부: Y 또는 N
    private String tradeHistoryPublicYn;

    private Long holdingQuantity;

    // 수익률
    private BigDecimal returnRate;

    // 수익금
    private Long profit;
}