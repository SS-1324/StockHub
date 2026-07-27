package com.kh.demo.community.service;

public interface TermHighlightService {

    // 원문 텍스트를 이스케이프한 뒤, 회원의 툴팁 설정이 켜져 있으면 용어사전과 매칭해 하이라이트 span으로 감싸서 반환
    String highlight(String rawText, String memberId);
}
