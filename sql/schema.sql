use sample;

-- 회원테이블(member)
CREATE TABLE IF NOT EXISTS member(
	member_id	VARCHAR(50)		NOT NULL COMMENT '로그인 아이디(PK)',
    member_pwd	VARCHAR(200)	NOT NULL COMMENT '암호화된 비밀번호',
    member_name	VARCHAR(50)		NOT NULL COMMENT '이름',
    nickname	VARCHAR(50)		NOT NULL COMMENT '닉네임',
    email 		VARCHAR(100)	NULL	 COMMENT '이메일',
    profile		VARCHAR(300)	NULL	 COMMENT '프로필 이미지 저장 경로',
    create_at	DATETIME		NOT NULL DEFAULT now() COMMENT '가입일시',
    PRIMARY KEY (member_id),
    UNIQUE KEY 	uq_member_nickname (nickname)
) COMMENT = '회원';

-- 멤버와 게시글은 한명의 멤버가 여러개의 게시글을 작성할 수 있는 일대다(1:N)의 관계
-- 1:N관계일 시 외래키는 N쪽에서 소유한다. 

-- 게시글테이블(board)
CREATE TABLE IF NOT EXISTS board(
	board_id	BIGINT			NOT NULL	AUTO_INCREMENT	COMMENT '게시글 번호(PK)',
    member_id	VARCHAR(50)		NULL		COMMENT '작성자(FK, 회원탈퇴시 NULL)',
    category	VARCHAR(20)		NOT NULL	DEFAULT '자유' COMMENT '카테고리(자유/질문/공지 등)',
    title		VARCHAR(200)	NOT NULL	COMMENT '제목',
    content		TEXT			NOT NULL	COMMENT '내용',
	count		INT				NOT NULL 	COMMENT	'조회수',
    create_at	DATETIME		NOT NULL	DEFAULT now() COMMENT '작성일',
    update_at	DATETIME		NOT NULL	DEFAULT now() ON UPDATE now() COMMENT '수정일',
    PRIMARY KEY (board_id),
    CONSTRAINT fk_board_member FOREIGN KEY (member_id) REFERENCES member(member_id) ON DELETE SET NULL
) COMMENT = '게시글';

-- 게시글 : 이미지 -> 일대다(1:N) 관계
-- 왜래키는 이미지테이블에 저장한다.

-- 첨부이미지 저장 테이블(board_image)
CREATE TABLE IF NOT EXISTS board_image(
	image_id	BIGINT			NOT NULL	AUTO_INCREMENT	COMMENT	'이미지번호(PK)',
    board_id	BIGINT			NOT NULL	COMMENT	'게시글 번호(FK)',
    original_name	VARCHAR(300) NOT NULL	COMMENT '업로드시 원본 파일명',
    save_name	VARCHAR(300)	NOT NULL	COMMENT	'서버에 저장된 이름(동일이름 충돌방지)',
    image_path	VARCHAR(300)	NOT NULL	COMMENT	'서버 저장 경로',
    image_order	INT				NOT NULL	DEFAULT	0	COMMENT	'대표이미지 정보(0=대표이미지)',
    create_at	DATETIME		NOT NULL	DEFAULT now() COMMENT '이미지 업로드 날짜',
    PRIMARY KEY (image_id),
    CONSTRAINT fk_board_image FOREIGN KEY (board_id) REFERENCES board(board_id) ON DELETE CASCADE
) COMMENT = '게시글 이미지';

-- 게시글 : 댓글 -> (일대다)1:N -> 게시글 지워지면 댓글도 삭제 ON DELETE CASCADE
-- 멤버	: 댓글 -> (일대다)1:N  -> 멤버가 지워지면 댓글은 남기고싶다면 ON DELETE SET NULL

-- 댓글테이블(comment)
CREATE TABLE IF NOT EXISTS COMMENT(
	comment_id	BIGINT	NOT NULL AUTO_INCREMENT	COMMENT	'댓글번호(PK)',
    board_id 	BIGINT	NOT NULL COMMENT '게시글 번호(FK)',
    member_id 	VARCHAR(50)	NULL COMMENT '작성자(FK, 회원탈퇴시 NULL)',
    content		VARCHAR(1000)	NOT NULL COMMENT '댓글 내용',
    create_at	DATETIME		NOT NULL DEFAULT now() COMMENT '댓글 작성일',
    PRIMARY KEY (comment_id),
    CONSTRAINT fk_comment_board FOREIGN KEY (board_id) REFERENCES board(board_id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_member FOREIGN KEY (member_id) REFERENCES member(member_id) ON DELETE SET NULL
) COMMENT = '댓글';


-- 관리자계정 생성
INSERT INTO member (member_id, member_pwd, member_name, nickname, email)
VALUES ('admin', '1234', '홍길동', '길동이', 'admin@kh.co.kr');

