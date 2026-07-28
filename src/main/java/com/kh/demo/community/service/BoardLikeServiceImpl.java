package com.kh.demo.community.service;

import com.kh.demo.community.dto.ToggleResultDto;
import com.kh.demo.community.mapper.BoardLikeMapper;
import com.kh.demo.community.mapper.BoardMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BoardLikeServiceImpl implements BoardLikeService {

    @Autowired
    private BoardLikeMapper boardLikeMapper;

    @Autowired
    private BoardMapper boardMapper;

    @Override
    @Transactional
    public ToggleResultDto toggleBoardLike(Long boardId, String memberId) {
        boolean alreadyLiked = boardLikeMapper.existsBoardLike(boardId, memberId);

        if (!alreadyLiked) {
            try {
                boardLikeMapper.insertBoardLike(boardId, memberId);
                boardMapper.increaseLikeCount(boardId);
            } catch (DuplicateKeyException e) {
                // 동시 클릭으로 인한 레이스 - 이미 등록된 것으로 간주(멱등 처리)
            }
            return new ToggleResultDto(true, boardMapper.selectLikeCount(boardId));
        }

        int deleted = boardLikeMapper.deleteBoardLike(boardId, memberId);
        if (deleted > 0) {
            boardMapper.decreaseLikeCount(boardId);
        }
        return new ToggleResultDto(false, boardMapper.selectLikeCount(boardId));
    }

    @Override
    public boolean isLiked(Long boardId, String memberId) {
        if (memberId == null) {
            return false;
        }
        return boardLikeMapper.existsBoardLike(boardId, memberId);
    }
}
