package com.kh.demo.brokerage.service;

import com.kh.demo.brokerage.dto.ProductTransactionDto;

import java.time.LocalDate;
import java.util.List;

public interface ProductTransactionService {

    // 계좌 소유자 검증 후 금융상품 가입/환매 이력 조회 (from/to는 선택 필터)
    List<ProductTransactionDto> getTransactions(String memberId, Long accountId, LocalDate from, LocalDate to);
}
