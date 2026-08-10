package com.kh.demo.ranking.controller;

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
    void adminCannotBypassPrivateRankingDetails() {
        List<RankingDto> returnRateRankings =
                List.of(new RankingDto());
        List<RankingDto> profitRankings =
                List.of(new RankingDto());
        ExtendedModelMap model = new ExtendedModelMap();

        when(rankingService.getRankingBoard(false, false))
                .thenReturn(returnRateRankings);
        when(rankingService.getRankingBoard(false, true))
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
        verify(rankingService).getRankingBoard(false, false);
        verify(rankingService).getRankingBoard(false, true);
    }

    @Test
    void regularUserCannotRequestPrivateRankingDetailsFromDataEndpoint() {
        List<RankingDto> rankings = List.of(new RankingDto());

        when(rankingService.getRankingBoard(false)).thenReturn(rankings);

        List<RankingDto> response = rankingController.getRanking(session);

        assertSame(rankings, response);
        verify(rankingService).getRankingBoard(false);
    }
}
