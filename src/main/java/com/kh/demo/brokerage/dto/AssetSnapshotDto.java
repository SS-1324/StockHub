package com.kh.demo.brokerage.dto;

import lombok.*;

import java.time.LocalDate;

/*
*   AssetSnapshotDto : asset_snapshot 테이블과 1:1로 대응되는 클래스
*
*   실제 증권사가 매일 밤 배치로 그날 마감 총자산을 저장해두는 것과 같은 역할.
*   기간별(1주/1달/1년) 손익을 "그때 총자산과 지금 총자산의 차이"로 정확히 계산하기 위해 필요하다.
* */

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AssetSnapshotDto {

    private Long snapshotId;
    private Long accountId;
    private LocalDate snapshotDate;
    private long totalAsset;
}
