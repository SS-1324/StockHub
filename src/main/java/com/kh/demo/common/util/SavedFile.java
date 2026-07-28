package com.kh.demo.common.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 저장이 끝난 파일 정보를 보관
@Getter
@AllArgsConstructor
public class SavedFile {
    private final String originalName; // 사용자가 올린 파일명
    private final String saveName; // 서버에 저장된 파일명
    private final String path; // 브라우저에서 사용할 경로
}
