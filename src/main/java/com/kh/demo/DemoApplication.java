package com.kh.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

// Spring Boot 애플리케이션의 시작 클래스
@SpringBootApplication
@EnableScheduling // 종목 채팅 보관 기간 정리 등 @Scheduled 배치 작업 활성화
public class DemoApplication {

	// Spring Boot와 내장 서버를 실행
	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}
}