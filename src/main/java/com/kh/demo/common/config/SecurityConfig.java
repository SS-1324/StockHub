package com.kh.demo.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


/*
 * Srping Security 설정 클래스
 *
 * pom.xml에 spring-boot-starter-security 의존성이 들어있으면, 별도의 설정이 없어도
 * spring security가 모든 요청에 로그인을 요구함.
 * 로그인이 안되어있으면 자동생성된 로그인 폼으로 모든 요청을 보내버린다.
 * 우리는 security로그인대신 HttpSession을 직접 활용해서 수동으로 로그인 방식을 구축할 것이다.
 * -> spring security의 기본설정을 비활성
 *
 * */


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http){
        http
                .csrf(AbstractHttpConfigurer::disable) // CSRF 토큰 검증을 끄겠다
                .formLogin(AbstractHttpConfigurer::disable) // 시큐리티의 자동 로그인 페이지를 끄겠다.
                .httpBasic(AbstractHttpConfigurer::disable) // http베이직 인증도 사용하지 않겠다.
                .logout(AbstractHttpConfigurer::disable) // 시큐리티가 제공하는 로그아웃처리도 사용하지 않겠다.
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll()); //모든요청 허용
        return http.build();
    }

    // BCryptPasswordEncoder(비밀번호 단방향 암호화)
    // 암호화 후 저장해서 로그인시에는 passwordEncoder.matches(입력값, 저장된암호문)로 일치여부를 검증
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
