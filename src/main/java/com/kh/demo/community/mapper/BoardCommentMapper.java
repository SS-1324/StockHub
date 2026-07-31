package com.kh.demo.community.mapper;

import com.kh.demo.community.dto.BoardCommentDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BoardCommentMapper {

    // 댓글/답글 등록 (parentCommentId가 null이면 최상위 댓글)
    int insertBoardComment(BoardCommentDto boardCommentDto);

    // 댓글 단건 조회 (답글 작성 시 부모 댓글 검증, 등록 직후 join된 row 재조회용)
    BoardCommentDto selectById(@Param("commentId") Long commentId);

    // 게시글에 달린 댓글+답글 전체 조회 (부모 댓글 바로 뒤에 그 답글들이 시간순으로 이어지도록 정렬됨)
    List<BoardCommentDto> selectByBoardId(@Param("boardId") Long boardId);

    // 댓글 삭제 (작성자 본인만 삭제 가능하도록 memberId도 함께 조건으로 사용)
    // parent_comment_id/board_id FK 모두 ON DELETE CASCADE라 답글/좋아요는 DB가 함께 정리해준다.
    int deleteById(@Param("boardId") Long boardId,
                   @Param("commentId") Long commentId,
                   @Param("memberId") String memberId);

    // 관리자 전용 댓글 수정/삭제 (컨트롤러에서 ADMIN 권한 확인 후 호출)
    int updateByIdAsAdmin(@Param("boardId") Long boardId,
                          @Param("commentId") Long commentId,
                          @Param("content") String content);

    int deleteByIdAsAdmin(@Param("boardId") Long boardId,
                          @Param("commentId") Long commentId);

    // 현재 좋아요 수 조회 (comment_like를 직접 세어서 반환, 토글 응답에 최신 카운트를 실어주기 위함)
    long selectLikeCount(@Param("commentId") Long commentId);
}
