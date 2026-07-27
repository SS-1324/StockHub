package com.kh.demo.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

// 회원 정보를 계층 사이에서 전달
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"memberPwd", "memberPwdConfirm"})
public class MemberDto {

    private String memberId; // 로그인 아이디
    private String memberPwd; // 암호화된 비밀번호
    private String memberPwdConfirm; // 회원가입 비밀번호 확인
    private String memberName; // 회원 이름
    private String nickname; // 화면에 표시할 닉네임
    private String email; // 회원 이메일
    private String profile; // 프로필 이미지 웹 경로
    private Boolean profilePublic; // 프로필 공개 여부
    private Boolean wordTooltip; // 주식 용어 툴팁 사용 여부
    private String brokerage; // 선택한 증권사 이름
    private String accountNo; // 숫자로만 된 계좌번호
    private LocalDateTime createAt; // 가입 일시
    private String createAtStr; // 화면용 가입 일시
}
