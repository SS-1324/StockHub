package com.kh.demo.member.dto;

import lombok.Getter;
import lombok.Setter;

// 팔로우·팔로잉 목록에 표시할 회원 정보
@Getter
@Setter
public class FollowDto {

    private String memberId; // 목록에 표시할 회원 아이디
    private String nickname; // 목록에 표시할 닉네임
    private String profile; // 프로필 이미지 경로
    private String followAtStr; // 팔로우한 날짜
}
