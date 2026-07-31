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

@Controller
@RequestMapping(CommunityUrls.BASE)
public class BoardController {

    private static final int PAGE_SIZE = 10;

    @Autowired
    private BoardService boardService;

    @Autowired
    private BoardCommentService boardCommentService;

    @Autowired
    private BoardImageService boardImageService;

    @GetMapping
    public String list(@RequestParam(required = false) String category, HttpSession session, Model model) {
        String loginMemberId = SessionUtil.currentMemberId(session);
        // 무한스크롤의 첫 페이지 - 2페이지부터는 /feed가 담당(아래 참고)
        List<BoardDto> boardList = boardService.getList(category, 1, PAGE_SIZE, loginMemberId);

        model.addAttribute("boardList", boardList);
        model.addAttribute("category", category);
        model.addAttribute("loginMemberId", loginMemberId);
        model.addAttribute("allowedCategories", boardService.getAllowedCategories());
        return "community/boardList";
    }

    // 무한스크롤 다음 페이지 - board.js가 화면 바닥의 sentinel이 보이면 이 엔드포인트를 호출해 카드 조각(HTML)만 받아 붙인다.
    // 더 가져올 게시글이 없으면 boardCards.jsp가 아무것도 출력하지 않아 빈 응답이 오고, JS는 그걸 보고 관찰을 멈춘다.
    @GetMapping("/feed")
    public String feed(@RequestParam(required = false) String category,
                       @RequestParam(defaultValue = "2") int page,
                       HttpSession session,
                       Model model) {
        String loginMemberId = SessionUtil.currentMemberId(session);
        List<BoardDto> boardList = boardService.getList(category, page, PAGE_SIZE, loginMemberId);
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

    @PostMapping("/write")
    public String write(@ModelAttribute BoardDto boardDto,
                        @RequestParam(required = false) List<MultipartFile> images,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {
        boardDto.setMemberId(SessionUtil.requireLoginMemberId(session));
        Long boardId;
        try {
            boardId = boardService.write(boardDto, images);
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/community/write";
        }

        return "redirect:/community/" + boardId;
    }

    // 이미지 용량 초과(전역 설정값을 넘는 파일) 업로드 시 처리 - 이 컨트롤러의 폼 제출 흐름에서만 사용
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxUploadSize(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", "첨부파일 용량이 너무 큽니다.");
        return "redirect:/community/write";
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
            board = boardService.getDetail(boardId, loginMemberId);
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
            board = boardService.getDetail(boardId, loginMemberId);
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
