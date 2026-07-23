package com.kh.demo.community.mapper;

import com.kh.demo.community.dto.BoardImageDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BoardImageMapper {

    // 게시글 이미지 등록
    int insertBoardImage(BoardImageDto boardImageDto);

    // 게시글에 속한 이미지 목록 조회 (대표이미지 순서대로)
    List<BoardImageDto> selectByBoardId(@Param("boardId") Long boardId);

    // 게시글에 속한 이미지 전체 삭제 (게시글 삭제 시 사용)
    int deleteByBoardId(@Param("boardId") Long boardId);
}
