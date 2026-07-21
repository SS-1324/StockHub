package com.kh.demo.brokerage.dto;

import lombok.*;

import java.time.LocalDateTime;

/*
*   HoldingDto : holding 테이블과 1:1로 대응되는 클래스
*
*   계좌별 "현재" 보유 스냅샷(매매가 일어날 때마다 갱신됨).
* */

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class HoldingDto {

    private Long holdingId;          // 보유내역 번호(PK)
    private Long accountId;          // 가상 계좌 번호
    private String stockCode;        // 종목 코드
    private String stockName;        // 조회 편의를 위한 조인 컬럼
    private Long quantity;           // 보유수량
    private Integer avgPrice;        // 평균매입단가
    private LocalDateTime updateAt;  // 최종 갱신일시
}
