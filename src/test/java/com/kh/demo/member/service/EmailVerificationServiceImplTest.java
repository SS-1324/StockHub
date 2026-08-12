package com.kh.demo.member.service;

import com.kh.demo.member.mapper.MemberMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmailVerificationServiceImplTest {

    private MemberMapper memberMapper;
    private JavaMailSender mailSender;
    private EmailVerificationServiceImpl service;

    @BeforeEach
    void setUp() {
        memberMapper = mock(MemberMapper.class);
        mailSender = mock(JavaMailSender.class);
        service = new EmailVerificationServiceImpl(
                memberMapper,
                mailSender,
                "stockhub.test@gmail.com"
        );
    }

    @Test
    void 사용하지_않는_이메일이면_6자리_코드를_저장하고_메일을_보낸다() {
        when(memberMapper.countByEmail("newuser@gmail.com")).thenReturn(0);
        when(memberMapper.insertEmailVerification(
                org.mockito.ArgumentMatchers.eq("newuser@gmail.com"),
                any(String.class)
        )).thenReturn(1);

        service.sendVerificationCode("NewUser@gmail.com");

        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
        verify(memberMapper).insertEmailVerification(
                org.mockito.ArgumentMatchers.eq("newuser@gmail.com"),
                codeCaptor.capture()
        );
        assertTrue(codeCaptor.getValue().matches("^[A-Z0-9]{6}$"));

        ArgumentCaptor<SimpleMailMessage> mailCaptor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(mailCaptor.capture());
        assertEquals("newuser@gmail.com", mailCaptor.getValue().getTo()[0]);
        assertTrue(mailCaptor.getValue().getText().contains(codeCaptor.getValue()));
        assertTrue(mailCaptor.getValue().getText().contains("3분"));
    }

    @Test
    void 이미_사용중인_이메일이면_메일을_보내지_않는다() {
        when(memberMapper.countByEmail("used@gmail.com")).thenReturn(1);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.sendVerificationCode("used@gmail.com")
        );

        assertEquals("사용불가한 이메일입니다.", exception.getMessage());
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void 만료된_인증번호를_구분한다() {
        when(memberMapper.countByEmail("newuser@gmail.com")).thenReturn(0);
        when(memberMapper.selectLatestEmailVerificationStatus(
                "newuser@gmail.com",
                "ABC234"
        )).thenReturn("EXPIRED");

        EmailVerificationResult result =
                service.verifyCode("newuser@gmail.com", "abc234");

        assertEquals(EmailVerificationResult.EXPIRED, result);
    }

    @Test
    void 일치하는_인증번호를_인증완료로_변경한다() {
        when(memberMapper.countByEmail("newuser@gmail.com")).thenReturn(0);
        when(memberMapper.selectLatestEmailVerificationStatus(
                "newuser@gmail.com",
                "ABC234"
        )).thenReturn("MATCHED");
        when(memberMapper.verifyEmailCode(
                "newuser@gmail.com",
                "ABC234"
        )).thenReturn(1);

        EmailVerificationResult result =
                service.verifyCode("newuser@gmail.com", "abc234");

        assertEquals(EmailVerificationResult.VERIFIED, result);
    }
}
