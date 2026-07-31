package com.kh.demo.dictionary.service;

import com.kh.demo.dictionary.dto.GlossaryDto;

import java.util.List;

public interface GlossaryService {

    // 전체 용어 조회
    List<GlossaryDto> selectGlossaryList();

    // 용어 상세 조회
    GlossaryDto selectGlossaryById(Long termId);

    // 카테고리별 조회
    List<GlossaryDto> selectGlossaryByCategory(String category);

    // 카테고리 목록 조회
    List<String> selectCategoryList();

    //검색어 포함 카테고리 조회
    List<String> selectCategoryCodesByKeyword(String keyword);

    //카테고리 내부 검색
    List<GlossaryDto> selectGlossaryByCategoryAndKeyword(String category, String keyword);
}
