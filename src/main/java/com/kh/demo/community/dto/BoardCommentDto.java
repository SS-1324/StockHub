package com.kh.demo.community.dto;

import lombok.*;

import java.time.LocalDateTime;

/*
 *   BoardCommentDto : board_comment 테이블과 1:1로 대응되는 클래스
 *   depth는 최대 1(댓글 -> 답글)까지만 허용. parentCommentId가 null이면 최상위 댓글.
 * */

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class BoardCommentDto {

    private Long commentId;
    private Long boardId;
    private String memberId;
    private Long parentCommentId;
    private String content;
    private Long likeCount;
    private Boolean hidden; // 관리자가 숨긴 댓글인지 여부
    private Boolean deleted; // 댓글 삭제시 부모따라 자식답글이 지워지는거 방지하기 위해 추가한 코드
    private LocalDateTime createAt;

    // jsp 화면표시용 포맷 문자열
    private String createAtStr;

    // 목록 조회 시 member 테이블과 join해서 채워주는 표시용 필드
    private String nickname;
    private String profile;

    // 내가 쓴 댓글 목록에서 댓글이 달린 게시글 제목을 표시하기 위한 필드
    private String boardTitle;

    // 답글인 경우 부모 댓글 작성자 닉네임(@멘션 표시용). 최상위 댓글이면 null.
    private String parentNickname;

    // 현재 로그인한 회원이 좋아요를 눌렀는지 여부 (서비스에서 채워주는 표시용 필드, DB 매핑 대상 아님)
    // Boolean(래퍼)로 두는 이유: 이 DTO가 @RequestBody로도 역직렬화되는데, 클라이언트가 안 보내는 필드라
    // primitive boolean이면 Jackson이 null을 못 받아서 파싱 자체가 실패한다.
    private Boolean liked;

    // 용어 하이라이트가 적용된 본문 (컨트롤러에서 TermHighlightService로 채워주는 표시용 필드, DB 매핑 대상 아님)
    private String highlightedContent;
}
