package com.kh.demo.dictionary.service;

import com.kh.demo.dictionary.dto.GlossaryDto;
import com.kh.demo.dictionary.mapper.GlossaryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor

public class GlossaryServiceImpl implements GlossaryService {

    private final GlossaryMapper glossaryMapper;

    // 전체 용어 조회
    @Override
    public List<GlossaryDto> selectGlossaryList() {
        return List.of();
    }

    // 용어 상세 조회
    @Override
    public GlossaryDto selectGlossaryById(Long termId) {
        return null;
    }

    // 용어 검색
    @Override
    public List<GlossaryDto> searchGlossary(String keyword) {
        return List.of();
    }

    // 카테고리별 조회
    @Override
    public List<GlossaryDto> selectGlossaryByCategory(String category) {
        return glossaryMapper.selectGlossaryByCategory(category);
    }

    // 카테고리 목록 조회
    @Override
    public List<String> selectCategoryList() {
        return List.of();
    }
}
