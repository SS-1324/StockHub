package com.kh.demo.common.controller;

import com.kh.demo.community.service.BoardService;
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
        // 전체 게시판에서 가장 최근에 작성된 글 5개를 이미지 정보와 함께 전달
        model.addAttribute(
                "latestBoards",
                boardService.getList(null, 1, 5, null)
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

        return "home/index";
    }
}
