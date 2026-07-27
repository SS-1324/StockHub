package com.kh.demo.member.service;

import com.kh.demo.common.util.FileUploadUtil;
import com.kh.demo.common.util.SavedFile;
import com.kh.demo.member.dto.MemberDto;
import com.kh.demo.member.dto.ProfileUpdateDto;
import com.kh.demo.member.mapper.MemberMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

// 회원 기능의 실제 처리 내용을 구현
@Service
public class MemberServiceImpl implements MemberService {

    // 아이디는 영문과 숫자만 6자 이상 사용
    private static final Pattern MEMBER_ID_PATTERN = Pattern.compile(
            "^[A-Za-z0-9]{6,50}$"
    );

    // 닉네임은 한글·영문·숫자만 2자 이상 10자 이하 사용
    private static final Pattern NICKNAME_PATTERN = Pattern.compile(
            "^[가-힣A-Za-z0-9]{2,10}$"
    );

    // 새 비밀번호의 필수 조합을 검사
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9])"
                    + "[\\x21-\\x7E]{10,100}$"
    );

    // 이메일 앞부분은 소문자 또는 소문자·숫자 조합을 사용
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^(?=.{3,100}$)(?=[a-z0-9]{1,50}@)"
                    + "(?=[^@]*[a-z])"
                    + "[a-z0-9]{1,50}@[a-z]+(?:\\.com|\\.co\\.kr|\\.net)$"
    );

    // 업로드할 수 있는 이미지 확장자
    private static final Set<String> IMAGE_EXTENSIONS = Set.of(
            ".jpg", ".jpeg", ".png", ".gif", ".webp"
    );

    // 프로필에서 선택할 수 있는 증권사 이름
    private static final Set<String> BROKERAGES = Set.of(
            "스톡증권", "허브증권", "KH투자증권"
    );

    private final PasswordEncoder passwordEncoder; // 비밀번호 암호화 도구
    private final FileUploadUtil fileUploadUtil; // 파일 저장 도구
    private final MemberMapper memberMapper; // 회원 DB 접근 객체

    // 프로필 이미지가 저장될 폴더
    @Value("${file.upload-dir.profile}")
    private String profileUploadDir;

    // 필요한 객체를 주입받는 생성자
    public MemberServiceImpl(PasswordEncoder passwordEncoder,
                             FileUploadUtil fileUploadUtil,
                             MemberMapper memberMapper) {
        this.passwordEncoder = passwordEncoder;
        this.fileUploadUtil = fileUploadUtil;
        this.memberMapper = memberMapper;
    }

    // 중복 확인, 암호화, 이미지 저장 후 회원을 추가
    @Override
    @Transactional
    public void join(MemberDto memberDto, MultipartFile profileImage) throws IOException {
        // 아이디 형식을 검사하고 앞뒤 공백을 제거
        String memberId = memberDto.getMemberId() == null
                ? ""
                : memberDto.getMemberId().trim();
        if (!MEMBER_ID_PATTERN.matcher(memberId).matches()) {
            throw new IllegalStateException(
                    "아이디는 한글과 특수문자 없이 영문·숫자로 6자 이상 입력해주세요."
            );
        }
        memberDto.setMemberId(memberId);

        // 같은 아이디가 있으면 가입을 중단
        if (isMemberIdCheck(memberId)) {
            throw new IllegalStateException("이미 사용 중인 아이디입니다.");
        }

        // 닉네임 형식과 중복 여부를 검사
        String nickname = memberDto.getNickname() == null
                ? ""
                : memberDto.getNickname().trim();
        if (!NICKNAME_PATTERN.matcher(nickname).matches()) {
            throw new IllegalStateException(
                    "닉네임은 특수문자 없이 한글·영문·숫자로 2자 이상 10자 이하로 입력해주세요."
            );
        }
        if (isNicknameCheck(nickname)) {
            throw new IllegalStateException("이미 사용 중인 닉네임입니다.");
        }
        memberDto.setNickname(nickname);

        // 이메일 형식과 중복 여부를 서버에서도 다시 검사
        String email = memberDto.getEmail() == null
                ? ""
                : memberDto.getEmail().trim().toLowerCase(Locale.ROOT);
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalStateException(
                    "이메일 형식을 다시 확인해주세요."
            );
        }
        if (memberMapper.countByEmail(email) > 0) {
            throw new IllegalStateException("이미 사용 중인 이메일입니다.");
        }
        if (!isEmailVerified(email)) {
            throw new IllegalStateException("이메일 인증을 완료해주세요.");
        }
        memberDto.setEmail(email);

        // 비밀번호의 필수 조합과 확인 값 일치를 검사
        String memberPwd = memberDto.getMemberPwd();
        if (memberPwd == null || !PASSWORD_PATTERN.matcher(memberPwd).matches()) {
            throw new IllegalStateException(
                    "비밀번호는 한글 없이 대문자·소문자·숫자·특수문자를 포함하여 10자 이상이어야 합니다."
            );
        }
        if (!memberPwd.equals(memberDto.getMemberPwdConfirm())) {
            throw new IllegalStateException("비밀번호가 서로 일치하지 않습니다.");
        }

        // 비밀번호를 BCrypt로 암호화
        String encodedPwd = passwordEncoder.encode(memberPwd);
        memberDto.setMemberPwd(encodedPwd);
        memberDto.setMemberPwdConfirm(null);

        // 선택한 프로필 이미지를 서버에 저장
        SavedFile saved = fileUploadUtil.save(
                profileImage,
                profileUploadDir,
                "/uploads/profile"
        );

        // 저장된 이미지 경로를 회원 정보에 입력
        if (saved != null) {
            memberDto.setProfile(saved.getPath());
        }

        // 회원 정보가 한 행 저장되지 않으면 실패 처리
        if (memberMapper.insertMember(memberDto) != 1) {
            throw new IllegalStateException("회원가입에 실패했습니다.");
        }

        // 사용한 이메일 인증 기록을 가입한 회원과 연결
        if (memberMapper.linkVerifiedEmailToMember(email, memberId) != 1) {
            throw new IllegalStateException("이메일 인증 정보를 확인할 수 없습니다.");
        }

        // 프로필 설정의 기본값을 함께 저장
        memberMapper.insertDefaultSettings(memberDto.getMemberId());
    }

    // 같은 아이디가 한 개 이상인지 확인
    @Override
    public boolean isMemberIdCheck(String memberId) {
        return memberMapper.countByMemberId(memberId) > 0;
    }

    // 같은 닉네임이 한 개 이상인지 확인
    @Override
    public boolean isNicknameCheck(String nickname) {
        return memberMapper.countByNickname(nickname) > 0;
    }

    // 가입 전 사용할 수 있는 이메일 인증 완료 기록이 있는지 확인
    @Override
    public boolean isEmailVerified(String email) {
        if (email == null) {
            return false;
        }

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        return EMAIL_PATTERN.matcher(normalizedEmail).matches()
                && memberMapper.countVerifiedEmail(normalizedEmail) > 0;
    }

    // 회원 조회 후 비밀번호 일치 여부를 확인
    @Override
    public MemberDto login(String memberId, String memberPwd) {
        // 입력한 아이디로 회원 정보를 조회
        MemberDto member = memberMapper.selectByMemberId(memberId);

        // 회원이 없거나 비밀번호가 다르면 로그인 실패
        if (member == null || !passwordEncoder.matches(memberPwd, member.getMemberPwd())) {
            throw new IllegalStateException("아이디 또는 비밀번호가 일치하지 않습니다.");
        }

        // 세션에 비밀번호가 저장되지 않도록 제거
        member.setMemberPwd(null);
        return member;
    }

    // 마이페이지에 필요한 회원과 설정 정보를 조회
    @Override
    public MemberDto getMemberProfile(String memberId) {
        MemberDto member = memberMapper.selectByMemberId(memberId);

        // 세션 회원이 DB에 없으면 처리를 중단
        if (member == null) {
            throw new IllegalStateException("회원 정보를 찾을 수 없습니다.");
        }

        // 비밀번호가 화면으로 전달되지 않도록 제거
        member.setMemberPwd(null);
        return member;
    }

    // 입력값 검사 후 회원 정보와 설정을 수정
    @Override
    @Transactional
    public MemberDto updateProfile(ProfileUpdateDto updateDto,
                                   MultipartFile profileImage) throws IOException {
        // 현재 저장된 회원 정보를 조회
        MemberDto member = memberMapper.selectByMemberId(updateDto.getMemberId());
        if (member == null) {
            throw new IllegalStateException("회원 정보를 찾을 수 없습니다.");
        }

        // 닉네임의 공백과 중복을 검사
        String nickname = updateDto.getNickname() == null
                ? ""
                : updateDto.getNickname().trim();
        if (!NICKNAME_PATTERN.matcher(nickname).matches()) {
            throw new IllegalStateException(
                    "닉네임은 특수문자 없이 한글·영문·숫자로 2자 이상 10자 이하로 입력해주세요."
            );
        }
        if (memberMapper.countByNicknameExceptMember(nickname, updateDto.getMemberId()) > 0) {
            throw new IllegalStateException("이미 사용 중인 닉네임입니다.");
        }

        // 비밀번호를 입력한 경우에만 규칙과 일치 여부를 검사
        String newPassword = updateDto.getNewPassword();
        String passwordConfirm = updateDto.getNewPasswordConfirm();
        if (newPassword != null && !newPassword.isBlank()) {
            if (!PASSWORD_PATTERN.matcher(newPassword).matches()) {
                throw new IllegalStateException(
                        "비밀번호는 대문자·소문자·숫자·특수문자를 포함하여 10자 이상이어야 합니다."
                );
            }
            if (!newPassword.equals(passwordConfirm)) {
                throw new IllegalStateException("변경할 비밀번호가 서로 일치하지 않습니다.");
            }
            member.setMemberPwd(passwordEncoder.encode(newPassword));
        } else {
            // 비밀번호를 비우면 기존 비밀번호를 유지
            member.setMemberPwd(null);
        }

        // 공개 여부와 툴팁 값이 없으면 기본값을 사용
        member.setNickname(nickname);
        member.setProfilePublic(Boolean.TRUE.equals(updateDto.getProfilePublic()));
        member.setWordTooltip(Boolean.TRUE.equals(updateDto.getWordTooltip()));

        // 증권사 이름과 숫자 계좌번호를 검사
        String brokerage = updateDto.getBrokerage() == null
                ? ""
                : updateDto.getBrokerage().trim();
        String accountNo = updateDto.getAccountNo() == null
                ? ""
                : updateDto.getAccountNo().trim();

        if (brokerage.isEmpty() != accountNo.isEmpty()) {
            throw new IllegalStateException("증권사와 계좌번호를 함께 입력해주세요.");
        }
        if (!brokerage.isEmpty()) {
            if (!BROKERAGES.contains(brokerage)) {
                throw new IllegalStateException("올바른 증권사를 선택해주세요.");
            }
            if (!accountNo.matches("^[0-9]{1,15}$")) {
                throw new IllegalStateException("계좌번호는 숫자만 최대 15자리까지 입력해주세요.");
            }
        }
        member.setBrokerage(brokerage.isEmpty() ? null : brokerage);
        member.setAccountNo(accountNo.isEmpty() ? null : accountNo);

        // 새 이미지가 있으면 이미지 파일인지 검사 후 저장
        String oldProfile = member.getProfile();
        SavedFile saved = null;
        if (profileImage != null && !profileImage.isEmpty()) {
            validateProfileImage(profileImage);
            saved = fileUploadUtil.save(
                    profileImage,
                    profileUploadDir,
                    "/uploads/profile"
            );
            member.setProfile(saved.getPath());
        }

        try {
            // 회원 기본 정보·증권사·계좌번호와 개인 설정을 수정
            if (memberMapper.updateMemberProfile(member) != 1) {
                throw new IllegalStateException("프로필 수정에 실패했습니다.");
            }
            memberMapper.upsertSettings(member);
        } catch (RuntimeException e) {
            // DB 저장 실패 시 새로 저장한 이미지를 삭제
            if (saved != null) {
                fileUploadUtil.delete(saved.getPath(), profileUploadDir);
            }
            throw e;
        }

        // 수정 성공 후 교체 전 프로필 이미지를 삭제
        if (saved != null && oldProfile != null && !oldProfile.equals(saved.getPath())) {
            fileUploadUtil.delete(oldProfile, profileUploadDir);
        }

        return getMemberProfile(updateDto.getMemberId());
    }

    // 비밀번호 확인 후 회원을 삭제
    @Override
    @Transactional
    public void withdraw(String memberId, String memberPwd) {
        // 탈퇴할 회원과 암호화된 비밀번호를 조회
        MemberDto member = memberMapper.selectByMemberId(memberId);
        if (member == null) {
            throw new IllegalStateException("회원 정보를 찾을 수 없습니다.");
        }

        // 현재 비밀번호가 일치하지 않으면 탈퇴를 중단
        if (memberPwd == null
                || memberPwd.isBlank()
                || !passwordEncoder.matches(memberPwd, member.getMemberPwd())) {
            throw new IllegalStateException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 회원 기본 정보를 삭제
        if (memberMapper.deleteMemberById(memberId) != 1) {
            throw new IllegalStateException("회원 탈퇴에 실패했습니다.");
        }

        // DB 삭제 성공 후 서버의 프로필 이미지도 삭제
        if (member.getProfile() != null && !member.getProfile().isBlank()) {
            fileUploadUtil.delete(member.getProfile(), profileUploadDir);
        }
    }

    // 파일 형식과 확장자가 이미지인지 검사
    private void validateProfileImage(MultipartFile profileImage) {
        String contentType = profileImage.getContentType();
        String originalName = profileImage.getOriginalFilename();

        if (contentType == null || !contentType.startsWith("image/") || originalName == null) {
            throw new IllegalStateException("이미지 파일만 업로드할 수 있습니다.");
        }

        String lowerName = originalName.toLowerCase(Locale.ROOT);
        boolean allowed = IMAGE_EXTENSIONS.stream().anyMatch(lowerName::endsWith);
        if (!allowed) {
            throw new IllegalStateException("JPG, PNG, GIF, WEBP 이미지만 업로드할 수 있습니다.");
        }
    }
}
