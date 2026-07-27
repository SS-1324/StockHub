package com.kh.demo.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
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
}
