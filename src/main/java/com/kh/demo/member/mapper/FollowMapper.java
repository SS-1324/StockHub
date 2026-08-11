package com.kh.demo.member.mapper;

import com.kh.demo.member.dto.FollowDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

// follow 테이블의 수와 회원 목록을 조회
@Mapper
public interface FollowMapper {

    long countFollowers(@Param("memberId") String memberId);

    long countFollowing(@Param("memberId") String memberId);

    List<FollowDto> selectFollowers(@Param("memberId") String memberId);

    List<FollowDto> selectFollowing(@Param("memberId") String memberId);

    /* [팔로우토글-4] 두 회원 사이에 현재 팔로우 관계가 있는지 확인한다. */
    boolean existsFollow(
            @Param("followerId") String followerId,
            @Param("followeeId") String followeeId
    );

    int insertFollow(
            @Param("followerId") String followerId,
            @Param("followeeId") String followeeId
    );

    int deleteFollow(
            @Param("followerId") String followerId,
            @Param("followeeId") String followeeId
    );
}
