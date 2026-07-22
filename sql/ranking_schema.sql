CREATE TABLE `ranking_board` (
    `ranking_id`   BIGINT NOT NULL AUTO_INCREMENT COMMENT '랭킹레코드 번호(PK)',
    `member_id`    VARCHAR(50)    NOT NULL   COMMENT '대상 회원(FK)',
    `stock_code`   VARCHAR(20)    NULL   COMMENT '종목별 랭킹인 경우 대상 종목(FK). NULL이면 전체 랭킹',
    `rank_date`    DATE   NOT NULL   COMMENT '랭킹 산정일자',
    `rank_position`    INT    NOT NULL   COMMENT '순위',
    `return_rate`  DECIMAL(7, 4)  NOT NULL   COMMENT '수익률(%)',
    `benefit_received` VARCHAR(200)   NULL   COMMENT '상위 랭커 혜택 내용',
    `create_at`    DATETIME   NOT NULL   DEFAULT now()  COMMENT '레코드 생성일시',
    PRIMARY KEY (`ranking_id`),
    CONSTRAINT fk_ranking_member FOREIGN KEY (member_id) REFERENCES member(member_id)
) COMMENT = '랭킹보드';

INSERT INTO ranking_board (member_id, stock_code, rank_date, rank_position, return_rate, benefit_received)
VALUES
  ('admin', NULL, CURDATE(), 1, 312.4000, '수수료 할인 쿠폰'),
  ('admin', NULL, CURDATE(), 2, 287.1000, NULL);