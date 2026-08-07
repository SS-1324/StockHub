package com.kh.demo.brokerage.service;

import com.kh.demo.brokerage.dto.CashTransactionDto;

import java.time.LocalDate;
import java.util.List;

public interface CashTransactionService {

    // 계좌 소유자 검증 후 입출금 이력 조회 (from/to는 선택 필터)
    List<CashTransactionDto> getTransactions(String memberId, Long accountId, LocalDate from, LocalDate to);

    // 로그인 회원의 모든 계좌를 통틀어 최근 입출금이력 조회 (대시보드 타임라인용)
    List<CashTransactionDto> getMyTransactions(String memberId);
}
