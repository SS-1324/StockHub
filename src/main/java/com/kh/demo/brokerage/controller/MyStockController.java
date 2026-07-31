package com.kh.demo.brokerage.controller;

import com.kh.demo.brokerage.service.MyStockService;
import com.kh.demo.common.util.SessionUtil;
import com.kh.demo.member.service.MemberService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

// 헤더의 '내 주식' 메뉴에서 사용하는 화면 요청을 처리
@Controller
public class MyStockController {

    private final MyStockService myStockService;
    private final MemberService memberService;

    public MyStockController(MyStockService myStockService,
                             MemberService memberService) {
        this.myStockService = myStockService;
        this.memberService = memberService;
    }

    @GetMapping("/member/stocks")
    public String myStocks(HttpSession session, Model model) {
        String memberId = SessionUtil.requireLoginMemberId(session);

        model.addAttribute("member", memberService.getMemberProfile(memberId));
        model.addAttribute("stockSummary", myStockService.getMyStockSummary(memberId));

        return "member/myStocks";
    }
}
