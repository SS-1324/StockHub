package com.kh.demo.brokerage.service;

import com.kh.demo.brokerage.dto.CashTransactionDto;

import java.time.LocalDate;
import java.util.List;

public interface CashTransactionService {

    // 계좌 소유자 검증 후 입출금 이력 조회 (from/to는 선택 필터)
    List<CashTransactionDto> getTransactions(String memberId, Long accountId, LocalDate from, LocalDate to);

    // 로그인 회원의 모든 계좌를 통틀어 최근 입출금이력 조회 (대시보드 타임라인용)
    List<CashTransactionDto> getMyTransactions(String memberId);

    // 순입금액(입금-출금) 합계. since가 null이면 전체 기간(=총 투자원금)
    long getMyNetDeposits(String memberId, LocalDate since);

    // 기간별 손익 계산 전용: 계좌별 기준 스냅샷 날짜 이후의 순입금액만 합산(이중 차감 방지)
    long getMyNetDepositsSinceBaseline(String memberId, LocalDate periodStart);
}
