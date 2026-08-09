package com.kh.demo.brokerage.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/*
*   RealizedProfitDto : "언제 사서, 얼마에, 몇 주 뒤에, 얼마에 팔아, 얼마를 벌었는가" 한 줄.
*
*   평단가(가중평균) 방식이라 매도 1건이 매수 1건과 정확히 1:1로 안 묶이지만, 그 매도 직전의
*   평단가와, 그 평단가를 만든 가장 최근 매수 시각을 "샀던 시점"으로 근사해서 보여준다.
* */
@Getter
@AllArgsConstructor
public class RealizedProfitDto {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    private String itemType;   // "STOCK" / "PRODUCT"
    private String itemName;
    private LocalDateTime buyAt;   // 근사: 이 평단가를 만든 가장 최근 매수/가입 시각
    private LocalDateTime sellAt;  // 매도/환매 시각
    private BigDecimal buyPrice;   // 매도 시점 기준 평단가
    private BigDecimal sellPrice;  // 매도/환매 체결가
    private BigDecimal quantity;
    private long profitAmount;
    private BigDecimal returnRate;
    private long holdingDays;

    public String getBuyAtText() {
        return buyAt == null ? "-" : buyAt.format(DATE_FORMAT);
    }

    public String getSellAtText() {
        return sellAt.format(DATE_FORMAT);
    }
}
