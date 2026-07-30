package com.kh.demo.community.mapper;

import com.kh.demo.community.dto.BoardDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BoardMapper {

    // 게시글 등록
    int insertBoard(BoardDto boardDto);

    // 게시글 목록 조회 (카테고리 필터 + 키워드 검색 + 페이징)
    List<BoardDto> selectBoardList(@Param("category") String category,
                                   @Param("keyword") String keyword,
                                   @Param("offset") int offset,
                                   @Param("size") int size);

    // 게시글 목록 총 개수 (페이징 계산용)
    int countBoardList(@Param("category") String category,
                       @Param("keyword") String keyword);

    // 게시글 상세 조회
    BoardDto selectBoardDetail(@Param("boardId") Long boardId);

    // 조회수 증가
    int increaseCount(@Param("boardId") Long boardId);

    // 게시글 수정 (작성자 본인만 수정 가능하도록 memberId도 함께 조건으로 사용)
    int updateBoard(BoardDto boardDto);

    // 게시글 삭제 (작성자 본인만 삭제 가능하도록 memberId도 함께 조건으로 사용)
    int deleteBoard(@Param("boardId") Long boardId, @Param("memberId") String memberId);

    // 게시글 존재 여부 확인 (조회수 증가 부작용 없이 존재만 확인할 때 사용)
    boolean existsById(@Param("boardId") Long boardId);

    // 좋아요 수 증가/감소 (좋아요 토글 시 사용)
    int increaseLikeCount(@Param("boardId") Long boardId);

    int decreaseLikeCount(@Param("boardId") Long boardId);

    // 현재 좋아요 수 조회 (토글 응답에 최신 카운트를 실어주기 위함)
    long selectLikeCount(@Param("boardId") Long boardId);
}