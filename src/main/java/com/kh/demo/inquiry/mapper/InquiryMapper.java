package com.kh.demo.inquiry.mapper;

import com.kh.demo.inquiry.dto.InquiryDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

// 문의 기능에서 사용할 SQL을 InquiryMapper.xml과 연결
@Mapper
public interface InquiryMapper {

    // 회원이 작성한 문의를 저장
    int insertInquiry(InquiryDto inquiryDto);

    // 관리자 페이지에 표시할 문의를 최신순으로 조회
    List<InquiryDto> selectAllInquiries();

    // 로그인 회원이 작성한 문의만 최신순으로 조회
    List<InquiryDto> selectInquiriesByMemberId(
            @Param("memberId") String memberId
    );

    // 아직 접수 상태인 문의에 관리자 답변을 한 번만 저장
    int answerInquiry(@Param("inquiryId") Long inquiryId,
                      @Param("answeredBy") String answeredBy,
                      @Param("answer") String answer);

    // 관리자가 선택한 문의 한 건을 DB에서 완전히 삭제
    int deleteInquiry(@Param("inquiryId") Long inquiryId);
}
