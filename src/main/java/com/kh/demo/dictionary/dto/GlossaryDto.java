package com.kh.demo.dictionary.dto;

import lombok.*;

/*
 *   GlossaryDto : glossary 테이블과 1:1로 대응되는 클래스
 * */

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GlossaryDto {

    private Long termId;
    private String term;
    private String definition;
    private String category;
}
