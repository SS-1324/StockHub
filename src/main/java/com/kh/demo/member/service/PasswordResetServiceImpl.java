package com.kh.demo.member.service;

import com.kh.demo.member.dto.PasswordResetTokenDto;
import com.kh.demo.member.mapper.PasswordResetTokenMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Locale;
import java.util.regex.Pattern;

// 비밀번호 찾기 이메일 인증과 일회성 토큰 처리를 구현
@Service
public class PasswordResetServiceImpl implements PasswordResetService {

    // 혼동하기 쉬운 I·O·0·1을 제외한 대문자 영문과 숫자
    private static final String CODE_CHARACTERS =
            "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    // 이메일 인증번호 길이
    private static final int CODE_LENGTH = 6;

    // 회원가입과 동일한 비밀번호 조합 규칙
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9])"
                    + "[\\x21-\\x7E]{10,100}$"
    );

    // 인증번호와 비밀번호 재설정 토큰 생성에 사용할 난수 객체
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

    // 가입된 이메일에 비밀번호 재설정 인증번호를 발급
    @Override
    @Transactional
    public String createDevelopmentCode(String email) {
        String normalizedEmail =
                emailVerificationService.normalizeAndValidateEmail(email);

        String memberId =
                passwordResetTokenMapper.selectMemberIdByEmail(
                        normalizedEmail
                );

        if (memberId == null) {
            throw new IllegalStateException(
                    "가입된 회원의 이메일을 입력해주세요."
            );
        }

        // 비밀번호 찾기 전용 영문·숫자 6자리 인증번호 생성
        String code = createVerificationCode();

        // 이전에 발급한 비밀번호 재설정 인증번호를 만료 처리
        passwordResetTokenMapper.expireEmailVerificationCodes(memberId);

        // 새 인증번호를 DB에 저장
        if (passwordResetTokenMapper.insertEmailVerification(
                memberId,
                normalizedEmail,
                code
        ) != 1) {
            throw new IllegalStateException(
                    "인증 코드 저장에 실패했습니다."
            );
        }

        // 생성한 인증번호를 실제 Gmail로 발송
        emailVerificationService.sendEmail(
                normalizedEmail,
                code
        );

        return "인증번호가 메일로 발송되었습니다.";
    }

    // 인증번호를 확인한 뒤 일회성 비밀번호 변경 토큰을 발급
    @Override
    @Transactional
    public String verifyCodeAndCreateToken(
            String email,
            String code
    ) {
        String normalizedEmail =
                emailVerificationService.normalizeAndValidateEmail(email);

        // 공백을 제거하고 영문 소문자를 대문자로 변환
        String normalizedCode = code == null
                ? ""
                : code.trim().toUpperCase(Locale.ROOT);

        // 대문자 영문과 숫자로 된 6자리 코드인지 확인
        if (!normalizedCode.matches("^[A-Z0-9]{6}$")) {
            throw new IllegalStateException(
                    "인증 코드를 다시 확인해주세요."
            );
        }

        String memberId =
                passwordResetTokenMapper.selectMemberIdByEmail(
                        normalizedEmail
                );

        // 가입된 회원이면서 유효한 인증번호와 일치하는지 확인
        if (memberId == null
                || passwordResetTokenMapper.verifyEmailCode(
                memberId,
                normalizedEmail,
                normalizedCode
        ) != 1) {

            throw new IllegalStateException(
                    "인증 코드를 다시 확인해주세요."
            );
        }

        // 이전에 발급한 비밀번호 변경 토큰을 무효화
        passwordResetTokenMapper.expirePasswordResetTokens(memberId);

        // 새로운 일회성 비밀번호 변경 토큰 생성
        String token = createSecureToken();

        if (passwordResetTokenMapper.insertPasswordResetToken(
                memberId,
                token
        ) != 1) {
            throw new IllegalStateException(
                    "비밀번호 변경 토큰 생성에 실패했습니다."
            );
        }

        return token;
    }

    // 토큰과 새 비밀번호를 검사한 뒤 비밀번호를 변경
    @Override
    @Transactional
    public void resetPassword(
            String token,
            String newPassword,
            String newPasswordConfirm
    ) {
        String normalizedToken =
                token == null ? "" : token.trim();

        PasswordResetTokenDto resetToken =
                passwordResetTokenMapper.selectValidToken(
                        normalizedToken
                );

        if (resetToken == null) {
            throw new IllegalStateException(
                    "비밀번호 찾기 인증이 만료되었습니다. "
                            + "이메일 인증을 다시 진행해주세요."
            );
        }

        // 새 비밀번호 형식 확인
        if (newPassword == null
                || !PASSWORD_PATTERN.matcher(newPassword).matches()) {

            throw new IllegalStateException(
                    "비밀번호는 한글 없이 대문자·소문자·숫자·"
                            + "특수문자를 포함하여 10자 이상이어야 합니다."
            );
        }

        // 새 비밀번호와 확인값이 같은지 확인
        if (!newPassword.equals(newPasswordConfirm)) {
            throw new IllegalStateException(
                    "새 비밀번호가 서로 일치하지 않습니다."
            );
        }

        // 현재 저장된 비밀번호 해시 조회
        String currentEncodedPassword =
                passwordResetTokenMapper.selectEncodedPassword(
                        resetToken.getMemberId()
                );

        if (currentEncodedPassword == null) {
            throw new IllegalStateException(
                    "회원 정보를 찾을 수 없습니다."
            );
        }

        // 기존 비밀번호와 동일한 비밀번호인지 확인
        if (passwordEncoder.matches(
                newPassword,
                currentEncodedPassword
        )) {
            throw new IllegalStateException(
                    "현재 비밀번호와 다른 비밀번호를 입력해주세요."
            );
        }

        // 새 비밀번호를 BCrypt로 해시
        String encodedPassword =
                passwordEncoder.encode(newPassword);

        // 새로운 비밀번호 해시를 DB에 저장
        if (passwordResetTokenMapper.updateMemberPassword(
                resetToken.getMemberId(),
                encodedPassword
        ) != 1) {
            throw new IllegalStateException(
                    "비밀번호 변경에 실패했습니다."
            );
        }

        // 사용한 토큰을 다시 사용하지 못하도록 처리
        if (passwordResetTokenMapper.markTokenUsed(
                normalizedToken
        ) != 1) {
            throw new IllegalStateException(
                    "비밀번호 찾기 인증이 만료되었습니다."
            );
        }
    }

    // 대문자 영문과 숫자로 된 6자리 인증번호 생성
    private String createVerificationCode() {
        StringBuilder code =
                new StringBuilder(CODE_LENGTH);

        for (int i = 0; i < CODE_LENGTH; i++) {
            int index = secureRandom.nextInt(
                    CODE_CHARACTERS.length()
            );

            code.append(
                    CODE_CHARACTERS.charAt(index)
            );
        }

        return code.toString();
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