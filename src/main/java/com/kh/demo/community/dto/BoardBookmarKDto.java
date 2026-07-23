package com.kh.demo.community.dto;

import lombok.*;

/*
 *   BoardBookmarKDto : board_bookmark 테이블과 1:1로 대응되는 클래스
 *   게시글 전용 북마크(댓글 북마크는 지원하지 않음 -> board_id만 존재, target-type 없음).
 * */

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class BoardBookmarKDto {

    private Long bookmarkId;
    private Long boardId;
    private String memberId;
}
