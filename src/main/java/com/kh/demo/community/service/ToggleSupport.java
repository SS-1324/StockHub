package com.kh.demo.community.service;

import org.springframework.dao.DuplicateKeyException;

// 좋아요/북마크류 "존재하면 삭제, 없으면 삽입(동시 클릭 충돌은 멱등 처리)" 토글 로직의 공용 골격.
// BoardLikeServiceImpl/CommentLikeServiceImpl/BoardBookmarkServiceImpl이 구조가 동일해서 추출함.
// insert/delete에 필요한 실제 매퍼 호출(및 좋아요 수 증감 같은 부가 작업)은 각 서비스가 람다로 넘긴다.
final class ToggleSupport {

    private ToggleSupport() {}

    static boolean toggle(boolean alreadyActive, Runnable insert, Runnable delete) {
        if (!alreadyActive) {
            try {
                insert.run();
            } catch (DuplicateKeyException e) {
                // 동시 클릭으로 인한 레이스 - 이미 등록된 것으로 간주(멱등 처리)
            }
            return true;
        }

        delete.run();
        return false;
    }
}
