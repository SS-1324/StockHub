package com.kh.demo.community.service;

import com.kh.demo.dictionary.dto.GlossaryDto;
import com.kh.demo.dictionary.mapper.GlossaryMapper;
import com.kh.demo.setting.dto.SettingDto;
import com.kh.demo.setting.mapper.SettingMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
* 게시글/댓글 본문에서 용어사전(dictionary) 단어를 찾아 하이라이트 span으로 감싸주는 서비스 (F-COM-01-06).
*
* 순서가 중요함: 사용자 입력 원문을 먼저 HTML 이스케이프한 뒤에만 용어를 매칭해서 감싼다.
* 원문을 이스케이프하지 않고 먼저 매칭/치환하면 사용자가 입력한 <script> 등이 그대로 브라우저에서 실행되는
* XSS 취약점이 생기기 때문에 반드시 "이스케이프 -> 매칭" 순서를 지켜야 한다.
* */
@Service
public class TermHighlightServiceImpl implements TermHighlightService {

    @Autowired
    private GlossaryMapper glossaryMapper;

    @Autowired
    private SettingMapper settingMapper;

    @Override
    public String highlight(String rawText, String memberId) {
        String escaped = HtmlUtils.htmlEscape(rawText == null ? "" : rawText, "UTF-8");

        if (!isTooltipEnabled(memberId)) {
            return escaped;
        }

        List<GlossaryDto> terms = glossaryMapper.selectAllTerms();
        if (terms.isEmpty()) {
            return escaped;
        }

        Map<Character, List<GlossaryDto>> bucketsByFirstChar = groupTermsByFirstChar(terms);
        return applyHighlight(escaped, bucketsByFirstChar);
    }

    // 회원이 없거나(비로그인) 설정 row가 없거나 툴팁 옵션이 명시적으로 켜져있지 않으면 OFF로 취급
    private boolean isTooltipEnabled(String memberId) {
        if (memberId == null) {
            return false;
        }
        SettingDto setting = settingMapper.selectByMemberId(memberId);
        return setting != null && Boolean.TRUE.equals(setting.getIsWordTooltip());
    }

    // 첫 글자별로 버킷을 나누고, 버킷 안에서는 긴 용어부터 시도하도록 정렬(최장 일치 우선 = 유사어 우선순위 규칙)
    private Map<Character, List<GlossaryDto>> groupTermsByFirstChar(List<GlossaryDto> terms) {
        Map<Character, List<GlossaryDto>> buckets = new HashMap<>();
        for (GlossaryDto term : terms) {
            if (term.getTerm() == null || term.getTerm().isEmpty()) {
                continue;
            }
            char firstChar = term.getTerm().charAt(0);
            buckets.computeIfAbsent(firstChar, key -> new ArrayList<>()).add(term);
        }
        for (List<GlossaryDto> bucket : buckets.values()) {
            bucket.sort((a, b) -> b.getTerm().length() - a.getTerm().length());
        }
        return buckets;
    }

    // 이스케이프된 텍스트를 한 번만 왼쪽에서 오른쪽으로 스캔하며, 일치하는 용어가 있으면 감싸고 그만큼 건너뛴다.
    private String applyHighlight(String escapedText, Map<Character, List<GlossaryDto>> bucketsByFirstChar) {
        StringBuilder result = new StringBuilder(escapedText.length());
        int index = 0;
        while (index < escapedText.length()) {
            GlossaryDto matchedTerm = findLongestMatch(escapedText, index, bucketsByFirstChar);
            if (matchedTerm != null) {
                result.append(wrapWithHighlight(matchedTerm));
                index += matchedTerm.getTerm().length();
            } else {
                result.append(escapedText.charAt(index));
                index++;
            }
        }
        return result.toString();
    }

    private GlossaryDto findLongestMatch(String text, int index, Map<Character, List<GlossaryDto>> bucketsByFirstChar) {
        List<GlossaryDto> candidates = bucketsByFirstChar.get(text.charAt(index));
        if (candidates == null) {
            return null;
        }
        for (GlossaryDto candidate : candidates) {
            String term = candidate.getTerm();
            if (text.regionMatches(index, term, 0, term.length())) {
                return candidate;
            }
        }
        return null;
    }

    private String wrapWithHighlight(GlossaryDto term) {
        String termId = term.getTermId() == null ? "" : String.valueOf(term.getTermId());
        String escapedDefinition = HtmlUtils.htmlEscape(term.getDefinition() == null ? "" : term.getDefinition(), "UTF-8");
        return "<span class=\"term-highlight\" data-term-id=\"" + termId + "\" data-definition=\"" + escapedDefinition + "\">"
                + term.getTerm() + "</span>";
    }
}
