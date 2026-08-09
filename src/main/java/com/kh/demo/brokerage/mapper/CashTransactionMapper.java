package com.kh.demo.brokerage.mapper;

import com.kh.demo.brokerage.dto.CashTransactionDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface CashTransactionMapper {

    // 특정 계좌의 입출금 이력 (from/to는 선택 필터, 둘 다 null이면 전체)
    List<CashTransactionDto> selectTransactionsByAccount(@Param("accountId") Long accountId,
                                                           @Param("from") LocalDate from,
                                                           @Param("to") LocalDate to);

    // 로그인 회원의 모든 계좌를 통틀어 입출금이력 조회 (대시보드 타임라인용)
    List<CashTransactionDto> selectTransactionsByMember(String memberId);

    // 과거 시각을 직접 지정해 입출금이력을 등록 (데모 데이터 생성기 전용)
    int insertTransaction(CashTransactionDto cashTransactionDto);

    // 계좌의 입출금이력을 전부 삭제 (데모 데이터 생성기가 재생성 전 초기화할 때 사용)
    int deleteTransactionsByAccount(Long accountId);

    // 회원의 모든 연동 계좌를 통틀어 순입금액(입금-출금) 합계. since가 null이면 전체 기간(=총 투자원금)
    long sumNetDeposits(@Param("memberId") String memberId, @Param("since") LocalDate since);

    // 기간별 손익 계산 전용: 계좌별 "기준 스냅샷 날짜" 이후의 순입금액만 합산.
    // (기간 시작일이 아니라 실제 스냅샷 날짜를 기준으로 삼아야, 스냅샷에 이미 반영된 입출금이
    //  순입금액에서 다시 한번 빠지는 이중 차감을 막을 수 있다)
    long sumNetDepositsSinceBaseline(@Param("memberId") String memberId, @Param("periodStart") LocalDate periodStart);
}
