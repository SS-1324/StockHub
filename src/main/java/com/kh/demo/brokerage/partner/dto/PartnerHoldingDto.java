package com.kh.demo.brokerage.partner.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/*
*   PartnerHoldingDto : 제휴 증권사 API가 "계좌의 상품 보유내역"으로 내려준다고 가정한 응답 모양.
*
*   우리 내부 PK(productHoldingId) 대신, 파트너사가 자기 쪽 식별자로 계좌를 가리킬 때 쓸 법한
*   accountNo(우리쪽 account.account_no) 기준으로 필드를 구성했다.
* */

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PartnerHoldingDto {

    private String partnerAccountNo; // 파트너사 계좌번호
    private Long productId;          // 상품 번호 (우리 상품 카탈로그 기준 - 상품 자체는 우리가 이미 갖고 있음)
    private String productName;
    private String productType;
    private BigDecimal quantity;     // 보유 좌수
    private BigDecimal avgNav;       // 평균 매입 기준가
    private Long purchaseAmount;     // 누적 매입원금
    private LocalDateTime asOf;      // 이 보유내역의 기준 시각(파트너사 스냅샷 시각)
}
