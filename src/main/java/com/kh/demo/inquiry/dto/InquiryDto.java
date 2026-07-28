package com.kh.demo.inquiry.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// 회원 문의 정보를 계층 사이에서 전달
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InquiryDto {

    private Long inquiryId; // 문의 번호
    private String memberId; // 문의를 작성한 회원 아이디
    private String nickname; // 관리자 화면에 표시할 회원 닉네임
    private String title; // 20자 이내 문의 제목
    private String content; // 200자 이내 문의 내용
    private String status; // 문의 처리 상태
    private String answer; // 관리자가 작성한 답변
    private String answeredBy; // 답변한 관리자 아이디
    private String answeredByNickname; // 답변한 관리자 닉네임
    private LocalDateTime createAt; // 문의 접수 일시
    private String createAtStr; // 화면에 표시할 접수 일시
    private LocalDateTime answeredAt; // 관리자 답변 일시
    private String answeredAtStr; // 화면에 표시할 답변 일시
}
