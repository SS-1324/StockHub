package com.kh.demo.brokerage.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/*
*   BrokerageDto : brokerage 테이블과 1:1로 대응되는 클래스
*
*   실제 존재하는 증권사가 아니라 API 수준에서만 존재하는 "가상 증권사".
*   증권사마다 수수료율(feeRate)이 달라서, 매매 체결시 수수료 계산에 사용된다.
* */

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class BrokerageDto {

    private Long brokerageId;       // 증권사 번호(PK)
    private String brokerageName;   // 증권사 이름
    private BigDecimal feeRate;     // 거래 수수료율 (예: 0.00015 = 0.015%)
    private LocalDateTime createAt; // 생성일시
}
