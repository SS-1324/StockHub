package com.kh.demo.dictionary.service;

import com.kh.demo.dictionary.dto.GlossaryDto;

import java.util.List;

public interface GlossaryService {

    // 메인 화면 등에 표시할 용어를 무작위로 조회
    List<GlossaryDto> selectRandomGlossaryTerms(int limit);

    // 카테고리별 조회
    List<GlossaryDto> selectGlossaryByCategory(String category);

    // 카테고리 목록 조회
    List<String> selectCategoryList();

    //카테고리 내부 검색
    List<GlossaryDto> selectGlossaryByCategoryAndKeyword(String category, String keyword);

    //검색어 자동 완성
    List<String> AutoCompleteTerms(String keyword);

    // 전체 용어 검색
    List<GlossaryDto> searchGlossary(String keyword);
}
