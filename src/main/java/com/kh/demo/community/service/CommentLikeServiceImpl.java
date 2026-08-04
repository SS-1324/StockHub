package com.kh.demo.community.service;

import com.kh.demo.community.dto.ToggleResultDto;
import com.kh.demo.community.mapper.BoardCommentMapper;
import com.kh.demo.community.mapper.CommentLikeMapper;
import org.springframework.beans.factory.annotation.Autowired;
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

        boolean active = ToggleSupport.toggle(
                alreadyLiked,
                () -> commentLikeMapper.insertCommentLike(commentId, memberId),
                () -> commentLikeMapper.deleteCommentLike(commentId, memberId)
        );

        return new ToggleResultDto(active, currentLikeCount(commentId));
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
