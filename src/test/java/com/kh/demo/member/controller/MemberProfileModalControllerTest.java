package com.kh.demo.member.controller;

import com.kh.demo.common.SessionConst;
import com.kh.demo.common.dto.ApiResponse;
import com.kh.demo.community.service.BoardService;
import com.kh.demo.inquiry.service.InquiryService;
import com.kh.demo.member.dto.MemberDto;
import com.kh.demo.member.dto.MemberProfileModalDto;
import com.kh.demo.member.service.FollowService;
import com.kh.demo.member.service.MemberService;
import com.kh.demo.ranking.service.RankingService;
import com.kh.demo.ranking.dto.RankingDto;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ConcurrentModel;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberProfileModalControllerTest {

    @Mock private MemberService memberService;
    @Mock private BoardService boardService;
    @Mock private FollowService followService;
    @Mock private InquiryService inquiryService;
    @Mock private RankingService rankingService;
    @Mock private HttpSession session;

    private MemberProfileModalController controller;

    @BeforeEach
    void setUp() {
        controller = new MemberProfileModalController(
                memberService, boardService, followService, inquiryService, rankingService
        );
    }

    @Test
    void 공개프로필은팔로워와팔로잉숫자를반환한다() {
        prepareProfile("target", "viewer");
        when(boardService.getMemberPostCount("target")).thenReturn(5L);
        when(followService.getFollowerCount("target")).thenReturn(3L);
        when(followService.getFollowingCount("target")).thenReturn(4L);
        RankingDto summary = new RankingDto();
        summary.setReturnRate(new BigDecimal("12.34"));
        summary.setProfit(141013L);
        when(rankingService.getProfileInvestmentSummary("target")).thenReturn(summary);

        ResponseEntity<ApiResponse<MemberProfileModalDto>> response =
                controller.profile("target", "returnRate", session);

        MemberProfileModalDto data = response.getBody().getData();
        assertThat(data.detailsPublic()).isTrue();
        assertThat(data.postCount()).isEqualTo(5L);
        assertThat(data.followerCount()).isEqualTo(3L);
        assertThat(data.followingCount()).isEqualTo(4L);
        assertThat(data.returnRate()).isEqualByComparingTo("12.34");
        assertThat(data.profit()).isEqualTo(141013L);
    }

    @Test
    void 본인프로필도동일한구조로랭커표시를반환한다() {
        prepareProfile("target", "target");
        when(rankingService.getProfileRankPosition("target", false)).thenReturn(1);

        MemberProfileModalDto data =
                controller.profile("target", "returnRate", session).getBody().getData();

        assertThat(data.badge()).isEqualTo("RANKER");
        assertThat(data.rankPosition()).isEqualTo(1);
        assertThat(data.rankType()).isEqualTo("returnRate");
        assertThat(data.canFollow()).isFalse();
    }

    @Test
    void 수익금영역에서열면수익금순위를사용한다() {
        prepareProfile("target", "viewer");
        when(rankingService.getProfileRankPosition("target", true)).thenReturn(2);

        MemberProfileModalDto data =
                controller.profile("target", "profit", session).getBody().getData();

        assertThat(data.badge()).isEqualTo("RANKER");
        assertThat(data.rankPosition()).isEqualTo(2);
        assertThat(data.rankType()).isEqualTo("profit");
        verify(rankingService).getProfileRankPosition("target", true);
    }

    @Test
    void 팔로우토글후갱신된팔로잉상태를반환한다() {
        prepareProfile("target", "viewer");
        when(followService.toggleFollow("viewer", "target")).thenReturn(true);
        when(followService.isFollowing("viewer", "target")).thenReturn(true);

        MemberProfileModalDto data = controller
                .toggleFollow("target", "returnRate", session)
                .getBody()
                .getData();

        assertThat(data.canFollow()).isTrue();
        assertThat(data.followingTarget()).isTrue();
        verify(followService).toggleFollow("viewer", "target");
    }

    @Test
    void 타인은문의글전용페이지에접근할수없다() {
        MemberDto viewer = new MemberDto();
        viewer.setMemberId("viewer");
        when(session.getAttribute(SessionConst.LOGIN_MEMBER)).thenReturn(viewer);

        assertThatThrownBy(() -> controller.memberInquiries("target", session, null))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
    }

    @Test
    void 공개회원의작성글페이지는타인도볼수있다() {
        MemberDto target = new MemberDto();
        target.setMemberId("target");
        target.setStockPublic(true);
        MemberDto viewer = new MemberDto();
        viewer.setMemberId("viewer");
        when(memberService.getMemberProfile("target")).thenReturn(target);
        when(session.getAttribute(SessionConst.LOGIN_MEMBER)).thenReturn(viewer);
        when(boardService.getMemberPosts("target", "viewer")).thenReturn(List.of());

        String view = controller.memberPosts("target", session, new ConcurrentModel());

        assertThat(view).isEqualTo("member/myPosts");
    }

    @Test
    void 비공개회원도기본프로필을반환하고세부수치는감춘다() {
        MemberDto target = new MemberDto();
        target.setMemberId("target");
        target.setNickname("비공개회원");
        target.setStockPublic(false);
        when(memberService.getMemberProfile("target")).thenReturn(target);
        when(boardService.getMemberPostCount("target")).thenReturn(6L);
        when(followService.getFollowerCount("target")).thenReturn(1L);
        when(followService.getFollowingCount("target")).thenReturn(2L);

        ResponseEntity<ApiResponse<MemberProfileModalDto>> response =
                controller.profile("target", "returnRate", session);

        assertThat(response.getStatusCode()).isEqualTo(org.springframework.http.HttpStatus.OK);
        assertThat(response.getBody().isSuccess()).isTrue();
        MemberProfileModalDto data = response.getBody().getData();
        assertThat(data.detailsPublic()).isFalse();
        assertThat(data.nickname()).isEqualTo("비공개회원");
        assertThat(data.postCount()).isEqualTo(6L);
        assertThat(data.followerCount()).isEqualTo(1L);
        assertThat(data.followingCount()).isEqualTo(2L);
        assertThat(data.returnRate()).isNull();
        assertThat(data.profit()).isNull();
    }

    @Test
    void privateMemberPublicPostsCanBeViewedByAnotherMember() {
        MemberDto target = new MemberDto();
        target.setMemberId("target");
        target.setStockPublic(false);
        MemberDto viewer = new MemberDto();
        viewer.setMemberId("viewer");
        when(memberService.getMemberProfile("target")).thenReturn(target);
        when(session.getAttribute(SessionConst.LOGIN_MEMBER)).thenReturn(viewer);
        when(boardService.getMemberPosts("target", "viewer")).thenReturn(List.of());

        String view = controller.memberPosts("target", session, new ConcurrentModel());

        assertThat(view).isEqualTo("member/myPosts");
    }

    @Test
    void 관리자회원은공개여부와관계없이ADMIN배지를반환한다() {
        MemberDto admin = new MemberDto();
        admin.setMemberId("admin01");
        admin.setNickname("스톡허브");
        admin.setMemberRole("ADMIN");
        admin.setStockPublic(false);
        when(memberService.getMemberProfile("admin01")).thenReturn(admin);

        MemberProfileModalDto data =
                controller.profile("admin01", "returnRate", session).getBody().getData();

        assertThat(data.badge()).isEqualTo("ADMIN");
        assertThat(data.rankPosition()).isNull();
        assertThat(data.detailsPublic()).isFalse();
    }

    private void prepareProfile(String targetId, String viewerId) {
        MemberDto target = new MemberDto();
        target.setMemberId(targetId);
        target.setNickname("테스트회원");
        target.setProfile("/images/common_member.png");
        target.setStockPublic(true);

        MemberDto viewer = new MemberDto();
        viewer.setMemberId(viewerId);

        when(memberService.getMemberProfile(targetId)).thenReturn(target);
        when(session.getAttribute(SessionConst.LOGIN_MEMBER)).thenReturn(viewer);
    }
}
