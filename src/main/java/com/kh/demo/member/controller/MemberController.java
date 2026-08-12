package com.kh.demo.member.controller;

import com.kh.demo.common.SessionConst;
import com.kh.demo.common.dto.ApiResponse;
import com.kh.demo.member.dto.MemberDto;
import com.kh.demo.member.dto.ProfileUpdateDto;
import com.kh.demo.member.service.EmailVerificationResult;
import com.kh.demo.member.service.EmailVerificationService;
import com.kh.demo.member.service.MemberService;
import com.kh.demo.member.service.PasswordResetService;
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

    // 프로필 수정 전 비밀번호 확인 정보를 저장할 세션 키
    private static final String PROFILE_EDIT_VERIFIED_MEMBER_ID =
            "profileEditVerifiedMemberId";
    private static final String PROFILE_EDIT_VERIFIED_AT =
            "profileEditVerifiedAt";

    // 현재 비밀번호 확인 결과는 10분 동안만 사용
    private static final long PROFILE_EDIT_VERIFICATION_VALID_MILLIS =
            10L * 60 * 1000;

    // 회원 기능을 처리할 Service
    private final MemberService memberService;
    private final EmailVerificationService emailVerificationService;
    private final PasswordResetService passwordResetService;

    // 회원 기능과 이메일 인증 기능을 주입받는 생성자
    public MemberController(MemberService memberService,
                            EmailVerificationService emailVerificationService,
                            PasswordResetService passwordResetService) {
        this.memberService = memberService;
        this.emailVerificationService = emailVerificationService;
        this.passwordResetService = passwordResetService;
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

    // 3분 동안 사용할 인증번호를 실제 이메일로 발송
    @PostMapping("/email/send")
    @ResponseBody
    public ApiResponse<Void> sendEmailVerificationCode(@RequestParam String email,
                                                       HttpSession session) {
        try {
            emailVerificationService.sendVerificationCode(email);
            // 새 코드가 생성되면 이전 세션의 인증 완료 상태를 제거
            session.removeAttribute(VERIFIED_EMAIL);
            return ApiResponse.success("인증번호가 발송되었습니다.", null);
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
            EmailVerificationResult result =
                    emailVerificationService.verifyCode(normalizedEmail, code);

            if (result == EmailVerificationResult.VERIFIED) {
                // 가입 요청에서도 같은 이메일인지 확인할 수 있도록 세션에 저장
                session.setAttribute(VERIFIED_EMAIL, normalizedEmail);
                return ApiResponse.success("사용가능한 이메일입니다.", true);
            }

            session.removeAttribute(VERIFIED_EMAIL);
            if (result == EmailVerificationResult.EXPIRED) {
                return ApiResponse.fail(
                        "인증 시간이 지났습니다. 코드를 재발급받으세요."
                );
            }
            return ApiResponse.fail("잘못된 코드입니다.");
        } catch (IllegalStateException e) {
            session.removeAttribute(VERIFIED_EMAIL);
            return ApiResponse.fail(e.getMessage());
        }
    }

    // 비밀번호 찾기 화면을 반환
    @GetMapping("/password-reset")
    public String passwordResetForm() {
        return "member/passwordReset";
    }

    // 가입된 이메일에 비밀번호 찾기용 개발 코드 생성
    @PostMapping("/password-reset/email/send")
    @ResponseBody
    public ApiResponse<String> createPasswordResetCode(@RequestParam String email) {
        try {
            String code = passwordResetService.createDevelopmentCode(email);
            return ApiResponse.success("개발용 인증코드가 생성되었습니다.", code);
        } catch (IllegalStateException e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    // 이메일 인증 코드가 맞으면 일회성 비밀번호 변경 토큰 발급
    @PostMapping("/password-reset/email/verify")
    @ResponseBody
    public ApiResponse<String> verifyPasswordResetCode(@RequestParam String email,
                                                       @RequestParam String code) {
        try {
            String token = passwordResetService.verifyCodeAndCreateToken(email, code);
            return ApiResponse.success("인증되었습니다.", token);
        } catch (IllegalStateException e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    // 일회성 토큰을 확인한 뒤 새 비밀번호로 변경
    @PostMapping("/password-reset")
    public String resetPassword(@RequestParam String resetToken,
                                @RequestParam String newPassword,
                                @RequestParam String newPasswordConfirm,
                                HttpSession session,
                                RedirectAttributes ra) {
        try {
            passwordResetService.resetPassword(
                    resetToken,
                    newPassword,
                    newPasswordConfirm
            );
            // 로그인 중 비밀번호 찾기를 사용한 경우 새 비밀번호로 다시 로그인
            session.removeAttribute(SessionConst.LOGIN_MEMBER);
            clearProfileEditVerification(session);
            ra.addFlashAttribute("passwordResetSuccess", true);
            return "redirect:/member/login";
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/member/password-reset";
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", "비밀번호 찾기 처리 중 오류가 발생했습니다.");
            return "redirect:/member/password-reset";
        }
    }

    // 로그인 화면을 반환
    @GetMapping("/login")
    public String loginForm(HttpSession session) {
        // 이미 로그인한 회원이 주소를 직접 입력해도 로그인 화면을 다시 보여주지 않음
        if (session.getAttribute(SessionConst.LOGIN_MEMBER) != null) {
            return "redirect:/";
        }

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
            // ADMIN 권한이면 세션 만료 시간을 무제한(-1)으로 설정
            if (isAdmin(member)) {
                session.setMaxInactiveInterval(-1); // 세션 만료 없음
            }
            clearProfileEditVerification(session);
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

    // 프로필 수정 전에 현재 비밀번호 확인 화면을 반환
    @GetMapping("/mypage/password-check")
    public String profilePasswordCheckForm(HttpSession session) {
        MemberDto loginMember =
                (MemberDto) session.getAttribute(SessionConst.LOGIN_MEMBER);

        // ADMIN 계정은 프로필 수정 기능을 사용하지 않음
        if (isAdmin(loginMember)) {
            clearProfileEditVerification(session);
            return "redirect:/admin";
        }

        // 화면에 다시 들어올 때는 이전 확인 상태를 제거하고 새로 확인
        clearProfileEditVerification(session);
        return "member/profilePasswordCheck";
    }

    // 현재 비밀번호가 일치하면 프로필 수정 화면 접근을 허용
    @PostMapping("/mypage/password-check")
    public String verifyProfilePassword(@RequestParam String currentPassword,
                                        HttpSession session,
                                        RedirectAttributes ra) {
        MemberDto loginMember =
                (MemberDto) session.getAttribute(SessionConst.LOGIN_MEMBER);

        if (isAdmin(loginMember)) {
            clearProfileEditVerification(session);
            return "redirect:/admin";
        }

        try {
            memberService.verifyCurrentPassword(
                    loginMember.getMemberId(),
                    currentPassword
            );
            session.setAttribute(
                    PROFILE_EDIT_VERIFIED_MEMBER_ID,
                    loginMember.getMemberId()
            );
            session.setAttribute(
                    PROFILE_EDIT_VERIFIED_AT,
                    System.currentTimeMillis()
            );
            return "redirect:/member/mypage";
        } catch (IllegalStateException e) {
            clearProfileEditVerification(session);
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/member/mypage/password-check";
        }
    }

    // 현재 비밀번호 확인을 마친 로그인 회원의 프로필 수정 화면을 반환
    @GetMapping("/mypage")
    public String mypage(HttpSession session, Model model) {
        // 세션에서 로그인 회원을 확인
        MemberDto loginMember = (MemberDto) session.getAttribute(SessionConst.LOGIN_MEMBER);

        if (isAdmin(loginMember)) {
            clearProfileEditVerification(session);
            return "redirect:/admin";
        }

        if (!isProfileEditVerified(session, loginMember.getMemberId())) {
            return "redirect:/member/mypage/password-check";
        }

        try {
            // DB의 최신 프로필을 전달
            MemberDto member = memberService.getMemberProfile(loginMember.getMemberId());
            model.addAttribute("member", member);
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

        if (isAdmin(loginMember)) {
            clearProfileEditVerification(session);
            return "redirect:/admin";
        }

        if (!isProfileEditVerified(session, loginMember.getMemberId())) {
            return "redirect:/member/mypage/password-check";
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

    // 새 비밀번호가 현재 비밀번호와 같은지 프로필 화면에 JSON으로 반환
    @PostMapping("/mypage/password/current-check")
    @ResponseBody
    public ApiResponse<Boolean> checkCurrentPasswordForProfile(
            @RequestParam String newPassword,
            HttpSession session) {
        MemberDto loginMember =
                (MemberDto) session.getAttribute(SessionConst.LOGIN_MEMBER);

        if (isAdmin(loginMember)) {
            return ApiResponse.fail("관리자는 프로필 수정 기능을 사용할 수 없습니다.");
        }
        if (!isProfileEditVerified(session, loginMember.getMemberId())) {
            return ApiResponse.fail("현재 비밀번호 확인이 필요합니다.");
        }

        try {
            boolean sameAsCurrentPassword = memberService.isCurrentPassword(
                    loginMember.getMemberId(),
                    newPassword
            );
            String message = sameAsCurrentPassword
                    ? "현재 비밀번호와 동일한 비밀번호는 사용 불가합니다."
                    : "사용 가능한 비밀번호입니다.";
            return ApiResponse.success(message, sameAsCurrentPassword);
        } catch (IllegalStateException e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    // 로그인 회원의 프로필 이미지를 삭제하고 기본 이미지로 변경
    @PostMapping("/mypage/profile-image/delete")
    public String deleteProfileImage(HttpSession session,
                                     RedirectAttributes ra) {
        MemberDto loginMember = (MemberDto) session.getAttribute(SessionConst.LOGIN_MEMBER);

        if (isAdmin(loginMember)) {
            clearProfileEditVerification(session);
            return "redirect:/admin";
        }

        if (!isProfileEditVerified(session, loginMember.getMemberId())) {
            return "redirect:/member/mypage/password-check";
        }

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

    // 세션의 프로필 수정 비밀번호 확인 상태가 현재 로그인 회원에게 유효한지 확인
    private boolean isProfileEditVerified(HttpSession session, String memberId) {
        Object verifiedMemberId =
                session.getAttribute(PROFILE_EDIT_VERIFIED_MEMBER_ID);
        Object verifiedAtValue =
                session.getAttribute(PROFILE_EDIT_VERIFIED_AT);

        if (!memberId.equals(verifiedMemberId)
                || !(verifiedAtValue instanceof Long verifiedAt)) {
            clearProfileEditVerification(session);
            return false;
        }

        long elapsed = System.currentTimeMillis() - verifiedAt;
        if (elapsed < 0 || elapsed > PROFILE_EDIT_VERIFICATION_VALID_MILLIS) {
            clearProfileEditVerification(session);
            return false;
        }

        return true;
    }

    // 프로필 수정 비밀번호 확인 상태를 세션에서 제거
    private void clearProfileEditVerification(HttpSession session) {
        session.removeAttribute(PROFILE_EDIT_VERIFIED_MEMBER_ID);
        session.removeAttribute(PROFILE_EDIT_VERIFIED_AT);
    }

    // 현재 로그인 회원이 ADMIN 권한인지 확인
    private boolean isAdmin(MemberDto loginMember) {
        return loginMember != null
                && "ADMIN".equalsIgnoreCase(loginMember.getMemberRole());
    }

}
