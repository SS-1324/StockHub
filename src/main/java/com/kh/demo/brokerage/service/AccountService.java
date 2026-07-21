package com.kh.demo.brokerage.service;

import com.kh.demo.brokerage.dto.AccountDto;
import com.kh.demo.brokerage.dto.AccountLinkRequestDto;
import com.kh.demo.brokerage.dto.HoldingDto;

import java.util.List;

public interface AccountService {

    // 증권사에 완전히 새로운 계좌를 개설 (연동할 기존 계좌가 없는 경우)
    AccountDto openAccount(String memberId, String memberName, Long brokerageId);

    // 회원이 연동한 모든 가상 계좌
    List<AccountDto> getMyAccounts(String memberId);

    // 특정 증권사의 "아직 연동되지 않은" 계좌 목록 (연동 후보 조회용)
    List<AccountDto> getUnlinkedAccounts(Long brokerageId);

    // 계좌번호+예금주명으로 미연동 계좌를 본인확인 후 연동(claim)
    // -> 증권사에 미리 있던 보유내역/거래이력이 그대로 이 회원의 것이 됨
    AccountDto linkAccount(String memberId, AccountLinkRequestDto request);

    // 계좌 소유자 검증 후 보유내역 조회 (다른 사람의 계좌는 조회 불가)
    List<HoldingDto> getHoldings(String memberId, Long accountId);
}
