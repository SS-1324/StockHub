package com.kh.demo.brokerage.mapper;

import com.kh.demo.brokerage.dto.FinancialProductDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FinancialProductMapper {

    // 증권사별 상품 모아보기 - brokerageId/productType은 선택 필터(둘 다 null이면 전체 상품)
    List<FinancialProductDto> selectProducts(@Param("brokerageId") Long brokerageId,
                                             @Param("productType") String productType);
}
