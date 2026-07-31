package com.kh.demo.community.controller;

import com.kh.demo.common.util.SessionUtil;
import com.kh.demo.community.CommunityUrls;
import com.kh.demo.community.dto.BoardCommentDto;
import com.kh.demo.community.dto.BoardDto;
import com.kh.demo.community.service.BoardCommentService;
import com.kh.demo.community.service.BoardImageService;
import com.kh.demo.community.service.BoardService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.NoSuchElementException;

/*
 * 커뮤니티 게시판의 화면 이동과 요청을 처리하는 Controller
 *
 * 주요 주소
 * GET  /community              게시글 목록 및 검색
 * GET  /community/feed         무한스크롤 다음 페이지
 * GET  /community/write        글쓰기 화면
 * POST /community/write        게시글 등록
 * GET  /community/{boardId}    게시글 상세
 * GET  /community/edit/{id}    수정 화면
 */
@Controller
@RequestMapping(CommunityUrls.BASE)
public class BoardController {

    /*
     * 목록 한 페이지에 표시할 게시글 수
     * 첫 화면은 1페이지, 무한스크롤은 2페이지부터 가져온다.
     */
    private static final int PAGE_SIZE = 10;

    @Autowired
    private BoardService boardService;

    @Autowired
    private BoardCommentService boardCommentService;

    @Autowired
    private BoardImageService boardImageService;

