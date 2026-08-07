package com.kh.demo.goal.mapper;

import com.kh.demo.goal.dto.GoalDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface GoalMapper {

    // 목표 등록
    int insertGoal(GoalDto goalDto);

    // 회원이 가장 최근에 설정한 목표 (없으면 null) - v1은 회원당 "현재 목표" 하나만 보여준다
    GoalDto selectLatestGoalByMember(@Param("memberId") String memberId);
}
