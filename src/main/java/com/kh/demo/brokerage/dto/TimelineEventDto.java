package com.kh.demo.brokerage.dto;

import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// 대시보드의 "최근 활동" 타임라인 한 줄 (trade/product_transaction/cash_transaction을 한 모양으로 합친 것)
@Getter
public class TimelineEventDto {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM.dd HH:mm");

    private final LocalDateTime occurredAt;
    private final String category;    // "buy"/"sell"/"subscribe"/"redeem"/"deposit"/"withdrawal" - CSS 클래스용 영문 키
    private final String badge;       // "매수" / "매도" / "가입" / "환매" / "입금" / "출금" - 화면 표시용
    private final String description; // 종목/상품명 + 수량, 또는 입출금 메모
    private final long amount;        // 거래금액(항상 양수)
    private final String occurredAtText; // JSP에서 그대로 출력할 포맷된 날짜(JSTL fmt 태그는 LocalDateTime을 못 다룸)
    private final Long accountId;      // 이 활동이 발생한 계좌 - 증권사별 필터링용
    private final String brokerageName; // 조회 편의를 위한 조인 값 - 증권사별 필터링용

    public TimelineEventDto(LocalDateTime occurredAt, String category, String badge, String description, long amount,
                             Long accountId, String brokerageName) {
        this.occurredAt = occurredAt;
        this.category = category;
        this.badge = badge;
        this.description = description;
        this.amount = amount;
        this.occurredAtText = occurredAt.format(DATE_FORMAT);
        this.accountId = accountId;
        this.brokerageName = brokerageName;
    }
}
