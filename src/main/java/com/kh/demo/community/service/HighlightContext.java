package com.kh.demo.community.service;

import com.kh.demo.dictionary.dto.GlossaryDto;

import java.util.List;
import java.util.Map;

// TermHighlightService.highlight()가 호출할 때마다 DB(설정/용어사전)를 다시 긁어오지 않도록,
// "이번 요청에서 한 번만 준비해서 여러 번 재사용하는 상태"를 담는 값 객체.
// 게시글 하나 + 댓글 여러 개를 한 화면에서 하이라이트할 때 prepare()를 한 번만 부르고 이 객체를 반복 재사용한다.
final class HighlightContext {

    private final boolean enabled;
    private final Map<Character, List<GlossaryDto>> bucketsByFirstChar;

    private HighlightContext(boolean enabled, Map<Character, List<GlossaryDto>> bucketsByFirstChar) {
        this.enabled = enabled;
        this.bucketsByFirstChar = bucketsByFirstChar;
    }

    static HighlightContext disabled() {
        return new HighlightContext(false, null);
    }

    static HighlightContext enabled(Map<Character, List<GlossaryDto>> bucketsByFirstChar) {
        return new HighlightContext(true, bucketsByFirstChar);
    }

    boolean isEnabled() {
        return enabled;
    }

    Map<Character, List<GlossaryDto>> getBucketsByFirstChar() {
        return bucketsByFirstChar;
    }
}
