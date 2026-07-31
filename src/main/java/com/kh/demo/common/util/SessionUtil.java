package com.kh.demo.common.util;

import com.kh.demo.common.SessionConst;
import com.kh.demo.member.dto.MemberDto;
import jakarta.servlet.http.HttpSession;

public class SessionUtil {

    private SessionUtil() {}

    // 로그인이 필수일 때의 ID 추출 로직
    public static String requireLoginMemberId(HttpSession session) {
        MemberDto loginMember = (MemberDto) session.getAttribute(SessionConst.LOGIN_MEMBER);
        return loginMember.getMemberId();
    }

    // 로그인이 필수가 아닐 때(비로그인 사용자도 사용 가능한 기능에서)의 ID 추출 로직
    public static String currentMemberId(HttpSession session){
        if (session == null) { return null; }
        MemberDto loginMember = (MemberDto) session.getAttribute(SessionConst.LOGIN_MEMBER);
        return loginMember != null ? loginMember.getMemberId() : null;
    }

    // 로그인 회원이 ADMIN 권한인지 확인 (비로그인이면 false)
    public static boolean isAdmin(HttpSession session) {
        if (session == null) { return false; }
        MemberDto loginMember = (MemberDto) session.getAttribute(SessionConst.LOGIN_MEMBER);
        return loginMember != null && "ADMIN".equalsIgnoreCase(loginMember.getMemberRole());
    }
}