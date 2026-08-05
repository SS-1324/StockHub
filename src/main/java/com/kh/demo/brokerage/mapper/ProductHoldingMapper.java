package com.kh.demo.brokerage.mapper;

import com.kh.demo.brokerage.dto.ProductHoldingDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ProductHoldingMapper {

    // 특정 계좌의 금융상품 보유내역
    List<ProductHoldingDto> selectHoldingsByAccount(Long accountId);

    // 보유내역 등록 (데모 데이터 생성기 전용)
    int insertHolding(ProductHoldingDto productHoldingDto);

    // 계좌의 상품 보유내역을 전부 삭제 (데모 데이터 생성기가 재생성 전 초기화할 때 사용)
    int deleteHoldingsByAccount(Long accountId);
}
