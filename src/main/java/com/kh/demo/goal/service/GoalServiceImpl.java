package com.kh.demo.goal.service;

import com.kh.demo.goal.dto.GoalDto;
import com.kh.demo.goal.mapper.GoalMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
public class GoalServiceImpl implements GoalService {

    // 대시보드 원형 그래프는 최대 3개까지만 보여준다
    private static final int MAX_ACTIVE_GOALS_SHOWN = 3;

    private static final Set<String> VALID_TYPES = Set.of("RETURN_RATE", "PROFIT_AMOUNT");

    @Autowired
    private GoalMapper goalMapper;

    @Override
    public List<GoalDto> getMyActiveGoals(String memberId) {
        List<GoalDto> goals = goalMapper.selectActiveGoalsByMember(memberId);
        return goals.size() > MAX_ACTIVE_GOALS_SHOWN
                ? goals.subList(0, MAX_ACTIVE_GOALS_SHOWN)
                : goals;
    }

    @Override
    public List<GoalDto> getMyGoalHistory(String memberId) {
        return goalMapper.selectGoalHistoryByMember(memberId);
    }

    @Override
    public GoalDto setGoal(String memberId, String goalType, String title, BigDecimal targetValue, LocalDate targetDate) {
        if (goalType == null || !VALID_TYPES.contains(goalType)) {
            throw new IllegalArgumentException("목표 종류는 수익률 또는 수익금이어야 합니다.");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("목표 이름을 입력해주세요.");
        }
        if (targetValue == null || targetValue.signum() <= 0) {
            throw new IllegalArgumentException("목표치는 0보다 커야 합니다.");
        }
        if (targetDate != null && targetDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("목표 기한은 오늘 이후여야 합니다.");
        }

        GoalDto dto = new GoalDto();
        dto.setMemberId(memberId);
        dto.setGoalType(goalType);
        dto.setTitle(title.trim());
        dto.setTargetValue(targetValue);
        dto.setTargetDate(targetDate);
        goalMapper.insertGoal(dto);

        return dto;
    }

    @Override
    public void cancelGoal(String memberId, Long goalId) {
        if (goalMapper.deleteGoal(goalId, memberId) != 1) {
            throw new IllegalArgumentException("취소할 목표를 찾을 수 없습니다.");
        }
    }
}
