package com.kh.demo.member.controller;

import com.kh.demo.common.SessionConst;
import com.kh.demo.common.dto.ApiResponse;
import com.kh.demo.community.service.BoardService;
import com.kh.demo.common.util.SessionUtil;
import com.kh.demo.inquiry.service.InquiryService;
import com.kh.demo.member.dto.FollowDto;
import com.kh.demo.member.dto.MemberDto;
import com.kh.demo.member.dto.MemberProfileModalDto;
import com.kh.demo.member.service.FollowService;
import com.kh.demo.member.service.MemberService;
import com.kh.demo.ranking.service.RankingService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/** 커뮤니티와 랭킹이 함께 사용하는 회원 프로필 모달 API. */
@Controller
@RequestMapping("/member/profile")
public class MemberProfileModalController {

    private final MemberService memberService;
    private final BoardService boardService;
    private final FollowService followService;
    private final InquiryService inquiryService;
    private final RankingService rankingService;

    public MemberProfileModalController(MemberService memberService,
                                        BoardService boardService,
                                        FollowService followService,
                                        InquiryService inquiryService,
                                        RankingService rankingService) {
        this.memberService = memberService;
        this.boardService = boardService;
        this.followService = followService;
        this.inquiryService = inquiryService;
        this.rankingService = rankingService;
    }

    @GetMapping("/{memberId}")
    @ResponseBody
    public ResponseEntity<ApiResponse<MemberProfileModalDto>> profile(
            @PathVariable String memberId,
            @RequestParam(defaultValue = "returnRate") String rankType,
            HttpSession session
    ) {
        try {
            MemberDto member = memberService.getMemberProfile(memberId);
            MemberDto loginMember = (MemberDto) session.getAttribute(SessionConst.LOGIN_MEMBER);
            String viewerId = loginMember == null ? null : loginMember.getMemberId();
            boolean ownProfile = memberId.equals(viewerId);

            /*
             * [프로필순위-2] 클라이언트 문자열을 서버가 허용하는 두 값으로 정규화한다.
             * profit만 수익금 기준이며, 누락되거나 잘못된 값은 기존 수익률 기준으로 처리한다.
             */
            boolean sortByProfit = "profit".equalsIgnoreCase(rankType);
            String normalizedRankType = sortByProfit ? "profit" : "returnRate";
            Integer rankPosition = rankingService.getProfileRankPosition(memberId, sortByProfit);
            String badge = rankPosition == null ? "USER" : "RANKER";

            /* [프로필게시글-1] 모달에는 목록 전체가 아니라 개수만 내려 응답을 가볍게 유지한다. */
            long postCount = boardService.getMemberPostCount(memberId);

            List<MemberProfileModalDto.FollowSummary> followers =
                    followService.getFollowers(memberId).stream()
                            .map(this::toFollowSummary)
                            .toList();

            List<MemberProfileModalDto.FollowSummary> following =
                    followService.getFollowing(memberId).stream()
                            .map(this::toFollowSummary)
                            .toList();

            /* [프로필문의-1] 문의도 작성글과 같이 모달에는 공개 개수만 전달한다. */
            long inquiryCount = inquiryService.getMemberInquiries(memberId).size();

            MemberProfileModalDto response = new MemberProfileModalDto(
                    member.getMemberId(),
                    member.getNickname(),
                    member.getProfile(),
                    badge,
                    rankPosition,
                    normalizedRankType,
                    ownProfile,
                    postCount,
                    followers.size(),
                    following.size(),
                    inquiryCount,
                    followers,
                    following
            );

            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.fail("회원 정보를 찾을 수 없습니다."));
        }
    }

    /** 프로필의 작성글 숫자를 눌렀을 때 해당 회원의 공개 게시글을 별도 페이지에서 보여준다. */
    @GetMapping("/{memberId}/posts")
    public String memberPosts(@PathVariable String memberId,
                              HttpSession session,
                              Model model) {
        MemberDto profileOwner = memberService.getMemberProfile(memberId);
        String viewerId = SessionUtil.currentMemberId(session);

        model.addAttribute("profileOwner", profileOwner);
        model.addAttribute("ownProfile", memberId.equals(viewerId));
        model.addAttribute("boardList", boardService.getMemberPosts(memberId, viewerId));
        model.addAttribute("totalCount", boardService.getMemberPostCount(memberId));
        model.addAttribute("allowedCategories", boardService.getAllowedCategories());
        return "member/myPosts";
    }

    /** 문의글 숫자를 누르면 로그인 여부와 관계없이 해당 회원의 문의·답변 목록을 보여준다. */
    @GetMapping("/{memberId}/inquiries")
    public String memberInquiries(@PathVariable String memberId,
                                  HttpSession session,
                                  Model model) {
        MemberDto profileOwner = memberService.getMemberProfile(memberId);
        String viewerId = SessionUtil.currentMemberId(session);
        var inquiryList = inquiryService.getMemberInquiries(memberId);

        model.addAttribute("profileOwner", profileOwner);
        model.addAttribute("ownProfile", memberId.equals(viewerId));
        model.addAttribute("inquiryList", inquiryList);
        model.addAttribute("totalCount", inquiryList.size());
        return "member/memberInquiries";
    }

    private MemberProfileModalDto.FollowSummary toFollowSummary(FollowDto follow) {
        return new MemberProfileModalDto.FollowSummary(
                follow.getMemberId(),
                follow.getNickname(),
                follow.getProfile(),
                follow.getFollowAtStr()
        );
    }

}
