package com.kh.demo.hub.service;

import com.kh.demo.hub.dto.ChatMessageDto;
import com.kh.demo.hub.mapper.StockChatMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockChatServiceImplTest {

    @Mock
    private StockChatMapper stockChatMapper;

    private StockChatServiceImpl stockChatService;

    @BeforeEach
    void setUp() {
        stockChatService = new StockChatServiceImpl(stockChatMapper);
    }

    @Test
    void sendMessageRejectsMissingLogin() {
        assertThrows(IllegalStateException.class,
                () -> stockChatService.sendMessage("AAPL", null, "안녕하세요", null));
    }

    @Test
    void sendMessageRejectsBlankContent() {
        assertThrows(IllegalStateException.class,
                () -> stockChatService.sendMessage("AAPL", "member1", "   ", null));
    }

    @Test
    void sendMessageRejectsContentOverMaxLength() {
        String tooLong = "가".repeat(201);

        assertThrows(IllegalStateException.class,
                () -> stockChatService.sendMessage("AAPL", "member1", tooLong, null));
    }

    @Test
    void sendMessageLocksOutAfterBurstLimit() throws InterruptedException {
        when(stockChatMapper.selectRecentChats(any(), any(Integer.class)))
                .thenReturn(List.of(new ChatMessageDto()));

        // 최소 간격(100ms) 제한에 걸리지 않도록 약간의 텀을 두고 3번 연속 전송 -> 4번째는 잠김
        for (int i = 0; i < 3; i++) {
            stockChatService.sendMessage("AAPL", "member1", "메시지" + i, null);
            Thread.sleep(120);
        }

        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> stockChatService.sendMessage("AAPL", "member1", "메시지4", null));
        assertTrue(e.getMessage().contains("초 동안"));
    }

    @Test
    void getRecentMessagesReturnsOldestFirst() {
        ChatMessageDto newest = new ChatMessageDto();
        newest.setChatId(2L);
        ChatMessageDto oldest = new ChatMessageDto();
        oldest.setChatId(1L);
        // selectRecentChats는 최신순으로 내려주므로 서비스가 뒤집어서 오래된순으로 돌려줘야 함
        when(stockChatMapper.selectRecentChats("AAPL", 50)).thenReturn(
                new java.util.ArrayList<>(List.of(newest, oldest)));

        List<ChatMessageDto> result = stockChatService.getRecentMessages("AAPL", 50);

        assertEquals(1L, result.get(0).getChatId());
        assertEquals(2L, result.get(1).getChatId());
        verify(stockChatMapper, times(1)).selectRecentChats("AAPL", 50);
    }
}
