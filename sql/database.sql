SHOW DATABASES;
SELECT DATABASE();
SHOW TABLES;

-- 회원
CREATE TABLE member (
	member_id	VARCHAR(50)	NOT NULL	COMMENT '로그인 아이디(PK)',
	member_pwd	VARCHAR(200)	NOT NULL	COMMENT '암호화된 비밀번호',
	member_name	VARCHAR(50)	NOT NULL	COMMENT '이름',
	nickname	VARCHAR(50)	NOT NULL	COMMENT '닉네임',
	email	VARCHAR(100)	NULL	COMMENT '이메일',
	profile	VARCHAR(300)	NULL	COMMENT '프로필 이미지 저장 경로',
	is_profile_public	BOOLEAN	NOT NULL	DEFAULT true	COMMENT '프로필 공개여부(팔로우/랭킹보드 노출용)',
	is_tooltip_enabled	BOOLEAN	NOT NULL	DEFAULT true	COMMENT '주식 용어 툴팁 활성화 여부',
	create_at	DATETIME	NOT NULL	DEFAULT now()	COMMENT '가입일시',
	bank_account	VARCHAR(50)	NULL	COMMENT '계좌번호',
	bank	VARCHAR(50)	NULL	COMMENT '은행명'
);

/*
-- 주식 종목 정보
CREATE TABLE stock (
	stock_code		VARCHAR(20)	NOT NULL	COMMENT '종목 코드(PK, 예: NVDA)',
	stock_name		VARCHAR(100)	NOT NULL	COMMENT '종목 이름',
	descriptions		TEXT	NULL	COMMENT '기업정보 및 설명(거래허브 "주식회사 정보" 패널용)',
	listing_date		DATETIME	NOT NULL	DEFAULT now()	COMMENT '상장(등록)일시',
	stock_value		BIGINT	NULL,
	stock_total		BIGINT	NULL,
	current_price	INT	NOT NULL	DEFAULT 0	COMMENT '현재가',
	news				VARCHAR(500)	NULL
);

-- 관심종목
CREATE TABLE watchlist (
	member_id	VARCHAR(50)	NOT NULL	COMMENT '회원(FK)',
	stock_code	VARCHAR(20)	NOT NULL	COMMENT '관심 등록한 종목(FK)',
	watch_at	DATETIME	NOT NULL	DEFAULT now()	COMMENT '등록일'
);

-- 주식 댓글
CREATE TABLE stock_chat (
	chat_id	BIGINT	NOT NULL	COMMENT '채팅 번호(PK)',
	stock_code	VARCHAR(20)	NOT NULL	COMMENT '어느 종목 차트의 채팅인지(FK)',
	member_id	VARCHAR(50)	NULL	COMMENT '작성자(FK, 회원탈퇴시 NULL로)',
	content	VARCHAR(500)	NOT NULL	COMMENT '채팅 내용',
	chart_price	INT	NULL	COMMENT '채팅 작성 시점의 종목 가격(차트 위 위치 표시용 스냅샷)',
	create_at	DATETIME	NOT NULL	DEFAULT now()	COMMENT '채팅 작성 시각 = 차트 위 타임스탬프'
);

-- 가상 계좌
CREATE TABLE account (
	account_id	BIGINT	NOT NULL	COMMENT '계좌 번호(PK)',
	account_no	VARCHAR(30)	NOT NULL	COMMENT '증권사측 계좌번호(연동시 식별자)',
	owner_name	VARCHAR(50)	NOT NULL	COMMENT '증권사에 등록된 예금주명(연동시 본인확인용)',
	member_id	VARCHAR(50)	NULL	COMMENT '연동된 사이트 회원(FK). 연동 전에는 NULL',
	brokerage_id	BIGINT	NOT NULL	COMMENT '개설된 증권사(FK)',
	balance	BIGINT	NOT NULL	DEFAULT 10000000	COMMENT '계좌 잔고 (기본 투자금 1000만 원)',
	create_at	DATETIME	NOT NULL	DEFAULT now()	COMMENT '계좌 개설일(증권사측 기준)',
	linked_at	DATETIME	NULL	COMMENT '사이트 회원과 연동된 일시'
);

-- 거래 체결 이력
CREATE TABLE trade (
	trade_id	BIGINT	NOT NULL	COMMENT '거래내역 번호(PK)',
	account_id	BIGINT	NOT NULL	COMMENT '가상 계좌(FK)',
	stock_code	VARCHAR(20)	NOT NULL	COMMENT '종목 코드(FK)',
	trade_type	VARCHAR(10)	NOT NULL	COMMENT '매수/매도 구분(BUY/SELL)',
	quantity	BIGINT	NOT NULL	COMMENT '거래수량',
	price	INT	NOT NULL	COMMENT '체결단가',
	fee	INT	NOT NULL	DEFAULT 0	COMMENT '거래수수료(체결시점 brokerage.fee_rate 적용값 스냅샷)',
	trade_at	DATETIME	NOT NULL	DEFAULT now()	COMMENT '체결일시'
);

-- 게시글 좋아요
CREATE TABLE board_like (
	like_id	BIGINT	NOT NULL	COMMENT '좋아요 번호(PK)',
	board_id	BIGINT	NOT NULL	COMMENT '대상 게시글(FK)',
	member_id VARCHAR(50)	NOT NULL	COMMENT '좋아요 누른 회원(FK)',
	like_date	DATETIME	NOT NULL	DEFAULT now()	COMMENT '좋아요 누른 일시'
);

-- 가상 증권사
CREATE TABLE brokerage (
	brokerage_id	BIGINT	NOT NULL	COMMENT '증권사 번호(PK)',
	brokerage_name	VARCHAR(50)	NOT NULL	COMMENT '증권사 이름',
	fee_rate	DECIMAL(6, 5)	NOT NULL	DEFAULT 0.00015	COMMENT '거래 수수료율(예: 0.00015 = 0.015%)'
);

-- 보유 종목
CREATE TABLE holding (
	holding_id	BIGINT	NOT NULL	COMMENT '보유내역 번호(PK)',
	account_id	BIGINT	NOT NULL	COMMENT '가상 계좌(FK)',
	stock_code	VARCHAR(20)	NOT NULL	COMMENT '종목 코드(FK)',
	quantity	BIGINT	NOT NULL	DEFAULT 0	COMMENT '보유수량',
	avg_price	INT	NOT NULL	DEFAULT 0	COMMENT '평균매입단가',
	update_at	DATETIME	NOT NULL	DEFAULT now()	COMMENT '최종 갱신일시'
);

-- 게시글 이미지
CREATE TABLE board_image (
	img_id	BIGINT	NOT NULL	COMMENT '이미지 번호',
	board_id	BIGINT	NOT NULL	COMMENT '게시글 번호(PK)',
	original_name	VARCHAR(300)	NOT NULL	COMMENT '업로드시 원본 파일명',
	save_name	VARCHAR(300)	NOT NULL	COMMENT '서버에 저장된 이름(동일 이름 충돌 방지용)',
	img_path	VARCHAR(300)	NOT NULL	COMMENT '서버 저장 경로',
	img_order	INT	NOT NULL	DEFAULT 0	COMMENT '대표이미지 정보(0 = 대표이미지)',
	create_at	DATETIME	NOT NULL	DEFAULT now()	COMMENT '페이지 업로드 날짜'
);

-- 랭킹보드
CREATE TABLE ranking_board (
	ranking_id	BIGINT	NOT NULL	COMMENT '랭킹레코드 번호(PK)',
	member_id	VARCHAR(50)	NOT NULL	COMMENT '대상 회원(FK)',
	stock_code	VARCHAR(20)	NULL	COMMENT '종목별 랭킹인 경우 대상 종목(FK). NULL이면 전체 랭킹',
	rank_date	DATE	NOT NULL	COMMENT '랭킹 산정일자',
	rank_position	INT	NOT NULL	COMMENT '순위',
	return_rate	DECIMAL(7, 4)	NOT NULL	COMMENT '수익률(%)',
	benefit_received	VARCHAR(200)	NULL	COMMENT '상위 랭커 혜택 내용',
	create_at	DATETIME	NOT NULL	DEFAULT now()	COMMENT '레코드 생성일시'
);

CREATE TABLE `board_comment` (
	`comment_id`	BIGINT	NOT NULL	COMMENT '댓글번호(PK)',
	`board_id`	BIGINT	NOT NULL	COMMENT '게시글 번호(FK)',
	`member_id`	VARCHAR(50)	NULL	COMMENT '작성자(FK, 회원탈퇴시 NULL로)',
	`parent_comment_id`	BIGINT	NULL	COMMENT '대댓글인 경우 부모 댓글(FK, self). NULL이면 최상위 댓글',
	`content`	VARCHAR(1500)	NOT NULL	COMMENT '댓글 내용',
	`create_at`	DATETIME	NOT NULL	DEFAULT now()	COMMENT '댓글 작성일'
);

CREATE TABLE `stock_price_history` (
	`history_id`	BIGINT	NOT NULL	COMMENT '시세 기록 번호(PK)',
	`stock_code`	VARCHAR(20)	NOT NULL	COMMENT '종목 코드(FK)',
	`price`	INT	NOT NULL	COMMENT '해당 시점 가격',
	`recorded_at`	DATETIME	NOT NULL	DEFAULT now()	COMMENT '시세 기록 시각'
);

CREATE TABLE `glossary` (
	`term_id`	INT	NOT NULL	COMMENT '용어 번호(PK)',
	`term`	VARCHAR(100)	NOT NULL	COMMENT '용어명',
	`definition`	TEXT	NOT NULL	COMMENT '용어 정의 및 설명',
	`category`	VARCHAR(20)	NOT NULL	COMMENT '구분(전문용어/은어 등)'
);

CREATE TABLE `share` (
	`Key`	VARCHAR(255)	NOT NULL,
	`Field`	VARCHAR(255)	NULL
);

CREATE TABLE `follow` (
	`follower_id`	VARCHAR(50)	NOT NULL	COMMENT '팔로우를 신청한 회원(FK)',
	`followee_id`	VARCHAR(50)	NOT NULL	COMMENT '팔로우 당한 회원(FK)',
	`follow_at`	DATETIME	NOT NULL	DEFAULT now()	COMMENT '팔로우한 일시'
);

CREATE TABLE `board_bookmark` (
	`bookmark_id`	BIGINT	NOT NULL	COMMENT '북마크 번호(PK)',
	`board_id`	BIGINT	NOT NULL	COMMENT '대상 게시글(FK)',
	`member_id`	VARCHAR(50)	NOT NULL	COMMENT '북마크한 회원(FK)'
);

CREATE TABLE `financial_product` (
	`product_id`	BIGINT	NOT NULL	COMMENT '상품 번호(PK)',
	`brokerage_id`	BIGINT	NOT NULL	COMMENT '이 상품을 판매하는 증권사(FK) - 필수',
	`product_type`	VARCHAR(10)	NOT NULL	COMMENT '상품유형(FUND/BOND/ELS)',
	`product_name`	VARCHAR(200)	NOT NULL	COMMENT '상품명',
	`description`	TEXT	NULL	COMMENT '상품 설명',
	`nav`	INT	NOT NULL	DEFAULT 0	COMMENT '기준가/평가금액(펀드 기준가 등으로 활용, 채권·ELS는 액면가 개념으로 사용)',
	`maturity_date`	DATE	NULL	COMMENT '만기일(채권/ELS에서 사용, 펀드는 NULL 가능)',
	`launch_date`	DATETIME	NOT NULL	DEFAULT now()	COMMENT '판매 개시일'
);

CREATE TABLE `board` (
	`board_id`	BIGINT	NOT NULL	COMMENT '게시글 번호(PK)',
	`member_id`	VARCHAR(50)	NULL	COMMENT '작성자(FK), 회원이 탈퇴할 시 NULL로 처리',
	`category`	VARCHAR(20)	NOT NULL	DEFAULT '자유'	COMMENT '카테고리(자유/질문/공지... 등)',
	`title`	VARCHAR(200)	NOT NULL	COMMENT '제목',
	`content`	TEXT	NOT NULL	COMMENT '내용',
	`count`	INT	NOT NULL	DEFAULT 0	COMMENT '조회수',
	`like_count`	INT	NOT NULL	DEFAULT 0	COMMENT '좋아요 수(board_like 증감에 맞춰 애플리케이션에서 갱신)',
	`create_at`	DATETIME	NOT NULL	DEFAULT now()	COMMENT '작성일',
	`update_at`	DATETIME	NOT NULL	DEFAULT now()	COMMENT '수정일'
);

ALTER TABLE `stock` ADD CONSTRAINT `PK_STOCK` PRIMARY KEY (
	`stock_code`
);

ALTER TABLE `watchlist` ADD CONSTRAINT `PK_WATCHLIST` PRIMARY KEY (
	`member_id`,
	`stock_code`
);

ALTER TABLE `stock_chat` ADD CONSTRAINT `PK_STOCK_CHAT` PRIMARY KEY (
	`chat_id`
);

ALTER TABLE `account` ADD CONSTRAINT `PK_ACCOUNT` PRIMARY KEY (
	`account_id`
);

ALTER TABLE `trade` ADD CONSTRAINT `PK_TRADE` PRIMARY KEY (
	`trade_id`
);

ALTER TABLE `member` ADD CONSTRAINT `PK_MEMBER` PRIMARY KEY (
	`member_id`
);

ALTER TABLE `board_like` ADD CONSTRAINT `PK_BOARD_LIKE` PRIMARY KEY (
	`like_id`
);

ALTER TABLE `brokerage` ADD CONSTRAINT `PK_BROKERAGE` PRIMARY KEY (
	`brokerage_id`
);

ALTER TABLE `holding` ADD CONSTRAINT `PK_HOLDING` PRIMARY KEY (
	`holding_id`
);

ALTER TABLE `board_image` ADD CONSTRAINT `PK_BOARD_IMAGE` PRIMARY KEY (
	`img_id`
);

ALTER TABLE `ranking_board` ADD CONSTRAINT `PK_RANKING_BOARD` PRIMARY KEY (
	`ranking_id`
);

ALTER TABLE `board_comment` ADD CONSTRAINT `PK_BOARD_COMMENT` PRIMARY KEY (
	`comment_id`
);

ALTER TABLE `stock_price_history` ADD CONSTRAINT `PK_STOCK_PRICE_HISTORY` PRIMARY KEY (
	`history_id`
);

ALTER TABLE `glossary` ADD CONSTRAINT `PK_GLOSSARY` PRIMARY KEY (
	`term_id`
);

ALTER TABLE `share` ADD CONSTRAINT `PK_SHARE` PRIMARY KEY (
	`Key`
);

ALTER TABLE `follow` ADD CONSTRAINT `PK_FOLLOW` PRIMARY KEY (
	`follower_id`,
	`followee_id`
);

ALTER TABLE `board_bookmark` ADD CONSTRAINT `PK_BOARD_BOOKMARK` PRIMARY KEY (
	`bookmark_id`
);

ALTER TABLE `financial_product` ADD CONSTRAINT `PK_FINANCIAL_PRODUCT` PRIMARY KEY (
	`product_id`
);

ALTER TABLE `board` ADD CONSTRAINT `PK_BOARD` PRIMARY KEY (
	`board_id`
);

ALTER TABLE `watchlist` ADD CONSTRAINT `FK_member_TO_watchlist_1` FOREIGN KEY (
	`member_id`
)
REFERENCES `member` (
	`member_id`
);

ALTER TABLE `watchlist` ADD CONSTRAINT `FK_stock_TO_watchlist_1` FOREIGN KEY (
	`stock_code`
)
REFERENCES `stock` (
	`stock_code`
);

ALTER TABLE `follow` ADD CONSTRAINT `FK_member_TO_follow_1` FOREIGN KEY (
	`follower_id`
)
REFERENCES `member` (
	`member_id`
);

ALTER TABLE `follow` ADD CONSTRAINT `FK_member_TO_follow_2` FOREIGN KEY (
	`followee_id`
)
REFERENCES `member` (
	`member_id`
);
*/

-- 관리자 계정 생성
INSERT INTO member (member_id, member_pwd, member_name, nickname, email)
VALUES ('semiadmin2', 'sh1234', '관리자', '스톡허브', 'admin@sh.co.kr');