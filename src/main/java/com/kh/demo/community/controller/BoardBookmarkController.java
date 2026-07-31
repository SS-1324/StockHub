package com.kh.demo.community.controller;

import com.kh.demo.common.dto.ApiResponse;
import com.kh.demo.common.util.SessionUtil;
import com.kh.demo.community.CommunityUrls;
import com.kh.demo.community.service.BoardBookmarkService;
import com.kh.demo.community.service.BoardService;
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
* 게시글 북마크 토글 API (F-COM-01-05). 게시글 전용 - 댓글을 북마크하는 API 자체가 없으므로
* "댓글 대상 북마크 요청 시 차단" 요구사항은 구조적으로 항상 만족된다.
* 이 경로는 WebConfig의 LoginInterceptor가 보호하므로 진입 시점에는 항상 로그인 상태가 보장된다.
* */
@RestController
@RequestMapping(CommunityUrls.BOOKMARK)
public class BoardBookmarkController {

    @Autowired
    private BoardBookmarkService boardBookmarkService;

    @Autowired
    private BoardService boardService;

    @PostMapping("/{boardId}")
    public ResponseEntity<ApiResponse<Boolean>> toggleBookmark(@PathVariable Long boardId, HttpSession session) {
        try {
            if (!boardService.exists(boardId)) {
                throw new NoSuchElementException("게시글을 찾을 수 없습니다.");
            }
            String memberId = SessionUtil.requireLoginMemberId(session);
            boolean bookmarked = boardBookmarkService.toggleBookmark(boardId, memberId);
            return ResponseEntity.ok(ApiResponse.success(bookmarked));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail(e.getMessage()));
        }
    }
}
