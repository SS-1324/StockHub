package com.kh.demo.community.service;

import com.kh.demo.community.dto.BoardDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface BoardService {

    // 게시글 작성 가능한 카테고리 key -> 화면 표시용 라벨 (header.jsp 커뮤니티 드롭다운과 동일한 key 체계, 순서 보장)
    Map<String, String> getAllowedCategories();

    // 카테고리 미선택 시 적용되는 기본 카테고리
    String getDefaultCategory();

    // 게시글 작성 (카테고리 검증/기본값 처리 + 이미지 업로드 포함) -> 생성된 게시글 번호 반환
    Long write(BoardDto boardDto, List<MultipartFile> images);

    // 게시글 목록 조회 (카테고리/검색어 필터 + 페이징 + 로그인 회원 기준 좋아요/북마크 상태)
    List<BoardDto> getList(String category, String keyword, int page, int size, String loginMemberId);

    // 검색어 없이 목록을 조회하던 기존 호출도 계속 사용할 수 있도록 제공한다.
    default List<BoardDto> getList(String category, int page, int size, String loginMemberId) {
        return getList(category, null, page, size, loginMemberId);
    }

    // 카테고리와 검색 조건에 맞는 전체 게시글 수
    long getTotalCount(String category, String keyword);

    // 내 정보 화면에서 로그인 회원이 작성한 게시글만 조회
    List<BoardDto> getMemberPosts(String memberId, String loginMemberId);

    // 로그인 회원이 작성한 공개 게시글 수
    long getMemberPostCount(String memberId);

    // 게시글 존재 여부 확인 (좋아요/북마크 등 다른 기능에서 대상 게시글 유효성 검사용)
    boolean exists(Long boardId);

    // 게시글 상세 조회 (조회수 증가 + 로그인 회원의 좋아요/북마크 여부 + 용어 하이라이트까지 채워서 반환)
    BoardDto getDetail(Long boardId, String loginMemberId);

    // 관리자 상세 조회 (숨김 처리된 게시글도 관리자 페이지 링크에서 확인 가능)
    BoardDto getDetailAsAdmin(Long boardId, String loginMemberId);

    // 게시글 수정 (작성자 본인 확인 + 이미지 선택 삭제/추가까지 함께 처리)
    void update(Long boardId, BoardDto boardDto, String loginMemberId,
               List<Long> deleteImageIds, List<MultipartFile> newImages);

    // 게시글 삭제 (작성자 본인 확인, 댓글/좋아요/북마크/이미지까지 함께 정리)
    void delete(Long boardId, String loginMemberId);

    // 관리자가 작성자와 관계없이 게시글과 첨부 이미지를 수정 - 컨트롤러가 ADMIN 권한 확인 후에만 호출
    void updateAsAdmin(Long boardId, BoardDto boardDto,
                       List<Long> deleteImageIds, List<MultipartFile> newImages);

    // 관리자가 작성자와 관계없이 게시글을 삭제
    void deleteAsAdmin(Long boardId);
}
