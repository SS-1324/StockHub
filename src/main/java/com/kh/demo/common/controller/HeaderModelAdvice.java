package com.kh.demo.common.controller;

import com.kh.demo.common.util.SessionUtil;
import com.kh.demo.ranking.service.RankingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

// 모든 일반 화면의 공통 헤더에 로그인 회원의 현재 수익률 순위를 전달한다.
@ControllerAdvice
public class HeaderModelAdvice {

    private final RankingService rankingService;

    public HeaderModelAdvice(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    @ModelAttribute("headerRankPosition")
    public Integer headerRankPosition(HttpServletRequest request) {
        // JSON API와 화면 조각 AJAX에는 헤더가 없으므로 불필요한 랭킹 DB 조회를 생략한다.
        if (request.getServletPath().startsWith("/api/")
                || "XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
            return null;
        }

        HttpSession session = request.getSession(false);
        String memberId = SessionUtil.currentMemberId(session);

        if (memberId == null || SessionUtil.isAdmin(session)) {
            return null;
        }

        return rankingService.getHeaderRankPosition(memberId);
    }
}
