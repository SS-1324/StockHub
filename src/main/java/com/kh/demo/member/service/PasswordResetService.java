package com.kh.demo.member.service;

// 이메일 인증과 일회성 토큰을 이용한 비밀번호 재설정 기능을 정의
public interface PasswordResetService {

    // 개발용 이메일 인증 코드를 생성
    String createDevelopmentCode(String email);

    // 이메일 인증 코드가 맞으면 일회성 재설정 토큰을 발급
    String verifyCodeAndCreateToken(String email, String code);

    // 유효한 일회성 토큰으로 새 비밀번호를 저장
    void resetPassword(String token,
                       String newPassword,
                       String newPasswordConfirm);
}
