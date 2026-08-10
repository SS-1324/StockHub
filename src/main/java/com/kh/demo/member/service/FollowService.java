package com.kh.demo.member.service;

import com.kh.demo.member.dto.FollowDto;

import java.util.List;

// 내 정보 화면에서 사용할 팔로우·팔로잉 조회 기능
public interface FollowService {

    long getFollowerCount(String memberId);

    long getFollowingCount(String memberId);

    List<FollowDto> getFollowers(String memberId);

    List<FollowDto> getFollowing(String memberId);

    boolean isFollowing(String followerId, String followeeId);

    /* [팔로우토글-3] 현재 관계가 없으면 추가하고, 있으면 삭제한 뒤 최종 상태를 반환한다. */
    boolean toggleFollow(String followerId, String followeeId);
}
