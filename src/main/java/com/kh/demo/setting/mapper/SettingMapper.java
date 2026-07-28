package com.kh.demo.setting.mapper;

import com.kh.demo.setting.dto.SettingDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/*
* 개인 환경설정 매퍼 - 게시판 전문용어 툴팁 on/off(F-SET-01-01)가 의존하는 최소 read 경로만 제공.
* 설정 도메인 자체의 등록/수정은 설정 담당자가 별도로 구현.
* */
@Mapper
public interface SettingMapper {

    // 회원의 설정 조회 (row가 없으면 null -> 하이라이트 서비스에서 OFF로 취급)
    SettingDto selectByMemberId(@Param("memberId") String memberId);
}
