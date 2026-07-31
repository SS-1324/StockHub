package com.kh.demo.community.service;

import com.kh.demo.community.dto.BoardDto;
import com.kh.demo.community.dto.BoardImageDto;
import com.kh.demo.community.mapper.BoardLikeMapper;
import com.kh.demo.community.mapper.BoardBookmarkMapper;
import com.kh.demo.community.mapper.BoardMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BoardServiceImpl implements BoardService {

    // 카테고리 key -> 화면 표시용 한글 라벨. header.jsp의 커뮤니티 드롭다운(communityFreeUrl 등)과 반드시 같은 key를 써야
    // 헤더에서 들어온 ?category=free 같은 링크가 이 화이트리스트를 통과한다. 순서는 header.jsp 드롭다운 순서와 동일.
    private static final Map<String, String> CATEGORY_LABELS = new LinkedHashMap<>();
    static {
        CATEGORY_LABELS.put("free", "자유");
        CATEGORY_LABELS.put("trade", "살까?팔까?");
        CATEGORY_LABELS.put("tip", "팁 공유");
        CATEGORY_LABELS.put("profit", "수익인증");
        CATEGORY_LABELS.put("review", "반성");
    }
    private static final String DEFAULT_CATEGORY = "free";

    // board 테이블 컬럼 크기와 맞춘 길이 제한 (DB 예외로 새는 대신 명확한 메시지로 미리 검증)
    private static final int MAX_TITLE_LENGTH = 200;
    private static final int MAX_CONTENT_LENGTH = 3000;

    @Autowired
    private BoardMapper boardMapper;

    @Autowired
    private BoardLikeMapper boardLikeMapper;

    @Autowired
    private BoardBookmarkMapper boardBookmarkMapper;

    @Autowired
    private BoardImageService boardImageService;

    @Autowired
    private TermHighlightService termHighlightService;

    @Override
    public Map<String, String> getAllowedCategories() {
        return CATEGORY_LABELS;
    }

    @Override
    public String getDefaultCategory() {
        return DEFAULT_CATEGORY;
    }

    @Override
    @Transactional
    public Long write(BoardDto boardDto, List<MultipartFile> images) {
        boardDto.setCategory(resolveCategory(boardDto.getCategory()));
        validateTitleAndContent(boardDto);

        boardMapper.insertBoard(boardDto);
        boardImageService.uploadImages(boardDto.getBoardId(), images);

        return boardDto.getBoardId();
    }

    @Override
    public List<BoardDto> getList(String category, int page, int size, String loginMemberId) {
        int safePage = Math.max(page, 1);
        int offset = (safePage - 1) * size;
        List<BoardDto> boardList = boardMapper.selectBoardList(category, offset, size);
        if (boardList.isEmpty()) {
            return boardList;
        }

        List<Long> boardIds = boardList.stream().map(BoardDto::getBoardId).collect(Collectors.toList());
        attachImages(boardList, boardIds);
        attachViewerState(boardList, boardIds, loginMemberId);
        return boardList;
    }

    // 목록(피드) 카드에서 이미지를 보여주기 위해, 이번 페이지 게시글들의 이미지를 한 번에 조회해서 채워 넣는다
    private void attachImages(List<BoardDto> boardList, List<Long> boardIds) {
        Map<Long, List<BoardImageDto>> imagesByBoardId = boardImageService.getByBoardIds(boardIds);
        for (BoardDto board : boardList) {
            board.setImageList(imagesByBoardId.getOrDefault(board.getBoardId(), List.of()));
        }
    }

    // 목록(피드) 카드의 좋아요/북마크 버튼이 현재 로그인 회원 기준으로 활성 상태를 보여주도록 채워 넣는다 (비로그인이면 전부 false)
    private void attachViewerState(List<BoardDto> boardList, List<Long> boardIds, String loginMemberId) {
        if (loginMemberId == null) {
            return;
        }
        Set<Long> likedIds = new HashSet<>(boardLikeMapper.selectLikedBoardIds(loginMemberId, boardIds));
        Set<Long> bookmarkedIds = new HashSet<>(boardBookmarkMapper.selectBookmarkedBoardIds(loginMemberId, boardIds));
        for (BoardDto board : boardList) {
            board.setLiked(likedIds.contains(board.getBoardId()));
            board.setBookmarked(bookmarkedIds.contains(board.getBoardId()));
        }
    }

    @Override
    public boolean exists(Long boardId) {
        return boardMapper.existsById(boardId);
    }

    @Override
    public BoardDto getDetail(Long boardId, String loginMemberId) {
        boardMapper.increaseCount(boardId);

        BoardDto board = boardMapper.selectBoardDetail(boardId);
        if (board == null) {
            throw new NoSuchElementException("게시글을 찾을 수 없습니다.");
        }

        board.setLiked(loginMemberId != null && boardLikeMapper.existsBoardLike(boardId, loginMemberId));
        board.setBookmarked(loginMemberId != null && boardBookmarkMapper.existsBoardBookmark(boardId, loginMemberId));
        board.setHighlightedContent(termHighlightService.highlight(board.getContent(), loginMemberId));

        return board;
    }

    @Override
    @Transactional
    public void update(Long boardId, BoardDto boardDto, String loginMemberId,
                       List<Long> deleteImageIds, List<MultipartFile> newImages) {
        boardDto.setBoardId(boardId);
        boardDto.setMemberId(loginMemberId);
        boardDto.setCategory(resolveCategory(boardDto.getCategory()));
        validateTitleAndContent(boardDto);

        int updated = boardMapper.updateBoard(boardDto);
        if (updated == 0) {
            throw new IllegalStateException("게시글이 존재하지 않거나 수정 권한이 없습니다.");
        }

        boardImageService.updateImages(boardId, deleteImageIds, newImages);
    }

    @Override
    @Transactional
    public void delete(Long boardId, String loginMemberId) {
        BoardDto board = boardMapper.selectBoardDetail(boardId);
        if (board == null || !loginMemberId.equals(board.getMemberId())) {
            throw new IllegalStateException("게시글이 존재하지 않거나 삭제 권한이 없습니다.");
        }

        // board_comment/comment_like/board_like/board_bookmark/board_image 행은 FK의 ON DELETE CASCADE로
        // board 삭제 시 DB가 알아서 함께 정리해준다. 다만 실제 디스크의 이미지 파일은 DB가 모르므로
        // 소유권 확인 후 행이 사라지기 전에 애플리케이션에서 먼저 지운다.
        boardImageService.deleteByBoardId(boardId);

        int deleted = boardMapper.deleteBoard(boardId, loginMemberId);
        if (deleted == 0) {
            throw new IllegalStateException("게시글이 존재하지 않거나 삭제 권한이 없습니다.");
        }
    }

    @Override
    @Transactional
    public void updateAsAdmin(Long boardId, BoardDto boardDto,
                              List<Long> deleteImageIds, List<MultipartFile> newImages) {
        boardDto.setBoardId(boardId);
        boardDto.setCategory(resolveCategory(boardDto.getCategory()));
        validateTitleAndContent(boardDto);

        int updated = boardMapper.updateBoardAsAdmin(boardDto);
        if (updated == 0) {
            throw new IllegalStateException("게시글을 찾을 수 없습니다.");
        }

        boardImageService.updateImages(boardId, deleteImageIds, newImages);
    }

    @Override
    @Transactional
    public void deleteAsAdmin(Long boardId) {
        if (!boardMapper.existsById(boardId)) {
            throw new IllegalStateException("게시글을 찾을 수 없습니다.");
        }

        boardImageService.deleteByBoardId(boardId);

        int deleted = boardMapper.deleteBoardAsAdmin(boardId);
        if (deleted == 0) {
            throw new IllegalStateException("게시글을 삭제하지 못했습니다.");
        }
    }

    // 카테고리 미선택 시 기본값, 선택했는데 허용 목록에 없으면 검증 실패
    private String resolveCategory(String category) {
        if (category == null || category.isBlank()) {
            return DEFAULT_CATEGORY;
        }
        if (!CATEGORY_LABELS.containsKey(category)) {
            throw new IllegalArgumentException("허용되지 않는 카테고리입니다: " + category);
        }
        return category;
    }

    // 제목은 선택 입력(스레드형 게시판: 제목 없이 이미지+내용만으로도 작성 가능).
    // board.title 컬럼이 NOT NULL이라 미입력 시 빈 문자열로 채운다(JSP의 empty 체크는 null/""를 동일하게 취급함).
    private void validateTitleAndContent(BoardDto boardDto) {
        if (boardDto.getTitle() == null) {
            boardDto.setTitle("");
        }
        if (boardDto.getTitle().length() > MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException("제목은 " + MAX_TITLE_LENGTH + "자를 넘을 수 없습니다.");
        }
        if (boardDto.getContent() == null || boardDto.getContent().isBlank()) {
            throw new IllegalArgumentException("내용을 입력해주세요.");
        }
        if (boardDto.getContent().length() > MAX_CONTENT_LENGTH) {
            throw new IllegalArgumentException("내용은 " + MAX_CONTENT_LENGTH + "자를 넘을 수 없습니다.");
        }
    }
}
