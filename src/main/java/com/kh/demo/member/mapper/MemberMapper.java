package com.kh.demo.member.mapper;

import com.kh.demo.member.dto.MemberDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MemberMapper {
    int insertMember(MemberDto memberDto);
    int countByMemberId(@Param("memberId") String memberId);
    MemberDto selectByMemberId(@Param("memberId") String memberId);
}
