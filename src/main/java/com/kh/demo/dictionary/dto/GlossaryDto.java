package com.kh.demo.dictionary.dto;

import lombok.*;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GlossaryDto {
/*   GlossaryDto : GlossaryService랑 연결되는 DTO
 *   주식 용어에 대한 설명이 들어있는 사전
 * */

    private Long termId;                // 용어 번호(PK)
    private String term;                // 용어명
    private String definition;      // 용어 정의 및 설명
    private String category;     // 용어 구분 카테고리
}
