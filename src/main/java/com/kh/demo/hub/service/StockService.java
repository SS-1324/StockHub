package com.kh.demo.hub.service;

import com.kh.demo.hub.dto.CandleDto;
import com.kh.demo.hub.dto.StockRankingDto;

import java.util.List;

public interface StockService {
    // 종목 코드로 캔들 데이터 목록 조회

    List<CandleDto> getCandles(String code);

    // 등락률 상위 종목 랭킹 조회 (count: 조회할 종목 수)
    List<StockRankingDto> getTopGainers(int count);
}
