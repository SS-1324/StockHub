package com.kh.demo.hub.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StockController {

    @GetMapping("/hub/chart")
    public String chart() {
        return "hub/chart";
    }

}
