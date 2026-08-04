package com.kh.demo.hub.service;

import com.kh.demo.hub.dto.ChatMessageDto;
import com.kh.demo.hub.mapper.StockChatMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

// 종목 채팅 입력값 검사와 DB 저장/조회를 처리
@Service
public class StockChatServiceImpl implements StockChatService {

    private static final int MAX_CONTENT_LENGTH = 200;

    private final StockChatMapper stockChatMapper;

    public StockChatServiceImpl(StockChatMapper stockChatMapper) {
        this.stockChatMapper = stockChatMapper;
    }

    @Override
    @Transactional
    public ChatMessageDto sendMessage(String stockCode, String memberId, String content, Long chartPrice) {
        if (memberId == null || memberId.isBlank()) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }
        if (stockCode == null || stockCode.isBlank()) {
            throw new IllegalStateException("종목 코드가 없습니다.");
        }

        String normalizedContent = content == null ? "" : content.trim();
        if (normalizedContent.isEmpty()) {
            throw new IllegalStateException("메시지를 입력해주세요.");
        }
        if (normalizedContent.length() > MAX_CONTENT_LENGTH) {
            throw new IllegalStateException("메시지는 " + MAX_CONTENT_LENGTH + "자 이내로 입력해주세요.");
        }

        ChatMessageDto chatMessageDto = new ChatMessageDto();
        chatMessageDto.setStockCode(stockCode);
        chatMessageDto.setMemberId(memberId);
        chatMessageDto.setContent(normalizedContent);
        chatMessageDto.setChartPrice(chartPrice);

        try {
            stockChatMapper.insertChat(chatMessageDto);
        } catch (DataIntegrityViolationException e) {
            // stock 테이블에 없는 종목 코드로 저장을 시도한 경우(FK 위반).
            // TradingView 위젯이 지원하는 해외 종목이 stock 테이블 시드에는 아직 없을 수 있음.
            throw new IllegalStateException("채팅을 지원하지 않는 종목입니다: " + stockCode);
        }

        // 방금 저장한 메시지를 닉네임까지 채워서 그대로 돌려주기 위해 최근 1건 재조회
        List<ChatMessageDto> recent = stockChatMapper.selectRecentChats(stockCode, 1);
        return recent.isEmpty() ? chatMessageDto : recent.get(0);
    }

    @Override
    public List<ChatMessageDto> getRecentMessages(String stockCode, int limit) {
        List<ChatMessageDto> recent = stockChatMapper.selectRecentChats(stockCode, limit);
        Collections.reverse(recent);
        return recent;
    }
}
