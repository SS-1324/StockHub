package com.kh.demo.common.config;

import com.kh.demo.common.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
/*
*   WebMvcConfigurer : Spring MVC의 공통 설정 구현체
*
*   1) 업로드 이미지 맵핑 : 업로드된 이미지 파일은 src/main/resources/static가 아니라
*      프로젝트 바깥의 uploads/ 폴더를 만들고 거기에 저장하겠다.
*       -> /uploads/**로 들어오는 요청을 실제 디스크 경로로 연결해주는 맵핑이 필요.
*   2) 인터셉터 등록
* */

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String absoultePath = new File(uploadDir).getAbsolutePath();

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + absoultePath + File.separator);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor())
                //로그인 해야만 접근 가능한 페이지 경로
                .addPathPatterns(
                        "/member/mypage",
                        "/member/withdraw",
                        "/community/board/write",
                        "/community/board/edit/**",
                        "/community/board/delete/**",
                        "/community/board/like/**",
                        "/community/comment/like/**",
                        "/community/board/bookmark/**",
                        "/community/board/*/comment/**"
                );
    }
}
