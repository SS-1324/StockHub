package com.kh.demo.community.service;

import com.kh.demo.community.dto.BoardDto;
import com.kh.demo.community.dto.BoardImageDto;
import com.kh.demo.community.mapper.BoardBookmarkMapper;
import com.kh.demo.community.mapper.BoardLikeMapper;
import com.kh.demo.community.mapper.BoardMapper;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BoardServiceImpl implements BoardService {

    // 카테고리 key -> 화면 표시용 한글 라벨
    private static final Map<String, String> CATEGORY_LABELS
            = new LinkedHashMap<>();

    static {
        CATEGORY_LABELS.put("free", "자유");
        CATEGORY_LABELS.put("trade", "살까?팔까?");
        CATEGORY_LABELS.put("tip", "팁 공유");
        CATEGORY_LABELS.put("profit", "수익인증");
        CATEGORY_LABELS.put("review", "반성");
    }

    private static final String DEFAULT_CATEGORY = "free";

    // board 테이블 컬럼 크기에 맞춘 길이 제한
    private static final int MAX_TITLE_LENGTH = 200;
    private static final int MAX_CONTENT_LENGTH = 3000;

    // Quill 에디터에서 허용할 HTML 태그와 속성
    private static final Safelist CONTENT_SAFELIST = Safelist.none()
            .addTags(
                    "p",
                    "br",
                    "strong",
                    "em",
                    "s",
                    "a",
                    "ol",
                    "ul",
                    "li",
                    "span"
            )
            .addAttributes(
                    "a",
                    "href",
                    "target",
                    "rel"
            )
            .addAttributes(
                    "span",
                    "class"
            )
            .addProtocols(
                    "a",
                    "href",
                    "http",
                    "https"
            )
            .addEnforcedAttribute(
                    "a",
                    "target",
                    "_blank"
            )
            .addEnforcedAttribute(
                    "a",
                    "rel",
                    "noopener noreferrer"
            );

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
    public Long write(
            BoardDto boardDto,
            List<MultipartFile> images
    ) {
        boardDto.setCategory(
                resolveCategory(boardDto.getCategory())
        );

        validateTitleAndContent(boardDto);

        boardDto.setContent(
                sanitizeContent(boardDto.getContent())
        );

        boardMapper.insertBoard(boardDto);

        boardImageService.uploadImages(
                boardDto.getBoardId(),
                images
        );

        return boardDto.getBoardId();
    }

    @Override
    public List<BoardDto> getList(
            String category,
            String keyword,
            int page,
            int size,
            String loginMemberId
    ) {
        int safePage = Math.max(page, 1);
        int offset = (safePage - 1) * size;

        List<String> keywords;

        if (keyword == null || keyword.isBlank()) {
            keywords = List.of();
        } else {
            keywords = Arrays.asList(
                    keyword.trim().split("\\s+")
            );
        }

        List<BoardDto> boardList
                = boardMapper.selectBoardList(
                category,
                keywords,
                offset,
                size
        );

        if (boardList.isEmpty()) {
            return boardList;
        }

        List<Long> boardIds = boardList.stream()
                .map(BoardDto::getBoardId)
                .collect(Collectors.toList());

        attachImages(boardList, boardIds);

        attachViewerState(
                boardList,
                boardIds,
                loginMemberId
        );

        stripHtmlForPreview(boardList);

        return boardList;
    }

    // 검색 조건에 맞는 전체 게시글 개수 조회
    @Override
    public long getTotalCount(
            String category,
            String keyword
    ) {
        List<String> keywords;

        if (keyword == null || keyword.isBlank()) {
            keywords = List.of();
        } else {
            keywords = Arrays.asList(
                    keyword.trim().split("\\s+")
            );
        }

        return boardMapper.selectBoardCount(
                category,
                keywords
        );
    }

    // 목록에서는 HTML 태그를 제거하고 순수한 글자만 표시
    private void stripHtmlForPreview(
            List<BoardDto> boardList
    ) {
        for (BoardDto board : boardList) {
            String content = board.getContent() == null
                    ? ""
                    : board.getContent();

            board.setContent(
                    Jsoup.parse(content).text()
            );
        }
    }

    // 게시글 목록에 이미지 정보 추가
    private void attachImages(
            List<BoardDto> boardList,
            List<Long> boardIds
    ) {
        Map<Long, List<BoardImageDto>> imagesByBoardId
                = boardImageService.getByBoardIds(boardIds);

        for (BoardDto board : boardList) {
            board.setImageList(
                    imagesByBoardId.getOrDefault(
                            board.getBoardId(),
                            List.of()
                    )
            );
        }
    }

    // 로그인한 사용자의 좋아요 및 북마크 상태 추가
    private void attachViewerState(
            List<BoardDto> boardList,
            List<Long> boardIds,
            String loginMemberId
    ) {
        if (loginMemberId == null) {
            return;
        }

        Set<Long> likedIds = new HashSet<>(
                boardLikeMapper.selectLikedBoardIds(
                        loginMemberId,
                        boardIds
                )
        );

        Set<Long> bookmarkedIds = new HashSet<>(
                boardBookmarkMapper.selectBookmarkedBoardIds(
                        loginMemberId,
                        boardIds
                )
        );

        for (BoardDto board : boardList) {
            board.setLiked(
                    likedIds.contains(board.getBoardId())
            );

            board.setBookmarked(
                    bookmarkedIds.contains(board.getBoardId())
            );
        }
    }

    @Override
    public boolean exists(Long boardId) {
        return boardMapper.existsById(boardId);
    }

    @Override
    public BoardDto getDetail(
            Long boardId,
            String loginMemberId
    ) {
        boardMapper.increaseCount(boardId);

        BoardDto board
                = boardMapper.selectBoardDetail(boardId);

        if (board == null) {
            throw new NoSuchElementException(
                    "게시글을 찾을 수 없습니다."
            );
        }

        board.setLiked(
                loginMemberId != null
                        && boardLikeMapper.existsBoardLike(
                        boardId,
                        loginMemberId
                )
        );

        board.setBookmarked(
                loginMemberId != null
                        && boardBookmarkMapper.existsBoardBookmark(
                        boardId,
                        loginMemberId
                )
        );

        board.setHighlightedContent(
                termHighlightService.highlightHtml(
                        board.getContent(),
                        loginMemberId
                )
        );

        attachAdjacentBoards(board);

        return board;
    }

    // 같은 카테고리의 이전글과 다음글 정보 추가
    private void attachAdjacentBoards(BoardDto board) {
        BoardDto prev = boardMapper.selectPrevBoard(
                board.getBoardId(),
                board.getCategory()
        );

        if (prev != null) {
            board.setPrevBoardId(prev.getBoardId());
            board.setPrevTitle(prev.getTitle());
        }

        BoardDto next = boardMapper.selectNextBoard(
                board.getBoardId(),
                board.getCategory()
        );

        if (next != null) {
            board.setNextBoardId(next.getBoardId());
            board.setNextTitle(next.getTitle());
        }
    }

    @Override
    @Transactional
    public void update(
            Long boardId,
            BoardDto boardDto,
            String loginMemberId,
            List<Long> deleteImageIds,
            List<MultipartFile> newImages
    ) {
        boardDto.setBoardId(boardId);
        boardDto.setMemberId(loginMemberId);

        boardDto.setCategory(
                resolveCategory(boardDto.getCategory())
        );

        validateTitleAndContent(boardDto);

        boardDto.setContent(
                sanitizeContent(boardDto.getContent())
        );

        int updated = boardMapper.updateBoard(boardDto);

        if (updated == 0) {
            throw new IllegalStateException(
                    "게시글이 존재하지 않거나 수정 권한이 없습니다."
            );
        }

        boardImageService.updateImages(
                boardId,
                deleteImageIds,
                newImages
        );
    }

    @Override
    @Transactional
    public void delete(
            Long boardId,
            String loginMemberId
    ) {
        BoardDto board
                = boardMapper.selectBoardDetail(boardId);

        if (board == null
                || !loginMemberId.equals(board.getMemberId())) {

            throw new IllegalStateException(
                    "게시글이 존재하지 않거나 삭제 권한이 없습니다."
            );
        }

        boardImageService.deleteByBoardId(boardId);

        int deleted = boardMapper.deleteBoard(
                boardId,
                loginMemberId
        );

        if (deleted == 0) {
            throw new IllegalStateException(
                    "게시글이 존재하지 않거나 삭제 권한이 없습니다."
            );
        }
    }

    @Override
    @Transactional
    public void updateAsAdmin(
            Long boardId,
            BoardDto boardDto,
            List<Long> deleteImageIds,
            List<MultipartFile> newImages
    ) {
        boardDto.setBoardId(boardId);

        boardDto.setCategory(
                resolveCategory(boardDto.getCategory())
        );

        validateTitleAndContent(boardDto);

        boardDto.setContent(
                sanitizeContent(boardDto.getContent())
        );

        int updated
                = boardMapper.updateBoardAsAdmin(boardDto);

        if (updated == 0) {
            throw new IllegalStateException(
                    "게시글을 찾을 수 없습니다."
            );
        }

        boardImageService.updateImages(
                boardId,
                deleteImageIds,
                newImages
        );
    }

    @Override
    @Transactional
    public void deleteAsAdmin(Long boardId) {
        if (!boardMapper.existsById(boardId)) {
            throw new IllegalStateException(
                    "게시글을 찾을 수 없습니다."
            );
        }

        boardImageService.deleteByBoardId(boardId);

        int deleted
                = boardMapper.deleteBoardAsAdmin(boardId);

        if (deleted == 0) {
            throw new IllegalStateException(
                    "게시글을 삭제하지 못했습니다."
            );
        }
    }

    // 허용된 태그와 속성만 남기고 나머지는 제거
    private String sanitizeContent(String rawHtml) {
        return Jsoup.clean(
                rawHtml == null ? "" : rawHtml,
                CONTENT_SAFELIST
        );
    }

    // 카테고리 값 검증
    private String resolveCategory(String category) {
        if (category == null || category.isBlank()) {
            return DEFAULT_CATEGORY;
        }

        if (!CATEGORY_LABELS.containsKey(category)) {
            throw new IllegalArgumentException(
                    "허용되지 않는 카테고리입니다: "
                            + category
            );
        }

        return category;
    }

    // 제목 및 본문 입력값 검증
    private void validateTitleAndContent(
            BoardDto boardDto
    ) {
        if (boardDto.getTitle() == null) {
            boardDto.setTitle("");
        }

        if (boardDto.getTitle().length()
                > MAX_TITLE_LENGTH) {

            throw new IllegalArgumentException(
                    "제목은 "
                            + MAX_TITLE_LENGTH
                            + "자를 넘을 수 없습니다."
            );
        }

        if (boardDto.getContent() == null
                || boardDto.getContent().isBlank()) {

            throw new IllegalArgumentException(
                    "내용을 입력해주세요."
            );
        }

        if (boardDto.getContent().length()
                > MAX_CONTENT_LENGTH) {

            throw new IllegalArgumentException(
                    "내용은 "
                            + MAX_CONTENT_LENGTH
                            + "자를 넘을 수 없습니다."
            );
        }
    }
}