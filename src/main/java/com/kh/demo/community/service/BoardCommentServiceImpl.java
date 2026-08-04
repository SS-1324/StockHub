package com.kh.demo.community.service;

import com.kh.demo.community.dto.BoardCommentDto;
import com.kh.demo.community.mapper.BoardCommentMapper;
import com.kh.demo.community.mapper.BoardMapper;
import com.kh.demo.community.mapper.CommentLikeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class BoardCommentServiceImpl implements BoardCommentService {

    // board_comment.content 컬럼 크기(VARCHAR(1500))와 맞춘 길이 제한 - DB 예외로 새는 대신 명확한 메시지로 미리 검증
    private static final int MAX_CONTENT_LENGTH = 1500;

    @Autowired
    private BoardCommentMapper boardCommentMapper;

    @Autowired
    private BoardMapper boardMapper;

    @Autowired
    private CommentLikeMapper commentLikeMapper;

    @Autowired
    private TermHighlightService termHighlightService;

    @Override
    @Transactional
    public BoardCommentDto write(BoardCommentDto boardCommentDto, String loginMemberId) {
        validateContent(boardCommentDto.getContent());

        if (!boardMapper.existsById(boardCommentDto.getBoardId())) {
            throw new NoSuchElementException("게시글을 찾을 수 없습니다.");
        }

        if (boardCommentDto.getParentCommentId() != null) {
            validateParent(boardCommentDto);
        }

        boardCommentDto.setMemberId(loginMemberId);
        boardCommentMapper.insertBoardComment(boardCommentDto);

        BoardCommentDto saved = boardCommentMapper.selectById(boardCommentDto.getCommentId());
        saved.setHighlightedContent(termHighlightService.highlight(saved.getContent(), loginMemberId));
        saved.setLiked(false);
        return saved;
    }

    @Override
    public List<BoardCommentDto> getList(Long boardId, String loginMemberId) {
        List<BoardCommentDto> comments = boardCommentMapper.selectByBoardId(boardId);
        for (BoardCommentDto comment : comments) {
            comment.setHighlightedContent(termHighlightService.highlight(comment.getContent(), loginMemberId));
            comment.setLiked(loginMemberId != null && commentLikeMapper.existsCommentLike(comment.getCommentId(), loginMemberId));
        }
        return comments;
    }

    @Override
    @Transactional
    public void delete(Long boardId, Long commentId, String loginMemberId) {
        BoardCommentDto comment = boardCommentMapper.selectById(commentId);
        if (comment == null || !boardId.equals(comment.getBoardId())) {
            throw new NoSuchElementException("댓글을 찾을 수 없습니다.");
        }
        if (!loginMemberId.equals(comment.getMemberId())) {
            throw new IllegalStateException("댓글을 삭제할 권한이 없습니다.");
        }

        // parent_comment_id/comment_like 모두 board_comment에 ON DELETE CASCADE로 걸려있어서
        // 이 행 하나만 지우면 답글과 좋아요는 DB가 함께 정리해준다.
        int deleted = boardCommentMapper.deleteById(boardId, commentId, loginMemberId);
        if (deleted == 0) {
            throw new IllegalStateException("댓글이 존재하지 않거나 삭제 권한이 없습니다.");
        }
    }

    @Override
    @Transactional
    public void updateAsAdmin(Long boardId, Long commentId, String content) {
        validateContent(content);
        int updated = boardCommentMapper.updateByIdAsAdmin(boardId, commentId, content);
        if (updated == 0) {
            throw new NoSuchElementException("댓글을 찾을 수 없습니다.");
        }
    }

    @Override
    @Transactional
    public void deleteAsAdmin(Long boardId, Long commentId) {
        int deleted = boardCommentMapper.deleteByIdAsAdmin(boardId, commentId);
        if (deleted == 0) {
            throw new NoSuchElementException("댓글을 찾을 수 없습니다.");
        }
    }

    @Override
    public boolean exists(Long commentId) {
        return boardCommentMapper.selectById(commentId) != null;
    }

    private void validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("빈 댓글은 작성할 수 없습니다.");
        }
        if (content.length() > MAX_CONTENT_LENGTH) {
            throw new IllegalArgumentException("댓글은 " + MAX_CONTENT_LENGTH + "자를 넘을 수 없습니다.");
        }
    }

    // 답글은 깊이 1까지만 허용 -> 부모가 존재하고, 같은 게시글이고, 그 부모 자신은 최상위 댓글이어야 함
    private void validateParent(BoardCommentDto boardCommentDto) {
        BoardCommentDto parent = boardCommentMapper.selectById(boardCommentDto.getParentCommentId());
        if (parent == null) {
            throw new NoSuchElementException("답글을 달 댓글을 찾을 수 없습니다.");
        }
        if (!parent.getBoardId().equals(boardCommentDto.getBoardId())) {
            throw new IllegalArgumentException("잘못된 댓글 요청입니다.");
        }
        if (parent.getParentCommentId() != null) {
            throw new IllegalArgumentException("답글에는 답글을 달 수 없습니다.");
        }
    }
}
