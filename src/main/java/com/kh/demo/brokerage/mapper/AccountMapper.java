package com.kh.demo.brokerage.mapper;

import com.kh.demo.brokerage.dto.AccountDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AccountMapper {

    // 계좌 개설 (증권사에 완전히 새 계좌를 만드는 경우. account_no/owner_name까지 채워서 넘겨야 함)
    int insertAccount(AccountDto accountDto);

    // 회원이 연동한 가상 계좌 목록 (여러 증권사에 각각 하나씩 연동할 수 있음)
    List<AccountDto> selectAccountsByMember(String memberId);

    // 계좌 단건 조회
    AccountDto selectAccountById(Long accountId);

    // 증권사+계좌번호로 단건 조회 (연동 여부 무관) - 파트너 API 클라이언트가 외부 식별자(accountNo)를 내부 PK로 되짚을 때 사용
    AccountDto selectAccountByAccountNo(@Param("brokerageId") Long brokerageId,
                                         @Param("accountNo") String accountNo);

    // 특정 증권사의 "아직 연동되지 않은" 계좌 목록 (연동 후보 선택 화면용)
    List<AccountDto> selectUnlinkedAccountsByBrokerage(Long brokerageId);

    // 계좌번호+예금주명+증권사로 미연동 계좌를 찾음 (연동 전 본인확인용)
    AccountDto selectUnlinkedAccount(@Param("brokerageId") Long brokerageId,
                                      @Param("accountNo") String accountNo,
                                      @Param("ownerName") String ownerName);

    // 미연동 계좌를 회원에게 연동(claim) 처리
    int linkAccount(@Param("accountId") Long accountId, @Param("memberId") String memberId);

    // 잔고 갱신 (매수/매도 체결시 사용)
    int updateBalance(@Param("accountId") Long accountId, @Param("balance") Long balance);
}
