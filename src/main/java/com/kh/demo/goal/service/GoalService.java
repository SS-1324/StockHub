package com.kh.demo.goal.service;

import com.kh.demo.goal.dto.GoalDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface GoalService {

    // 대시보드에 보여줄 진행 중인 목표 (최대 3개, 기한이 가까운 순)
    List<GoalDto> getMyActiveGoals(String memberId);

    // 기한+유예기간이 지나 대시보드에서 빠진 목표 히스토리
    List<GoalDto> getMyGoalHistory(String memberId);

    // 새 목표 설정 (targetDate는 없으면 null - 무기한)
    GoalDto setGoal(String memberId, String goalType, String title, BigDecimal targetValue, LocalDate targetDate);

    // 목표 취소(본인 것만 취소 가능)
    void cancelGoal(String memberId, Long goalId);
}
