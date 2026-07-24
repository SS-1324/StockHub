package com.kh.demo.member.controller;

import com.kh.demo.common.SessionConst;
import com.kh.demo.common.dto.ApiResponse;
import com.kh.demo.member.dto.MemberDto;
import com.kh.demo.member.dto.ProfileUpdateDto;
import com.kh.demo.member.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

// 회원가입, 로그인, 프로필 요청을 처리
@Controller
@RequestMapping("/member")
public class MemberController {

    // 회원 기능을 처리할 Service
    private final MemberService memberService;

    // MemberService를 주입받는 생성자
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    // 회원가입 화면을 반환
    @GetMapping("/join")
    public String joinForm() {
        return "member/join";
    }

    // 회원가입 정보와 프로필 사진을 저장
    @PostMapping("/join")
    public String join(@ModelAttribute MemberDto memberDto,
                       @RequestParam(required = false) MultipartFile profileImage,
                       RedirectAttributes ra) {
        try {
            // 회원가입 처리를 Service에 요청
            memberService.join(memberDto, profileImage);
            // 이동 후 보여줄 가입 성공 값을 저장
            ra.addFlashAttribute("joinSuccess", true);
            return "redirect:/member/login";
        } catch (IOException | IllegalStateException e) {
            // 파일 또는 입력 오류 메시지를 전달
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/member/join";
        } catch (RuntimeException e) {
            // 예상하지 못한 오류 메시지를 전달
            ra.addFlashAttribute("error", "회원가입 처리 중 오류가 발생했습니다.");
            return "redirect:/member/join";
        }
    }

    // 아이디 중복 여부를 JSON으로 반환
    @GetMapping("/checkId")
    @ResponseBody
    public ApiResponse<Boolean> checkId(@RequestParam String memberId) {
        // DB에서 같은 아이디가 있는지 확인
        boolean duplicate = memberService.isMemberIdCheck(memberId);
        String message = duplicate
                ? "이미 사용 중인 아이디입니다."
                : "사용 가능한 아이디입니다.";

        return ApiResponse.success(message, duplicate);
    }

    // 닉네임 중복 여부를 JSON으로 반환
    @GetMapping("/checkNickname")
    @ResponseBody
    public ApiResponse<Boolean> checkNickname(@RequestParam String nickname) {
        // DB에서 같은 닉네임이 있는지 확인
        boolean duplicate = memberService.isNicknameCheck(nickname);
        String message = duplicate
                ? "이미 사용 중인 닉네임입니다."
                : "사용 가능한 닉네임입니다.";

        return ApiResponse.success(message, duplicate);
    }

    // 로그인 화면을 반환
    @GetMapping("/login")
    public String loginForm() {
        return "member/login";
    }

    // 아이디와 비밀번호로 로그인을 처리
    @PostMapping("/login")
    public String login(@RequestParam String memberId,
                        @RequestParam String memberPwd,
                        @RequestParam(required = false) String redirectURL,
                        HttpSession session,
                        RedirectAttributes ra) {
        try {
            // 입력 정보와 일치하는 회원을 조회
            MemberDto member = memberService.login(memberId, memberPwd);
            // 로그인 회원 정보를 세션에 저장
            session.setAttribute(SessionConst.LOGIN_MEMBER, member);
        } catch (IllegalStateException e) {
            // 로그인 실패 메시지를 전달
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/member/login";
        }

        // 로그인 전 요청한 안전한 내부 주소로 이동
        if (redirectURL != null
                && redirectURL.startsWith("/")
                && !redirectURL.startsWith("//")) {
            return "redirect:" + redirectURL;
        }

        return "redirect:/";
    }

    // 세션을 삭제하고 로그아웃
    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        // 기존 세션이 있을 때만 가져옴
        HttpSession session = request.getSession(false);

        // 세션의 모든 로그인 정보를 삭제
        if (session != null) {
            session.invalidate();
        }

