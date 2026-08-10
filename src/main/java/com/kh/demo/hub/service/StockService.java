package com.kh.demo.hub.service;

// 거래 허브 차트 페이지의 요청 파라미터(종목 코드/캔들 주기) 기본값 결정을 담당
public interface StockService {

    // 종목 코드가 없거나 빈 값이면 기본 종목 코드로 대체
    String resolveCode(String code);

    // 캔들 주기가 없거나 빈 값이면 기본 주기(day)로 대체
    String resolvePeriod(String period);
}
