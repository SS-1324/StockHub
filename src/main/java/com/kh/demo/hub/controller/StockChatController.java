package com.kh.demo.hub.controller;

import com.kh.demo.common.SessionConst;
import com.kh.demo.common.dto.ApiResponse;
import com.kh.demo.hub.dto.ChatMessageDto;
import com.kh.demo.hub.service.StockChatService;
import com.kh.demo.member.dto.MemberDto;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

// 거래 허브 종목 채팅의 과거 메시지 조회(REST)와 실시간 송수신(WebSocket/STOMP)을 처리
@Controller
public class StockChatController {

    private static final int RECENT_MESSAGE_LIMIT = 50;

    private final StockChatService stockChatService;
    private final SimpMessagingTemplate messagingTemplate;

    public StockChatController(StockChatService stockChatService,
                                SimpMessagingTemplate messagingTemplate) {
        this.stockChatService = stockChatService;
        this.messagingTemplate = messagingTemplate;
    }

    // 채팅 패널을 열었을 때 과거 메시지를 먼저 불러오는 REST API
    @GetMapping("/api/hub/chat/{stockCode}/recent")
    @ResponseBody
    public ApiResponse<List<ChatMessageDto>> recentMessages(@PathVariable String stockCode) {
        return ApiResponse.success(
                stockChatService.getRecentMessages(stockCode, RECENT_MESSAGE_LIMIT)
        );
    }

    // 클라이언트가 /app/chat/{stockCode}로 보낸 메시지를 저장 후 같은 종목 토픽(/topic/chat/{stockCode})으로 broadcast
    @MessageMapping("/chat/{stockCode}")
    public void sendChat(@DestinationVariable String stockCode,
                          Map<String, Object> payload,
                          SimpMessageHeaderAccessor headerAccessor) {

        // WebSocketConfig의 HttpSessionHandshakeInterceptor 덕분에 로그인 세션의
        // loginMember가 WebSocket 세션에도 그대로 들어있음
        Object sessionLoginMember = headerAccessor.getSessionAttributes() != null
                ? headerAccessor.getSessionAttributes().get(SessionConst.LOGIN_MEMBER)
                : null;

        if (!(sessionLoginMember instanceof MemberDto loginMember)) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        Object contentValue = payload.get("content");
        Object chartPriceValue = payload.get("chartPrice");

        Long chartPrice = null;
        if (chartPriceValue != null) {
            try {
                chartPrice = Long.valueOf(String.valueOf(chartPriceValue));
            } catch (NumberFormatException e) {
                // 클라이언트가 chartPrice에 숫자로 변환할 수 없는 값을 보낸 경우.
                // 처리되지 않은 예외로 새어나가지 않도록 IllegalStateException으로 통일해
                // handleChatError가 보낸 사람에게 에러를 돌려줄 수 있게 함
                throw new IllegalStateException("가격 정보가 올바르지 않습니다.");
            }
        }

        ChatMessageDto saved = stockChatService.sendMessage(
                stockCode,
                loginMember.getMemberId(),
                contentValue != null ? String.valueOf(contentValue) : null,
                chartPrice
        );

        messagingTemplate.convertAndSend("/topic/chat/" + stockCode, saved);
    }

    // sendChat에서 발생한 검증 실패(비로그인, 빈 메시지, 지원하지 않는 종목 등)를
    // 보낸 사람에게만 돌려줌. 클라이언트는 "/user/queue/errors"를 구독해서 받음
    @MessageExceptionHandler(IllegalStateException.class)
    @SendToUser("/queue/errors")
    public String handleChatError(IllegalStateException e) {
        return e.getMessage();
    }
}
