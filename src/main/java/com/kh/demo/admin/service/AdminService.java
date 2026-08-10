package com.kh.demo.admin.service;

import com.kh.demo.admin.dto.AdminDashboardDto;
import com.kh.demo.admin.dto.AdminLogDto;
import com.kh.demo.community.dto.BoardCommentDto;
import com.kh.demo.community.dto.BoardDto;
import com.kh.demo.dictionary.dto.GlossaryDto;
import com.kh.demo.inquiry.dto.InquiryDto;
import com.kh.demo.member.dto.MemberDto;

import java.util.List;

// 관리자 페이지에서 제공할 최소 관리 기능을 정의
public interface AdminService {

    AdminDashboardDto getDashboard();

    List<MemberDto> getMembers();

    List<BoardDto> getBoards();

    List<BoardCommentDto> getComments();

    List<InquiryDto> getInquiries();

    List<GlossaryDto> getGlossaryTerms();

    List<AdminLogDto> getAdminLogs();

    void updateMemberStatus(String adminId, String memberId, String memberStatus);

    void updateMemberRole(String adminId, String memberId, String memberRole);

    void updateBoardHidden(String adminId, Long boardId, boolean hidden);

    void deleteBoard(String adminId, Long boardId);

    void updateCommentHidden(String adminId, Long commentId, boolean hidden);

    void deleteComment(String adminId, Long boardId, Long commentId);

    void replyInquiry(String adminId, Long inquiryId, String answer);

    void completeInquiry(String adminId, Long inquiryId);

    void deleteInquiry(String adminId, Long inquiryId);

    void createGlossary(String adminId, GlossaryDto glossaryDto);

    void updateGlossary(String adminId, GlossaryDto glossaryDto);

    void deleteGlossary(String adminId, Long termId);
}
