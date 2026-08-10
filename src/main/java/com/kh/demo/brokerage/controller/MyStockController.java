package com.kh.demo.brokerage.controller;

import com.kh.demo.common.dto.ApiResponse;
import com.kh.demo.common.util.SessionUtil;
import com.kh.demo.community.service.BoardBookmarkService;
import com.kh.demo.community.service.BoardCommentService;
import com.kh.demo.community.service.BoardService;
import com.kh.demo.inquiry.service.InquiryService;
import com.kh.demo.member.dto.FollowDto;
import com.kh.demo.member.service.FollowService;
import com.kh.demo.member.service.MemberService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

// 헤더의 '내 정보' 메뉴와 회원의 게시글·팔로우·문의 정보를 처리
@Controller
public class MyStockController {

    private final MemberService memberService;
    private final BoardService boardService;
    private final BoardCommentService boardCommentService;
    private final BoardBookmarkService boardBookmarkService;
    private final FollowService followService;
    private final InquiryService inquiryService;

    public MyStockController(MemberService memberService,
                             BoardService boardService,
                             BoardCommentService boardCommentService,
                             BoardBookmarkService boardBookmarkService,
                             FollowService followService,
                             InquiryService inquiryService) {
        this.memberService = memberService;
        this.boardService = boardService;
        this.boardCommentService = boardCommentService;
        this.boardBookmarkService = boardBookmarkService;
        this.followService = followService;
        this.inquiryService = inquiryService;
    }

    // 프로필과 회원 활동 수를 한 번에 보여주는 내 정보 화면
    @GetMapping("/member/stocks")
    public String myStocks(HttpSession session, Model model) {
        if (SessionUtil.isAdmin(session)) {
            return "redirect:/admin";
        }

        String memberId = SessionUtil.requireLoginMemberId(session);

        model.addAttribute("member", memberService.getMemberProfile(memberId));
        model.addAttribute("myPostCount", boardService.getMemberPostCount(memberId));
        model.addAttribute("myCommentCount", boardCommentService.getMemberCommentCount(memberId));
        model.addAttribute("bookmarkCount", boardBookmarkService.getBookmarkedBoardCount(memberId));
        model.addAttribute("followerCount", followService.getFollowerCount(memberId));
        model.addAttribute("followingCount", followService.getFollowingCount(memberId));
        model.addAttribute(
                "inquiryCount",
                inquiryService.getMemberInquiries(memberId).size()
        );

        return "member/myStocks";
    }

    // 로그인 회원 본인이 작성한 게시글만 별도 페이지에 표시
    @GetMapping("/member/stocks/posts")
    public String myPosts(HttpSession session, Model model) {
        if (SessionUtil.isAdmin(session)) {
            return "redirect:/admin";
        }

        String memberId = SessionUtil.requireLoginMemberId(session);
        model.addAttribute(
                "boardList",
                boardService.getMemberPosts(memberId, memberId)
        );
        model.addAttribute("totalCount", boardService.getMemberPostCount(memberId));
        model.addAttribute("allowedCategories", boardService.getAllowedCategories());
        return "member/myPosts";
    }

    // 로그인 회원 본인이 작성한 댓글과 답글만 별도 페이지에 표시
    @GetMapping("/member/stocks/comments")
    public String myComments(HttpSession session, Model model) {
        if (SessionUtil.isAdmin(session)) {
            return "redirect:/admin";
        }

        String memberId = SessionUtil.requireLoginMemberId(session);
        model.addAttribute(
                "commentList",
                boardCommentService.getMemberComments(memberId)
        );
        model.addAttribute(
                "totalCount",
                boardCommentService.getMemberCommentCount(memberId)
        );
        return "member/myComments";
    }

    // 로그인 회원이 북마크한 공개 게시글만 별도 페이지에 표시
    @GetMapping("/member/stocks/bookmarks")
    public String bookmarkedPosts(HttpSession session, Model model) {
        if (SessionUtil.isAdmin(session)) {
            return "redirect:/admin";
        }

        String memberId = SessionUtil.requireLoginMemberId(session);
        model.addAttribute(
                "boardList",
                boardBookmarkService.getBookmarkedBoards(memberId)
        );
        model.addAttribute(
                "totalCount",
                boardBookmarkService.getBookmarkedBoardCount(memberId)
        );
        model.addAttribute("allowedCategories", boardService.getAllowedCategories());
        return "member/bookmarkedPosts";
    }

    // 나를 팔로우하는 회원 목록을 모달에 전달
    @GetMapping("/member/stocks/followers")
    @ResponseBody
    public ResponseEntity<ApiResponse<List<FollowDto>>> followers(
            HttpSession session
    ) {
        String memberId = SessionUtil.requireLoginMemberId(session);
        return ResponseEntity.ok(
                ApiResponse.success(followService.getFollowers(memberId))
        );
    }

    // 내가 팔로우하는 회원 목록을 모달에 전달
    @GetMapping("/member/stocks/following")
    @ResponseBody
    public ResponseEntity<ApiResponse<List<FollowDto>>> following(
            HttpSession session
    ) {
        String memberId = SessionUtil.requireLoginMemberId(session);
        return ResponseEntity.ok(
                ApiResponse.success(followService.getFollowing(memberId))
        );
    }
}
