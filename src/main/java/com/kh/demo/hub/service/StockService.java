package com.kh.demo.hub.service;

import com.kh.demo.brokerage.dto.StockDto;

import java.util.List;

// 거래 허브 차트 페이지의 요청 파라미터(종목 코드/캔들 주기) 기본값 결정과 종목 검색을 담당
public interface StockService {

    // 종목 코드가 없거나 빈 값이면 기본 종목 코드로 대체
    String resolveCode(String code);

    // 캔들 주기가 없거나 빈 값이면 기본 주기(day)로 대체
    String resolvePeriod(String period);

    // 종목 코드로 단건 조회 (없으면 null)
    StockDto findStock(String code);

    // 코드/이름에 keyword가 포함된 해외 종목 검색 (검색창 자동완성용)
    List<StockDto> searchStocks(String keyword);

    // 빠른전환 버튼 5종목의 거래소 정보를 조회 (버튼 클릭 시 트레이딩뷰 심볼을 조립하는 데 필요)
    List<StockDto> resolveQuickSwitchStocks();
}
