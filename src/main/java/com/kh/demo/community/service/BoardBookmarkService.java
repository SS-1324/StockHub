package com.kh.demo.community.service;

public interface BoardBookmarkService {

    // 게시글 북마크 토글 (게시글 전용, 댓글 대상 북마크는 API 자체가 존재하지 않아 구조적으로 차단됨) -> 토글 후 상태 반환
    boolean toggleBookmark(Long boardId, String memberId);

    // 현재 로그인 회원이 이 게시글을 북마크했는지 여부
    boolean isBookmarked(Long boardId, String memberId);
}
