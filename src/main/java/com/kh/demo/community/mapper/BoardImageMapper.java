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

    // 목록(피드) 카드용 - 여러 게시글의 이미지를 한 번에 조회 (N+1 방지, board_id/img_order 순으로 정렬돼 내려옴)
    List<BoardImageDto> selectByBoardIds(@Param("boardIds") List<Long> boardIds);

    // 게시글에 속한 이미지 전체 삭제 (게시글 삭제 시 사용)
    int deleteByBoardId(@Param("boardId") Long boardId);

    // 게시글 수정 화면에서 선택 삭제한 이미지들만 삭제 (board_id로도 다시 좁혀서 다른 글 이미지가 섞이지 않게 함)
    int deleteByIds(@Param("boardId") Long boardId, @Param("imageIds") List<Long> imageIds);
}
