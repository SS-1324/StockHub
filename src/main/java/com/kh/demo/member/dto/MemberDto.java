package com.kh.demo.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "memberPwd")
public class MemberDto {

    private String memberId;
    private String memberPwd;
    private String memberName;
    private String nickname;
    private String email;
    private String profile;
    private LocalDateTime createAt;
    private String createAtStr;
}
