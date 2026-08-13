package com.kh.demo.ranking.mapper;

import com.kh.demo.ranking.dto.RankingDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RankingMapper {

    /*
     * 랭킹에 참여할 수 있는 공개 회원 목록 조회
     *
     * 여기서는 회원 기본정보만 가져온다.
     * 실제 실현손익 / 실현수익률 계산은
     * RankingServiceImpl에서 RealizedProfitService를 이용해 처리한다.
     */
    List<RankingDto> selectPublicRankingMembers();

    /*
     * 공개 프로필 팝업에서 사용할 회원 한 명의 기본정보 조회
     */
    RankingDto selectPublicRankingMember(
            @Param("memberId") String memberId
    );
}