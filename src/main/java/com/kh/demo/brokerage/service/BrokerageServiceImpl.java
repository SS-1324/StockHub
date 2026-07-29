package com.kh.demo.brokerage.service;

import com.kh.demo.brokerage.dto.BrokerageDto;
import com.kh.demo.brokerage.dto.StockDto;
import com.kh.demo.brokerage.mapper.BrokerageMapper;
import com.kh.demo.brokerage.mapper.StockMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BrokerageServiceImpl implements BrokerageService {

    @Autowired
    private BrokerageMapper brokerageMapper;

    @Autowired
    private StockMapper stockMapper;

    @Override
    public List<BrokerageDto> getAllBrokerages() {
        return brokerageMapper.selectAllBrokerages();
    }

    @Override
    public List<StockDto> getAllStocks() {
        return stockMapper.selectAllStocks();
    }

    @Override
    public StockDto getStock(String stockCode) {
        return stockMapper.selectStockByCode(stockCode);
    }
}
