package com.kh.demo.member.dto;

import java.math.BigDecimal;

/**
 * 커뮤니티·랭킹에서 사용하는 회원 프로필 모달 응답.
 * 이메일, 실명, 투자 정보처럼 모달에 필요하지 않은 개인정보는 의도적으로 포함하지 않는다.
 */
public record MemberProfileModalDto(
        String memberId,
        String nickname,
        String profile,
        String badge,
        Integer rankPosition,
        String rankType,
        boolean canFollow,
        boolean followingTarget,
        boolean detailsPublic,
        long postCount,
        long followerCount,
        long followingCount,
        BigDecimal returnRate,
        Long profit
) {
}
