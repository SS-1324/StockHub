package com.kh.demo.hub.controller;

import com.kh.demo.common.SessionConst;
import com.kh.demo.common.dto.ApiResponse;
import com.kh.demo.hub.dto.StockVoteResultDto;
import com.kh.demo.hub.dto.VoteRequestDto;
import com.kh.demo.hub.service.StockVoteService;
import com.kh.demo.member.dto.MemberDto;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockVoteControllerTest {

    @Mock
    private StockVoteService stockVoteService;

    @Mock
    private HttpSession session;

    private StockVoteController stockVoteController;

    @BeforeEach
    void setUp() {
        stockVoteController = new StockVoteController(stockVoteService);
    }

    private MemberDto loggedInAs(String memberId) {
        MemberDto member = new MemberDto();
        member.setMemberId(memberId);
        when(session.getAttribute(SessionConst.LOGIN_MEMBER)).thenReturn(member);
        return member;
    }

    @Test
    void resultReturnsNotFoundWhenStockMissing() {
        when(session.getAttribute(SessionConst.LOGIN_MEMBER)).thenReturn(null);
        when(stockVoteService.getResult("XXXX", null))
                .thenThrow(new NoSuchElementException("종목을 찾을 수 없습니다: XXXX"));

        ResponseEntity<ApiResponse<StockVoteResultDto>> response =
                stockVoteController.result("XXXX", session);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
    }

    @Test
    void voteRejectsWhenNotLoggedIn() {
        when(session.getAttribute(SessionConst.LOGIN_MEMBER)).thenReturn(null);
        VoteRequestDto request = new VoteRequestDto();
        request.setVoteType("UP");

        ResponseEntity<ApiResponse<StockVoteResultDto>> response =
                stockVoteController.vote("AAPL", request, session);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(stockVoteService, never()).vote(anyString(), anyString(), anyString());
    }

    @Test
    void voteReturnsBadRequestForInvalidVoteType() {
        loggedInAs("member1");

        VoteRequestDto request = new VoteRequestDto();
        request.setVoteType("SIDEWAYS");
        when(stockVoteService.vote("AAPL", "member1", "SIDEWAYS"))
                .thenThrow(new IllegalArgumentException("올바르지 않은 투표값입니다."));

        ResponseEntity<ApiResponse<StockVoteResultDto>> response =
                stockVoteController.vote("AAPL", request, session);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void voteReturnsUpdatedResultOnSuccess() {
        loggedInAs("member1");

        VoteRequestDto request = new VoteRequestDto();
        request.setVoteType("UP");
        StockVoteResultDto expected = new StockVoteResultDto(
                "AAPL", "Apple", 200, 4, 1, 5, 80, 20, "UP");
        when(stockVoteService.vote("AAPL", "member1", "UP")).thenReturn(expected);

        ResponseEntity<ApiResponse<StockVoteResultDto>> response =
                stockVoteController.vote("AAPL", request, session);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expected, response.getBody().getData());
    }
}
