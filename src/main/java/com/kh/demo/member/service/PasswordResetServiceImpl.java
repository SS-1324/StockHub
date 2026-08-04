package com.kh.demo.member.service;

import com.kh.demo.member.dto.PasswordResetTokenDto;
import com.kh.demo.member.mapper.PasswordResetTokenMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.regex.Pattern;

// 비밀번호 찾기 이메일 인증과 일회성 토큰 처리를 구현
@Service
public class PasswordResetServiceImpl implements PasswordResetService {

    // 회원가입과 동일한 비밀번호 조합 규칙
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9])"
                    + "[\\x21-\\x7E]{10,100}$"
    );

    private final SecureRandom secureRandom = new SecureRandom();
    private final PasswordResetTokenMapper passwordResetTokenMapper;
    private final EmailVerificationService emailVerificationService;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetServiceImpl(
            PasswordResetTokenMapper passwordResetTokenMapper,
            EmailVerificationService emailVerificationService,
            PasswordEncoder passwordEncoder) {
        this.passwordResetTokenMapper = passwordResetTokenMapper;
        this.emailVerificationService = emailVerificationService;
        this.passwordEncoder = passwordEncoder;
    }

    // 가입된 이메일인지 확인한 뒤 개발용 6자리 코드를 생성
    @Override
    @Transactional
    public String createDevelopmentCode(String email) {
        String normalizedEmail =
                emailVerificationService.normalizeAndValidateEmail(email);
        String memberId =
                passwordResetTokenMapper.selectMemberIdByEmail(normalizedEmail);

        if (memberId == null) {
            throw new IllegalStateException("가입된 회원의 이메일을 입력해주세요.");
        }

        String code = String.format(
                "%06d",
                secureRandom.nextInt(1_000_000)
        );

        passwordResetTokenMapper.expireEmailVerificationCodes(memberId);
        if (passwordResetTokenMapper.insertEmailVerification(
                memberId,
                normalizedEmail,
                code
        ) != 1) {
            throw new IllegalStateException("인증 코드 저장에 실패했습니다.");
        }

        System.out.println(
                "[StockHub 개발용 비밀번호 찾기 인증] "
                        + normalizedEmail
                        + " / 인증코드: "
                        + code
        );

        return code;
    }

    // 유효한 이메일 인증 코드는 한 번만 사용하고 재설정 토큰을 발급
    @Override
    @Transactional
    public String verifyCodeAndCreateToken(String email, String code) {
        String normalizedEmail =
                emailVerificationService.normalizeAndValidateEmail(email);
        String normalizedCode = code == null ? "" : code.trim();

        if (!normalizedCode.matches("^[0-9]{6}$")) {
            throw new IllegalStateException("인증 코드를 다시 확인해주세요.");
        }

        String memberId =
                passwordResetTokenMapper.selectMemberIdByEmail(normalizedEmail);
        if (memberId == null
                || passwordResetTokenMapper.verifyEmailCode(
                        memberId,
                        normalizedEmail,
                        normalizedCode
                ) != 1) {
            throw new IllegalStateException("인증 코드를 다시 확인해주세요.");
        }

        // 이전 토큰을 무효화하고 예측하기 어려운 새 토큰을 생성
        passwordResetTokenMapper.expirePasswordResetTokens(memberId);
        String token = createSecureToken();

        if (passwordResetTokenMapper.insertPasswordResetToken(
                memberId,
                token
        ) != 1) {
            throw new IllegalStateException("비밀번호 변경 토큰 생성에 실패했습니다.");
        }

        return token;
    }

    // 토큰과 새 비밀번호를 검사한 뒤 비밀번호를 변경하고 토큰을 폐기
    @Override
    @Transactional
    public void resetPassword(String token,
                              String newPassword,
                              String newPasswordConfirm) {
        String normalizedToken = token == null ? "" : token.trim();
        PasswordResetTokenDto resetToken =
                passwordResetTokenMapper.selectValidToken(normalizedToken);

        if (resetToken == null) {
            throw new IllegalStateException(
                    "비밀번호 찾기 인증이 만료되었습니다. 이메일 인증을 다시 진행해주세요."
            );
        }

        if (newPassword == null
                || !PASSWORD_PATTERN.matcher(newPassword).matches()) {
            throw new IllegalStateException(
                    "비밀번호는 한글 없이 대문자·소문자·숫자·특수문자를 포함하여 10자 이상이어야 합니다."
            );
        }

        if (!newPassword.equals(newPasswordConfirm)) {
            throw new IllegalStateException("새 비밀번호가 서로 일치하지 않습니다.");
        }

        String currentEncodedPassword =
                passwordResetTokenMapper.selectEncodedPassword(resetToken.getMemberId());
        if (currentEncodedPassword == null) {
            throw new IllegalStateException("회원 정보를 찾을 수 없습니다.");
        }
        if (passwordEncoder.matches(newPassword, currentEncodedPassword)) {
            throw new IllegalStateException("현재 비밀번호와 다른 비밀번호를 입력해주세요.");
        }

        String encodedPassword = passwordEncoder.encode(newPassword);
        if (passwordResetTokenMapper.updateMemberPassword(
                resetToken.getMemberId(),
                encodedPassword
        ) != 1) {
            throw new IllegalStateException("비밀번호 변경에 실패했습니다.");
        }

        if (passwordResetTokenMapper.markTokenUsed(normalizedToken) != 1) {
            throw new IllegalStateException("비밀번호 찾기 인증이 만료되었습니다.");
        }
    }

    // 32바이트 난수를 URL에 안전한 문자열로 변환
    private String createSecureToken() {
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(tokenBytes);
    }
}
