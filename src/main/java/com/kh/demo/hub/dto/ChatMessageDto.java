package com.kh.demo.hub.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// 종목 채팅 메시지를 계층 사이에서 전달
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDto {

    private Long chatId; // 채팅 번호
    private String stockCode; // 채팅이 달린 종목 코드
    private String memberId; // 작성자 아이디 (회원 탈퇴 시 NULL)
    private String nickname; // 화면에 표시할 작성자 닉네임
    private String content; // 채팅 내용
    private Long chartPrice; // 작성 시점의 종목 가격(참고용)
    private LocalDateTime createAt; // 작성 일시
    private String createAtStr; // 화면용 작성 일시(HH:mm)
}
