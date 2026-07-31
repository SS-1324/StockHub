package com.kh.demo.community.service;

import com.kh.demo.community.dto.BoardImageDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface BoardImageService {

    // 게시글당 첨부 가능한 최대 이미지 수 (JSP/JS가 이 값을 하드코딩하지 않고 가져다 쓰기 위함)
    int getMaxImageCount();

    // 목록(피드) 카드용 - 여러 게시글의 이미지를 한 번에 조회해서 boardId별로 묶어 반환 (N+1 방지)
    Map<Long, List<BoardImageDto>> getByBoardIds(List<Long> boardIds);

    // 이미지 업로드(개수/확장자 검증 포함) + board_image 행 등록, 등록된 행 목록 반환
    List<BoardImageDto> uploadImages(Long boardId, List<MultipartFile> images);

    // 게시글 수정 화면 전용: 선택한 기존 이미지를 지우고(파일+행), 남은 개수 기준으로 새 이미지를 이어붙인다
    List<BoardImageDto> updateImages(Long boardId, List<Long> deleteImageIds, List<MultipartFile> newImages);

    // 게시글에 속한 이미지 목록 조회
    List<BoardImageDto> getByBoardId(Long boardId);

    // 게시글에 속한 이미지 전체 삭제 (행 + 실제 파일)
    void deleteByBoardId(Long boardId);
}
