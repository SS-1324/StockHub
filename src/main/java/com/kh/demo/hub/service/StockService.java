package com.kh.demo.hub.service;

import com.kh.demo.hub.dto.CandleDto;

import java.util.List;

public interface StockService {
    // 종목 코드로 캔들 데이터 목록 조회
    
    List<CandleDto> getCandles(String code);
}
