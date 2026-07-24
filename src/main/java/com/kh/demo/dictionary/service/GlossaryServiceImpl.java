package com.kh.demo.dictionary.service;

import com.kh.demo.dictionary.dto.GlossaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor

public class GlossaryServiceImpl implements GlossaryService {

    @Override
    public List<GlossaryDto> selectGlossaryList() {
        return List.of();
    }

    @Override
    public GlossaryDto selectGlossaryById(Long termId) {
        return null;
    }

    @Override
    public List<GlossaryDto> searchGlossary(String keyword) {
        return List.of();
    }

    @Override
    public List<GlossaryDto> selectGlossaryByCategory(String category) {
        return List.of();
    }

    @Override
    public List<String> selectCategoryList() {
        return List.of();
    }
}
