package com.kh.demo.community.controller;

import com.kh.demo.common.dto.ApiResponse;
import com.kh.demo.common.util.SessionUtil;
import com.kh.demo.community.CommunityUrls;
import com.kh.demo.community.dto.BoardCommentDto;
import com.kh.demo.community.dto.BoardDto;
import com.kh.demo.community.service.BoardCommentService;
import com.kh.demo.community.service.BoardImageService;
import com.kh.demo.community.service.BoardService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

@Controller
@RequestMapping(CommunityUrls.BASE)
public class BoardController {

    private static final int PAGE_SIZE = 10;
    private static final int SIDEBAR_BOARD_SIZE = 3;

    @Autowired
    private BoardService boardService;

    @Autowired
    private BoardCommentService boardCommentService;

    @Autowired
    private BoardImageService boardImageService;

    @GetMapping
    public String list(@RequestParam(required = false) String category,
                       @RequestParam(required = false) String keyword,
                       HttpSession session,
                       Model model) {
        String loginMemberId = SessionUtil.currentMemberId(session);
        // 무한스크롤의 첫 페이지 - 2페이지부터는 /feed가 담당(아래 참고)
        List<BoardDto> boardList = boardService.getList(category, keyword, 1, PAGE_SIZE, loginMemberId);
        long totalCount = boardService.getTotalCount(category, keyword);

        model.addAttribute("boardList", boardList);
        model.addAttribute("category", category);
        model.addAttribute("keyword", keyword);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("loginMemberId", loginMemberId);
        model.addAttribute("allowedCategories", boardService.getAllowedCategories());
        /* [커뮤니티위젯-2] 필터된 본문과 별개로 최근 24시간 공개 게시글의 HOT·조회수 순위를 제공한다. */
        model.addAttribute("hotBoards", boardService.getHotBoards(SIDEBAR_BOARD_SIZE));
        model.addAttribute("popularBoards", boardService.getPopularBoards(SIDEBAR_BOARD_SIZE));
        return "community/boardList";
    }

    // 무한스크롤 다음 페이지 - board.js가 화면 바닥의 sentinel이 보이면 이 엔드포인트를 호출해 카드 조각(HTML)만 받아 붙인다.
    // 더 가져올 게시글이 없으면 boardCards.jsp가 아무것도 출력하지 않아 빈 응답이 오고, JS는 그걸 보고 관찰을 멈춘다.
    @GetMapping("/feed")
    public String feed(@RequestParam(required = false) String category,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(defaultValue = "2") int page,
                       HttpSession session,
                       Model model) {
        String loginMemberId = SessionUtil.currentMemberId(session);
        List<BoardDto> boardList = boardService.getList(category, keyword, page, PAGE_SIZE, loginMemberId);
        model.addAttribute("boardList", boardList);
        model.addAttribute("allowedCategories", boardService.getAllowedCategories());
        return "community/boardCards";
    }

    @GetMapping("/write")
    public String writeForm(Model model) {
        model.addAttribute("allowedCategories", boardService.getAllowedCategories());
        model.addAttribute("defaultCategory", boardService.getDefaultCategory());
        model.addAttribute("maxImageCount", boardImageService.getMaxImageCount());
        return "community/boardWrite";
    }

