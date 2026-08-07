package com.kh.demo.hub.dto;

import lombok.Getter;
import lombok.Setter;

// 투표 등록/변경 요청 바디 - { "voteType": "UP" | "DOWN" }
@Getter
@Setter
public class VoteRequestDto {

    private String voteType;
}
