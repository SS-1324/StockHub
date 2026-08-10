package com.kh.demo.hub.service;

import com.kh.demo.brokerage.dto.StockDto;
import com.kh.demo.brokerage.mapper.StockMapper;
import com.kh.demo.hub.dto.StockVoteResultDto;
import com.kh.demo.hub.dto.VoteCountsDto;
import com.kh.demo.hub.mapper.StockVoteMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockVoteServiceImplTest {

    @Mock
    private StockVoteMapper stockVoteMapper;

    @Mock
    private StockMapper stockMapper;

    private StockVoteServiceImpl stockVoteService;

    @BeforeEach
    void setUp() {
        stockVoteService = new StockVoteServiceImpl(stockVoteMapper, stockMapper);
    }

    private StockDto stock(String code, String name, int price) {
        return new StockDto(code, name, price, "설명", null);
    }

    private VoteCountsDto counts(long up, long down) {
        VoteCountsDto counts = new VoteCountsDto();
        counts.setUpCount(up);
        counts.setDownCount(down);
        return counts;
    }

    @Test
    void getResultReturnsFiftyFiftyWhenNoVotesYet() {
        when(stockMapper.selectStockByCode("AAPL")).thenReturn(stock("AAPL", "Apple", 200));
        when(stockVoteMapper.selectVoteCounts("AAPL")).thenReturn(counts(0, 0));

        StockVoteResultDto result = stockVoteService.getResult("AAPL", null);

        assertEquals(50, result.getUpPercent());
        assertEquals(50, result.getDownPercent());
        assertEquals(0, result.getTotalCount());
        assertNull(result.getMyVote());
    }

    @Test
    void getResultThrowsWhenStockDoesNotExist() {
        when(stockMapper.selectStockByCode("XXXX")).thenReturn(null);

        assertThrows(NoSuchElementException.class,
                () -> stockVoteService.getResult("XXXX", "member1"));
    }

    @Test
    void getResultSkipsMyVoteLookupWhenNotLoggedIn() {
        when(stockMapper.selectStockByCode("AAPL")).thenReturn(stock("AAPL", "Apple", 200));
        when(stockVoteMapper.selectVoteCounts("AAPL")).thenReturn(counts(1, 1));

        stockVoteService.getResult("AAPL", null);

        verify(stockVoteMapper, never()).selectMyVote(anyString(), anyString());
    }

    @Test
    void voteRejectsInvalidVoteType() {
        assertThrows(IllegalArgumentException.class,
                () -> stockVoteService.vote("AAPL", "member1", "SIDEWAYS"));

        verify(stockVoteMapper, never()).upsertVote(anyString(), anyString(), anyString());
    }

    @Test
    void voteWrapsForeignKeyViolationAsNotFound() {
        when(stockVoteMapper.upsertVote("XXXX", "member1", "UP"))
                .thenThrow(new DataIntegrityViolationException("FK violation"));

        assertThrows(NoSuchElementException.class,
                () -> stockVoteService.vote("XXXX", "member1", "UP"));
    }

    @Test
    void voteUsesJustCastVoteInsteadOfReQueryingIt() {
        when(stockMapper.selectStockByCode("AAPL")).thenReturn(stock("AAPL", "Apple", 200));
        when(stockVoteMapper.selectVoteCounts("AAPL")).thenReturn(counts(3, 1));

        StockVoteResultDto result = stockVoteService.vote("AAPL", "member1", "UP");

        assertEquals("UP", result.getMyVote());
        assertEquals(75, result.getUpPercent());
        assertEquals(25, result.getDownPercent());
        // 방금 저장한 값(voteType)을 그대로 쓰므로 myVote 재조회 쿼리가 없어야 함
        verify(stockVoteMapper, never()).selectMyVote(anyString(), anyString());
    }
}
