package com.kh.demo.hub.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StockController {

    // 거래 허브 메인 (헤더 메뉴에서 연결되는 경로)
    @GetMapping("/trade-hub")
    public String tradeHub() {
        return "hub/chart";
    }

    // jhk측에서 작업 중 쓰던 경로도 그대로 유지 (같은 화면)
    @GetMapping("/hub/chart")
    public String chart() {
        return "hub/chart";
    }
}