package com.kh.demo.community.controller;

import com.kh.demo.common.dto.ApiResponse;
import com.kh.demo.common.util.SessionUtil;
import com.kh.demo.community.CommunityUrls;
import com.kh.demo.community.dto.BoardCommentDto;
import com.kh.demo.community.service.BoardCommentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

/*
* 댓글/답글 작성·삭제 API (F-COM-01-03). 이 경로는 WebConfig의 LoginInterceptor가 보호하므로
* 진입 시점에는 항상 로그인 상태가 보장된다.
* */
@RestController
@RequestMapping(CommunityUrls.BASE)
public class BoardCommentController {

    @Autowired
    private BoardCommentService boardCommentService;

    @PostMapping("/{boardId}/comment")
    public ResponseEntity<ApiResponse<BoardCommentDto>> write(@PathVariable Long boardId,
                                                               @RequestBody BoardCommentDto boardCommentDto,
                                                               HttpSession session) {
        try {
            boardCommentDto.setBoardId(boardId);
            String memberId = SessionUtil.requireLoginMemberId(session);
            BoardCommentDto saved = boardCommentService.write(boardCommentDto, memberId);
            return ResponseEntity.ok(ApiResponse.success(saved));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.fail(e.getMessage()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail(e.getMessage()));
        }
    }

    @PostMapping("/{boardId}/comment/{commentId}/edit")
    public ResponseEntity<ApiResponse<Void>> editAsAdmin(@PathVariable Long boardId,
                                                          @PathVariable Long commentId,
                                                          @RequestBody BoardCommentDto boardCommentDto,
                                                          HttpSession session) {
        if (!SessionUtil.isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.fail("관리자만 댓글을 수정할 수 있습니다."));
        }

        try {
            boardCommentService.updateAsAdmin(boardId, commentId, boardCommentDto.getContent());
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.fail(e.getMessage()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail(e.getMessage()));
        }
    }

    @PostMapping("/{boardId}/comment/{commentId}/delete")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long boardId,
                                                     @PathVariable Long commentId,
                                                     HttpSession session) {
        try {
            if (SessionUtil.isAdmin(session)) {
                boardCommentService.deleteAsAdmin(boardId, commentId);
            } else {
                String memberId = SessionUtil.requireLoginMemberId(session);
                boardCommentService.delete(boardId, commentId, memberId);
            }
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.fail(e.getMessage()));
        }
    }
}
