package com.kh.demo.dictionary.mapper;

import com.kh.demo.dictionary.dto.GlossaryDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/*
 * 용어사전 매퍼 - 게시판의 전문용어 하이라이트(F-COM-01-06)가 소용하는 최소 read 경로도 함께 제공.
 * 용어사전 화면 자체의 등록/수정/삭제는 사전 담당자가 별도로 구현.
 * */
@Mapper
public interface GlossaryMapper {

    // 전체 용어 조회
    // GlossaryDto 객체들을 List로 반환하는 기능
    // 그 기능을 담당하는 메서드의 이름은 selectGlossaryList
    List<GlossaryDto> selectGlossaryList();


    // 용어 번호 기준으로 조회하는 메서드(selectGlossaryById)
    GlossaryDto selectGlossaryById(@Param("termId") Long termId);

    // 키워드 기준으로 조회하는 메서드(searchGlossary)
    List<GlossaryDto> searchGlossary(@Param("keyword") String keyword);

    // 카테고리별 조회를 위한 메서드
    List<GlossaryDto> selectGlossaryByCategory(@Param("category") String category);

    // 카테고리 목록 조회를 위한 메서드
    List<String> selectCategoryList();

    //검색어가 들어있는 카테고리 조회를 위한 메서드
    List<String> selectCategoryCodesByKeyword(
            @Param("keyword") String keyword
    );

    //키워드를 활용한 사전 조회를 위한 메서드
    List<GlossaryDto> selectGlossaryByCategoryAndKeyword(
            @Param("category") String category,
            @Param("keyword") String keyword
    );

    // 하이라이트가 전체 용어 목록을 조회
    List<GlossaryDto> selectAllTerms();

    // 검색어 자동 완성 기능을 위한 메서드
    List<String> AutoCompleteTerms(
            @Param("keyword") String keyword
    );
}