package com.kh.demo.member.controller;

import com.kh.demo.common.SessionConst;
import com.kh.demo.common.dto.ApiResponse;
import com.kh.demo.member.dto.MemberDto;
import com.kh.demo.member.dto.ProfileUpdateDto;
import com.kh.demo.member.service.EmailVerificationService;
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

    // 세션에 저장할 이메일 인증 완료 값의 이름
    private static final String VERIFIED_EMAIL = "verifiedEmail";

    // 회원 기능을 처리할 Service
    private final MemberService memberService;
    private final EmailVerificationService emailVerificationService;

    // 회원 기능과 이메일 인증 기능을 주입받는 생성자
    public MemberController(MemberService memberService,
                            EmailVerificationService emailVerificationService) {
        this.memberService = memberService;
        this.emailVerificationService = emailVerificationService;
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
                       HttpSession session,
                       RedirectAttributes ra) {
        try {
            // 현재 브라우저에서 인증한 이메일과 가입 이메일이 같은지 확인
            String normalizedEmail =
                    emailVerificationService.normalizeAndValidateEmail(memberDto.getEmail());
            String verifiedEmail = (String) session.getAttribute(VERIFIED_EMAIL);
            if (!normalizedEmail.equals(verifiedEmail)) {
                throw new IllegalStateException("이메일 인증을 완료해주세요.");
            }
            memberDto.setEmail(normalizedEmail);

            // 회원가입 처리를 Service에 요청
            memberService.join(memberDto, profileImage);
            session.removeAttribute(VERIFIED_EMAIL);
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

    // 외부 메일 없이 테스트할 수 있는 개발용 인증 코드를 생성
    @PostMapping("/email/send")
    @ResponseBody
    public ApiResponse<String> createEmailVerificationCode(@RequestParam String email,
                                                           HttpSession session) {
        try {
            String code = emailVerificationService.createDevelopmentCode(email);
            // 새 코드가 생성되면 이전 세션의 인증 완료 상태를 제거
            session.removeAttribute(VERIFIED_EMAIL);
            return ApiResponse.success("개발용 인증코드가 생성되었습니다.", code);
        } catch (IllegalStateException e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    // 이메일로 받은 6자리 인증 코드가 맞는지 확인
    @PostMapping("/email/verify")
    @ResponseBody
    public ApiResponse<Boolean> verifyEmailCode(@RequestParam String email,
                                                @RequestParam String code,
                                                HttpSession session) {
        try {
            String normalizedEmail =
                    emailVerificationService.normalizeAndValidateEmail(email);
            boolean verified =
                    emailVerificationService.verifyCode(normalizedEmail, code);

            if (verified) {
                // 가입 요청에서도 같은 이메일인지 확인할 수 있도록 세션에 저장
                session.setAttribute(VERIFIED_EMAIL, normalizedEmail);
                return ApiResponse.success("인증되었습니다.", true);
            }

            session.removeAttribute(VERIFIED_EMAIL);
            return ApiResponse.success("코드를 다시 확인해주세요.", false);
        } catch (IllegalStateException e) {
            session.removeAttribute(VERIFIED_EMAIL);
            return ApiResponse.fail(e.getMessage());
        }
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

    // 로그인 회원의 프로필 이미지를 삭제하고 기본 이미지로 변경
    @PostMapping("/mypage/profile-image/delete")
    public String deleteProfileImage(HttpSession session,
                                     RedirectAttributes ra) {
        MemberDto loginMember = (MemberDto) session.getAttribute(SessionConst.LOGIN_MEMBER);

        try {
            MemberDto updatedMember =
                    memberService.deleteProfileImage(loginMember.getMemberId());
            session.setAttribute(SessionConst.LOGIN_MEMBER, updatedMember);
            ra.addFlashAttribute("profileImageDeleted", true);
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", "프로필 이미지 삭제 중 오류가 발생했습니다.");
        }

        return "redirect:/member/mypage";
    }

    // 회원 탈퇴 확인 화면을 반환
    @GetMapping("/withdraw")
    public String withdrawForm() {
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
