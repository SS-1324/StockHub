package com.kh.demo.member.controller;

import com.kh.demo.common.SessionConst;
import com.kh.demo.common.dto.ApiResponse;
import com.kh.demo.community.service.BoardService;
import com.kh.demo.inquiry.dto.InquiryDto;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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
    void 다른회원프로필에서도문의글개수를반환한다() {
        prepareProfile("target", "viewer");
        InquiryDto inquiry = new InquiryDto();
        when(inquiryService.getMemberInquiries("target")).thenReturn(List.of(inquiry));

        ResponseEntity<ApiResponse<MemberProfileModalDto>> response =
                controller.profile("target", "returnRate", session);

        MemberProfileModalDto data = response.getBody().getData();
        assertThat(data.ownProfile()).isFalse();
        assertThat(data.inquiryCount()).isEqualTo(1);
    }

    @Test
    void 본인프로필에서문의글개수와랭커표시를반환한다() {
        prepareProfile("target", "target");
        InquiryDto inquiry = new InquiryDto();
        inquiry.setInquiryId(1L);
        inquiry.setTitle("문의 제목");
        inquiry.setStatus("ANSWERED");
        inquiry.setAnswer("답변 내용");
        when(inquiryService.getMemberInquiries("target")).thenReturn(List.of(inquiry));
        when(rankingService.getProfileRankPosition("target", false)).thenReturn(1);

        MemberProfileModalDto data =
                controller.profile("target", "returnRate", session).getBody().getData();

        assertThat(data.ownProfile()).isTrue();
        assertThat(data.badge()).isEqualTo("RANKER");
        assertThat(data.rankPosition()).isEqualTo(1);
        assertThat(data.rankType()).isEqualTo("returnRate");
        assertThat(data.inquiryCount()).isEqualTo(1);
    }

    @Test
    void 수익금영역에서열면수익금순위를사용한다() {
        prepareProfile("target", "viewer");
        when(inquiryService.getMemberInquiries("target")).thenReturn(List.of());
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

    private void prepareProfile(String targetId, String viewerId) {
        MemberDto target = new MemberDto();
        target.setMemberId(targetId);
        target.setNickname("테스트회원");
        target.setProfile("/images/common_member.png");

        MemberDto viewer = new MemberDto();
        viewer.setMemberId(viewerId);

        when(memberService.getMemberProfile(targetId)).thenReturn(target);
        when(session.getAttribute(SessionConst.LOGIN_MEMBER)).thenReturn(viewer);
        when(boardService.getMemberPostCount(targetId)).thenReturn(0L);
        when(followService.getFollowers(targetId)).thenReturn(List.of());
        when(followService.getFollowing(targetId)).thenReturn(List.of());
    }
}
