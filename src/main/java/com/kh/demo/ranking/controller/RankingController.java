package com.kh.demo.ranking.controller;

import com.kh.demo.ranking.dto.RankingDto;
import com.kh.demo.ranking.service.RankingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Set;

// 랭킹 화면과 랭킹 데이터 요청을 처리
@Controller
@RequestMapping("/ranking")
public class RankingController {

    // 기본 선택 기간은 월간
    private static final String DEFAULT_PERIOD = "monthly";

    // 주소로 받을 수 있는 기간 값
    private static final Set<String> ALLOWED_PERIODS =
            Set.of("daily", "weekly", "monthly", "yearly");

    private final RankingService rankingService;

    public RankingController(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    // 공통 헤더에서 '랭킹'을 눌렀을 때 JSP 화면을 반환
    @GetMapping
    public String rankingBoard(
            @RequestParam(defaultValue = DEFAULT_PERIOD) String period,
            Model model) {

        String selectedPeriod = normalizePeriod(period);

        model.addAttribute("selectedPeriod", selectedPeriod);
        model.addAttribute(
                "rankingList",
                rankingService.getRankingBoard(selectedPeriod)
        );

        return "ranking/memberRanking";
    }

    // 랭킹 목록을 JSON으로 조회할 때 사용하는 주소
    // 예: /ranking/data?period=monthly
    @GetMapping("/data")
    @ResponseBody
    public List<RankingDto> getRanking(
            @RequestParam(defaultValue = DEFAULT_PERIOD) String period) {

        return rankingService.getRankingBoard(
                normalizePeriod(period)
        );
    }

    // 잘못된 기간 값이 들어오면 월간으로 변경
    private String normalizePeriod(String period) {
        if (period == null) {
            return DEFAULT_PERIOD;
        }

        String normalized = period.toLowerCase();

        return ALLOWED_PERIODS.contains(normalized)
                ? normalized
                : DEFAULT_PERIOD;
    }
}