package com.kh.demo.brokerage.mapper;

import com.kh.demo.brokerage.dto.StockDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface StockMapper {

    // 전체 상품 일괄 조회 (금융거래 허브 "상품 일괄 조회" 기능용) - 모든 증권사에서 공통으로 거래 가능
    List<StockDto> selectAllStocks();

    // 종목코드로 상품 단건 조회 (매매 체결시 현재가 조회에 사용)
    StockDto selectStockByCode(String stockCode);
}
