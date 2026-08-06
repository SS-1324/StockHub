package com.kh.demo.hub.service;

import com.kh.demo.hub.dto.ChatMessageDto;
import com.kh.demo.hub.mapper.StockChatMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// 종목 채팅 입력값 검사와 DB 저장/조회를 처리
@Service
public class StockChatServiceImpl implements StockChatService {

    private static final Logger log = LoggerFactory.getLogger(StockChatServiceImpl.class);

    private static final int MAX_CONTENT_LENGTH = 200;
    // 도배 방지: 최소 전송 간격, 짧은 시간 안에 너무 많이 보내면 잠그는 기준/시간
    private static final Duration MIN_MESSAGE_INTERVAL = Duration.ofMillis(100);
    private static final Duration BURST_WINDOW = Duration.ofSeconds(1);
    private static final int BURST_LIMIT = 3;
    private static final Duration LOCKOUT_DURATION = Duration.ofSeconds(3);
    // 채팅 보관 기간: 30일보다 오래된 메시지는 매일 새벽에 정리
    private static final int RETENTION_DAYS = 30;

    private final StockChatMapper stockChatMapper;
    // 회원별 전송 속도 상태. 단일 인스턴스 기준 도배 방지용이라 별도 저장소 없이 메모리로 충분함
    private final Map<String, RateState> rateStateByMember = new ConcurrentHashMap<>();

    public StockChatServiceImpl(StockChatMapper stockChatMapper) {
        this.stockChatMapper = stockChatMapper;
    }

    // 회원 한 명의 최근 전송 시각들과, 도배로 걸려 잠긴 경우 풀리는 시각을 들고 있음.
    // 필드 접근은 항상 이 인스턴스에 synchronized로 감싸서 여러 탭/요청이 겹쳐도 안전하게 함
    private static final class RateState {
        private Instant lastMessageAt;
        private Instant lockedUntil;
        private final Deque<Instant> recentTimestamps = new ArrayDeque<>();
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

        RateState state = rateStateByMember.computeIfAbsent(memberId, id -> new RateState());
        synchronized (state) {
            Instant now = Instant.now();

            if (state.lockedUntil != null && now.isBefore(state.lockedUntil)) {
                long remainingSeconds = (Duration.between(now, state.lockedUntil).toMillis() + 999) / 1000;
                throw new IllegalStateException(
                        "메시지를 너무 많이 보내서 " + remainingSeconds + "초 동안 채팅이 제한됩니다.");
            }
            if (state.lastMessageAt != null
                    && Duration.between(state.lastMessageAt, now).compareTo(MIN_MESSAGE_INTERVAL) < 0) {
                throw new IllegalStateException("메시지를 너무 빠르게 보내고 있어요.");
            }

            state.lastMessageAt = now;
            state.recentTimestamps.addLast(now);
            while (!state.recentTimestamps.isEmpty()
                    && Duration.between(state.recentTimestamps.peekFirst(), now).compareTo(BURST_WINDOW) > 0) {
                state.recentTimestamps.pollFirst();
            }
            if (state.recentTimestamps.size() >= BURST_LIMIT) {
                state.lockedUntil = now.plus(LOCKOUT_DURATION);
            }
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

    // 매일 새벽 4시, 보관 기간(30일)이 지난 채팅을 정리
    @Scheduled(cron = "0 0 4 * * *")
    public void cleanupOldMessages() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(RETENTION_DAYS);
        int deleted = stockChatMapper.deleteChatsBefore(cutoff);
        if (deleted > 0) {
            log.info("[종목채팅 정리] {}일 지난 채팅 {}건 삭제 (기준: {})", RETENTION_DAYS, deleted, cutoff);
        }
    }
}
