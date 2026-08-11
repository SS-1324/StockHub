package com.kh.demo.member.controller;

import com.kh.demo.common.dto.ApiResponse;
import com.kh.demo.community.service.BoardService;
import com.kh.demo.common.util.SessionUtil;
import com.kh.demo.inquiry.service.InquiryService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

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
            if (!Boolean.TRUE.equals(member.getStockPublic())) {
                /* [프로필공개-1] 기존 주식정보 공개 설정이 꺼진 회원은 프로필 JSON을 반환하지 않는다. */
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.fail("비공개 프로필입니다."));
            }
            String viewerId = SessionUtil.currentMemberId(session);
            return ResponseEntity.ok(ApiResponse.success(
                    buildProfileResponse(member, viewerId, rankType)
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.fail("회원 정보를 찾을 수 없습니다."));
        }
    }

    /** 프로필의 팔로우 버튼을 누르면 현재 상태와 반대로 변경하고 갱신된 프로필을 반환한다. */
    @PostMapping("/{memberId}/follow")
    @ResponseBody
    public ResponseEntity<ApiResponse<MemberProfileModalDto>> toggleFollow(
            @PathVariable String memberId,
            @RequestParam(defaultValue = "returnRate") String rankType,
            HttpSession session
    ) {
        String viewerId = SessionUtil.currentMemberId(session);
        if (viewerId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("로그인 후 팔로우할 수 있습니다."));
        }
        if (viewerId.equals(memberId)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail("본인은 팔로우할 수 없습니다."));
        }

        try {
            /*
             * [팔로우토글-2] 대상 회원이 실제로 존재하는지 먼저 확인한 후 관계를 변경한다.
             * 변경 직후 전체 프로필을 다시 구성해 버튼 상태, 팔로워 숫자와 목록을 한 번에 동기화한다.
             */
            MemberDto member = memberService.getMemberProfile(memberId);
            if (!Boolean.TRUE.equals(member.getStockPublic())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.fail("비공개 프로필입니다."));
            }
            followService.toggleFollow(viewerId, memberId);
            return ResponseEntity.ok(ApiResponse.success(
                    buildProfileResponse(member, viewerId, rankType)
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.fail("회원 정보를 찾을 수 없습니다."));
        }
    }

    /** [프로필공개범위-2] 작성글 전용 페이지는 프로필 소유자 본인만 볼 수 있다. */
    @GetMapping("/{memberId}/posts")
    public String memberPosts(@PathVariable String memberId,
                              HttpSession session,
                              Model model) {
        requireProfileOwner(memberId, session);
        MemberDto profileOwner = memberService.getMemberProfile(memberId);

        model.addAttribute("profileOwner", profileOwner);
        model.addAttribute("ownProfile", true);
        model.addAttribute("boardList", boardService.getMemberPosts(memberId, memberId));
        model.addAttribute("totalCount", boardService.getMemberPostCount(memberId));
        model.addAttribute("allowedCategories", boardService.getAllowedCategories());
        return "member/myPosts";
    }

    /** [프로필공개범위-2] 문의글 전용 페이지도 프로필 소유자 본인만 볼 수 있다. */
    @GetMapping("/{memberId}/inquiries")
    public String memberInquiries(@PathVariable String memberId,
                                  HttpSession session,
                                  Model model) {
        requireProfileOwner(memberId, session);
        MemberDto profileOwner = memberService.getMemberProfile(memberId);
        var inquiryList = inquiryService.getMemberInquiries(memberId);

        model.addAttribute("profileOwner", profileOwner);
        model.addAttribute("ownProfile", true);
        model.addAttribute("inquiryList", inquiryList);
        model.addAttribute("totalCount", inquiryList.size());
        return "member/memberInquiries";
    }

    private MemberProfileModalDto buildProfileResponse(MemberDto member,
                                                       String viewerId,
                                                       String rankType) {
        String memberId = member.getMemberId();
        boolean canFollow = viewerId != null && !memberId.equals(viewerId);
        boolean followingTarget = canFollow && followService.isFollowing(viewerId, memberId);

        /*
         * [프로필순위-2] 클라이언트 문자열을 서버가 허용하는 두 값으로 정규화한다.
         * profit만 수익금 기준이며, 누락되거나 잘못된 값은 기존 수익률 기준으로 처리한다.
         */
        boolean sortByProfit = "profit".equalsIgnoreCase(rankType);
        String normalizedRankType = sortByProfit ? "profit" : "returnRate";
        Integer rankPosition = rankingService.getProfileRankPosition(memberId, sortByProfit);
        String badge = rankPosition == null ? "USER" : "RANKER";

        /* [프로필공개-2] 본인·타인 구분 없이 공개 프로필에는 팔로워·팔로잉 숫자만 전달한다. */
        long followerCount = followService.getFollowerCount(memberId);
        long followingCount = followService.getFollowingCount(memberId);

        return new MemberProfileModalDto(
                memberId,
                member.getNickname(),
                member.getProfile(),
                badge,
                rankPosition,
                normalizedRankType,
                canFollow,
                followingTarget,
                followerCount,
                followingCount
        );
    }

    private void requireProfileOwner(String memberId, HttpSession session) {
        if (!memberId.equals(SessionUtil.currentMemberId(session))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인의 정보만 볼 수 있습니다.");
        }
    }

}
