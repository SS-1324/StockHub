package com.kh.demo.brokerage.partner.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/*
*   PartnerApiResponse : 제휴 증권사 API가 돌려준다고 가정한 응답 봉투(envelope).
*
*   실제 외부 API를 부르는 게 아니라 우리 DB를 읽어오는 것뿐이지만("전제"이지 실제 통신이 아님),
*   나중에 진짜 파트너사 HTTP API로 교체될 걸 가정해서 결과코드/조회시각까지 담는 모양으로 맞춰뒀다.
*   실제 파트너사라면 이 시점에 인증 실패, 타임아웃 같은 실패 응답도 있을 수 있는 자리.
* */

@Getter
@AllArgsConstructor
public class PartnerApiResponse<T> {

    private String resultCode;       // 파트너사 응답 코드 (예: "0000" = 정상)
    private String resultMessage;    // 파트너사 응답 메시지
    private LocalDateTime retrievedAt; // 파트너사로부터 조회한 시각
    private List<T> data;            // 실제 데이터 목록

    public static <T> PartnerApiResponse<T> ok(List<T> data) {
        return new PartnerApiResponse<>("0000", "OK", LocalDateTime.now(), data);
    }
}
