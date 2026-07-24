package com.kh.demo.member.service;

import com.kh.demo.member.dto.MemberDto;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class MemberServiceImpl implements MemberService {
    @Override
    public void join(MemberDto memberDto, MultipartFile profileImage) throws IOException {

    }

    @Override
    public boolean isMemberIdCheck(String memberId) {
        return false;
    }

    @Override
    public MemberDto login(String memberId, String memberPwd) throws IllegalStateException {
        return null;
    }

    @Override
    public void withdraw(String memberId) {

    }
}
