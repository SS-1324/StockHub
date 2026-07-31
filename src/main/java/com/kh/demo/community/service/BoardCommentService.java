package com.kh.demo.community.service;

import com.kh.demo.community.dto.BoardCommentDto;

import java.util.List;

public interface BoardCommentService {

    // 댓글/답글 작성 (빈 댓글 방지, 답글은 깊이 1까지만 허용) -> 등록된 댓글(닉네임/멘션까지 채워진) 반환
    BoardCommentDto write(BoardCommentDto boardCommentDto, String loginMemberId);

    // 게시글의 댓글+답글 전체 조회 (로그인 회원의 좋아요 여부, 용어 하이라이트까지 채워서 반환)
    List<BoardCommentDto> getList(Long boardId, String loginMemberId);

    // 댓글 삭제 (작성자 본인 확인, 최상위 댓글이면 딸린 답글까지 함께 삭제)
    void delete(Long boardId, Long commentId, String loginMemberId);

    // 관리자가 작성자와 관계없이 댓글 내용을 수정
    void updateAsAdmin(Long boardId, Long commentId, String content);

    // 관리자가 작성자와 관계없이 댓글을 삭제
    void deleteAsAdmin(Long boardId, Long commentId);

    // 댓글 존재 여부 확인 (댓글 좋아요 등 다른 기능에서 대상 댓글 유효성 검사용)
    boolean exists(Long commentId);
}
