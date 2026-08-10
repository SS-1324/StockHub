package com.kh.demo.community.mapper;

import com.kh.demo.community.dto.BoardDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BoardBookmarkMapper {

    boolean existsBoardBookmark(@Param("boardId") Long boardId, @Param("memberId") String memberId);

    int insertBoardBookmark(@Param("boardId") Long boardId, @Param("memberId") String memberId);

    int deleteBoardBookmark(@Param("boardId") Long boardId, @Param("memberId") String memberId);

    // 목록(피드) 카드에서 북마크 버튼 활성 상태를 보여주기 위함 - 여러 게시글을 한 번에 조회(N+1 방지)
    List<Long> selectBookmarkedBoardIds(@Param("memberId") String memberId, @Param("boardIds") List<Long> boardIds);

    List<BoardDto> selectBookmarkedBoards(@Param("memberId") String memberId);

    long countBookmarkedBoards(@Param("memberId") String memberId);
}
