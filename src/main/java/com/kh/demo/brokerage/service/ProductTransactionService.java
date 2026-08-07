package com.kh.demo.brokerage.service;

import com.kh.demo.brokerage.dto.ProductTransactionDto;

import java.time.LocalDate;
import java.util.List;

public interface ProductTransactionService {

    // 계좌 소유자 검증 후 금융상품 가입/환매 이력 조회 (from/to는 선택 필터)
    List<ProductTransactionDto> getTransactions(String memberId, Long accountId, LocalDate from, LocalDate to);

    // 로그인 회원의 모든 계좌를 통틀어 최근 상품 거래이력 조회 (대시보드 타임라인용, 소유자 검증 불필요 - 이미 memberId 기준)
    List<ProductTransactionDto> getMyTransactions(String memberId);
}
