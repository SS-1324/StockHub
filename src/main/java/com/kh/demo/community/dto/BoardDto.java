package com.kh.demo.community.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/*
 *   BoardDto : board 테이블과 1:1로 대응되는 클래스
 * */

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class BoardDto {

    private Long boardId;
    private String memberId;
    private String category;
    private String title;
    private String content;
    private Long count;
    private Long likeCount;
    private Long commentCount;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;

    // jsp 화면표시용 포맷 문자열 (sql에서 DATE_FORMAT으로 변환해서 받아줌)
    private String createAtStr;
    private String updateAtStr;

    // 목록/상세 조회 시 member 테이블과 join해서 채워주는 표시용 필드
    private String nickname;
    private String profile;

    // 현재 로그인한 회원의 좋아요/북마크 여부 (서비스에서 채워주는 표시용 필드, DB 매핑 대상 아님)
    // Boolean(래퍼)로 두는 이유: BoardCommentDto와 같은 이유 - primitive는 JSON 역직렬화 시 값이 없으면 실패한다.
    private Boolean liked;
    private Boolean bookmarked;

    // 용어 하이라이트가 적용된 본문 (컨트롤러에서 TermHighlightService로 채워주는 표시용 필드, DB 매핑 대상 아님)
    private String highlightedContent;

    // 목록(피드) 카드에 썸네일로 보여줄 이미지 목록 (서비스에서 채워주는 표시용 필드, DB 매핑 대상 아님).
    // 이름을 "images"로 하면 안 됨 - 글쓰기/수정 폼의 파일 input이 name="images"라서, @ModelAttribute 바인딩 시
    // 업로드 파일들을 이 필드(List<BoardImageDto>)에 억지로 바인딩하려다 타입이 안 맞아 400 에러가 났었음.
    private List<BoardImageDto> imageList;
}
