package com.kh.demo.hub.mapper;

import com.kh.demo.hub.dto.ChatMessageDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

// 종목 채팅 기능에서 사용할 SQL을 StockChatMapper.xml과 연결
@Mapper
public interface StockChatMapper {

    // 채팅 메시지를 저장 (stock_code가 stock 테이블에 없으면 FK 위반 예외 발생)
    int insertChat(ChatMessageDto chatMessageDto);

    // 특정 종목의 최근 채팅을 최신순으로 조회
    List<ChatMessageDto> selectRecentChats(
            @Param("stockCode") String stockCode,
            @Param("limit") int limit
    );

    // 기준 시각(before)보다 오래된 채팅을 모두 삭제하고 삭제된 건수를 반환
    int deleteChatsBefore(@Param("before") LocalDateTime before);
}
