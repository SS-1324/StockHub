package com.kh.demo.member.service;

import com.kh.demo.common.util.FileUploadUtil;
import com.kh.demo.common.util.SavedFile;
import com.kh.demo.member.dto.MemberDto;
import com.kh.demo.member.mapper.MemberMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class MemberServiceImpl implements MemberService {

    private final PasswordEncoder passwordEncoder;
    private final FileUploadUtil fileUploadUtil;
    private final MemberMapper memberMapper;

    @Value("${file.upload-dir.profile}")
    private String profileUploadDir;

    public MemberServiceImpl(PasswordEncoder passwordEncoder,
                             FileUploadUtil fileUploadUtil,
                             MemberMapper memberMapper) {
        this.passwordEncoder = passwordEncoder;
        this.fileUploadUtil = fileUploadUtil;
        this.memberMapper = memberMapper;
    }

    @Override
    public void join(MemberDto memberDto, MultipartFile profileImage) throws IOException {
        if (isMemberIdCheck(memberDto.getMemberId())) {
            throw new IllegalStateException("이미 사용 중인 아이디입니다.");
        }

        String encodedPwd = passwordEncoder.encode(memberDto.getMemberPwd());
        memberDto.setMemberPwd(encodedPwd);

        SavedFile saved = fileUploadUtil.save(
                profileImage,
                profileUploadDir,
                "/uploads/profile"
        );

        if (saved != null) {
            memberDto.setProfile(saved.getPath());
        }

        if (memberMapper.insertMember(memberDto) != 1) {
            throw new IllegalStateException("회원가입에 실패했습니다.");
        }
    }

    @Override
    public boolean isMemberIdCheck(String memberId) {
        return memberMapper.countByMemberId(memberId) > 0;
    }

    @Override
    public MemberDto login(String memberId, String memberPwd) {
        MemberDto member = memberMapper.selectByMemberId(memberId);

        if (member == null || !passwordEncoder.matches(memberPwd, member.getMemberPwd())) {
            throw new IllegalStateException("아이디 또는 비밀번호가 일치하지 않습니다.");
        }

        member.setMemberPwd(null);
        return member;
    }
}
