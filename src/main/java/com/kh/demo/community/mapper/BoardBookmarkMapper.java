package com.kh.demo.community.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BoardBookmarkMapper {

    boolean existsBoardBookmark(@Param("boardId") Long boardId, @Param("memberId") String memberId);

    int insertBoardBookmark(@Param("boardId") Long boardId, @Param("memberId") String memberId);

    int deleteBoardBookmark(@Param("boardId") Long boardId, @Param("memberId") String memberId);
}
