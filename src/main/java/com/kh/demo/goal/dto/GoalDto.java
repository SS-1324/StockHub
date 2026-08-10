package com.kh.demo.goal.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/*
*   GoalDto : goal 테이블과 1:1로 대응되는 클래스
*
*   회원이 대시보드에서 직접 설정하는 목표(예: "이번 달 +5%", "30만원 모으기").
*   증권사(파트너) 데이터가 아니라 순수하게 우리 사이트 자체 기능.
* */

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GoalDto {

    private Long goalId;            // 목표 번호(PK)
    private String memberId;        // 목표를 설정한 회원
    private String goalType;        // 목표 종류("RETURN_RATE" / "PROFIT_AMOUNT")
    private String title;           // 목표 이름
    private BigDecimal targetValue; // 목표치(수익률=%, 수익금=원)
    private LocalDate targetDate;   // 목표 기한(없으면 무기한)
    private LocalDateTime createAt; // 목표 설정일시

    // JSP(JSTL fmt 태그는 java.time을 못 다룸)에서 그대로 출력할 포맷된 문자열
    public String getTargetDateText() {
        return targetDate == null ? null : targetDate.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
    }

    public String getCreateAtText() {
        return createAt == null ? null : createAt.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
    }
}
