package com.kh.demo.dictionary.service;

import com.kh.demo.dictionary.dto.GlossaryDto;
import com.kh.demo.dictionary.mapper.GlossaryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)

public class GlossaryServiceImpl implements GlossaryService {

    private final GlossaryMapper glossaryMapper;

    // 전체 용어 조회
    @Override
    public List<GlossaryDto> selectGlossaryList() {
        return glossaryMapper.selectGlossaryList();
    }

    // 용어 상세 조회
    @Override
    public GlossaryDto selectGlossaryById(Long termId) {
        return glossaryMapper.selectGlossaryById(termId);
    }

    // 카테고리별 조회
    @Override
    public List<GlossaryDto> selectGlossaryByCategory(String category) {
        return glossaryMapper.selectGlossaryByCategory(category);
    }

    // 카테고리 목록 조회
    @Override
    public List<String> selectCategoryList() {
        return glossaryMapper.selectCategoryList();
    }

    //검색어 포함 카테고리 조회
    @Override
    public List<String> selectCategoryCodesByKeyword(String keyword){
        return glossaryMapper.selectCategoryCodesByKeyword(keyword.trim());
    }

    //카테고리 내부 검색
    @Override
    public List<GlossaryDto> selectGlossaryByCategoryAndKeyword(
            String category,
            String keyword
    ) {
        return glossaryMapper.selectGlossaryByCategoryAndKeyword(
                category,
                keyword.trim()
        );
    }
}
