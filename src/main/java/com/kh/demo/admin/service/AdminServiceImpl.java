package com.kh.demo.admin.service;

import com.kh.demo.admin.dto.AdminDashboardDto;
import com.kh.demo.admin.dto.AdminLogDto;
import com.kh.demo.admin.mapper.AdminMapper;
import com.kh.demo.community.dto.BoardCommentDto;
import com.kh.demo.community.dto.BoardDto;
import com.kh.demo.community.service.BoardCommentService;
import com.kh.demo.community.service.BoardService;
import com.kh.demo.dictionary.dto.GlossaryDto;
import com.kh.demo.inquiry.dto.InquiryDto;
import com.kh.demo.inquiry.service.InquiryService;
import com.kh.demo.member.dto.MemberDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;

// 관리자 입력값 검사, 상태 변경, 작업 이력 저장을 한 곳에서 처리
@Service
@Transactional(readOnly = true)
public class AdminServiceImpl implements AdminService {

    private static final Set<String> MEMBER_STATUSES = Set.of("ACTIVE", "RESTRICTED");
    private static final Set<String> MEMBER_ROLES = Set.of("USER", "ADMIN");

    private final AdminMapper adminMapper;
    private final BoardService boardService;
    private final BoardCommentService boardCommentService;
    private final InquiryService inquiryService;

    public AdminServiceImpl(AdminMapper adminMapper,
                            BoardService boardService,
                            BoardCommentService boardCommentService,
                            InquiryService inquiryService) {
        this.adminMapper = adminMapper;
        this.boardService = boardService;
        this.boardCommentService = boardCommentService;
        this.inquiryService = inquiryService;
    }

    @Override
    public AdminDashboardDto getDashboard() {
        return adminMapper.selectDashboard();
    }

    @Override
    public List<MemberDto> getMembers() {
        return adminMapper.selectMembers();
    }

    @Override
    public List<BoardDto> getBoards() {
        return adminMapper.selectBoards();
    }

    @Override
    public List<BoardCommentDto> getComments() {
        return adminMapper.selectComments();
    }

    @Override
    public List<InquiryDto> getInquiries() {
        return inquiryService.getAllInquiries();
    }

    @Override
    public List<GlossaryDto> getGlossaryTerms() {
        return adminMapper.selectGlossaryTerms();
    }

    @Override
    public List<AdminLogDto> getAdminLogs() {
        return adminMapper.selectAdminLogs();
    }

    @Override
    @Transactional
    public void updateMemberStatus(String adminId,
                                   String memberId,
                                   String memberStatus) {
        requireDifferentMember(adminId, memberId);
        MemberDto target = requireMember(memberId);
        String status = normalizeChoice(memberStatus, MEMBER_STATUSES, "회원 상태");

        if (adminMapper.updateMemberStatus(memberId, status) != 1) {
            throw new IllegalStateException("회원 상태를 변경하지 못했습니다.");
        }
        log(adminId, "STATUS_CHANGE", "MEMBER", memberId,
                target.getMemberStatus() + " → " + status);
    }

    @Override
    @Transactional
    public void updateMemberRole(String adminId,
                                 String memberId,
                                 String memberRole) {
        requireDifferentMember(adminId, memberId);
        MemberDto target = requireMember(memberId);
        String role = normalizeChoice(memberRole, MEMBER_ROLES, "회원 권한");

        if (adminMapper.updateMemberRole(memberId, role) != 1) {
            throw new IllegalStateException("회원 권한을 변경하지 못했습니다.");
        }
        log(adminId, "ROLE_CHANGE", "MEMBER", memberId,
                target.getMemberRole() + " → " + role);
    }

    @Override
    @Transactional
    public void updateBoardHidden(String adminId, Long boardId, boolean hidden) {
        if (adminMapper.updateBoardHidden(boardId, hidden) != 1) {
            throw new IllegalStateException("게시글을 찾을 수 없습니다.");
        }
        log(adminId, hidden ? "HIDE" : "RESTORE", "BOARD",
                String.valueOf(boardId), hidden ? "게시글 숨김" : "게시글 공개 복원");
    }

    @Override
    @Transactional
    public void deleteBoard(String adminId, Long boardId) {
        boardService.deleteAsAdmin(boardId);
        log(adminId, "DELETE", "BOARD", String.valueOf(boardId), "게시글 영구 삭제");
    }

    @Override
    @Transactional
    public void updateCommentHidden(String adminId, Long commentId, boolean hidden) {
        if (adminMapper.updateCommentHidden(commentId, hidden) != 1) {
            throw new IllegalStateException("댓글을 찾을 수 없습니다.");
        }
        log(adminId, hidden ? "HIDE" : "RESTORE", "COMMENT",
                String.valueOf(commentId), hidden ? "댓글 숨김" : "댓글 공개 복원");
    }

