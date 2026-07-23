package com.kh.demo.member.controller;

import com.kh.demo.common.SessionConst;
import com.kh.demo.common.dto.ApiResponse;
import com.kh.demo.member.dto.MemberDto;
import com.kh.demo.member.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/join")
    public String joinForm() {
        return "member/join";
    }

    @PostMapping("/join")
    public String join(@ModelAttribute MemberDto memberDto,
                       @RequestParam(required = false) MultipartFile profileImage,
                       RedirectAttributes ra) {
        try {
            memberService.join(memberDto, profileImage);
            ra.addFlashAttribute("joinSuccess", true);
            return "redirect:/member/login";
        } catch (IOException | IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/member/join";
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", "회원가입 처리 중 오류가 발생했습니다.");
            return "redirect:/member/join";
        }
    }

    @GetMapping("/checkId")
    @ResponseBody
    public ApiResponse<Boolean> checkId(@RequestParam String memberId) {
        boolean duplicate = memberService.isMemberIdCheck(memberId);
        String message = duplicate
                ? "이미 사용 중인 아이디입니다."
                : "사용 가능한 아이디입니다.";

        return ApiResponse.success(message, duplicate);
    }

    @GetMapping("/login")
    public String loginForm() {
        return "member/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String memberId,
                        @RequestParam String memberPwd,
                        @RequestParam(required = false) String redirectURL,
                        HttpSession session,
                        RedirectAttributes ra) {
        try {
            MemberDto member = memberService.login(memberId, memberPwd);
            session.setAttribute(SessionConst.LOGIN_MEMBER, member);
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/member/login";
        }

        if (redirectURL != null
                && redirectURL.startsWith("/")
                && !redirectURL.startsWith("//")) {
            return "redirect:" + redirectURL;
        }

        return "redirect:/";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }

        return "redirect:/";
    }

}
