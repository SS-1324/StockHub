package com.kh.demo.community.service;

import com.kh.demo.community.dto.ToggleResultDto;
import com.kh.demo.community.mapper.BoardCommentMapper;
import com.kh.demo.community.mapper.CommentLikeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentLikeServiceImpl implements CommentLikeService {

    @Autowired
    private CommentLikeMapper commentLikeMapper;

    @Autowired
    private BoardCommentMapper boardCommentMapper;

    @Override
    @Transactional
    public ToggleResultDto toggleCommentLike(Long commentId, String memberId) {
        boolean alreadyLiked = commentLikeMapper.existsCommentLike(commentId, memberId);

        if (!alreadyLiked) {
            try {
                commentLikeMapper.insertCommentLike(commentId, memberId);
            } catch (DuplicateKeyException e) {
                // 동시 클릭으로 인한 레이스 - 이미 등록된 것으로 간주(멱등 처리)
            }
            return new ToggleResultDto(true, currentLikeCount(commentId));
        }

        commentLikeMapper.deleteCommentLike(commentId, memberId);
        return new ToggleResultDto(false, currentLikeCount(commentId));
    }

    @Override
    public boolean isLiked(Long commentId, String memberId) {
        if (memberId == null) {
            return false;
        }
        return commentLikeMapper.existsCommentLike(commentId, memberId);
    }

    private long currentLikeCount(Long commentId) {
        return boardCommentMapper.selectLikeCount(commentId);
    }
}
