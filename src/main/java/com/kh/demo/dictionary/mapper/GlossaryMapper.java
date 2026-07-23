package com.kh.demo.dictionary.mapper;

import com.kh.demo.dictionary.dto.GlossaryDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GlossaryMapper{

    // 전체 용어 조회
    // GlossaryDto 객체들을 List로 반환하는 기능
    // 그 기능이 담긴 메서드의 이름이 selectGlossaryList
    List<GlossaryDto> selectGlossaryList();

    // 용어 번호 기준으로 조회하는 메서드(selectGlossaryById)
    // Dto에 들어있는 termId(용어 번호)를 기준 삼아 Mapper.xml에서 사용
    GlossaryDto selectGlossaryById( @Param("termId") Long termId );

    
    List<GlossaryDto> searchGlossary(
            @Param("keyword") String keyword
    );
    }