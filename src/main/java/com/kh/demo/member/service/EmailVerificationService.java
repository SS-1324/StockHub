package com.kh.demo.member.service;

// 이메일 인증 기능에서 사용할 메서드를 정의
public interface EmailVerificationService {

    // 3분 동안 사용할 이메일 인증번호를 생성하고 실제 메일로 발송
    void sendVerificationCode(String email);

    // 이메일과 인증번호의 확인 결과를 반환
    EmailVerificationResult verifyCode(String email, String code);

    // 이메일을 소문자로 정리하고 형식을 검사
    String normalizeAndValidateEmail(String email);
}
