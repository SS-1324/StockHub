package com.kh.demo.community.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BoardLikeMapper {

    // 이미 좋아요를 눌렀는지 여부 (토글 방향 결정용)
    boolean existsBoardLike(@Param("boardId") Long boardId, @Param("memberId") String memberId);

    int insertBoardLike(@Param("boardId") Long boardId, @Param("memberId") String memberId);

    int deleteBoardLike(@Param("boardId") Long boardId, @Param("memberId") String memberId);

    // 목록(피드) 카드에서 좋아요 버튼 활성 상태를 보여주기 위함 - 여러 게시글을 한 번에 조회(N+1 방지)
    List<Long> selectLikedBoardIds(@Param("memberId") String memberId, @Param("boardIds") List<Long> boardIds);
}
