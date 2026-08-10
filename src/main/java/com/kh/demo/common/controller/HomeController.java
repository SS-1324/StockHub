package com.kh.demo.common.controller;

import com.kh.demo.community.service.BoardService;
import com.kh.demo.dictionary.dto.GlossaryDto;
import com.kh.demo.dictionary.service.GlossaryService;
import com.kh.demo.ranking.dto.RankingDto;
import com.kh.demo.ranking.service.RankingService;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

// 메인 화면 요청을 처리
@Controller
public class HomeController {

    private static final List<String> FEATURED_GLOSSARY_TERMS =
            List.of("본전", "상승세", "하락세");

    private final BoardService boardService;
    private final RankingService rankingService;
    private final GlossaryService glossaryService;

    public HomeController(BoardService boardService,
                          RankingService rankingService,
                          GlossaryService glossaryService) {
        this.boardService = boardService;
        this.rankingService = rankingService;
        this.glossaryService = glossaryService;
    }

    // 루트 주소에서 메인 JSP를 반환
    @GetMapping("/")
    public String home(Model model){
        // 전체 게시판에서 가장 최근에 작성된 글 6개를 이미지 정보와 함께 전달
        model.addAttribute(
                "latestBoards",
                boardService.getList(null, 1, 6, null)
        );
        model.addAttribute(
                "allowedCategories",
                boardService.getAllowedCategories()
        );

        // 랭킹 조회가 실패하더라도 게시글이 있는 메인 화면은 정상적으로 표시
        List<RankingDto> rankingList;
        try {
            rankingList = rankingService.getRankingBoard();
        } catch (DataAccessException e) {
            rankingList = List.of();
        }

        // 기존 투자 랭킹 결과에서 상위 5명만 메인 화면에 전달
        model.addAttribute(
                "topRankings",
                rankingList.subList(0, Math.min(5, rankingList.size()))
        );

        // DB의 용어 중 메인 화면에 소개할 세 항목만 지정한 순서대로 전달
        List<GlossaryDto> featuredGlossaryTerms;
        try {
            featuredGlossaryTerms = glossaryService.selectGlossaryByTerms(
                    FEATURED_GLOSSARY_TERMS
            );
        } catch (DataAccessException e) {
            featuredGlossaryTerms = List.of();
        }
        model.addAttribute("featuredGlossaryTerms", featuredGlossaryTerms);

        return "home/index";
    }
}
