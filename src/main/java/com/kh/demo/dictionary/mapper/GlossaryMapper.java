package com.kh.demo.dictionary.mapper;

import com.kh.demo.dictionary.dto.GlossaryDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/*
* 용어사전 매퍼 - 게시판 전문용어 하이라이트(F-COM-01-06)가 의존하는 최소 read 경로만 제공.
* 용어사전 도메인 자체의 등록/수정/삭제는 사전 담당자가 별도로 구현.
* */
@Mapper
public interface GlossaryMapper {

    // 하이라이트 매칭용 전체 용어 목록 조회
    List<GlossaryDto> selectAllTerms();
}
