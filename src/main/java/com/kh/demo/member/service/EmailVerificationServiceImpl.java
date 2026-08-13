package com.kh.demo.member.service;

import com.kh.demo.member.mapper.MemberMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.regex.Pattern;

// 이메일 인증 기능의 실제 처리 내용을 구현
@Service
public class EmailVerificationServiceImpl
        implements EmailVerificationService {

    private static final Logger log =
            LoggerFactory.getLogger(EmailVerificationServiceImpl.class);

    // 혼동하기 쉬운 I·O·0·1을 제외한 대문자 영문과 숫자
    private static final String CODE_CHARACTERS =
            "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 6;

    // 이메일 아이디의 일반적인 기호와 .com·.co.kr·.net 도메인을 허용
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^(?=.{3,100}$)(?=[^@]{1,64}@)(?=[^@]*[a-z])"
                    + "[a-z0-9]+(?:[._%+\\-][a-z0-9]+)*@"
                    + "[a-z0-9]+(?:-[a-z0-9]+)*"
                    + "(?:\\.[a-z0-9]+(?:-[a-z0-9]+)*)*"
                    + "(?:\\.com|\\.net|\\.co\\.kr)$"
    );

    // 인증 코드를 안전하게 생성할 난수 도구
    private final SecureRandom secureRandom = new SecureRandom();

    // 회원과 이메일 인증 DB에 접근
    private final MemberMapper memberMapper;
    // Spring Boot가 Gmail SMTP 설정으로 생성하는 메일 발송 객체
    private final JavaMailSender mailSender;
    // 환경변수 STOCKHUB_MAIL_USERNAME으로 입력한 발신 Gmail 주소
    private final String senderAddress;

    public EmailVerificationServiceImpl(
            MemberMapper memberMapper,
            JavaMailSender mailSender,
            @Value("${spring.mail.username:}") String senderAddress) {
        this.memberMapper = memberMapper;
        this.mailSender = mailSender;
        this.senderAddress = senderAddress == null
                ? ""
                : senderAddress.trim();
    }

    // 대문자 영문과 숫자로 된 6자리 코드를 생성하고 Gmail로 발송
    @Override
    @Transactional
    public void sendVerificationCode(String email) {
        String normalizedEmail = normalizeAndValidateEmail(email);

        if (memberMapper.countByEmail(normalizedEmail) > 0) {
            throw new IllegalStateException("사용불가한 이메일입니다.");
        }

        if (senderAddress.isBlank()) {
            throw new IllegalStateException(
                    "메일 발송 계정이 설정되지 않았습니다."
            );
        }

        String code = createCode();

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

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(senderAddress);
            message.setTo(normalizedEmail);
            message.setSubject("[StockHub] 이메일 인증번호 안내");
            message.setText(
                    "StockHub 회원가입 이메일 인증번호입니다.\n\n"
                            + "인증번호: " + code + "\n\n"
                            + "인증번호는 발급 후 3분 동안 사용할 수 있습니다.\n"
                            + "본인이 요청하지 않았다면 이 메일을 무시해주세요."
            );
            mailSender.send(message);
        } catch (MailException e) {
            // 앱 비밀번호 같은 비밀값은 남기지 않고 SMTP 실패 원인만 서버 로그에 기록
            log.error(
                    "Gmail SMTP 인증번호 발송 실패. "
                            + "발신 계정, 앱 비밀번호, 587 포트 연결을 확인하세요.",
                    e
            );
            // 메일 전송 실패 시 현재 트랜잭션을 롤백하여 사용할 수 없는 코드를 남기지 않음
            throw new IllegalStateException(
                    "인증번호 발송에 실패했습니다. 잠시 후 다시 시도해주세요.",
                    e
            );
        }
    }

    // 외부에서 전달받은 이메일과 코드로 메일을 발송 (비밀번호 찾기 등에 사용)
    @Override
    public void sendEmail(String toEmail, String code) {
        if (senderAddress.isBlank()) {
            throw new IllegalStateException("메일 발송 계정이 설정되지 않았습니다.");
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(senderAddress);
            message.setTo(toEmail);
            message.setSubject("[StockHub] 비밀번호 재설정 인증번호 안내");
            message.setText(
                    "StockHub 비밀번호 재설정 이메일 인증번호입니다.\n\n"
                            + "인증번호: " + code + "\n\n"
                            + "본인이 요청하지 않았다면 이 메일을 무시해주세요."
            );
            mailSender.send(message);
        } catch (MailException e) {
            log.error("Gmail SMTP 메일 발송 실패", e);
            throw new IllegalStateException("인증번호 메일 발송에 실패했습니다. 잠시 후 다시 시도해주세요.", e);
        }
    }

    // 입력한 인증번호가 최신 번호와 일치하는지, 3분이 지났는지 확인
    @Override
    @Transactional
    public EmailVerificationResult verifyCode(String email, String code) {
        String normalizedEmail =
                normalizeAndValidateEmail(email);

        if (memberMapper.countByEmail(normalizedEmail) > 0) {
            throw new IllegalStateException("사용불가한 이메일입니다.");
        }

        String normalizedCode =
                code == null
                        ? ""
                        : code.trim().toUpperCase(Locale.ROOT);

        if (!normalizedCode.matches("^[A-Z0-9]{6}$")) {
            return EmailVerificationResult.INVALID;
        }

        String status = memberMapper.selectLatestEmailVerificationStatus(
                normalizedEmail,
                normalizedCode
        );

        if (status == null || "INVALID".equals(status)) {
            return EmailVerificationResult.INVALID;
        }
        if ("EXPIRED".equals(status)) {
            return EmailVerificationResult.EXPIRED;
        }
        if ("VERIFIED".equals(status)) {
            return EmailVerificationResult.VERIFIED;
        }

        return memberMapper.verifyEmailCode(
                normalizedEmail,
                normalizedCode
        ) == 1
                ? EmailVerificationResult.VERIFIED
                : EmailVerificationResult.INVALID;
    }

    // 예측하기 어려운 6자리 인증번호를 생성
    private String createCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            int index = secureRandom.nextInt(CODE_CHARACTERS.length());
            code.append(CODE_CHARACTERS.charAt(index));
        }
        return code.toString();
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
