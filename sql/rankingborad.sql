CREATE TABLE `ranking_board` (
	`ranking_id`	BIGINT	NOT NULL	COMMENT '랭킹레코드 번호(PK)',
	`member_id`	VARCHAR(50)	NOT NULL	COMMENT '대상 회원(FK)',
	`stock_code`	VARCHAR(20)	NULL	COMMENT '종목별 랭킹인 경우 대상 종목(FK). NULL이면 전체 랭킹',
	`rank_date`	DATE	NOT NULL	COMMENT '랭킹 산정일자',
	`rank_position`	INT	NOT NULL	COMMENT '순위',
	`return_rate`	DECIMAL(7, 4)	NOT NULL	COMMENT '수익률(%)',
	`benefit_received`	VARCHAR(200)	NULL	COMMENT '상위 랭커 혜택 내용',
	`create_at`	DATETIME	NOT NULL	DEFAULT now()	COMMENT '레코드 생성일시'
);