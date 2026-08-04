package com.kh.demo.brokerage.mapper;

import com.kh.demo.brokerage.dto.ProductHoldingDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ProductHoldingMapper {

    // 특정 계좌의 금융상품 보유내역
    List<ProductHoldingDto> selectHoldingsByAccount(Long accountId);
}
