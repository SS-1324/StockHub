package com.kh.demo.brokerage.service;

import com.kh.demo.brokerage.dto.ProductHoldingDto;

import java.util.List;

public interface ProductHoldingService {

    // 계좌 소유자 검증 후 금융상품(펀드/채권/ELS) 보유내역 조회
    List<ProductHoldingDto> getHoldings(String memberId, Long accountId);
}
