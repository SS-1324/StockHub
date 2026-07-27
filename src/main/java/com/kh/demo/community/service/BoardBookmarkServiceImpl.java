package com.kh.demo.community.service;

import com.kh.demo.community.mapper.BoardBookmarkMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BoardBookmarkServiceImpl implements BoardBookmarkService {

    @Autowired
    private BoardBookmarkMapper boardBookmarkMapper;

    @Override
    @Transactional
    public boolean toggleBookmark(Long boardId, String memberId) {
        boolean alreadyBookmarked = boardBookmarkMapper.existsBoardBookmark(boardId, memberId);

        if (!alreadyBookmarked) {
            try {
                boardBookmarkMapper.insertBoardBookmark(boardId, memberId);
            } catch (DuplicateKeyException e) {
                // 동시 클릭으로 인한 레이스 - 이미 등록된 것으로 간주(멱등 처리)
            }
            return true;
        }

        boardBookmarkMapper.deleteBoardBookmark(boardId, memberId);
        return false;
    }

    @Override
    public boolean isBookmarked(Long boardId, String memberId) {
        if (memberId == null) {
            return false;
        }
        return boardBookmarkMapper.existsBoardBookmark(boardId, memberId);
    }
}
