package com.kh.demo.common.interceptor;

import com.kh.demo.common.SessionConst;
import com.kh.demo.member.dto.MemberDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

// 관리자 페이지 요청에서 로그인 여부와 ADMIN 권한을 확인
public class AdminInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        MemberDto loginMember = session == null
                ? null
                : (MemberDto) session.getAttribute(SessionConst.LOGIN_MEMBER);

        // 로그인하지 않은 사용자는 로그인 후 관리자 페이지로 돌아오도록 처리
        if (loginMember == null) {
            String redirectURL = URLEncoder.encode(
                    request.getRequestURI(),
                    StandardCharsets.UTF_8
            );
            response.sendRedirect(
                    request.getContextPath()
                            + "/member/login?redirectURL="
                            + redirectURL
            );
            return false;
        }

        // member_role이 ADMIN인 회원만 요청을 계속 진행
        if (!"ADMIN".equalsIgnoreCase(loginMember.getMemberRole())) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "관리자만 접근할 수 있습니다."
            );
            return false;
        }

        return true;
    }
}
