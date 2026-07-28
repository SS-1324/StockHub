package com.kh.demo.community.service;

import com.kh.demo.community.dto.ToggleResultDto;

public interface CommentLikeService {

    // 댓글 좋아요 토글 (누른 적 없으면 등록+카운트 증가, 있으면 취소+카운트 감소)
    ToggleResultDto toggleCommentLike(Long commentId, String memberId);

    // 현재 로그인 회원이 이 댓글에 좋아요를 눌렀는지 여부
    boolean isLiked(Long commentId, String memberId);
}
