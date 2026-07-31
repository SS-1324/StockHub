package com.kh.demo.common.interceptor;

import com.kh.demo.common.SessionConst;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/*
* HandlerInterceptor - 서블릿과 컨트롤러 사이에서 공통로직을 끼워넣을 수 있는 스프링 MVC 확장 지점
*
* 로그인이 필요한 기능마다 매번
* if(session.getAttribute("loginMember") != null)코드를 반복해야한다.
* 이 인터셉터를 통해 로그인이 필요한 경로와 아닌 경로를 분리해서 로그인 여부를 검사한다.
* */
public class LoginInterceptor implements HandlerInterceptor {

    //controller로 진입하는 시점에 동작
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false); // false : 세션이 없으면 null반환
        boolean isLoggedIn = session != null && session.getAttribute(SessionConst.LOGIN_MEMBER) != null;
        if (isLoggedIn){
            return true; //로그인 되어있으면 그대로 controller로 진행
        }

        if(isApiRequest(request)){ //데이터만 주고받는 Ajax요청이다
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 응답 http상태코드를 401로 전달
            response.setContentType("application/json; charset=UTF-8");
            try (PrintWriter writer = response.getWriter()){
                writer.write("{\"success\":false, \"message\":\"로그인이 필요합니다\"}");
            }
        } else {
            //일반적인 경우
            String redirectURL = URLEncoder.encode(request.getRequestURI(), StandardCharsets.UTF_8);
            response.sendRedirect("/member/login?redirectURL=" + redirectURL);
        }

        return false;
    }

    private boolean isApiRequest(HttpServletRequest request){
        String uri = request.getRequestURI();
        String requestedWith = request.getHeader("X-Requested-With");
        String accept = request.getHeader("Accept");
        return uri.startsWith(request.getContextPath() + "/api/")
                || "XMLHttpRequest".equals(requestedWith)
                || (accept != null && accept.contains("application/json"));

    }
}
