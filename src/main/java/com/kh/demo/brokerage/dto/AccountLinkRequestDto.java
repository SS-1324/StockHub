package com.kh.demo.brokerage.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/*
*   AccountLinkRequestDto : 계좌 연동(claim) API 요청 바디
*
*   POST /api/accounts/link 요청시 JSON body로 전달됨.
*   실제 계좌연동 서비스가 "계좌번호 + 예금주명"으로 본인확인하는 것과 동일한 방식.
*   예) { "brokerageId": 1, "accountNo": "JMT-0001", "ownerName": "오지훈" }
* */

@ToString
@Getter
@Setter
public class AccountLinkRequestDto {
    private Long brokerageId; // 연동하려는 증권사 번호
    private String accountNo; // 증권사측 계좌번호(본인확인용)
    private String ownerName; // 증권사에 등록된 예금주명(본인확인용)
}
