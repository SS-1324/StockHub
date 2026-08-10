package com.kh.demo.community.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TermHighlightServiceImplTest {

    private final TermHighlightServiceImpl service = new TermHighlightServiceImpl();

    @Test
    void keepsExistingQuillLinkUnchanged() {
        String html = "<p><a href=\"https://example.com\" target=\"_blank\" "
                + "rel=\"noopener noreferrer\">https://example.com</a></p>";

        assertEquals(html, service.highlightHtml(html, null));
    }

    @Test
    void linkifiesOnlyPlainTextOutsideHtmlTags() {
        String html = "<p>공식 사이트: https://example.com</p>";
        String expected = "<p>공식 사이트: <a href=\"https://example.com\" target=\"_blank\" "
                + "rel=\"noopener noreferrer\">https://example.com</a></p>";

        assertEquals(expected, service.highlightHtml(html, null));
    }
}
