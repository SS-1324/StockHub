package com.kh.demo.community.dto;

import lombok.*;

import java.time.LocalDateTime;

/*
 *   BoardImageDto : board_image 테이블과 1:1로 대응되는 클래스
 * */

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class BoardImageDto {

    private Long imgId;
    private Long boardId;
    private String originalName;
    private String saveName;
    private String imgPath;
    private int imgOrder;
    private LocalDateTime createAt;
}