    // AJAX(fetch)로만 호출됨 - JSON으로 응답한다. 글쓰기 폼 페이지 자체를 다시 거치지 않고 바로 상세로 이동시켜서,
    // 성공 후 "뒤로가기"를 눌렀을 때 이미 제출한 글쓰기 폼이 아니라 목록으로 돌아가게 하기 위함
    // (board.js가 성공 시 location.replace()로 이동시켜서 글쓰기 폼이 히스토리에 안 남는다).
    @PostMapping("/write")
    @ResponseBody
    public ResponseEntity<ApiResponse<Long>> write(@ModelAttribute BoardDto boardDto,
                        @RequestParam(required = false) List<MultipartFile> images,
                        HttpSession session) {
        boardDto.setMemberId(SessionUtil.requireLoginMemberId(session));
        try {
            Long boardId = boardService.write(boardDto, images);
            return ResponseEntity.ok(ApiResponse.success(boardId));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.fail(e.getMessage()));
        }
    }

    // 이미지 용량 초과(전역 설정값을 넘는 파일) 업로드 시 처리. write()는 AJAX 전용이라 JSON으로,
    // 아직 전통적인 폼 제출 방식인 edit()에서 발생하면 원래대로 플래시 메시지 + 리다이렉트로 응답한다.
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public void handleMaxUploadSize(HttpServletRequest request, HttpServletResponse response,
                                    RedirectAttributes redirectAttributes) throws IOException {
        if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"message\":\"첨부파일 용량이 너무 큽니다.\"}");
            return;
        }
        redirectAttributes.addFlashAttribute("error", "첨부파일 용량이 너무 큽니다.");
        response.sendRedirect(request.getContextPath() + "/community/write");
    }

    @GetMapping("/{boardId}")
    public String detail(@PathVariable Long boardId,
                         HttpSession session,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        String loginMemberId = SessionUtil.currentMemberId(session);

        BoardDto board;
        List<BoardCommentDto> comments;
        try {
            board = SessionUtil.isAdmin(session)
                    ? boardService.getDetailAsAdmin(boardId, loginMemberId)
                    : boardService.getDetail(boardId, loginMemberId);
        } catch (NoSuchElementException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/community";
        }
        comments = boardCommentService.getList(boardId, loginMemberId);

        model.addAttribute("board", board);
        model.addAttribute("images", boardImageService.getByBoardId(boardId));
        model.addAttribute("comments", comments);
        model.addAttribute("loginMemberId", loginMemberId);
        model.addAttribute("allowedCategories", boardService.getAllowedCategories());
        return "community/boardDetail";
    }

    @GetMapping("/edit/{boardId}")
    public String editForm(@PathVariable Long boardId,
                           HttpSession session,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        String loginMemberId = SessionUtil.requireLoginMemberId(session);

        BoardDto board;
        try {
            board = SessionUtil.isAdmin(session)
                    ? boardService.getDetailAsAdmin(boardId, loginMemberId)
                    : boardService.getDetail(boardId, loginMemberId);
        } catch (NoSuchElementException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/community";
        }

        boolean isOwner = loginMemberId.equals(board.getMemberId());
        if (!isOwner && !SessionUtil.isAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "수정 권한이 없습니다.");
            return "redirect:/community/" + boardId;
        }

        model.addAttribute("board", board);
        model.addAttribute("images", boardImageService.getByBoardId(boardId));
        model.addAttribute("allowedCategories", boardService.getAllowedCategories());
        model.addAttribute("maxImageCount", boardImageService.getMaxImageCount());
        return "community/boardEdit";
    }

    @PostMapping("/edit/{boardId}")
    public String edit(@PathVariable Long boardId,
                       @ModelAttribute BoardDto boardDto,
                       @RequestParam(required = false) List<Long> deleteImageIds,
                       @RequestParam(required = false) List<MultipartFile> images,
                       HttpSession session,
                       RedirectAttributes redirectAttributes) {
        try {
            if (SessionUtil.isAdmin(session)) {
                boardService.updateAsAdmin(boardId, boardDto, deleteImageIds, images);
            } else {
                boardService.update(
                        boardId,
                        boardDto,
                        SessionUtil.requireLoginMemberId(session),
                        deleteImageIds,
                        images
                );
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/community/edit/" + boardId;
        }

        return "redirect:/community/" + boardId;
    }

    @PostMapping("/delete/{boardId}")
    public String delete(@PathVariable Long boardId,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        try {
            if (SessionUtil.isAdmin(session)) {
                boardService.deleteAsAdmin(boardId);
            } else {
                boardService.delete(boardId, SessionUtil.requireLoginMemberId(session));
            }
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/community/" + boardId;
        }

        return "redirect:/community";
    }
}
