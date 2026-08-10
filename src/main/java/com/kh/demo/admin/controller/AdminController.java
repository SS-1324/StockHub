package com.kh.demo.admin.controller;

import com.kh.demo.admin.service.AdminService;
import com.kh.demo.common.SessionConst;
import com.kh.demo.dictionary.dto.GlossaryDto;
import com.kh.demo.member.dto.MemberDto;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// ADMIN 권한 회원에게 전체 관리 화면과 최소 관리 기능을 제공
@Controller
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/admin")
    public String adminPage(Model model) {
        model.addAttribute("dashboard", adminService.getDashboard());
        model.addAttribute("members", adminService.getMembers());
        model.addAttribute("boards", adminService.getBoards());
        model.addAttribute("comments", adminService.getComments());
        model.addAttribute("inquiries", adminService.getInquiries());
        model.addAttribute("glossaryTerms", adminService.getGlossaryTerms());
        model.addAttribute("adminLogs", adminService.getAdminLogs());
        return "admin/index";
    }

    @PostMapping("/admin/member/{memberId}/status")
    public String updateMemberStatus(@PathVariable String memberId,
                                     @RequestParam String memberStatus,
                                     HttpSession session,
                                     RedirectAttributes ra) {
        return handle("members", "회원 이용 상태를 변경했습니다.", ra,
                () -> adminService.updateMemberStatus(
                        adminId(session), memberId, memberStatus));
    }

    @PostMapping("/admin/member/{memberId}/role")
    public String updateMemberRole(@PathVariable String memberId,
                                   @RequestParam String memberRole,
                                   HttpSession session,
                                   RedirectAttributes ra) {
        return handle("members", "회원 권한을 변경했습니다.", ra,
                () -> adminService.updateMemberRole(
                        adminId(session), memberId, memberRole));
    }

    @PostMapping("/admin/board/{boardId}/visibility")
    public String updateBoardVisibility(@PathVariable Long boardId,
                                        @RequestParam boolean hidden,
                                        HttpSession session,
                                        RedirectAttributes ra) {
        return handle("boards", hidden ? "게시글을 숨겼습니다." : "게시글을 다시 공개했습니다.", ra,
                () -> adminService.updateBoardHidden(adminId(session), boardId, hidden));
    }

    @PostMapping("/admin/board/{boardId}/delete")
    public String deleteBoard(@PathVariable Long boardId,
                              HttpSession session,
                              RedirectAttributes ra) {
        return handle("boards", "게시글을 삭제했습니다.", ra,
                () -> adminService.deleteBoard(adminId(session), boardId));
    }

    @PostMapping("/admin/comment/{commentId}/visibility")
    public String updateCommentVisibility(@PathVariable Long commentId,
                                          @RequestParam boolean hidden,
                                          HttpSession session,
                                          RedirectAttributes ra) {
        return handle("comments", hidden ? "댓글을 숨겼습니다." : "댓글을 다시 공개했습니다.", ra,
                () -> adminService.updateCommentHidden(adminId(session), commentId, hidden));
    }

    @PostMapping("/admin/comment/{commentId}/delete")
    public String deleteComment(@PathVariable Long commentId,
                                @RequestParam Long boardId,
                                HttpSession session,
                                RedirectAttributes ra) {
        return handle("comments", "댓글을 삭제했습니다.", ra,
                () -> adminService.deleteComment(adminId(session), boardId, commentId));
    }

    @PostMapping("/admin/inquiry/{inquiryId}/reply")
    public String replyInquiry(@PathVariable Long inquiryId,
                               @RequestParam String answer,
                               HttpSession session,
                               RedirectAttributes ra) {
        return handle("inquiries", "문의 답변을 저장했습니다.", ra,
                () -> adminService.replyInquiry(adminId(session), inquiryId, answer));
    }

    @PostMapping("/admin/inquiry/{inquiryId}/complete")
    public String completeInquiry(@PathVariable Long inquiryId,
                                  HttpSession session,
                                  RedirectAttributes ra) {
        return handle("inquiries", "문의를 처리 완료했습니다.", ra,
                () -> adminService.completeInquiry(adminId(session), inquiryId));
    }

    @PostMapping("/admin/inquiry/{inquiryId}/delete")
    public String deleteInquiry(@PathVariable Long inquiryId,
                                HttpSession session,
                                RedirectAttributes ra) {
        return handle("inquiries", "문의를 삭제했습니다.", ra,
                () -> adminService.deleteInquiry(adminId(session), inquiryId));
    }

    @PostMapping("/admin/glossary/create")
    public String createGlossary(@ModelAttribute GlossaryDto glossaryDto,
                                 HttpSession session,
                                 RedirectAttributes ra) {
        return handle("glossary", "용어를 추가했습니다.", ra,
                () -> adminService.createGlossary(adminId(session), glossaryDto));
    }

    @PostMapping("/admin/glossary/{termId}/update")
    public String updateGlossary(@PathVariable Long termId,
                                 @ModelAttribute GlossaryDto glossaryDto,
                                 HttpSession session,
                                 RedirectAttributes ra) {
        glossaryDto.setTermId(termId);
        return handle("glossary", "용어를 수정했습니다.", ra,
                () -> adminService.updateGlossary(adminId(session), glossaryDto));
    }

    @PostMapping("/admin/glossary/{termId}/delete")
    public String deleteGlossary(@PathVariable Long termId,
                                 HttpSession session,
                                 RedirectAttributes ra) {
        return handle("glossary", "용어를 삭제했습니다.", ra,
                () -> adminService.deleteGlossary(adminId(session), termId));
    }

    private String adminId(HttpSession session) {
        MemberDto loginMember =
                (MemberDto) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            throw new IllegalStateException("관리자 로그인이 필요합니다.");
        }
        return loginMember.getMemberId();
    }

    private String handle(String tab,
                          String successMessage,
                          RedirectAttributes ra,
                          AdminAction action) {
        try {
            action.run();
            ra.addFlashAttribute("adminSuccess", successMessage);
        } catch (IllegalArgumentException | IllegalStateException e) {
            ra.addFlashAttribute("adminError", e.getMessage());
        } catch (RuntimeException e) {
            ra.addFlashAttribute("adminError", "관리 작업 중 오류가 발생했습니다.");
        }
        return "redirect:/admin?tab=" + tab;
    }

    @FunctionalInterface
    private interface AdminAction {
        void run();
    }
}
