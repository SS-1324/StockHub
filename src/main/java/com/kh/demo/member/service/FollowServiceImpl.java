package com.kh.demo.member.service;

import com.kh.demo.member.dto.FollowDto;
import com.kh.demo.member.mapper.FollowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// follow 테이블의 실제 조회를 처리
@Service
public class FollowServiceImpl implements FollowService {

    private final FollowMapper followMapper;

    public FollowServiceImpl(FollowMapper followMapper) {
        this.followMapper = followMapper;
    }

    @Override
    public long getFollowerCount(String memberId) {
        return followMapper.countFollowers(memberId);
    }

    @Override
    public long getFollowingCount(String memberId) {
        return followMapper.countFollowing(memberId);
    }

    @Override
    public List<FollowDto> getFollowers(String memberId) {
        return followMapper.selectFollowers(memberId);
    }

    @Override
    public List<FollowDto> getFollowing(String memberId) {
        return followMapper.selectFollowing(memberId);
    }

    @Override
    public boolean isFollowing(String followerId, String followeeId) {
        return followMapper.existsFollow(followerId, followeeId);
    }

    @Override
    @Transactional
    public boolean toggleFollow(String followerId, String followeeId) {
        /*
         * [팔로우토글-3] 조회와 변경을 한 트랜잭션으로 묶는다.
         * 이미 팔로우 중이면 DELETE 후 false, 아니면 INSERT 후 true를 반환한다.
         */
        if (followMapper.existsFollow(followerId, followeeId)) {
            followMapper.deleteFollow(followerId, followeeId);
            return false;
        }
        followMapper.insertFollow(followerId, followeeId);
        return true;
    }
}
