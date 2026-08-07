package com.kh.demo.goal.service;

import com.kh.demo.goal.dto.GoalDto;

import java.math.BigDecimal;

public interface GoalService {

    // 회원의 현재 목표 조회 (설정한 적 없으면 null)
    GoalDto getMyGoal(String memberId);

    // 새 목표 설정
    GoalDto setGoal(String memberId, String goalType, String title, BigDecimal targetValue);
}
