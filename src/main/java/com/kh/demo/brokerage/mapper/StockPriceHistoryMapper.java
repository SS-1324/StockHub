package com.kh.demo.brokerage.mapper;

import com.kh.demo.brokerage.dto.StockPriceHistoryDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface StockPriceHistoryMapper {

    // 특정 종목의 시세 이력 (오래된 순)
    List<StockPriceHistoryDto> selectHistoryByStock(String stockCode);

    // 특정 종목의 특정 시점 이전 최신 시세 (모멘텀/추이 계산용)
    StockPriceHistoryDto selectLatestAsOf(@Param("stockCode") String stockCode, @Param("asOf") LocalDateTime asOf);

    // 데모 데이터 생성기 전용 - 재실행 시 전체를 지우고 다시 채운다
    int deleteAll();

    // 데모 데이터 생성기 전용 - 여러 시점을 한 번에 등록
    int insertBatch(List<StockPriceHistoryDto> history);
}
