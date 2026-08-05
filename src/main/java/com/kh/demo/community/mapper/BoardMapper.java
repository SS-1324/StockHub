package com.kh.demo.community.mapper;

import com.kh.demo.community.dto.BoardDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BoardMapper {

    // 게시글 등록
    int insertBoard(BoardDto boardDto);

    List<BoardDto> selectBoardList(
            @Param("category") String category,
            @Param("keywords") List<String> keywords,
            @Param("offset") int offset,
            @Param("size") int size
    );

    long selectBoardCount(
            @Param("category") String category,
            @Param("keywords") List<String> keywords
    );
    // 게시글 상세 조회
    BoardDto selectBoardDetail(
            @Param("boardId") Long boardId
    );

    // 관리자 전용 상세 조회 (숨김 게시글 포함)
    BoardDto selectBoardDetailAsAdmin(
            @Param("boardId") Long boardId
    );

    // 조회수 증가
    int increaseCount(
            @Param("boardId") Long boardId
    );

    // 게시글 수정
    int updateBoard(BoardDto boardDto);

    // 게시글 삭제
    int deleteBoard(
            @Param("boardId") Long boardId,
            @Param("memberId") String memberId
    );

    // 관리자 전용 게시글 수정
    int updateBoardAsAdmin(BoardDto boardDto);

    // 관리자 전용 게시글 삭제
    int deleteBoardAsAdmin(
            @Param("boardId") Long boardId
    );

    // 게시글 존재 여부 확인
    boolean existsById(
            @Param("boardId") Long boardId
    );

    // 좋아요 수 증가
    int increaseLikeCount(
            @Param("boardId") Long boardId
    );

    // 좋아요 수 감소
    int decreaseLikeCount(
            @Param("boardId") Long boardId
    );

    // 현재 좋아요 수 조회
    long selectLikeCount(
            @Param("boardId") Long boardId
    );

    // 같은 카테고리의 이전 게시글 조회
    BoardDto selectPrevBoard(
            @Param("boardId") Long boardId,
            @Param("category") String category
    );

    // 같은 카테고리의 다음 게시글 조회
    BoardDto selectNextBoard(
            @Param("boardId") Long boardId,
            @Param("category") String category
    );
}
