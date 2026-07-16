package com.kh.demo.member.mapper;

import com.kh.demo.member.dto.MemberDto;
import org.apache.ibatis.annotations.Mapper;
/*
* MyBatis 매퍼 인터페이스
*
* 해당 인터페이스는 구현체가 따로 없다.
* @Mapper 어노테이션을 붙이면 MyBatis-Spring이 애플케이션 시작 시점에 인터페이스를 확인해서
* 구현체를 자동으로 스프링빈에 등록해준다.
*
* 각 메서드는 resources/mappers/MemberMapper.xml안에 동일한 id를 가진
* <select>/<insert>/<update>/<delete> 태그와 1:1로 연결.
*
* xml파일과 class파일은 xml의 namespace속성을 해당 클래스 설정해서 매핑하고,
* 각 메서드와 태그는 id와 메서드명으로 매핑한다.
* */

@Mapper
public interface MemberMapper {

    //회원가입
    int insertMember(MemberDto memberDto);

    //아이디 중복확인
    int countByMemberId(String memberId);

    //아이디를 통한 회원 조회
    MemberDto selectByMemberId(String memberId);
}
