package com.kh.demo.member.mapper;

import com.kh.demo.member.dto.PasswordResetTokenDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

// 비밀번호 재설정용 이메일 인증·토큰·비밀번호 SQL을 연결
@Mapper
public interface PasswordResetTokenMapper {

    // 이메일에 해당하는 회원 아이디를 조회
    String selectMemberIdByEmail(@Param("email") String email);

    // 회원의 이전 비밀번호 재설정 인증 코드를 만료 처리
    int expireEmailVerificationCodes(@Param("memberId") String memberId);

    // 비밀번호 재설정용 이메일 인증 코드를 저장
    int insertEmailVerification(@Param("memberId") String memberId,
                                @Param("email") String email,
                                @Param("code") String code);

    // 유효한 비밀번호 재설정 인증 코드를 인증 완료로 변경
    int verifyEmailCode(@Param("memberId") String memberId,
                        @Param("email") String email,
                        @Param("code") String code);

    // 회원에게 발급된 이전 재설정 토큰을 사용 완료로 변경
    int expirePasswordResetTokens(@Param("memberId") String memberId);

    // 새 일회성 재설정 토큰을 저장
    int insertPasswordResetToken(@Param("memberId") String memberId,
                                 @Param("token") String token);

    // 아직 사용하지 않았고 만료되지 않은 토큰을 조회
    PasswordResetTokenDto selectValidToken(@Param("token") String token);

    // 현재 저장된 암호화 비밀번호를 조회
    String selectEncodedPassword(@Param("memberId") String memberId);

    // 새 암호화 비밀번호를 저장
    int updateMemberPassword(@Param("memberId") String memberId,
                             @Param("memberPwd") String memberPwd);

    // 사용한 토큰을 다시 사용할 수 없도록 처리
    int markTokenUsed(@Param("token") String token);
}
