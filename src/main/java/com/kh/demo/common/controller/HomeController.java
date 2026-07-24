package com.kh.demo.common.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// 메인 화면 요청을 처리
@Controller
public class HomeController {

    // 루트 주소에서 메인 JSP를 반환
    @GetMapping("/")
    public String home(){
        return "home/index";
    }
}
