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

    // 활성 목표는 최대 3개까지만 "등록"할 수 있다 - 예전엔 표시만 3개로 자르고 등록은 무제한 허용해서,
    // 4개 이상 쌓인 상태에서 화면에 안 보이던 목표가 하나 취소하면 뜬금없이 나타나는 것처럼 보였다.
    // 등록 자체를 3개로 막으면 "안 보이던 목표"가 존재할 수 없으니 이 문제가 근본적으로 사라진다.
    private static final int MAX_ACTIVE_GOALS = 3;

    private static final int TITLE_MAX_LENGTH = 20;

    private static final Set<String> VALID_TYPES = Set.of("RETURN_RATE", "PROFIT_AMOUNT");

    @Autowired
    private GoalMapper goalMapper;

    @Override
    public List<GoalDto> getMyActiveGoals(String memberId) {
        return goalMapper.selectActiveGoalsByMember(memberId);
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
        if (title.trim().length() > TITLE_MAX_LENGTH) {
            throw new IllegalArgumentException("목표 이름은 " + TITLE_MAX_LENGTH + "자 이내로 입력해주세요.");
        }
        if (targetValue == null || targetValue.signum() <= 0) {
            throw new IllegalArgumentException("목표치는 0보다 커야 합니다.");
        }
        if (targetDate != null && targetDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("목표 기한은 오늘 이후여야 합니다.");
        }
        if (goalMapper.selectActiveGoalsByMember(memberId).size() >= MAX_ACTIVE_GOALS) {
            throw new IllegalStateException("목표는 최대 " + MAX_ACTIVE_GOALS + "개까지 등록할 수 있습니다. 기존 목표를 취소한 뒤 다시 시도해주세요.");
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
