package com.kh.demo.member.mapper;

import com.kh.demo.member.dto.MemberDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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

    // 이전에 발급한 인증 기록을 무효화
    int expireEmailVerificationCodes(@Param("email") String email);

    // 새 이메일 인증 코드를 저장
    int insertEmailVerification(@Param("email") String email,
                                @Param("code") String code);

    // 이메일과 코드가 일치하면 인증 완료로 변경
    int verifyEmailCode(@Param("email") String email,
                        @Param("code") String code);

    // 가입에 사용할 수 있는 인증 완료 기록 수를 조회
    int countVerifiedEmail(@Param("email") String email);

    // 인증 완료 기록을 가입한 회원과 연결
    int linkVerifiedEmailToMember(@Param("email") String email,
                                  @Param("memberId") String memberId);

    // 아이디로 회원 한 명을 조회
    MemberDto selectByMemberId(@Param("memberId") String memberId);

    // 가입한 회원의 기본 설정을 추가
    int insertDefaultSettings(@Param("memberId") String memberId);

    // 현재 회원을 제외한 같은 닉네임 수를 조회
    int countByNicknameExceptMember(@Param("nickname") String nickname,
                                    @Param("memberId") String memberId);

    // 닉네임·비밀번호·프로필 이미지·증권사·계좌번호를 수정
    int updateMemberProfile(MemberDto memberDto);

    // 프로필 공개 여부와 툴팁 설정을 저장
    int upsertSettings(MemberDto memberDto);

    // 회원 정보를 삭제
    int deleteMemberById(@Param("memberId") String memberId);
}
