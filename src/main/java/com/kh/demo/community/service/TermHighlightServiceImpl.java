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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
* 게시글/댓글 본문에서 URL을 링크로 바꾸고, 용어사전(dictionary) 단어를 찾아 하이라이트 span으로 감싸주는 서비스 (F-COM-01-06).
*
* 순서가 중요함: 사용자 입력 원문을 먼저 HTML 이스케이프한 뒤에만 URL 링크화/용어 매칭을 한다.
* 원문을 이스케이프하지 않고 먼저 매칭/치환하면 사용자가 입력한 <script> 등이 그대로 브라우저에서 실행되는
* XSS 취약점이 생기기 때문에 반드시 "이스케이프 -> 변환" 순서를 지켜야 한다.
* */
@Service
public class TermHighlightServiceImpl implements TermHighlightService {

    // http(s):// 또는 www.로 시작하고 공백/'<' 전까지 이어지는 문자열을 URL로 간주 (완벽한 URL 파서는 아니고 실용적인 수준)
    private static final Pattern URL_PATTERN = Pattern.compile("((?:https?://|www\\.)[^\\s<]+)");
    private static final String TRAILING_PUNCTUATION = ".,!?)]}'\"";

    @Autowired
    private GlossaryMapper glossaryMapper;

    @Autowired
    private SettingMapper settingMapper;

    @Override
    public String highlight(String rawText, String memberId) {
        String escaped = HtmlUtils.htmlEscape(rawText == null ? "" : rawText, "UTF-8");
        String linkified = linkifyUrls(escaped);

        if (!isTooltipEnabled(memberId)) {
            return linkified;
        }

        List<GlossaryDto> terms = glossaryMapper.selectAllTerms();
        if (terms.isEmpty()) {
            return linkified;
        }

        Map<Character, List<GlossaryDto>> bucketsByFirstChar = groupTermsByFirstChar(terms);
        return applyHighlight(linkified, bucketsByFirstChar);
    }

    // 이스케이프된 텍스트에서 URL을 찾아 <a> 태그로 감싼다. 문장 끝 마침표/괄호 등은 링크에서 빼고 뒤에 그대로 남긴다.
    private String linkifyUrls(String escapedText) {
        Matcher matcher = URL_PATTERN.matcher(escapedText);
        StringBuilder result = new StringBuilder(escapedText.length());
        int lastEnd = 0;

        while (matcher.find()) {
            result.append(escapedText, lastEnd, matcher.start());

            String rawMatch = matcher.group(1);
            int trimEnd = rawMatch.length();
            while (trimEnd > 0 && TRAILING_PUNCTUATION.indexOf(rawMatch.charAt(trimEnd - 1)) >= 0) {
                trimEnd--;
            }
            String url = rawMatch.substring(0, trimEnd);
            String trailing = rawMatch.substring(trimEnd);

            if (!url.isEmpty()) {
                // www.로 시작하는 건 href에 쓸 스킴이 없으므로 https://를 붙여준다(화면에 보이는 글자는 원문 그대로 둠)
                String href = url.regionMatches(true, 0, "http", 0, 4) ? url : "https://" + url;
                result.append("<a href=\"").append(href).append("\" target=\"_blank\" rel=\"noopener noreferrer\">")
                        .append(url).append("</a>");
            }
            result.append(trailing);

            lastEnd = matcher.end();
        }
        result.append(escapedText, lastEnd, escapedText.length());
        return result.toString();
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

    // 이스케이프(+링크화)된 텍스트를 한 번만 왼쪽에서 오른쪽으로 스캔하며, 일치하는 용어가 있으면 감싸고 그만큼 건너뛴다.
    // linkifyUrls가 이미 만들어둔 <a href="...">...</a> 태그 내부(특히 속성값)에서 또 매칭을 시도하면 HTML이
    // 깨질 수 있으므로, '<'를 만나면 짝이 되는 '>'까지는 매칭 시도 없이 그대로 통과시킨다.
    private String applyHighlight(String escapedText, Map<Character, List<GlossaryDto>> bucketsByFirstChar) {
        StringBuilder result = new StringBuilder(escapedText.length());
        int index = 0;
        while (index < escapedText.length()) {
            char current = escapedText.charAt(index);

            if (current == '<') {
                int tagEnd = escapedText.indexOf('>', index);
                if (tagEnd == -1) {
                    result.append(escapedText.substring(index));
                    break;
                }
                result.append(escapedText, index, tagEnd + 1);
                index = tagEnd + 1;
                continue;
            }

            GlossaryDto matchedTerm = findLongestMatch(escapedText, index, bucketsByFirstChar);
            if (matchedTerm != null) {
                result.append(wrapWithHighlight(matchedTerm));
                index += matchedTerm.getTerm().length();
            } else {
                result.append(current);
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
