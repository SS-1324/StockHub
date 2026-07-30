package com.kh.demo.community.controller;

import com.kh.demo.common.SessionConst;
import com.kh.demo.community.dto.BoardCommentDto;
import com.kh.demo.community.dto.BoardDto;
import com.kh.demo.community.service.BoardCommentService;
import com.kh.demo.community.service.BoardImageService;
import com.kh.demo.community.service.BoardService;
import com.kh.demo.member.dto.MemberDto;
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
 * URL 설계
 * /board/{boardId}처럼 변수 하나짜리 패턴을 쓰지 않음
 * /board/list, /board/write, /board/detail/{boardID}처럼 각 기능마다 고정 문자열 세그먼트를 앞에 붙인다.
 * 고정문자열 : list, write, detail, edit, delete...
 *
 * /board/detail/{boardId}, /board/list 쓰는 것 대신 /board/{boardID}, /board/list로 작성해도 충돌하지는 않는다.
 * 다만 url의 의도를 명확히 드러내기가 어렵다.
 */

@Controller
@RequestMapping("/community")
public class BoardController {

    private static final int PAGE_SIZE = 10;

    @Autowired
    private BoardService boardService;

    @Autowired
    private BoardCommentService boardCommentService;

    @Autowired
    private BoardImageService boardImageService;

    @GetMapping
    public String list(@RequestParam(required = false) String category,
                       @RequestParam(defaultValue = "1") int page,
                       Model model) {
        List<BoardDto> boardList = boardService.getList(category, page, PAGE_SIZE);
        int totalCount = boardService.getListCount(category);
        int totalPages = Math.max((int) Math.ceil(totalCount / (double) PAGE_SIZE), 1);

        model.addAttribute("boardList", boardList);
        model.addAttribute("category", category);
        model.addAttribute("page", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("allowedCategories", boardService.getAllowedCategories());
        return "community/boardList";
    }

    @GetMapping("/write")
    public String writeForm(Model model) {
        model.addAttribute("allowedCategories", boardService.getAllowedCategories());
        model.addAttribute("defaultCategory", boardService.getDefaultCategory());
        return "community/boardWrite";
    }

    @PostMapping("/write")
    public String write(@ModelAttribute BoardDto boardDto,
                        @RequestParam(required = false) List<MultipartFile> images,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {
        MemberDto loginMember = (MemberDto) session.getAttribute(SessionConst.LOGIN_MEMBER);
        boardDto.setMemberId(loginMember.getMemberId());

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
        String loginMemberId = currentMemberId(session);

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
        return "community/boardDetail";
    }

    @GetMapping("/edit/{boardId}")
    public String editForm(@PathVariable Long boardId,
                           HttpSession session,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        MemberDto loginMember = (MemberDto) session.getAttribute(SessionConst.LOGIN_MEMBER);

        BoardDto board;
        try {
            board = boardService.getDetail(boardId, loginMember.getMemberId());
        } catch (NoSuchElementException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/community";
        }

        if (!loginMember.getMemberId().equals(board.getMemberId())) {
            redirectAttributes.addFlashAttribute("error", "수정 권한이 없습니다.");
            return "redirect:/community/" + boardId;
        }

        model.addAttribute("board", board);
        model.addAttribute("images", boardImageService.getByBoardId(boardId));
        model.addAttribute("allowedCategories", boardService.getAllowedCategories());
        return "community/boardEdit";
    }

    @PostMapping("/edit/{boardId}")
    public String edit(@PathVariable Long boardId,
                       @ModelAttribute BoardDto boardDto,
                       HttpSession session,
                       RedirectAttributes redirectAttributes) {
        MemberDto loginMember = (MemberDto) session.getAttribute(SessionConst.LOGIN_MEMBER);

        try {
            boardService.update(boardId, boardDto, loginMember.getMemberId());
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
        MemberDto loginMember = (MemberDto) session.getAttribute(SessionConst.LOGIN_MEMBER);

        try {
            boardService.delete(boardId, loginMember.getMemberId());
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/community/" + boardId;
        }

        return "redirect:/community";
    }

    // 비로그인 상태로도 볼 수 있는 경로(목록/상세)에서 안전하게 로그인 회원 id를 꺼내기 위한 헬퍼
    private String currentMemberId(HttpSession session) {
        if (session == null) {
            return null;
        }
        MemberDto loginMember = (MemberDto) session.getAttribute(SessionConst.LOGIN_MEMBER);
        return loginMember == null ? null : loginMember.getMemberId();
    }
}