package com.kh.demo.dictionary.controller;

import com.kh.demo.dictionary.service.GlossaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GlossaryController {

    @Autowired
    private GlossaryService glossaryService;

    // 용어사전 메인 페이지 (카테고리 목록)
    @GetMapping("/dictionary")
    public String main() {
        return "dictionary/glossary";
    }
}