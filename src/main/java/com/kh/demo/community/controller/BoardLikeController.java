package com.kh.demo.community.controller;

import com.kh.demo.common.SessionConst;
import com.kh.demo.common.dto.ApiResponse;
import com.kh.demo.community.dto.ToggleResultDto;
import com.kh.demo.community.service.BoardLikeService;
import com.kh.demo.community.service.BoardService;
import com.kh.demo.member.dto.MemberDto;
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
* 게시글 좋아요 토글 API (F-COM-01-04). 이 경로는 WebConfig의 LoginInterceptor가 보호하므로
* 진입 시점에는 항상 로그인 상태가 보장된다.
* */
@RestController
@RequestMapping("/community/board/like")
public class BoardLikeController {

    @Autowired
    private BoardLikeService boardLikeService;

    @Autowired
    private BoardService boardService;

    @PostMapping("/{boardId}")
    public ResponseEntity<ApiResponse<ToggleResultDto>> toggleLike(@PathVariable Long boardId, HttpSession session) {
        try {
            if (!boardService.exists(boardId)) {
                throw new NoSuchElementException("게시글을 찾을 수 없습니다.");
            }
            String memberId = loginMemberId(session);
            ToggleResultDto result = boardLikeService.toggleBoardLike(boardId, memberId);
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail(e.getMessage()));
        }
    }

    private String loginMemberId(HttpSession session) {
        MemberDto loginMember = (MemberDto) session.getAttribute(SessionConst.LOGIN_MEMBER);
        return loginMember.getMemberId();
    }
}
