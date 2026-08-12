    package com.kh.demo.community.service;
    
    import com.kh.demo.common.util.HtmlTextUtil;
    import com.kh.demo.community.dto.BoardDto;
    import com.kh.demo.community.dto.BoardImageDto;
    import com.kh.demo.community.mapper.BoardBookmarkMapper;
    import com.kh.demo.community.mapper.BoardLikeMapper;
    import com.kh.demo.community.mapper.BoardMapper;
    import com.kh.demo.ranking.dto.RankingDto;
    import com.kh.demo.ranking.service.RankingService;
    import org.jsoup.Jsoup;
    import org.jsoup.nodes.Document;
    import org.jsoup.nodes.Element;
    import org.jsoup.safety.Safelist;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;
    import org.springframework.web.multipart.MultipartFile;
    
    import java.util.Arrays;
    import java.util.HashSet;
    import java.util.LinkedHashMap;
    import java.util.List;
    import java.util.Locale;
    import java.util.Map;
    import java.util.NoSuchElementException;
    import java.util.Set;
    import java.util.stream.Collectors;
    
    @Service
    public class BoardServiceImpl implements BoardService {
    
        /*
         * 카테고리 key는 이미 DB 게시글과 연결된 영구 식별자이므로 화면 이름이 바뀌어도 유지한다.
         * key까지 바꾸면 기존 tip/profit/review 글이 새 필터에서 조회되지 않으므로,
         * 사용자에게 보이는 한글 라벨만 종목토론/정보공유/반성일지로 변경한다.
         */
        private static final Map<String, String> CATEGORY_LABELS
                = new LinkedHashMap<>();
    
        static {
            CATEGORY_LABELS.put("free", "자유");
            CATEGORY_LABELS.put("tip", "종목토론");
            CATEGORY_LABELS.put("profit", "정보공유");
            CATEGORY_LABELS.put("review", "반성일지");
        }
    
        private static final String DEFAULT_CATEGORY = "free";
    
        // board 테이블 컬럼 크기에 맞춘 길이 제한
        private static final int MAX_TITLE_LENGTH = 200;
        private static final int MAX_CONTENT_LENGTH = 3000;
        // 빈 줄(<p><br></p>)은 눈에 보이는 글자 수가 거의 없어 MAX_CONTENT_LENGTH를 우회할 수 있으므로,
        // 줄바꿈 자체의 개수(p/br 태그 수)도 별도로 제한한다.
        private static final int MAX_CONTENT_LINES = 300;
        // 검색어가 너무 길면 공백 기준으로 쪼갠 단어 수만큼 LIKE 조건이 늘어나므로 상한을 둔다
        private static final int MAX_KEYWORD_LENGTH = 100;
        /*
         * HOT·인기글은 제목이 없을 때 본문 일부를 대신 표시한다.
         * 서버에서 최대 길이와 최소 단어 경계를 함께 관리해 JSP와 CSS가 임의로 문자열을 자르지 않게 한다.
         */
        private static final int WIDGET_PREVIEW_MAX_LENGTH = 44;
        private static final int WIDGET_PREVIEW_MIN_WORD_BREAK = 28;
    
        /*
         * Quill 글자색 팔레트와 동일한 값만 허용한다.
         * span의 style 전체를 무조건 허용하지 않고 color 한 속성만 검사해 저장한다.
         */
        private static final Set<String> ALLOWED_TEXT_COLOR_STYLES = Set.of(
                "color:rgb(230,0,0)",
                "color:rgb(255,153,0)",
                "color:rgb(0,138,0)",
                "color:rgb(0,102,204)",
                "color:rgb(153,51,255)",
                "color:#e60000",
                "color:#ff9900",
                "color:#008a00",
                "color:#0066cc",
                "color:#9933ff"
        );
    
        /*
         * [게시글HTML허용목록]
         * Quill이 만드는 문단·목록·문자 서식만 통과시키는 1차 XSS 방어선이다.
         * 링크는 http(s)만 허용하고, span style은 아래 sanitizeContent에서 색상 목록을 다시 검사한다.
         */
        private static final Safelist CONTENT_SAFELIST = Safelist.none()
                .addTags(
                        "p",
                        "br",
                        "strong",
                        "em",
                        "u",
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
                        "class",
                        "style"
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
    
        // 게시판 작성자의 현재 수익률 순위를 조회
        @Autowired
        private RankingService rankingService;
    
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

            List<String> keywords = splitKeywords(keyword);

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
            // 게시글 작성자에게 현재 수익률 순위를 연결
            attachRankPositions(boardList);
    
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
            List<String> keywords = splitKeywords(keyword);

            return boardMapper.selectBoardCount(
                    category,
                    keywords
            );
        }

        // 검색어를 공백 기준으로 쪼개기 전에 길이를 잘라, 지나치게 긴 입력이 AND 조건을 과도하게 늘리는 것을 막는다
        private List<String> splitKeywords(String keyword) {
            if (keyword == null || keyword.isBlank()) {
                return List.of();
            }

            String trimmed = keyword.trim();
            if (trimmed.length() > MAX_KEYWORD_LENGTH) {
                trimmed = trimmed.substring(0, MAX_KEYWORD_LENGTH);
            }

            return Arrays.asList(trimmed.split("\\s+"));
        }
    
        /* [커뮤니티위젯-1]
         * HOT과 인기글은 현재 피드 10개를 브라우저에서 다시 정렬하지 않고
         * DB의 최근 24시간 공개 게시글을 기준으로 각각의 정렬 규칙을 적용한다.
         */
        @Override
        public List<BoardDto> getHotBoards(int size) {
            List<BoardDto> boards = boardMapper.selectHotBoards(normalizeWidgetSize(size));
            prepareWidgetPreviews(boards);
            return boards;
        }
    
        @Override
        public List<BoardDto> getPopularBoards(int size) {
            List<BoardDto> boards = boardMapper.selectPopularBoards(normalizeWidgetSize(size));
            prepareWidgetPreviews(boards);
            return boards;
        }
    
        // 사이드바가 과도한 목록을 요청하지 않도록 1~10개 범위로 제한한다.
        private int normalizeWidgetSize(int size) {
            return Math.max(1, Math.min(size, 10));
        }
    
        /*
         * [커뮤니티사이드바-본문미리보기]
         * Quill 본문에는 <p>, <strong>, <ol> 같은 HTML 태그가 들어 있으므로 그대로 출력하지 않는다.
         * 허용된 HTML만 남긴 뒤 화면에 보이는 글자만 추출하고, 줄바꿈과 연속 공백은 한 칸으로 합친다.
         * 마지막에는 44글자를 무조건 끊기보다 28글자 이후의 마지막 공백을 우선해 문장이 덜 어색하게 잘리도록 한다.
         */
        private void prepareWidgetPreviews(List<BoardDto> boards) {
            for (BoardDto board : boards) {
                String content = board.getContent() == null ? "" : board.getContent();
                String plainText = htmlToPlainTextPreservingLineBreaks(sanitizeContent(content))
                        .replace('\u00A0', ' ')
                        .replace("\u200B", "")
                        .replace("\uFEFF", "")
                        .replaceAll("\\s+", " ")
                        .trim();
    
                board.setContent(truncateWidgetPreview(plainText));
            }
        }
    
        /*
         * Unicode 코드 포인트 기준으로 잘라 이모지나 조합 문자가 중간에서 깨지는 것을 막는다.
         * 최대 44자 안의 마지막 공백이 28자 이후에 있으면 단어 단위로 자르고 말줄임표를 붙인다.
         */
        private String truncateWidgetPreview(String text) {
            int codePointCount = text.codePointCount(0, text.length());
            if (codePointCount <= WIDGET_PREVIEW_MAX_LENGTH) {
                return text;
            }
    
            int maxEndIndex = text.offsetByCodePoints(0, WIDGET_PREVIEW_MAX_LENGTH);
            String candidate = text.substring(0, maxEndIndex);
            int lastSpaceIndex = candidate.lastIndexOf(' ');
    
            /* 너무 앞에서 끊기는 것을 막고, 적당한 위치에 공백이 있을 때만 단어 경계를 사용한다. */
            if (lastSpaceIndex >= WIDGET_PREVIEW_MIN_WORD_BREAK) {
                candidate = candidate.substring(0, lastSpaceIndex);
            }
    
            return candidate.stripTrailing() + "…";
        }
    
        @Override
        public List<BoardDto> getMemberPosts(
                String memberId,
                String loginMemberId
        ) {
            List<BoardDto> boardList =
                    boardMapper.selectBoardListByMemberId(memberId);
    
            if (boardList.isEmpty()) {
                return boardList;
            }
    
            List<Long> boardIds = boardList.stream()
                    .map(BoardDto::getBoardId)
                    .collect(Collectors.toList());
    
            attachImages(boardList, boardIds);
            attachViewerState(boardList, boardIds, loginMemberId);
            stripHtmlForPreview(boardList);
            return boardList;
        }
    
        @Override
        public long getMemberPostCount(String memberId) {
            return boardMapper.selectBoardCountByMemberId(memberId);
        }
    
        private void stripHtmlForPreview(List<BoardDto> boardList) {
            for (BoardDto board : boardList) {
                String content = board.getContent() == null ? "" : board.getContent();
                String safeContent = sanitizeContent(content);
                String formattedPreview = buildCompactPreviewHtml(safeContent);
                String plainPreview = htmlToPlainTextPreservingLineBreaks(formattedPreview);
    
                String visiblePreview = plainPreview
                        .replace('\u00A0', ' ')
                        .replace("\u200B", "")
                        .replace("\uFEFF", "")
                        .trim();
    
                if (visiblePreview.isBlank()) {
                    board.setFormattedPreviewContent(null);
                    board.setContent("");
                } else {
                    board.setFormattedPreviewContent(formattedPreview);
                    board.setContent(plainPreview);
                }
            }
        }
    
        // 홈과 커뮤니티 목록 카드가 함께 쓰는 HTML로, 글쓰기 서식을 유지하면서 문단과 목록을 <br>로 바꾼다.
        // 두 화면이 같은 값을 출력해야 bold·italic·color·size가 한쪽에서만 보이는 불일치가 생기지 않는다.
        private String buildCompactPreviewHtml(String safeContent) {
            String normalizedContent = (safeContent == null ? "" : safeContent)
                    .replace("\r\n", "\n");
    
            // 예전 게시글처럼 HTML 태그 없이 실제 개행만 저장된 본문도 줄바꿈을 유지한다.
            if (!normalizedContent.contains("<")) {
                normalizedContent = normalizedContent.replace("\n", "<br>");
            }
    
            Document previewDocument = Jsoup.parseBodyFragment(normalizedContent);
            previewDocument.outputSettings().prettyPrint(false);
    
            // 홈 카드는 카드 전체가 상세 링크이므로 본문 안의 링크 태그만 벗긴다.
            previewDocument.select("a").forEach(Element::unwrap);
    
            // 목록은 번호·불릿과 줄바꿈은 유지하고, 여러 줄 말줄임을 방해하는 블록 구조만 없앤다.
            for (Element list : previewDocument.select("ol, ul")) {
                boolean ordered = list.tagName().equals("ol");
                int itemNumber = 1;
    
                for (Element item : list.children()) {
                    if (!item.tagName().equals("li")) {
                        continue;
                    }
    
                    item.prependText(ordered ? itemNumber++ + ". " : "• ");
                    item.tagName("span");
                    item.after("<br>");
                }
    
                list.unwrap();
            }
    
            // Quill의 각 문단을 인라인 서식 + 줄바꿈 형태로 바꾼다.
            for (Element paragraph : previewDocument.select("p")) {
                String paragraphText = paragraph.text()
                        .replace('\u00A0', ' ')
                        .replace("\u200B", "")
                        .replace("\uFEFF", "")
                        .trim();
    
                if (paragraphText.isBlank()) {
                    paragraph.empty();
                }
    
                paragraph.tagName("span");
                paragraph.after("<br>");
            }
    
            String previewHtml = previewDocument.body().html()
                    // 비어 있는 문단 래퍼는 제거하고 줄바꿈만 남긴다.
                    .replaceAll("(?i)<span(?:\\s+[^>]*)?>\\s*</span>", "")
                    // 연속된 빈 줄은 한 줄까지만 남겨 카드 내용이 아래로 밀리지 않게 한다.
                    .replaceAll("(?i)(?:\\s*<br\\s*/?>\\s*){3,}", "<br><br>")
                    .replaceAll("(?i)^(?:\\s*<br\\s*/?>\\s*)+", "")
                    .replaceAll("(?i)(?:\\s*<br\\s*/?>\\s*)+$", "")
                    .trim();
    
            return previewHtml;
        }
    
        private String htmlToPlainTextPreservingLineBreaks(String html) {
            // 옛날 데이터: <p>/<br> 태그 없이 순수 텍스트 안에 \n만 들어있는 경우도 있어서,
            // 파싱하기 전에 원본 문자열의 실제 개행 문자도 먼저 마커로 바꿔둔다.
            String prepared = (html == null ? "" : html)
                    .replace("\r\n", "\n")
                    .replace("\n", "\u2424");
    
            Document document = Jsoup.parse(prepared);
            document.select("br").append("\u2424");   // br 자리에 마커 삽입
            document.select("p").prepend("\u2424");   // p 시작 지점에 마커 삽입
    
            String text = document.text()
                    .replace("\u2424", "\n")
                    .replaceAll("\n{3,}", "\n\n")
                    .trim();
    
            return text;
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
            return getDetailInternal(boardId, loginMemberId, false);
        }
    
        @Override
        public BoardDto getDetailAsAdmin(
                Long boardId,
                String loginMemberId
        ) {
            return getDetailInternal(boardId, loginMemberId, true);
        }
    
        private BoardDto getDetailInternal(
                Long boardId,
                String loginMemberId,
                boolean includeHidden
        ) {
            boardMapper.increaseCount(boardId);
    
            BoardDto board
                    = includeHidden
                    ? boardMapper.selectBoardDetailAsAdmin(boardId)
                    : boardMapper.selectBoardDetail(boardId);
    
            if (board == null) {
                throw new NoSuchElementException(
                        "게시글을 찾을 수 없습니다."
                );
            }
            // 상세 게시글 작성자에게 현재 수익률 순위를 연결
            attachRankPosition(board);
    
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
    
            return board;
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
    
        /*
         * [게시글HTML정제]
         * 1단계로 Jsoup Safelist가 스크립트·이벤트 속성·허용되지 않은 태그를 제거한다.
         * 2단계로 남은 span style을 정규화한 뒤 Quill 팔레트의 color 값만 허용한다.
         * 이 메서드는 작성·수정·미리보기에서 공통 사용되므로 저장과 출력의 보안 기준이 동일하다.
         */
        private String sanitizeContent(String rawHtml) {
            String cleanedHtml = Jsoup.clean(
                    rawHtml == null ? "" : rawHtml,
                    CONTENT_SAFELIST
            );
    
            Document document = Jsoup.parseBodyFragment(cleanedHtml);
            for (Element span : document.select("span[style]")) {
                String normalizedStyle = span.attr("style")
                        .toLowerCase(Locale.ROOT)
                        .replaceAll("\\s+", "");
    
                if (normalizedStyle.endsWith(";")) {
                    normalizedStyle = normalizedStyle.substring(0, normalizedStyle.length() - 1);
                }
    
                if (!ALLOWED_TEXT_COLOR_STYLES.contains(normalizedStyle)) {
                    span.removeAttr("style");
                }
            }
    
            return document.body().html();
        }
    
        // 카테고리 값 검증
        private String resolveCategory(String category) {
            if (category == null || category.isBlank()) {
                return DEFAULT_CATEGORY;
            }
    
            if (!CATEGORY_LABELS.containsKey(category)) {
                throw new IllegalArgumentException(
                        "허용되지 않는 카테고리입니다."
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

            if (HtmlTextUtil.hasDisallowedControlCharacter(boardDto.getTitle())
                    || HtmlTextUtil.hasDisallowedControlCharacter(boardDto.getContent())) {
                throw new IllegalArgumentException(
                        "사용할 수 없는 문자가 포함되어 있습니다."
                );
            }

            Document contentDocument = Jsoup.parseBodyFragment(
                    sanitizeContent(boardDto.getContent())
            );

            // 글자 수만 세면 빈 줄은 텍스트가 거의 없어 MAX_CONTENT_LENGTH를 통과하므로,
            // 개행을 극단적으로 많이 넣어 화면을 무한정 늘리는 시도는 줄 수 자체로 따로 막는다.
            int lineCount = contentDocument.select("p, br, li").size();
            if (lineCount > MAX_CONTENT_LINES) {
                throw new IllegalArgumentException(
                        "줄바꿈은 최대 "
                                + MAX_CONTENT_LINES
                                + "번까지 사용할 수 있습니다."
                );
            }

            String visibleContent = contentDocument
                    .text()
                    .replace('\u00A0', ' ')
                    .replace("\u200B", "")
                    .replace("\uFEFF", "")
                    .trim();

            if (visibleContent.isBlank()) {
    
                throw new IllegalArgumentException(
                        "내용을 입력해주세요."
                );
            }
    
            // Quill의 서식용 HTML 길이가 아니라 사용자가 실제로 입력한 본문 글자 수를 센다.
            // 특히 붙여넣은 이미지의 data URL처럼 화면에 보이지 않는 값이 제한에 포함되면 안 된다.
            if (visibleContent.length()
                    > MAX_CONTENT_LENGTH) {
    
                throw new IllegalArgumentException(
                        "내용은 "
                                + MAX_CONTENT_LENGTH
                                + "자를 넘을 수 없습니다."
                );
            }
        }
    
        /*
         * 현재 수익률 랭킹의 1~3위를
         * memberId -> rankPosition 형태로 변환한다.
         */
        private Map<String, Integer> getTopRankPositions() {
    
            return rankingService.getRankingBoard()
                    .stream()
                    .filter(ranking ->
                            ranking.getMemberId() != null
                                    && ranking.getRankPosition() != null
                                    && ranking.getRankPosition() <= 3
                    )
                    .collect(Collectors.toMap(
                            RankingDto::getMemberId,
                            RankingDto::getRankPosition,
                            (existing, replacement) -> existing
                    ));
        }
    
        /*
         * 게시글 목록의 모든 작성자에게 순위를 연결한다.
         */
        private void attachRankPositions(
                List<BoardDto> boardList
        ) {
            Map<String, Integer> topRankPositions =
                    getTopRankPositions();
    
            for (BoardDto board : boardList) {
                board.setRankPosition(
                        topRankPositions.get(board.getMemberId())
                );
            }
        }
    
        /*
         * 게시글 상세 하나의 작성자에게 순위를 연결한다.
         */
        private void attachRankPosition(
                BoardDto board
        ) {
            if (board == null || board.getMemberId() == null) {
                return;
            }
    
            Map<String, Integer> topRankPositions =
                    getTopRankPositions();
    
            board.setRankPosition(
                    topRankPositions.get(board.getMemberId())
            );
        }
    }
