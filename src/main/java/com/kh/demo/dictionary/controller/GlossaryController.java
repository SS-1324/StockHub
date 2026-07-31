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
        String categoryName = switch (category) {
            case "trading" -> "매매와 투자 행동";
            case "risk-management" -> "투자자·자금·손익 관리";
            case "position" -> "상품과 포지션";
            case "market" -> "시장·지수·주문·거래 제도";
            case "fundamental" -> "종목 정보와 기업 분석";
            case "chart" -> "차트와 기술적 분석";
            default -> "주식 용어 사전";
        };

        model.addAttribute("categoryName", categoryName);
        model.addAttribute("glossaryList", glossaryList);

        return "dictionary/categoryList";
    }
}