    /*
     * 커뮤니티 첫 화면 및 검색 결과
     *
     * 예시:
     * /community
     * /community?keyword=삼성
     * /community?category=free&keyword=주식
     */
    @GetMapping
    public String list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            HttpSession session,
            Model model
    ) {
        /*
         * 검색어 앞뒤의 불필요한 공백을 제거한다.
         * 공백만 입력한 경우에는 검색하지 않고 전체 목록을 보여준다.
         */
        keyword = normalizeKeyword(keyword);

        // 현재 로그인한 회원의 아이디. 비로그인이면 null이다.
        String loginMemberId =
                SessionUtil.currentMemberId(session);

        /*
         * 첫 페이지 게시글을 조회한다.
         *
         * category: 선택된 게시판 카테고리
         * keyword: 제목/내용 검색어
         * 1: 첫 페이지
         * PAGE_SIZE: 한 페이지 게시글 수
         */
        List<BoardDto> boardList =
                boardService.getList(
                        category,
                        keyword,
                        1,
                        PAGE_SIZE,
                        loginMemberId
                );

        // JSP에서 게시글 카드를 출력할 때 사용한다.
        model.addAttribute("boardList", boardList);

        // 현재 선택된 카테고리를 유지한다.
        model.addAttribute("category", category);

        // 검색창에 검색어를 다시 표시한다.
        model.addAttribute("keyword", keyword);

        /*
         * 검색어가 있을 때만 전체 검색 결과 수를 조회한다.
         * JSP에서 "검색 결과 총 3건", "0건이 확인되었습니다"를 출력한다.
         */
        if (keyword != null) {
            long totalCount =
                    boardService.getTotalCount(
                            category,
                            keyword
                    );

            model.addAttribute("totalCount", totalCount);
        }

        // 로그인 여부에 따라 글쓰기 버튼 등을 다르게 표시할 때 사용한다.
        model.addAttribute(
                "loginMemberId",
                loginMemberId
        );

        // 자유·살까팔까·팁 공유 등의 카테고리 목록
        model.addAttribute(
                "allowedCategories",
                boardService.getAllowedCategories()
        );

        return "community/boardList";
    }

    /*
     * 무한스크롤의 다음 페이지를 조회한다.
     *
     * board.js에서 화면 아래의 sentinel이 보이면 호출한다.
     * 검색 중에는 keyword도 전달받아 다음 페이지에서도 검색을 유지한다.
     */
    @GetMapping("/feed")
    public String feed(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "2") int page,
            HttpSession session,
            Model model
    ) {
        // 첫 페이지와 동일한 검색어 정리 규칙을 사용한다.
        keyword = normalizeKeyword(keyword);

        String loginMemberId =
                SessionUtil.currentMemberId(session);

        /*
         * 요청받은 페이지의 게시글만 조회한다.
         * 결과는 전체 페이지가 아니라 boardCards.jsp 조각으로 반환된다.
         */
        List<BoardDto> boardList =
                boardService.getList(
                        category,
                        keyword,
                        page,
                        PAGE_SIZE,
                        loginMemberId
                );

        model.addAttribute("boardList", boardList);

        model.addAttribute(
                "allowedCategories",
                boardService.getAllowedCategories()
        );

        return "community/boardCards";
    }

    /*
     * 게시글 작성 화면
     */
    @GetMapping("/write")
    public String writeForm(Model model) {
        model.addAttribute(
                "allowedCategories",
                boardService.getAllowedCategories()
        );

        model.addAttribute(
                "defaultCategory",
                boardService.getDefaultCategory()
        );

        model.addAttribute(
                "maxImageCount",
                boardImageService.getMaxImageCount()
        );

        return "community/boardWrite";
    }

    /*
     * 게시글 등록
     *
     * 기존 팀 프로젝트의 일반 폼 제출 방식을 그대로 유지한다.
     */
    @PostMapping("/write")
    public String write(
            @ModelAttribute BoardDto boardDto,
            @RequestParam(required = false) List<MultipartFile> images,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        // 로그인한 회원을 게시글 작성자로 지정한다.
        boardDto.setMemberId(
                SessionUtil.requireLoginMemberId(session)
        );

        Long boardId;

        try {
            boardId =
                    boardService.write(
                            boardDto,
                            images
                    );
        } catch (IllegalArgumentException |
                 IllegalStateException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );

            return "redirect:/community/write";
        }

        // 등록이 완료되면 작성한 게시글 상세 화면으로 이동한다.
        return "redirect:/community/" + boardId;
    }

    /*
     * 서버에서 허용한 용량보다 큰 이미지가 업로드된 경우
     * 글쓰기 화면으로 돌아가 오류 메시지를 표시한다.
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxUploadSize(
            RedirectAttributes redirectAttributes
    ) {
        redirectAttributes.addFlashAttribute(
                "error",
                "첨부파일 용량이 너무 큽니다."
        );

        return "redirect:/community/write";
    }

    /*
     * 게시글 상세 화면
     */
    @GetMapping("/{boardId}")
    public String detail(
            @PathVariable Long boardId,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        String loginMemberId =
                SessionUtil.currentMemberId(session);

        BoardDto board;
        List<BoardCommentDto> comments;

        try {
            board =
                    boardService.getDetail(
                            boardId,
                            loginMemberId
                    );
        } catch (NoSuchElementException e) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );

            return "redirect:/community";
        }

        comments =
                boardCommentService.getList(
                        boardId,
                        loginMemberId
                );

        model.addAttribute("board", board);

        model.addAttribute(
                "images",
                boardImageService.getByBoardId(boardId)
        );

        model.addAttribute("comments", comments);
        model.addAttribute("loginMemberId", loginMemberId);

        model.addAttribute(
                "allowedCategories",
                boardService.getAllowedCategories()
        );

        return "community/boardDetail";
    }

    /*
     * 게시글 수정 화면
     */
    @GetMapping("/edit/{boardId}")
    public String editForm(
            @PathVariable Long boardId,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        String loginMemberId =
                SessionUtil.requireLoginMemberId(session);

        BoardDto board;

        try {
            board =
                    boardService.getDetail(
                            boardId,
                            loginMemberId
                    );
        } catch (NoSuchElementException e) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );

            return "redirect:/community";
        }

        /*
         * 작성자 본인이거나 관리자일 때만 수정할 수 있다.
         */
        boolean isOwner =
                loginMemberId.equals(board.getMemberId());

        if (!isOwner && !SessionUtil.isAdmin(session)) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "수정 권한이 없습니다."
            );

            return "redirect:/community/" + boardId;
        }

        model.addAttribute("board", board);

        model.addAttribute(
                "images",
                boardImageService.getByBoardId(boardId)
        );

        model.addAttribute(
                "allowedCategories",
                boardService.getAllowedCategories()
        );

        model.addAttribute(
                "maxImageCount",
                boardImageService.getMaxImageCount()
        );

        return "community/boardEdit";
    }

    /*
     * 게시글 수정 처리
     *
     * 관리자는 updateAsAdmin(),
     * 일반 회원은 작성자 검증이 포함된 update()를 사용한다.
     */
    @PostMapping("/edit/{boardId}")
    public String edit(
            @PathVariable Long boardId,
            @ModelAttribute BoardDto boardDto,
            @RequestParam(required = false) List<Long> deleteImageIds,
            @RequestParam(required = false) List<MultipartFile> images,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        try {
            if (SessionUtil.isAdmin(session)) {
                boardService.updateAsAdmin(
                        boardId,
                        boardDto,
                        deleteImageIds,
                        images
                );
            } else {
                boardService.update(
                        boardId,
                        boardDto,
                        SessionUtil.requireLoginMemberId(session),
                        deleteImageIds,
                        images
                );
            }
        } catch (IllegalArgumentException |
                 IllegalStateException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );

            return "redirect:/community/edit/" + boardId;
        }

        return "redirect:/community/" + boardId;
    }

    /*
     * 게시글 삭제 처리
     *
     * 관리자는 deleteAsAdmin(),
     * 일반 회원은 작성자 검증이 포함된 delete()를 사용한다.
     */
    @PostMapping("/delete/{boardId}")
    public String delete(
            @PathVariable Long boardId,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        try {
            if (SessionUtil.isAdmin(session)) {
                boardService.deleteAsAdmin(boardId);
            } else {
                boardService.delete(
                        boardId,
                        SessionUtil.requireLoginMemberId(session)
                );
            }
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );

            return "redirect:/community/" + boardId;
        }

        return "redirect:/community";
    }

    /*
     * 검색어를 DB에 전달하기 전에 정리한다.
     *
     * null      -> null
     * ""        -> null
     * "   "     -> null
     * " 삼성 "  -> "삼성"
     */
    private String normalizeKeyword(String keyword) {
        if (keyword == null ||
                keyword.trim().isEmpty()) {

            return null;
        }

        return keyword.trim();
    }
}