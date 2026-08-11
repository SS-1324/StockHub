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

    // 메인 화면 등에 표시할 여러 용어를 요청한 순서대로 조회
    @Override
    public List<GlossaryDto> selectGlossaryByTerms(List<String> terms) {
        if (terms == null || terms.isEmpty()) {
            return List.of();
        }

        return glossaryMapper.selectGlossaryByTerms(terms);
    }

    // 요청할 때마다 DB에서 무작위 용어를 지정한 개수만큼 조회
    @Override
    public List<GlossaryDto> selectRandomGlossaryTerms(int limit) {
        if (limit <= 0) {
            return List.of();
        }

        return glossaryMapper.selectRandomGlossaryTerms(limit);
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
