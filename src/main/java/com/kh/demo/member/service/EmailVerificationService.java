package com.kh.demo.member.service;

// 이메일 인증 기능에서 사용할 메서드를 정의
public interface EmailVerificationService {

    // 개발용 이메일 인증번호를 생성
    String createDevelopmentCode(String email);

    // 이메일과 인증번호가 일치하는지 확인
    boolean verifyCode(String email, String code);

    // 이메일을 소문자로 정리하고 형식을 검사
    String normalizeAndValidateEmail(String email);
}