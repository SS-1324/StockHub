package com.kh.demo.community.controller;

import com.kh.demo.common.dto.ApiResponse;
import com.kh.demo.common.util.SessionUtil;
import com.kh.demo.community.CommunityUrls;
import com.kh.demo.community.dto.ToggleResultDto;
import com.kh.demo.community.service.BoardCommentService;
import com.kh.demo.community.service.CommentLikeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.NoSuchElementException;

/*
* 댓글 좋아요 토글 API (F-COM-01-04). 이 경로는 WebConfig의 LoginInterceptor가 보호하므로
* 진입 시점에는 항상 로그인 상태가 보장된다.
* */
@RestController
@RequestMapping(CommunityUrls.COMMENT_LIKE)
public class CommentLikeController {

    @Autowired
    private CommentLikeService commentLikeService;

    @Autowired
    private BoardCommentService boardCommentService;

    @PostMapping("/{commentId}")
    public ResponseEntity<ApiResponse<ToggleResultDto>> toggleLike(@PathVariable Long commentId, HttpSession session) {
        try {
            if (!boardCommentService.exists(commentId)) {
                throw new NoSuchElementException("댓글을 찾을 수 없습니다.");
            }
            String memberId = SessionUtil.requireLoginMemberId(session);
            ToggleResultDto result = commentLikeService.toggleCommentLike(commentId, memberId);
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail(e.getMessage()));
        }
    }
}
