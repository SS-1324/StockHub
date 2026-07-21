package com.kh.demo.member.service;

import com.kh.demo.member.dto.MemberDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/*
*   서비스 인터페이스
*
*   컨트롤러는 무엇을 할지만 알면 되고 어떻게 하는지(비즈니스로직)은 몰라도 됨.
*   인터페이스와 구현체를 분리해서 구현체가 변경되야할 때 인터페이스는 그대로두고 구현체만 변경.
* */
public interface MemberService {
    void join(MemberDto memberDto, MultipartFile profileImage) throws IOException;
    boolean isMemberIdCheck(String memberId);
    MemberDto login(String memberId, String memberPwd) throws IllegalStateException;
    void withdraw(String memberId);
}
