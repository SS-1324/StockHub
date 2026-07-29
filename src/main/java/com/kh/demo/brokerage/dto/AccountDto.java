package com.kh.demo.brokerage.dto;

import lombok.*;

import java.time.LocalDateTime;

/*
*   AccountDto : account 테이블과 1:1로 대응되는 클래스
*
*   계좌의 주인은 증권사다. memberId는 사이트 회원이 이 계좌를 연동(claim)하기 전까지 null.
*   accountNo/ownerName은 연동시 본인확인용 식별자(실제 계좌연동 서비스의 계좌번호+예금주명과 동일한 역할).
* */

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AccountDto {

    private Long accountId;         // 계좌 번호(PK)
    private String accountNo;       // 증권사측 계좌번호(연동시 식별자)
    private String ownerName;       // 증권사에 등록된 예금주명(연동시 본인확인용)
    private String memberId;        // 연동된 사이트 회원 아이디, 연동 전이면 null
    private Long brokerageId;       // 개설된 증권사 번호
    private String brokerageName;   // 조회 편의를 위한 조인 컬럼
    private Long balance;           // 계좌 잔고(예수금)
    private LocalDateTime createAt; // 계좌 개설일(증권사측 기준)
    private LocalDateTime linkedAt; // 사이트 회원과 연동된 일시, 연동 전이면 null
}
