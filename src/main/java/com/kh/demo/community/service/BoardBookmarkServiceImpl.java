package com.kh.demo.community.service;

import com.kh.demo.common.util.HtmlTextUtil;
import com.kh.demo.community.dto.BoardDto;
import com.kh.demo.community.mapper.BoardBookmarkMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    @Override
    public List<BoardDto> getBookmarkedBoards(String memberId) {
        List<BoardDto> boardList = boardBookmarkMapper.selectBookmarkedBoards(memberId);

        // 북마크 목록에는 Quill의 p, ol, li 같은 태그를 제외한 실제 글자만 표시
        boardList.forEach(board ->
                board.setContent(HtmlTextUtil.toPlainText(board.getContent()))
        );
        return boardList;
    }

    @Override
    public long getBookmarkedBoardCount(String memberId) {
        return boardBookmarkMapper.countBookmarkedBoards(memberId);
    }
}
