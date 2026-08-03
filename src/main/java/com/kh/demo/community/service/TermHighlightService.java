package com.kh.demo.community.service;

public interface TermHighlightService {

    // 원문 텍스트를 이스케이프한 뒤, 회원의 툴팁 설정이 켜져 있으면 용어사전과 매칭해 하이라이트 span으로 감싸서 반환
    String highlight(String rawText, String memberId);

    // 게시글 본문 전용. content는 이미 write/update 시점에 jsoup으로 정제된 안전한 HTML(<p>, <strong> 등)이라
    // 여기서 또 이스케이프하면 <p> 같은 태그가 그대로 화면에 글자로 보이는 문제가 생긴다.
    // 그래서 이스케이프는 건너뛰고, URL 링크화/용어 하이라이트만 그 위에 적용한다.
    String highlightHtml(String safeHtml, String memberId);
}
