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

    // 커뮤니티 오른쪽 위젯: 좋아요·댓글·조회수를 합산한 참여도 순 게시글
    List<BoardDto> selectHotBoards(@Param("size") int size);

    // 커뮤니티 오른쪽 위젯: 누적 조회수가 높은 게시글
    List<BoardDto> selectPopularBoards(@Param("size") int size);

    // 로그인 회원이 작성한 공개 게시글 목록
    List<BoardDto> selectBoardListByMemberId(
            @Param("memberId") String memberId
    );

    // 로그인 회원이 작성한 공개 게시글 수
    long selectBoardCountByMemberId(
            @Param("memberId") String memberId
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

}
