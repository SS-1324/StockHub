package com.kh.demo.hub.service;

import com.kh.demo.hub.dto.ChatMessageDto;

import java.util.List;

// 종목 채팅 메시지 전송/조회 기능을 정의
public interface StockChatService {

    // 로그인 회원의 메시지를 검증 후 저장하고, 닉네임까지 채워서 반환
    ChatMessageDto sendMessage(String stockCode, String memberId, String content, Long chartPrice);

    // 특정 종목의 최근 채팅을 오래된순으로 반환 (화면에 위->아래 시간순으로 그리기 위함)
    List<ChatMessageDto> getRecentMessages(String stockCode, int limit);
}
