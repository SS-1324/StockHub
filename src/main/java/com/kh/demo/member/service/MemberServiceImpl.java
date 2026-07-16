package com.kh.demo.member.service;

import com.kh.demo.common.util.FileUploadUtil;
import com.kh.demo.common.util.SavedFile;
import com.kh.demo.member.dto.MemberDto;
import com.kh.demo.member.mapper.MemberMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class MemberServiceImpl implements MemberService{

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private FileUploadUtil fileUploadUtil;

    @Value("${file.upload-dir.profile}")
    private String profileUploadDir;

    @Autowired
    private MemberMapper memberMapper;

    //아이디가 중복인지?

    @Override
    public void join(MemberDto memberDto, MultipartFile profileImage) throws IOException {
        // 아이디 중복검사
        if(isMemberIdCheck(memberDto.getMemberId())){
            throw new IllegalStateException("이미 사용중인 아이디 입니다.");
        }

        //비밀번호는 항상 암호화해서 저장.
        String encodePwd = passwordEncoder.encode(memberDto.getMemberPwd());
        memberDto.setMemberPwd(encodePwd);

        //프로필 이미지를 업로드 했다면 디스크에 저장 후, 경로를 dto에 채워준다.
        SavedFile saved = fileUploadUtil.save(profileImage, profileUploadDir, "/uploads/profile");
        if(saved != null){
            memberDto.setProfile(saved.getPath());
        }

        memberMapper.insertMember(memberDto);
    }

    @Override
    public boolean isMemberIdCheck(String memberId) {
        return memberMapper.countByMemberId(memberId) > 0;
    }

    @Override
    public MemberDto login(String memberId, String memberPwd) throws IllegalStateException{
        MemberDto member = memberMapper.selectByMemberId(memberId);

        // member.getMemberPwd(); 암호화된 비밀번호
        // memberPwd 평문의 비밀번호
        // passwordEncoder.matches(평문, 암호문) -> 결과는 해당 평문과 암호문의 비교값 : true/false

        if(member == null || !passwordEncoder.matches(memberPwd, member.getMemberPwd())){
            // 런타임 예외 -> 나중에는 각 예외별 에러코드를 분리해서 관리
            throw new IllegalStateException("아이디 또는 비밀번호가 일치하지 않습니다.");
        }

        return member;
    }
}
