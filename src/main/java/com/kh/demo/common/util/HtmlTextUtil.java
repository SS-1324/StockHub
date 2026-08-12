package com.kh.demo.common.util;

import org.jsoup.Jsoup;

// Quill 편집기가 저장한 HTML을 목록 화면용 일반 문자열로 변환
public final class HtmlTextUtil {

    private HtmlTextUtil() {
    }

    public static String toPlainText(String html) {
        if (html == null || html.isBlank()) {
            return "";
        }

        return Jsoup.parseBodyFragment(html)
                .text()
                .replace('\u00A0', ' ')
                .replace("\u200B", "")
                .replace("\uFEFF", "")
                .trim();
    }

    // 제어/포맷 문자 검사: 화면에 보이지 않거나 표시 순서를 조작할 수 있는 문자(RTL 오버라이드, zero-width 문자, NULL 등)를 걸러낸다.
    // 줄바꿈(\n, \r)과 탭(\t)은 여러 줄 입력에 필요한 정상 문자라 예외로 둔다.
    public static boolean hasDisallowedControlCharacter(String text) {
        if (text == null) {
            return false;
        }

        return text.codePoints().anyMatch(codePoint -> {
            if (codePoint == '\n' || codePoint == '\r' || codePoint == '\t') {
                return false;
            }
            int type = Character.getType(codePoint);
            return type == Character.CONTROL || type == Character.FORMAT;
        });
    }
}
