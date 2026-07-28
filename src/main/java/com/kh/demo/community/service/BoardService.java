package com.kh.demo.community.service;

import com.kh.demo.community.dto.BoardDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BoardService {

    // 게시글 작성 가능한 카테고리 목록 (JSP/서비스가 공유하는 단일 소스, 하드코딩 금지용)
    List<String> getAllowedCategories();

    // 카테고리 미선택 시 적용되는 기본 카테고리
    String getDefaultCategory();

    // 게시글 작성 (카테고리 검증/기본값 처리 + 이미지 업로드 포함) -> 생성된 게시글 번호 반환
    Long write(BoardDto boardDto, List<MultipartFile> images);

    // 게시글 목록 조회 (카테고리 필터 + 페이징)
    List<BoardDto> getList(String category, int page, int size);

    // 게시글 목록 총 개수
    int getListCount(String category);

    // 게시글 존재 여부 확인 (좋아요/북마크 등 다른 기능에서 대상 게시글 유효성 검사용)
    boolean exists(Long boardId);

    // 게시글 상세 조회 (조회수 증가 + 로그인 회원의 좋아요/북마크 여부 + 용어 하이라이트까지 채워서 반환)
    BoardDto getDetail(Long boardId, String loginMemberId);

    // 게시글 수정 (작성자 본인 확인, 이미지 수정은 범위 밖)
    void update(Long boardId, BoardDto boardDto, String loginMemberId);

    // 게시글 삭제 (작성자 본인 확인, 댓글/좋아요/북마크/이미지까지 함께 정리)
    void delete(Long boardId, String loginMemberId);
}
