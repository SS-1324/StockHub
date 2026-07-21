package com.kh.demo.brokerage.service;

import com.kh.demo.brokerage.dto.BrokerageDto;
import com.kh.demo.brokerage.dto.StockDto;

import java.util.List;

public interface BrokerageService {

    // 가상 증권사 전체 목록
    List<BrokerageDto> getAllBrokerages();

    // 전체 상품 일괄 조회 ("상품 일괄 조회" 기능용) - 모든 증권사에서 공통으로 거래 가능
    List<StockDto> getAllStocks();

    // 종목코드로 상품 단건 조회
    StockDto getStock(String stockCode);
}
