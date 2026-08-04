package com.kh.demo.brokerage.mapper;

import com.kh.demo.brokerage.dto.CashTransactionDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface CashTransactionMapper {

    // 특정 계좌의 입출금 이력 (from/to는 선택 필터, 둘 다 null이면 전체)
    List<CashTransactionDto> selectTransactionsByAccount(@Param("accountId") Long accountId,
                                                           @Param("from") LocalDate from,
                                                           @Param("to") LocalDate to);
}
