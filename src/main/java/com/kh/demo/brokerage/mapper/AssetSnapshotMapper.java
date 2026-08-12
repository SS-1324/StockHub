package com.kh.demo.brokerage.mapper;

import com.kh.demo.brokerage.dto.AssetSnapshotDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface AssetSnapshotMapper {

    // 스냅샷 등록 (데모 데이터 생성기 전용)
    int insertSnapshot(@Param("accountId") Long accountId,
                        @Param("snapshotDate") LocalDate snapshotDate,
                        @Param("totalAsset") long totalAsset);

    // 계좌의 스냅샷을 전부 삭제 (데모 데이터 생성기가 재생성 전 초기화할 때 사용)
    int deleteSnapshotsByAccount(Long accountId);

    // 회원의 모든 연동 계좌를 통틀어, 지정한 날짜 "이전(포함)" 가장 최근 스냅샷들의 총자산 합계
    // (계좌별로 가장 최근 것 하나씩만 골라 합산 - 그 날짜에 존재하지 않았던 계좌는 0으로 취급)
    long selectTotalAssetAsOf(@Param("memberId") String memberId, @Param("date") LocalDate date);

    // 회원의 모든 연동 계좌를 통틀어, 날짜별 총자산 합계 추이 (자산 성장 그래프용)
    List<AssetSnapshotDto> selectAssetTrendByMember(String memberId);
}
