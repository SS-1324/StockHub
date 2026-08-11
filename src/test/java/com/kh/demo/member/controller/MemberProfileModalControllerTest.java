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
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

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
        when(followService.getFollowerCount("target")).thenReturn(3L);
        when(followService.getFollowingCount("target")).thenReturn(4L);

        ResponseEntity<ApiResponse<MemberProfileModalDto>> response =
                controller.profile("target", "returnRate", session);

        MemberProfileModalDto data = response.getBody().getData();
        assertThat(data.followerCount()).isEqualTo(3L);
        assertThat(data.followingCount()).isEqualTo(4L);
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
    void 타인은작성글과문의글전용페이지에접근할수없다() {
        MemberDto viewer = new MemberDto();
        viewer.setMemberId("viewer");
        when(session.getAttribute(SessionConst.LOGIN_MEMBER)).thenReturn(viewer);

        assertThatThrownBy(() -> controller.memberPosts("target", session, null))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
        assertThatThrownBy(() -> controller.memberInquiries("target", session, null))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
    }

    @Test
    void 비공개회원은프로필정보를반환하지않는다() {
        MemberDto target = new MemberDto();
        target.setMemberId("target");
        target.setStockPublic(false);
        when(memberService.getMemberProfile("target")).thenReturn(target);

        ResponseEntity<ApiResponse<MemberProfileModalDto>> response =
                controller.profile("target", "returnRate", session);

        assertThat(response.getStatusCode()).isEqualTo(org.springframework.http.HttpStatus.FORBIDDEN);
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getData()).isNull();
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
