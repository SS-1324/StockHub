package com.kh.demo.ranking.controller;

import com.kh.demo.ranking.dto.RankingDto;
import com.kh.demo.ranking.service.RankingService;
import jakarta.servlet.http.HttpSession;
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

    public RankingController(
            RankingService rankingService
    ) {
        this.rankingService = rankingService;
    }

    // 랭킹 JSP 화면을 반환
    @GetMapping
    public String rankingBoard(
            Model model,
            HttpSession session
    ) {
        /* [랭킹비공개보호-4]
         * 관리자도 회원의 is_stock_public 설정을 우회하지 않는다.
         * 기존 서비스 인자는 호환성을 위해 유지하되 항상 false로 전달한다.
         */
        boolean includePrivateDetails = false;

        /*
         * 같은 회원 데이터를 정렬 기준만 다르게 하여 두 번 조회한다.
         * false는 수익률순, true는 수익금순이라는 약속을
         * RankingService와 RankingMapper가 함께 사용한다.
         */
        List<RankingDto> returnRateRankingList =
                rankingService.getRankingBoard(
                        includePrivateDetails,
                        false
                );

        List<RankingDto> profitRankingList =
                rankingService.getRankingBoard(
                        includePrivateDetails,
                        true
                );

        /*
         * JSP에서 두 보드를 구분할 수 있도록 서로 다른 이름으로 전달한다.
         * memberRanking.jsp가 아래 이름을 그대로 사용하므로 이름을 함께 변경해야 한다.
         */
        model.addAttribute(
                "returnRateRankingList",
                returnRateRankingList
        );

        model.addAttribute(
                "profitRankingList",
                profitRankingList
        );

        return "ranking/memberRanking";
    }

    // 랭킹 목록을 JSON으로 반환
    @GetMapping("/data")
    @ResponseBody
    public List<RankingDto> getRanking(
            HttpSession session
    ) {
        /* JSON 응답도 화면과 동일하게 비공개 투자정보를 요청하지 않는다. */
        return rankingService.getRankingBoard(false);
    }
}
