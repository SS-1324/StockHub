package com.kh.demo.member.dto;

import lombok.Getter;
import lombok.Setter;

// 증권사 선택 목록을 전달
@Getter
@Setter
public class BrokerageDto {

    private Long brokerageId; // 증권사 번호
    private String brokerageName; // 증권사 이름
}
