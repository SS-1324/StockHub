package com.kh.demo.dictionary.dto;

import lombok.*;

/*
 *   GlossaryDto : glossary 테이블과 1:1로 대응되는 클래스
 *   GlossaryService와 연결되는 DTO
 *   주식 용어와 그 설명이 들어있는 사전
 * */
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GlossaryDto {

    private Long termId;                // 용어 번호(PK)
    private String term;                // 용어명
    private String definition;          // 용어 정의 및 설명
    private String category;            // 용어 구분 카테고리
}