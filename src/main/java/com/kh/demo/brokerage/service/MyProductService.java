package com.kh.demo.brokerage.service;

import com.kh.demo.brokerage.dto.MyProductSummaryDto;

public interface MyProductService {

    // 로그인 회원의 전체 계좌를 합산한 상품(펀드/채권/ELS) 보유 요약
    MyProductSummaryDto getMyProductSummary(String memberId);
}
