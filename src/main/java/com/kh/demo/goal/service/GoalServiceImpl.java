package com.kh.demo.goal.service;

import com.kh.demo.goal.dto.GoalDto;
import com.kh.demo.goal.mapper.GoalMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Set;

@Service
public class GoalServiceImpl implements GoalService {

    private static final Set<String> VALID_TYPES = Set.of("RETURN_RATE", "PROFIT_AMOUNT");

    @Autowired
    private GoalMapper goalMapper;

    @Override
    public GoalDto getMyGoal(String memberId) {
        return goalMapper.selectLatestGoalByMember(memberId);
    }

    @Override
    public GoalDto setGoal(String memberId, String goalType, String title, BigDecimal targetValue) {
        if (goalType == null || !VALID_TYPES.contains(goalType)) {
            throw new IllegalArgumentException("목표 종류는 수익률 또는 수익금이어야 합니다.");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("목표 이름을 입력해주세요.");
        }
        if (targetValue == null || targetValue.signum() <= 0) {
            throw new IllegalArgumentException("목표치는 0보다 커야 합니다.");
        }

        GoalDto dto = new GoalDto();
        dto.setMemberId(memberId);
        dto.setGoalType(goalType);
        dto.setTitle(title.trim());
        dto.setTargetValue(targetValue);
        goalMapper.insertGoal(dto);

        return goalMapper.selectLatestGoalByMember(memberId);
    }
}
