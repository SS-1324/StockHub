package com.kh.demo.community.service;

import com.kh.demo.community.dto.BoardImageDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BoardImageService {

    // 이미지 업로드(개수/확장자 검증 포함) + board_image 행 등록, 등록된 행 목록 반환
    List<BoardImageDto> uploadImages(Long boardId, List<MultipartFile> images);

    // 게시글에 속한 이미지 목록 조회
    List<BoardImageDto> getByBoardId(Long boardId);

    // 게시글에 속한 이미지 전체 삭제 (행 + 실제 파일)
    void deleteByBoardId(Long boardId);
}
