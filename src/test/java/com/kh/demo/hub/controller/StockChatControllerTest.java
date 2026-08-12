package com.kh.demo.hub.controller;

import com.kh.demo.common.SessionConst;
import com.kh.demo.common.dto.ApiResponse;
import com.kh.demo.hub.dto.ChatMessageDto;
import com.kh.demo.hub.service.StockChatService;
import com.kh.demo.member.dto.MemberDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockChatControllerTest {

    @Mock
    private StockChatService stockChatService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private StockChatController stockChatController;

    @BeforeEach
    void setUp() {
        stockChatController = new StockChatController(stockChatService, messagingTemplate);
    }

    private SimpMessageHeaderAccessor headerAccessorWithLoginMember(MemberDto member) {
        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.create();
        accessor.setSessionAttributes(
                member == null ? Map.of() : Map.of(SessionConst.LOGIN_MEMBER, member));
        return accessor;
    }

    @Test
    void recentMessagesDelegatesToService() {
        List<ChatMessageDto> messages = List.of(new ChatMessageDto());
        when(stockChatService.getRecentMessages("AAPL", 50)).thenReturn(messages);

        ApiResponse<List<ChatMessageDto>> response = stockChatController.recentMessages("AAPL");

        assertSame(messages, response.getData());
    }

    @Test
    void olderMessagesDelegatesToServiceWithBeforeChatIdAndPageSize() {
        List<ChatMessageDto> messages = List.of(new ChatMessageDto());
        when(stockChatService.getOlderMessages("AAPL", 100L, 25)).thenReturn(messages);

        ApiResponse<List<ChatMessageDto>> response = stockChatController.olderMessages("AAPL", 100L);

        assertSame(messages, response.getData());
    }

    @Test
    void sendChatRejectsWhenNoLoginInWebSocketSession() {
        SimpMessageHeaderAccessor accessor = headerAccessorWithLoginMember(null);

        assertThrows(IllegalStateException.class,
                () -> stockChatController.sendChat("AAPL", Map.of("content", "안녕"), accessor));

        verify(messagingTemplate, never()).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    void sendChatBroadcastsToStockTopicOnSuccess() {
        MemberDto member = new MemberDto();
        member.setMemberId("member1");
        SimpMessageHeaderAccessor accessor = headerAccessorWithLoginMember(member);

        ChatMessageDto saved = new ChatMessageDto();
        saved.setStockCode("AAPL");
        saved.setContent("안녕");
        when(stockChatService.sendMessage("AAPL", "member1", "안녕", null)).thenReturn(saved);

        stockChatController.sendChat("AAPL", Map.of("content", "안녕"), accessor);

        verify(messagingTemplate).convertAndSend("/topic/chat/AAPL", saved);
    }

    @Test
    void sendChatRejectsMalformedChartPriceInsteadOfThrowingRaw() {
        MemberDto member = new MemberDto();
        member.setMemberId("member1");
        SimpMessageHeaderAccessor accessor = headerAccessorWithLoginMember(member);

        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> stockChatController.sendChat(
                        "AAPL", Map.of("content", "안녕", "chartPrice", "abc"), accessor));

        assertEquals("가격 정보가 올바르지 않습니다.", e.getMessage());
        verify(messagingTemplate, never()).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    void handleChatErrorReturnsExceptionMessage() {
        IllegalStateException e = new IllegalStateException("로그인이 필요합니다.");

        String message = stockChatController.handleChatError(e);

        assertEquals("로그인이 필요합니다.", message);
    }
}
