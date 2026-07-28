package com.kh.demo.dictionary.controller;

import com.kh.demo.dictionary.dto.GlossaryDto;
import com.kh.demo.dictionary.service.GlossaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class GlossaryController {

    @Autowired
    private GlossaryService glossaryService;

    // 용어사전 메인 페이지 (카테고리 목록)
    @GetMapping("/dictionary")
    public String main() {
        return "dictionary/glossary";
    }

    // 용어사전 카테고리별 페이지 매핑
    @GetMapping("/dictionary/category/{category}")
    public String getCategoryList(
            @PathVariable("category") String category,
            Model model
    ) {
        List<GlossaryDto> glossaryList =
                glossaryService.selectGlossaryByCategory(category);

        model.addAttribute("categoryName", category);
        model.addAttribute("glossaryList", glossaryList);

        return "dictionary/categoryList";
    }
}