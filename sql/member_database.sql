CREATE TABLE member (
	member_id	VARCHAR(50)	    NOT NULL	COMMENT '로그인 아이디(PK)',
	member_pwd	VARCHAR(200)    NOT NULL	COMMENT '암호화된 비밀번호',
	member_name	VARCHAR(50)	    NOT NULL	COMMENT '이름',
	nickname	VARCHAR(50)	    NOT NULL	COMMENT '닉네임',
	email	    VARCHAR(100)	NULL	    COMMENT '이메일',
	profile	    VARCHAR(300)	NULL	    COMMENT '프로필 이미지 저장 경로',
	is_profile_public   BOOLEAN	NOT NULL	DEFAULT true	COMMENT '프로필 공개여부(팔로우/랭킹보드 노출용)',
	is_tooltip_enabled	BOOLEAN	NOT NULL	DEFAULT true	COMMENT '주식 용어 툴팁 활성화 여부',
	create_at	DATETIME	    NOT NULL	DEFAULT now()	COMMENT '가입일시',
	bank_account	VARCHAR(50)	NULL	COMMENT '계좌번호',
	bank	VARCHAR(50)	        NULL	COMMENT '은행명'
)COMMENT='회원';