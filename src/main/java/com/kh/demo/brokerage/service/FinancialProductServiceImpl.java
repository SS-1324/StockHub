package com.kh.demo.brokerage.service;

import com.kh.demo.brokerage.dto.FinancialProductDto;
import com.kh.demo.brokerage.mapper.FinancialProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FinancialProductServiceImpl implements FinancialProductService {

    @Autowired
    private FinancialProductMapper financialProductMapper;

    @Override
    public List<FinancialProductDto> getProducts(Long brokerageId, String productType) {
        return financialProductMapper.selectProducts(brokerageId, productType);
    }
}
