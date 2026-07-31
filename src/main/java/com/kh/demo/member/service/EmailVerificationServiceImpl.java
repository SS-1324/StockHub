package com.kh.demo.member.service;

import com.kh.demo.member.mapper.MemberMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.regex.Pattern;

// 이메일 인증 기능의 실제 처리 내용을 구현
@Service
public class EmailVerificationServiceImpl
        implements EmailVerificationService {

    // 이메일 앞부분은 소문자 또는 소문자·숫자 조합을 사용
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^(?=.{3,100}$)(?=[a-z0-9]{1,50}@)"
                    + "(?=[^@]*[a-z])"
                    + "[a-z0-9]{1,50}@[a-z]+(?:\\.com|\\.co\\.kr|\\.net)$"
    );

    // 인증 코드를 안전하게 생성할 난수 도구
    private final SecureRandom secureRandom = new SecureRandom();

    // 회원과 이메일 인증 DB에 접근
    private final MemberMapper memberMapper;

    public EmailVerificationServiceImpl(MemberMapper memberMapper) {
        this.memberMapper = memberMapper;
    }

    // 외부 메일 없이 테스트할 수 있는 개발용 6자리 코드를 생성
    @Override
    @Transactional
    public String createDevelopmentCode(String email) {
        String normalizedEmail = normalizeAndValidateEmail(email);

        if (memberMapper.countByEmail(normalizedEmail) > 0) {
            throw new IllegalStateException("이미 사용 중인 이메일입니다.");
        }

        String code = String.format(
                "%06d",
                secureRandom.nextInt(1_000_000)
        );

        // 이전 인증번호를 만료 처리
        memberMapper.expireEmailVerificationCodes(normalizedEmail);

        if (memberMapper.insertEmailVerification(
                normalizedEmail,
                code
        ) != 1) {
            throw new IllegalStateException(
                    "인증 코드 저장에 실패했습니다."
            );
        }

        // 개발 중 IntelliJ 콘솔에서 인증번호를 확인
        System.out.println(
                "[StockHub 개발용 이메일 인증] "
                        + normalizedEmail
                        + " / 인증코드: "
                        + code
        );

        return code;
    }

    // 입력한 인증번호가 유효한 인증번호와 일치하는지 확인
    @Override
    @Transactional
    public boolean verifyCode(String email, String code) {
        String normalizedEmail =
                normalizeAndValidateEmail(email);

        String normalizedCode =
                code == null ? "" : code.trim();

        if (!normalizedCode.matches("^[0-9]{6}$")) {
            return false;
        }

        return memberMapper.verifyEmailCode(
                normalizedEmail,
                normalizedCode
        ) == 1;
    }

    // 이메일을 비교하기 쉽게 소문자로 바꾸고 형식을 검사
    @Override
    public String normalizeAndValidateEmail(String email) {
        String normalizedEmail = email == null
                ? ""
                : email.trim().toLowerCase(Locale.ROOT);

        if (!EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
            throw new IllegalStateException(
                    "이메일 형식을 다시 확인해주세요."
            );
        }

        return normalizedEmail;
    }
}