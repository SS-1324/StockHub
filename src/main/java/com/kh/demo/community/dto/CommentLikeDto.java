package com.kh.demo.community.dto;

import lombok.*;

import java.time.LocalDateTime;

/*
 *   CommentLikeDto : comment_like 테이블과 1:1로 대응되는 클래스
 * */

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CommentLikeDto {

    private Long commentLikeId;
    private Long commentId;
    private String memberId;
    private LocalDateTime createAt;
}
