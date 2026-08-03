package com.kh.demo.brokerage.service;

import com.kh.demo.brokerage.dto.FinancialProductDto;

import java.util.List;

public interface FinancialProductService {

    // 증권사별 상품 모아보기 (F-BNK-01-01) - brokerageId/productType은 선택 필터
    List<FinancialProductDto> getProducts(Long brokerageId, String productType);
}
