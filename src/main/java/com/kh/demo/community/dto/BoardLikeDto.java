package com.kh.demo.community.dto;

import lombok.*;

import java.time.LocalDateTime;

/*
 *   BoardLikeDto : board_like 테이블과 1:1로 대응되는 클래스
 * */

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class BoardLikeDto {

    private Long likeId;
    private Long boardId;
    private String memberId;
    private LocalDateTime createAt;
}
