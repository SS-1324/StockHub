package com.kh.demo.member.dto;

import lombok.Getter;
import lombok.Setter;

// 프로필 수정 입력값을 전달
@Getter
@Setter
public class ProfileUpdateDto {

    private String memberId; // 로그인 회원 아이디
    private String memberName; // 변경할 이름
    private String nickname; // 변경할 닉네임
    private String newPassword; // 변경할 비밀번호
    private String newPasswordConfirm; // 변경 비밀번호 확인
    private Boolean stockPublic; // 내 주식 정보 공개 여부
    private Boolean wordTooltip; // 주식 용어 툴팁 사용 여부
}
