package com.kh.demo.inquiry.controller;

import com.kh.demo.common.SessionConst;
import com.kh.demo.common.dto.ApiResponse;
import com.kh.demo.inquiry.dto.InquiryDto;
import com.kh.demo.inquiry.service.InquiryService;
import com.kh.demo.member.dto.MemberDto;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

// 푸터의 문의하기 폼 요청을 처리
@Controller
public class InquiryController {

    private final InquiryService inquiryService;

    public InquiryController(InquiryService inquiryService) {
        this.inquiryService = inquiryService;
    }

    // 로그인 회원이 작성한 문의를 모달에 JSON으로 전달
    @GetMapping("/inquiry/my")
    @ResponseBody
    public ResponseEntity<ApiResponse<List<InquiryDto>>> getMyInquiries(
            HttpSession session
    ) {
        MemberDto loginMember =
                (MemberDto) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.<List<InquiryDto>>fail("로그인이 필요합니다."));
        }

        if ("ADMIN".equalsIgnoreCase(loginMember.getMemberRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.<List<InquiryDto>>fail("관리자 계정에서는 내 문의를 이용할 수 없습니다."));
        }

        return ResponseEntity.ok(
                ApiResponse.success(
                        inquiryService.getMemberInquiries(loginMember.getMemberId())
                )
        );
    }

    // 푸터 문의하기 모달에서 보낸 내용을 저장
    @PostMapping("/inquiry")
    public String createInquiry(@RequestParam String title,
                                @RequestParam String content,
                                HttpSession session,
                                RedirectAttributes ra) {
        MemberDto loginMember =
                (MemberDto) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            return "redirect:/member/login?redirectURL=/";
        }

        try {
            inquiryService.createInquiry(
                    loginMember.getMemberId(),
                    title,
                    content
            );
            ra.addFlashAttribute("inquirySuccess", true);
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("inquiryError", e.getMessage());
            ra.addFlashAttribute("inquiryTitle", title);
            ra.addFlashAttribute("inquiryContent", content);
        } catch (RuntimeException e) {
            ra.addFlashAttribute(
                    "inquiryError",
                    "문의 등록 중 오류가 발생했습니다."
            );
        }

        return "redirect:/";
    }
}
