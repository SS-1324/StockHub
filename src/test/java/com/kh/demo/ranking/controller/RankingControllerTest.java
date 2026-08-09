package com.kh.demo.ranking.controller;

import com.kh.demo.common.SessionConst;
import com.kh.demo.member.dto.MemberDto;
import com.kh.demo.ranking.dto.RankingDto;
import com.kh.demo.ranking.service.RankingService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RankingControllerTest {

    @Mock
    private RankingService rankingService;

    @Mock
    private HttpSession session;

    private RankingController rankingController;

    @BeforeEach
    void setUp() {
        rankingController = new RankingController(rankingService);
    }

    @Test
    void adminCanRequestPrivateRankingDetails() {
        MemberDto admin = new MemberDto();
        admin.setMemberId("admin");
        admin.setMemberRole("ADMIN");

        List<RankingDto> returnRateRankings =
                List.of(new RankingDto());
        List<RankingDto> profitRankings =
                List.of(new RankingDto());
        ExtendedModelMap model = new ExtendedModelMap();

        when(session.getAttribute(SessionConst.LOGIN_MEMBER)).thenReturn(admin);
        when(rankingService.getRankingBoard(true, false))
                .thenReturn(returnRateRankings);
        when(rankingService.getRankingBoard(true, true))
                .thenReturn(profitRankings);

        String viewName = rankingController.rankingBoard(model, session);

        assertEquals("ranking/memberRanking", viewName);
        assertSame(
                returnRateRankings,
                model.get("returnRateRankingList")
        );
        assertSame(
                profitRankings,
                model.get("profitRankingList")
        );
        verify(rankingService).getRankingBoard(true, false);
        verify(rankingService).getRankingBoard(true, true);
    }

    @Test
    void regularUserCannotRequestPrivateRankingDetailsFromDataEndpoint() {
        MemberDto member = new MemberDto();
        member.setMemberId("member1");
        member.setMemberRole("USER");

        List<RankingDto> rankings = List.of(new RankingDto());

        when(session.getAttribute(SessionConst.LOGIN_MEMBER)).thenReturn(member);
        when(rankingService.getRankingBoard(false)).thenReturn(rankings);

        List<RankingDto> response = rankingController.getRanking(session);

        assertSame(rankings, response);
        verify(rankingService).getRankingBoard(false);
    }
}
