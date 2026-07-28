package com.kh.demo.community.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/*
 *   좋아요 토글(게시글/댓글) 응답 전용 DTO
 *   active : 토글 후 현재 좋아요 상태(true=좋아요 누른 상태)
 *   count  : 토글 후 최신 좋아요 수
 * */
@Getter
@AllArgsConstructor
public class ToggleResultDto {

    private boolean active;
    private long count;
}
