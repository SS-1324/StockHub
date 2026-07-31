package com.kh.demo.brokerage.service;

import com.kh.demo.brokerage.dto.MyStockSummaryDto;

public interface MyStockService {

    // 로그인 회원의 모든 계좌를 합산한 보유 주식과 수익 정보를 조회
    MyStockSummaryDto getMyStockSummary(String memberId);
}
