package com.kh.demo.brokerage.mapper;

import com.kh.demo.brokerage.dto.BrokerageDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface BrokerageMapper {

    // 가상 증권사 전체 목록
    List<BrokerageDto> selectAllBrokerages();

    // 가상 증권사 단건 조회(수수료율 계산 등에 사용)
    BrokerageDto selectBrokerageById(Long brokerageId);
}
