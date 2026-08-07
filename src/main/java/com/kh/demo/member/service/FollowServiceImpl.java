package com.kh.demo.member.service;

import com.kh.demo.member.dto.FollowDto;
import com.kh.demo.member.mapper.FollowMapper;
import org.springframework.stereotype.Service;

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
}
