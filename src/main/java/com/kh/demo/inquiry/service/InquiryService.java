package com.kh.demo.inquiry.service;

import com.kh.demo.inquiry.dto.InquiryDto;

import java.util.List;

// 문의 등록, 조회, 답변, 삭제 기능을 정의
public interface InquiryService {

    // 로그인 회원의 문의를 저장
    void createInquiry(String memberId, String title, String content);

    // 모든 문의를 최신순으로 반환
    List<InquiryDto> getAllInquiries();

    // 로그인 회원이 작성한 문의만 최신순으로 반환
    List<InquiryDto> getMemberInquiries(String memberId);

    // 관리자의 답변을 저장하고 처리 완료 상태로 변경
    void answerInquiry(Long inquiryId, String answeredBy, String answer);

    // 선택한 문의를 DB에서 완전히 삭제
    void deleteInquiry(Long inquiryId);
}
