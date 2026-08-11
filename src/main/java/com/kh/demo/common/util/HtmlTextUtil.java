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
}
