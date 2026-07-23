package com.kh.demo.member.service;

import com.kh.demo.member.dto.MemberDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface MemberService {
    void join(MemberDto memberDto, MultipartFile profileImage) throws IOException;
    boolean isMemberIdCheck(String memberId);
    MemberDto login(String memberId, String memberPwd);
}
