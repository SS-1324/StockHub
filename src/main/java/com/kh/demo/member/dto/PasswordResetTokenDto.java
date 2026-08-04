package com.kh.demo.member.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

// 비밀번호 재설정에 사용하는 일회성 토큰 정보를 전달
@Getter
@Setter
public class PasswordResetTokenDto {

    private Long tokenId; // 토큰 번호
    private String memberId; // 비밀번호를 재설정할 회원 아이디
    private String token; // 일회성 토큰 문자열
    private Boolean used; // 토큰 사용 여부
    private LocalDateTime expiredAt; // 토큰 만료일시
    private LocalDateTime createAt; // 토큰 생성일시
}
