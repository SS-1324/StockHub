package com.kh.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Spring Boot 애플리케이션의 시작 클래스
@SpringBootApplication
public class DemoApplication {

	// Spring Boot와 내장 서버를 실행
	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

}
