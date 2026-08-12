package com.kh.demo.common.controller;

import com.kh.demo.community.service.BoardService;
import com.kh.demo.dictionary.dto.GlossaryDto;
import com.kh.demo.dictionary.service.GlossaryService;
import com.kh.demo.ranking.dto.RankingDto;
import com.kh.demo.ranking.service.RankingService;
import com.kh.demo.common.SessionConst;
import jakarta.servlet.http.HttpSession;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.HashMap;
import java.util.Map;

import java.util.List;

// 메인 화면 요청을 처리
@Controller
public class HomeController {

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
    public String home(Model model, HttpSession session){
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

        if (session.getAttribute(SessionConst.LOGIN_MEMBER) != null) {
            int sessionTimeoutSeconds = session.getMaxInactiveInterval();

            if (sessionTimeoutSeconds > 0) {
                long sessionExpiresAt =
                        session.getLastAccessedTime()
                                + (sessionTimeoutSeconds * 1000L);

                model.addAttribute(
                        "sessionExpiresAt",
                        sessionExpiresAt
                );
            }
        }
        // 새로고침할 때마다 DB 용어사전에서 무작위로 선택한 세 항목을 전달
        List<GlossaryDto> featuredGlossaryTerms;
        try {
            featuredGlossaryTerms = glossaryService.selectRandomGlossaryTerms(3);
        } catch (DataAccessException e) {
            featuredGlossaryTerms = List.of();
        }
        model.addAttribute("featuredGlossaryTerms", featuredGlossaryTerms);

        return "home/index";
    }
    // 세션 연장 API
    @PostMapping("/api/session/extend")
    @ResponseBody
    public Map<String, Boolean> extendSession(HttpSession session) {
        Map<String, Boolean> response = new HashMap<>();

        // 로그인한 사용자만 세션 연장 가능
        if (session.getAttribute(SessionConst.LOGIN_MEMBER) != null) {
            // 세션 타임아웃 30분으로 리셋 (자동 연장)
            session.setMaxInactiveInterval(30 * 60);  // 30분 = 1800초
            response.put("success", true);
        } else {
            response.put("success", false);
        }

        return response;
    }
}
