package com.kh.demo.hub.dto;

// 실제 증권사 홈페이지로 이동하는 버튼에 쓰는 정보.
// feeRate는 참고용 예시 수치이며, 실제 증권사 공시 수수료와 다를 수 있음.
public record BrokerLinkDto(String name, double feeRate, String url) {
}
