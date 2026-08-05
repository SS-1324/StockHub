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

    //카테고리 내부 검색
    @Override
    public List<GlossaryDto> selectGlossaryByCategoryAndKeyword(
            String category,
            String keyword
    ) {
        return glossaryMapper.selectGlossaryByCategoryKeyword(
                category,
                keyword.trim()
        );
    }

    //검색 자동 완성
    @Override
    public List<String> AutoCompleteTerms(String keyword){
        return glossaryMapper.AutoCompleteTerms(keyword.trim()
        );
    }

    //사전 검색 기능
    @Override
    public List<GlossaryDto> searchGlossary(String keyword){
        return glossaryMapper.searchGlossary(keyword.trim());
    }
}
