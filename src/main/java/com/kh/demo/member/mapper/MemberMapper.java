package com.kh.demo.member.mapper;

import com.kh.demo.member.dto.MemberDto;
import com.kh.demo.member.dto.BrokerageDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

// 회원 SQL을 MemberMapper.xml과 연결
@Mapper
public interface MemberMapper {

    // 회원 정보를 DB에 추가
    int insertMember(MemberDto memberDto);

    // 같은 아이디의 회원 수를 조회
    int countByMemberId(@Param("memberId") String memberId);

    // 같은 닉네임의 회원 수를 조회
    int countByNickname(@Param("nickname") String nickname);

    // 같은 이메일을 사용하는 회원 수를 조회
    int countByEmail(@Param("email") String email);

    // 아이디로 회원 한 명을 조회
    MemberDto selectByMemberId(@Param("memberId") String memberId);

    // 가입한 회원의 기본 설정을 추가
    int insertDefaultSettings(@Param("memberId") String memberId);

    // 현재 회원을 제외한 같은 닉네임 수를 조회
    int countByNicknameExceptMember(@Param("nickname") String nickname,
                                    @Param("memberId") String memberId);

    // 닉네임·비밀번호·프로필 이미지를 수정
    int updateMemberProfile(MemberDto memberDto);

    // 프로필 공개 여부와 툴팁 설정을 저장
    int upsertSettings(MemberDto memberDto);

    // 선택할 수 있는 증권사 목록을 조회
    List<BrokerageDto> selectBrokerages();

    // 증권사 번호가 실제로 존재하는지 확인
    int countByBrokerageId(@Param("brokerageId") Long brokerageId);

    // 다른 계정이 사용하는 계좌번호인지 확인
    int countByAccountNoExceptAccount(@Param("accountNo") String accountNo,
                                      @Param("accountId") Long accountId);

    // 회원에게 새 계좌를 연결
    int insertAccount(MemberDto memberDto);

    // 회원의 기존 계좌를 수정
    int updateAccount(MemberDto memberDto);

    // 현재 DB에 거래 테이블이 있는지 확인
    int countTradeTable();

    // 회원 계좌에 연결된 거래 내역을 삭제
    int deleteTradesByMemberId(@Param("memberId") String memberId);

    // 회원에게 연결된 계좌를 삭제
    int deleteAccountsByMemberId(@Param("memberId") String memberId);

    // 회원 정보를 삭제
    int deleteMemberById(@Param("memberId") String memberId);
}
