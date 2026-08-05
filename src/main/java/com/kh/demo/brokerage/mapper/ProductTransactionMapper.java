package com.kh.demo.brokerage.mapper;

import com.kh.demo.brokerage.dto.ProductTransactionDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ProductTransactionMapper {

    // 특정 계좌의 금융상품 가입/환매 이력 (from/to는 선택 필터, 둘 다 null이면 전체)
    List<ProductTransactionDto> selectTransactionsByAccount(@Param("accountId") Long accountId,
                                                              @Param("from") LocalDate from,
                                                              @Param("to") LocalDate to);

    // 과거 시각을 직접 지정해 거래이력을 등록 (데모 데이터 생성기 전용)
    int insertTransaction(ProductTransactionDto productTransactionDto);

    // 계좌의 상품 거래이력을 전부 삭제 (데모 데이터 생성기가 재생성 전 초기화할 때 사용)
    int deleteTransactionsByAccount(Long accountId);
}
