package com.kh.demo.common.controller;

import com.kh.demo.community.service.BoardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

// 메인 화면 요청을 처리
@Controller
public class HomeController {

    private final BoardService boardService;

    public HomeController(BoardService boardService) {
        this.boardService = boardService;
    }

    // 루트 주소에서 메인 JSP를 반환
    @GetMapping("/")
    public String home(Model model){
        // 전체 게시판에서 가장 최근에 작성된 글 3개를 메인 화면에 전달
        model.addAttribute("latestBoards", boardService.getList(null, 1, 3));
        return "home/index";
    }
}
