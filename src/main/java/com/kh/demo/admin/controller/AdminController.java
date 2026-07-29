package com.kh.demo.admin.controller;

import com.kh.demo.inquiry.service.InquiryService;
import com.kh.demo.common.SessionConst;
import com.kh.demo.member.dto.MemberDto;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// ADMIN 권한 회원에게 관리자 문의 화면을 제공
@Controller
public class AdminController {

    private final InquiryService inquiryService;

    public AdminController(InquiryService inquiryService) {
        this.inquiryService = inquiryService;
    }

    @GetMapping("/admin")
    public String adminPage(Model model) {
        model.addAttribute("inquiries", inquiryService.getAllInquiries());
        return "admin/index";
    }

    // 관리자 문의 상세 모달에서 작성한 답변을 저장
    @PostMapping("/admin/inquiry/{inquiryId}/reply")
    public String replyInquiry(@PathVariable Long inquiryId,
                               @RequestParam String answer,
                               HttpSession session,
                               RedirectAttributes ra) {
        MemberDto loginMember =
                (MemberDto) session.getAttribute(SessionConst.LOGIN_MEMBER);

        try {
            inquiryService.answerInquiry(
                    inquiryId,
                    loginMember.getMemberId(),
                    answer
            );
            ra.addFlashAttribute("adminSuccess", "답변을 전송했습니다.");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("adminError", e.getMessage());
        } catch (RuntimeException e) {
            ra.addFlashAttribute(
                    "adminError",
                    "답변 저장 중 오류가 발생했습니다."
            );
        }

        return "redirect:/admin";
    }

    // 확인한 문의를 DB에서 완전히 삭제
    @PostMapping("/admin/inquiry/{inquiryId}/delete")
    public String deleteInquiry(@PathVariable Long inquiryId,
                                RedirectAttributes ra) {
        try {
            inquiryService.deleteInquiry(inquiryId);
            ra.addFlashAttribute("adminSuccess", "문의를 삭제했습니다.");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("adminError", e.getMessage());
        } catch (RuntimeException e) {
            ra.addFlashAttribute(
                    "adminError",
                    "문의 삭제 중 오류가 발생했습니다."
            );
        }

        return "redirect:/admin";
    }
}
