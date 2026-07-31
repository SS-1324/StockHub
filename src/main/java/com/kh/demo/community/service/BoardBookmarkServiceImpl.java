package com.kh.demo.community.service;

import com.kh.demo.community.mapper.BoardBookmarkMapper;
import org.springframework.beans.factory.annotation.Autowired;
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

        return ToggleSupport.toggle(
                alreadyBookmarked,
                () -> boardBookmarkMapper.insertBoardBookmark(boardId, memberId),
                () -> boardBookmarkMapper.deleteBoardBookmark(boardId, memberId)
        );
    }

    @Override
    public boolean isBookmarked(Long boardId, String memberId) {
        if (memberId == null) {
            return false;
        }
        return boardBookmarkMapper.existsBoardBookmark(boardId, memberId);
    }
}
