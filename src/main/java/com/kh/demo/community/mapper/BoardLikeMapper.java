package com.kh.demo.community.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BoardLikeMapper {

    // 이미 좋아요를 눌렀는지 여부 (토글 방향 결정용)
    boolean existsBoardLike(@Param("boardId") Long boardId, @Param("memberId") String memberId);

    int insertBoardLike(@Param("boardId") Long boardId, @Param("memberId") String memberId);

    int deleteBoardLike(@Param("boardId") Long boardId, @Param("memberId") String memberId);
}
