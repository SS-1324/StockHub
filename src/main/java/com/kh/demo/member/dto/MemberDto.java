package com.kh.demo.member.dto;

import lombok.*;

import java.time.LocalDateTime;


/*
 *   DTO : 계층간에 데이터를 주고받기위한 전달용 데이터
 *
 *   MemberDto : Member테이블과 1:1로 대응되는 클래스
 * */

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MemberDto {

    private String memberId;
    private String memberPwd;
    private String memberName;
    private String nickname;
    private String email;
    private String profile;
    private LocalDateTime createAt;

    // jsp에서는 DATE만 포맷팅할 수 있고
    // LocalDateTime를 지원하지 않는다.
    // JSP에서 처리하려면 코드가 지저분 해지기때문에 sql에서 그대로 변환해서 받아줌.
    private String createAtStr; //가입일시(화면표시용 문자열)


}
