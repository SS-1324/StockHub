package com.kh.demo.dictionary.controller;

import com.kh.demo.dictionary.dto.GlossaryDto;
import com.kh.demo.dictionary.service.GlossaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class GlossaryController {

    @Autowired
    private GlossaryService glossaryService;

    // 용어사전 메인 페이지 (카테고리 목록)
    @GetMapping("/dictionary")
    public String main(
            @RequestParam(value = "keyword", defaultValue = "") String keyword,
            Model model
    ) {
        String searchKeyword = keyword.trim();

        if (searchKeyword.isBlank()) {

            // 검색어가 없을 때만 카테고리 목록이 필요함
            List<String> categoryCodes =
                    glossaryService.selectCategoryList();

            Map<String, Boolean> visibleCategories =
                    new LinkedHashMap<>();

            // 모든 카테고리 표시
            for (String category : categoryCodes) {
                visibleCategories.put(category, true);
            }

            // 카테고리 화면에서만 visible 전달
            model.addAttribute(
                    "visible",
                    visibleCategories
            );

            model.addAttribute(
                    "hasResult",
                    true
            );

        } else {

            // 검색어가 있으면 카테고리가 아니라 실제 용어 검색
            List<GlossaryDto> glossaryList =
                    glossaryService.searchGlossary(
                            searchKeyword
                    );

            // 실제 용어 목록을 JSP에 전달
            model.addAttribute(
                    "glossaryList",
                    glossaryList
            );

            model.addAttribute(
                    "hasResult",
                    !glossaryList.isEmpty()
            );
        }

        model.addAttribute(
                "keyword",
                searchKeyword
        );

        return "dictionary/glossary";
    }

    // 용어사전 카테고리별 페이지 매핑
    @GetMapping("/dictionary/category/{category}")
    public String getCategoryList(
            @PathVariable("category") String category,
            @RequestParam(value = "keyword", defaultValue = "") String keyword,
            Model model
    ) {
        String searchKeyword = keyword.trim();

        List<GlossaryDto> glossaryList;

        if (searchKeyword.isBlank()) {
            // 검색어가 없으면 해당 카테고리 전체 조회
            glossaryList =
                    glossaryService.selectGlossaryByCategory(category);
        } else {
            // 검색어가 있으면 해당 카테고리 안에서 검색
            glossaryList =
                    glossaryService.selectGlossaryByCategoryAndKeyword(
                            category,
                            searchKeyword
                    );
        }

        String categoryName = switch (category) {
            case "trading" -> "매매와 투자 행동";
            case "risk-management" -> "투자자·자금·손익 관리";
            case "position" -> "상품과 포지션";
            case "market" -> "시장·지수·주문·거래 제도";
            case "fundamental" -> "종목 정보와 기업 분석";
            case "chart" -> "차트와 기술적 분석";
            default -> "주식 용어 사전";
        };

        model.addAttribute("category", category);
        model.addAttribute("categoryName", categoryName);
        model.addAttribute("keyword", searchKeyword);
        model.addAttribute("glossaryList", glossaryList);

        return "dictionary/categoryList";
    }

    //검색어 자동 완성
    @GetMapping("/dictionary/autocomplete")
    @ResponseBody
    public List<String> autocomplete(
            @RequestParam(
                    value = "keyword",
                    defaultValue = ""
            ) String keyword
    ){
        String searchKeyword = keyword.trim();

        if(searchKeyword.isBlank()){
            return List.of();
        }
        return glossaryService.AutoCompleteTerms(
                searchKeyword
        );
    }

}
