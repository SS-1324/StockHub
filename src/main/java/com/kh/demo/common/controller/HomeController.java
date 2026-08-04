package com.kh.demo.common.controller;

import com.kh.demo.community.service.BoardService;
import com.kh.demo.ranking.dto.RankingDto;
import com.kh.demo.ranking.service.RankingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

// 메인 화면 요청을 처리
@Controller
public class HomeController {

    private final BoardService boardService;
    private final RankingService rankingService;

    public HomeController(BoardService boardService,
                          RankingService rankingService) {
        this.boardService = boardService;
        this.rankingService = rankingService;
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

        // 기존 투자 랭킹 결과에서 상위 5명만 메인 화면에 전달
        List<RankingDto> rankingList = rankingService.getRankingBoard();
        model.addAttribute(
                "topRankings",
                rankingList.subList(0, Math.min(5, rankingList.size()))
        );

        return "home/index";
    }
}
