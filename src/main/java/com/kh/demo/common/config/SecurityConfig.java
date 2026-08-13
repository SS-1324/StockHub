package com.kh.demo.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

// Spring Security의 인증과 암호화를 설정
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 기본 보안 로그인 기능을 끄고 모든 요청을 허용
    // CSRF는 세션 쿠키 기반 인증을 쓰는 이 앱에서 반드시 켜져 있어야 하므로 그대로 둔다(비활성화하지 않음).
    // 토큰은 common/header.jsp 등에서 <meta name="_csrf">로 내려주고, header.js가 모든 fetch 요청에
    // 자동으로 헤더를 붙이며, 일반 폼 제출은 각 <form>에 심어둔 hidden input으로 함께 전송된다.
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http){
        http
                // SockJS 폴백 전송(xhr-streaming 등)은 내부적으로 POST를 쓰는데 STOMP 클라이언트가
                // CSRF 토큰을 채워 보내지 않으므로, 종목 채팅 WebSocket 엔드포인트만 CSRF 검사에서 제외한다.
                .csrf(csrf -> csrf.ignoringRequestMatchers("/ws-chat/**"))
                .formLogin(AbstractHttpConfigurer::disable) // 기본 로그인 폼 비활성화
                .httpBasic(AbstractHttpConfigurer::disable) // HTTP Basic 인증 비활성화
                .logout(AbstractHttpConfigurer::disable) // 기본 로그아웃 기능 비활성화
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll()); // 모든 주소 접근 허용
        return http.build();
    }

    // BCrypt 비밀번호 암호화 객체를 등록
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