        return "redirect:/";
    }

    // 로그인 회원의 마이페이지 화면을 반환
    @GetMapping("/mypage")
    public String mypage(HttpSession session, Model model) {
        // 세션에서 로그인 회원을 확인
        MemberDto loginMember = (MemberDto) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            return "redirect:/member/login?redirectURL=/member/mypage";
        }

        try {
            // DB의 최신 프로필과 증권사 목록을 전달
            MemberDto member = memberService.getMemberProfile(loginMember.getMemberId());
            model.addAttribute("member", member);
            model.addAttribute("brokerages", memberService.getBrokerages());
            return "member/mypage";
        } catch (IllegalStateException e) {
            // DB에서 회원을 찾지 못하면 세션을 종료
            session.invalidate();
            return "redirect:/member/login";
        }
    }

    // 마이페이지에서 입력한 프로필 정보를 수정
    @PostMapping("/mypage")
    public String updateProfile(@ModelAttribute ProfileUpdateDto profileUpdateDto,
                                @RequestParam(required = false) MultipartFile profileImage,
                                HttpSession session,
                                RedirectAttributes ra) {
        // 로그인하지 않은 요청은 로그인 화면으로 이동
        MemberDto loginMember = (MemberDto) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            return "redirect:/member/login?redirectURL=/member/mypage";
        }

        // 화면에서 전달된 아이디 대신 세션 아이디를 사용
        profileUpdateDto.setMemberId(loginMember.getMemberId());

        try {
            // 프로필을 수정하고 세션 정보도 최신 값으로 교체
            MemberDto updatedMember = memberService.updateProfile(profileUpdateDto, profileImage);
            session.setAttribute(SessionConst.LOGIN_MEMBER, updatedMember);
            ra.addFlashAttribute("profileSuccess", true);
        } catch (IOException | IllegalStateException e) {
            // 입력 또는 이미지 저장 오류를 전달
            ra.addFlashAttribute("error", e.getMessage());
        } catch (RuntimeException e) {
            // 예상하지 못한 DB 오류를 전달
            ra.addFlashAttribute("error", "프로필 수정 중 오류가 발생했습니다.");
        }

        return "redirect:/member/mypage";
    }

    // 회원 탈퇴 확인 화면을 반환
    @GetMapping("/withdraw")
    public String withdrawForm(HttpSession session) {
        // 로그인하지 않은 요청은 로그인 화면으로 이동
        MemberDto loginMember = (MemberDto) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            return "redirect:/member/login?redirectURL=/member/withdraw";
        }

        return "member/withdraw";
    }

    // 비밀번호 확인 후 회원 정보를 삭제
    @PostMapping("/withdraw")
    public String withdraw(@RequestParam String memberPwd,
                           @RequestParam(required = false) Boolean confirmWithdraw,
                           HttpSession session,
                           RedirectAttributes ra) {
        // 로그인하지 않은 요청은 로그인 화면으로 이동
        MemberDto loginMember = (MemberDto) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            return "redirect:/member/login?redirectURL=/member/withdraw";
        }

        // 탈퇴 동의를 선택하지 않으면 요청을 중단
        if (!Boolean.TRUE.equals(confirmWithdraw)) {
            ra.addFlashAttribute("error", "회원 탈퇴 동의 항목을 확인해주세요.");
            return "redirect:/member/withdraw";
        }

        try {
            // 비밀번호 확인과 회원 데이터 삭제를 요청
            memberService.withdraw(loginMember.getMemberId(), memberPwd);
            // 탈퇴한 회원의 로그인 세션을 종료
            session.invalidate();
            ra.addFlashAttribute("withdrawSuccess", true);
            return "redirect:/";
        } catch (IllegalStateException e) {
            // 비밀번호 또는 회원 정보 오류를 전달
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/member/withdraw";
        } catch (RuntimeException e) {
            // 예상하지 못한 DB 오류를 전달
            ra.addFlashAttribute("error", "회원 탈퇴 처리 중 오류가 발생했습니다.");
            return "redirect:/member/withdraw";
        }
    }

}
