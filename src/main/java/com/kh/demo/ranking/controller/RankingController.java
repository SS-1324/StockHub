package com.kh.demo.ranking.controller;

import com.kh.demo.ranking.dto.RankingDto;
import com.kh.demo.ranking.service.RankingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

// 랭킹 화면과 랭킹 데이터 요청을 처리
@Controller
@RequestMapping("/ranking")
public class RankingController {

    private final RankingService rankingService;

    public RankingController(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    // 공통 헤더에서 '랭킹'을 눌렀을 때 JSP 화면을 반환
    @GetMapping
    public String rankingBoard(Model model) {

        model.addAttribute(
                "rankingList",
                rankingService.getRankingBoard()
        );

        return "ranking/memberRanking";
    }

    // 랭킹 목록을 JSON으로 조회할 때 사용하는 주소
    // 예: /ranking/data
    @GetMapping("/data")
    @ResponseBody
    public List<RankingDto> getRanking() {
        return rankingService.getRankingBoard();
    }
}