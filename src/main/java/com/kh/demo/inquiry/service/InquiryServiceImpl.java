package com.kh.demo.inquiry.service;

import com.kh.demo.inquiry.dto.InquiryDto;
import com.kh.demo.inquiry.mapper.InquiryMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// 문의 입력값 검사와 DB 저장을 처리
@Service
public class InquiryServiceImpl implements InquiryService {

    private static final int MAX_TITLE_LENGTH = 20;
    private static final int MAX_CONTENT_LENGTH = 200;
    private static final int MAX_ANSWER_LENGTH = 500;

    private final InquiryMapper inquiryMapper;

    public InquiryServiceImpl(InquiryMapper inquiryMapper) {
        this.inquiryMapper = inquiryMapper;
    }

    @Override
    @Transactional
    public void createInquiry(String memberId, String title, String content) {
        String normalizedTitle = title == null ? "" : title.trim();
        String normalizedContent = content == null ? "" : content.trim();

        if (normalizedTitle.isEmpty()) {
            throw new IllegalStateException("문의 제목을 입력해주세요.");
        }
        if (normalizedTitle.length() > MAX_TITLE_LENGTH) {
            throw new IllegalStateException("문의 제목은 20자 이내로 입력해주세요.");
        }
        if (normalizedContent.isEmpty()) {
            throw new IllegalStateException("문의 내용을 입력해주세요.");
        }
        if (normalizedContent.length() > MAX_CONTENT_LENGTH) {
            throw new IllegalStateException("문의 내용은 200자 이내로 입력해주세요.");
        }

        InquiryDto inquiry = new InquiryDto();
        inquiry.setMemberId(memberId);
        inquiry.setTitle(normalizedTitle);
        inquiry.setContent(normalizedContent);

        if (inquiryMapper.insertInquiry(inquiry) != 1) {
            throw new IllegalStateException("문의 등록에 실패했습니다.");
        }
    }

    @Override
    public List<InquiryDto> getAllInquiries() {
        return inquiryMapper.selectAllInquiries();
    }

    @Override
    public List<InquiryDto> getMemberInquiries(String memberId) {
        return inquiryMapper.selectInquiriesByMemberId(memberId);
    }

    @Override
    @Transactional
    public void answerInquiry(Long inquiryId,
                              String answeredBy,
                              String answer) {
        String normalizedAnswer = answer == null ? "" : answer.trim();

        if (normalizedAnswer.isEmpty()) {
            throw new IllegalStateException("답변 내용을 입력해주세요.");
        }
        if (normalizedAnswer.length() > MAX_ANSWER_LENGTH) {
            throw new IllegalStateException("답변은 500자 이내로 입력해주세요.");
        }

        // 먼저 처리한 관리자 답변이 있으면 덮어쓰지 않음
        int updatedRows = inquiryMapper.answerInquiry(
                inquiryId,
                answeredBy,
                normalizedAnswer
        );
        if (updatedRows != 1) {
            throw new IllegalStateException(
                    "이미 처리된 문의이거나 문의를 찾을 수 없습니다."
            );
        }
    }

    @Override
    @Transactional
    public void deleteInquiry(Long inquiryId) {
        if (inquiryMapper.deleteInquiry(inquiryId) != 1) {
            throw new IllegalStateException("삭제할 문의를 찾을 수 없습니다.");
        }
    }
}