    @Override
    @Transactional
    public void deleteComment(String adminId, Long boardId, Long commentId) {
        boardCommentService.deleteAsAdmin(boardId, commentId);
        log(adminId, "DELETE", "COMMENT", String.valueOf(commentId), "댓글 영구 삭제");
    }

    @Override
    @Transactional
    public void replyInquiry(String adminId, Long inquiryId, String answer) {
        inquiryService.answerInquiry(inquiryId, adminId, answer);
        log(adminId, "ANSWER", "INQUIRY", String.valueOf(inquiryId), "문의 답변 등록");
    }

    @Override
    @Transactional
    public void completeInquiry(String adminId, Long inquiryId) {
        if (adminMapper.completeInquiry(inquiryId, adminId) != 1) {
            throw new IllegalStateException("답변 완료 상태의 문의만 처리 완료할 수 있습니다.");
        }
        log(adminId, "COMPLETE", "INQUIRY", String.valueOf(inquiryId), "문의 처리 완료");
    }

    @Override
    @Transactional
    public void deleteInquiry(String adminId, Long inquiryId) {
        inquiryService.deleteInquiry(inquiryId);
        log(adminId, "DELETE", "INQUIRY", String.valueOf(inquiryId), "문의 영구 삭제");
    }

    @Override
    @Transactional
    public void createGlossary(String adminId, GlossaryDto glossaryDto) {
        normalizeGlossary(glossaryDto);
        if (adminMapper.countGlossaryTermExceptCurrent(glossaryDto.getTerm(), null) > 0) {
            throw new IllegalStateException("이미 등록된 용어입니다.");
        }
        if (adminMapper.insertGlossary(glossaryDto) != 1) {
            throw new IllegalStateException("용어를 추가하지 못했습니다.");
        }
        log(adminId, "CREATE", "GLOSSARY", String.valueOf(glossaryDto.getTermId()),
                glossaryDto.getTerm() + " 추가");
    }

    @Override
    @Transactional
    public void updateGlossary(String adminId, GlossaryDto glossaryDto) {
        if (glossaryDto.getTermId() == null) {
            throw new IllegalStateException("용어 번호가 필요합니다.");
        }
        normalizeGlossary(glossaryDto);
        if (adminMapper.countGlossaryTermExceptCurrent(
                glossaryDto.getTerm(), glossaryDto.getTermId()) > 0) {
            throw new IllegalStateException("이미 등록된 용어입니다.");
        }
        if (adminMapper.updateGlossary(glossaryDto) != 1) {
            throw new IllegalStateException("용어를 찾을 수 없습니다.");
        }
        log(adminId, "UPDATE", "GLOSSARY", String.valueOf(glossaryDto.getTermId()),
                glossaryDto.getTerm() + " 수정");
    }

    @Override
    @Transactional
    public void deleteGlossary(String adminId, Long termId) {
        if (adminMapper.deleteGlossary(termId) != 1) {
            throw new IllegalStateException("용어를 찾을 수 없습니다.");
        }
        log(adminId, "DELETE", "GLOSSARY", String.valueOf(termId), "용어 삭제");
    }

    private MemberDto requireMember(String memberId) {
        MemberDto member = adminMapper.selectMember(memberId);
        if (member == null) {
            throw new IllegalStateException("회원을 찾을 수 없습니다.");
        }
        return member;
    }

    private void requireDifferentMember(String adminId, String memberId) {
        if (adminId != null && adminId.equals(memberId)) {
            throw new IllegalStateException("현재 로그인한 관리자 자신의 상태나 권한은 변경할 수 없습니다.");
        }
    }

    private String normalizeChoice(String value, Set<String> allowed, String label) {
        String normalized = value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
        if (!allowed.contains(normalized)) {
            throw new IllegalStateException(label + " 값이 올바르지 않습니다.");
        }
        return normalized;
    }

    private String requireText(String value, String label, int maxLength) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty()) {
            throw new IllegalStateException(label + "을(를) 입력해주세요.");
        }
        if (normalized.length() > maxLength) {
            throw new IllegalStateException(label + "은(는) " + maxLength + "자 이내여야 합니다.");
        }
        return normalized;
    }

    private void normalizeGlossary(GlossaryDto glossaryDto) {
        glossaryDto.setTerm(requireText(glossaryDto.getTerm(), "용어", 100));
        glossaryDto.setDefinition(requireText(glossaryDto.getDefinition(), "설명", 5000));
        glossaryDto.setCategory(requireText(glossaryDto.getCategory(), "분류", 20));
    }

    private void log(String adminId,
                     String actionType,
                     String targetType,
                     String targetId,
                     String detail) {
        AdminLogDto log = new AdminLogDto();
        log.setAdminId(adminId);
        log.setActionType(actionType);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setDetail(detail);
        adminMapper.insertAdminLog(log);
    }
}
