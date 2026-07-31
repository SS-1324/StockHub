package com.kh.demo.common.config;

import com.kh.demo.common.interceptor.AdminInterceptor;
import com.kh.demo.common.interceptor.LoginInterceptor;
import com.kh.demo.community.CommunityUrls;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

// 웹에서 업로드 파일에 접근하도록 설정
@Configuration
public class WebConfig implements WebMvcConfigurer {

    // application.properties의 업로드 경로
    @Value("${file.upload-dir}")
    private String uploadDir;

    // /uploads 주소를 실제 폴더와 연결
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 업로드 폴더를 절대 경로로 변환
        String absolutePath = new File(uploadDir).getAbsolutePath();

        // /uploads 요청에서 실제 파일을 제공
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + absolutePath + File.separator);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor())
                // 로그인 이후에만 접근 가능한 페이지 경로
                .addPathPatterns(
                        "/member/mypage",
                        "/member/withdraw",
                        CommunityUrls.WRITE,
                        CommunityUrls.EDIT_ANY,
                        CommunityUrls.DELETE_ANY,
                        CommunityUrls.LIKE_ANY,
                        CommunityUrls.COMMENT_LIKE_ANY,
                        CommunityUrls.BOOKMARK_ANY,
                        CommunityUrls.COMMENT_ANY
                );

        // ADMIN 권한을 가진 로그인 회원만 관리자 페이지에 접근
        registry.addInterceptor(new AdminInterceptor())
                .addPathPatterns("/admin", "/admin/**");
    }
}
