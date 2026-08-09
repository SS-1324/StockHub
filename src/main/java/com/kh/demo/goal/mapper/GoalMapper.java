package com.kh.demo.goal.mapper;

import com.kh.demo.goal.dto.GoalDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GoalMapper {

    // 목표 등록
    int insertGoal(GoalDto goalDto);

    // 진행 중인 목표(기한 없음 또는 기한+유예기간 이내) - 대시보드 원형 그래프용
    List<GoalDto> selectActiveGoalsByMember(@Param("memberId") String memberId);

    // 기한+유예기간이 지나 대시보드에서 빠진 지난 목표 - 목표 히스토리용
    List<GoalDto> selectGoalHistoryByMember(@Param("memberId") String memberId);

    // 본인 목표 취소(삭제) - 본인 것만 지울 수 있도록 memberId까지 조건에 건다
    int deleteGoal(@Param("goalId") Long goalId, @Param("memberId") String memberId);
}
