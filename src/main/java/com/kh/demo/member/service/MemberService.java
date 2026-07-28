package com.kh.demo.member.service;

import com.kh.demo.member.dto.BrokerageDto;
import com.kh.demo.member.dto.MemberDto;
import com.kh.demo.member.dto.ProfileUpdateDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

// 회원 기능에서 사용할 메서드를 정의
public interface MemberService {

    // 회원 정보와 프로필 사진을 저장
    void join(MemberDto memberDto, MultipartFile profileImage) throws IOException;

    // 아이디 중복 여부를 확인
    boolean isMemberIdCheck(String memberId);

    // 닉네임 중복 여부를 확인
    boolean isNicknameCheck(String nickname);

    // 회원가입에 사용할 이메일이 인증되었는지 확인
    boolean isEmailVerified(String email);

    // 입력 정보가 맞으면 회원을 반환
    MemberDto login(String memberId, String memberPwd);

    // 마이페이지에 표시할 회원 정보를 조회
    MemberDto getMemberProfile(String memberId);

    // 선택할 수 있는 증권사 목록을 조회
    List<BrokerageDto> getBrokerages();

    // 입력한 내용으로 프로필을 수정
    MemberDto updateProfile(ProfileUpdateDto profileUpdateDto,
                            MultipartFile profileImage) throws IOException;

    // 현재 프로필 이미지를 삭제하고 기본 이미지로 변경
    MemberDto deleteProfileImage(String memberId);

    // 비밀번호 확인 후 회원 정보를 삭제
    void withdraw(String memberId, String memberPwd);
}
