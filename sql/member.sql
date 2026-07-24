CREATE DATABASE IF NOT EXISTS stockhub
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE stockhub;

CREATE TABLE IF NOT EXISTS members (
    member_id VARCHAR(50) NOT NULL COMMENT '로그인 아이디',
    member_pwd VARCHAR(255) NOT NULL COMMENT '암호화된 비밀번호',
    member_name VARCHAR(50) NOT NULL COMMENT '이름',
    nickname VARCHAR(50) NOT NULL COMMENT '닉네임',
    email VARCHAR(100) NOT NULL COMMENT '이메일',
    profile VARCHAR(300) NULL COMMENT '프로필 이미지 경로',
    create_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '가입일시',
    CONSTRAINT pk_members PRIMARY KEY (member_id),
    CONSTRAINT uq_members_nickname UNIQUE (nickname),
    CONSTRAINT uq_members_email UNIQUE (email)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='회원';

-- 회원별 프로필 공개와 툴팁 설정
CREATE TABLE IF NOT EXISTS settings (
    member_id VARCHAR(50) NOT NULL COMMENT '회원 아이디',
    is_profile_public BOOLEAN NOT NULL DEFAULT TRUE COMMENT '프로필 공개 여부',
    is_word_tooltip BOOLEAN NOT NULL DEFAULT TRUE COMMENT '주식 용어 툴팁 사용 여부',
    is_light_mode BOOLEAN NOT NULL DEFAULT TRUE COMMENT '라이트 모드 사용 여부',
    update_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                           ON UPDATE CURRENT_TIMESTAMP COMMENT '설정 수정일시',
    CONSTRAINT pk_settings PRIMARY KEY (member_id),
    CONSTRAINT fk_members_to_settings
        FOREIGN KEY (member_id) REFERENCES members (member_id)
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='회원별 환경설정';

-- 마이페이지에서 선택할 가상 증권사
CREATE TABLE IF NOT EXISTS brokerage (
    brokerage_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '증권사 번호',
    brokerage_name VARCHAR(50) NOT NULL COMMENT '증권사 이름',
    fee_rate DECIMAL(8, 7) NOT NULL DEFAULT 0.0001500 COMMENT '거래 수수료율',
    CONSTRAINT pk_brokerage PRIMARY KEY (brokerage_id),
    CONSTRAINT uq_brokerage_name UNIQUE (brokerage_name),
    CONSTRAINT ck_brokerage_fee_rate CHECK (fee_rate >= 0)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='가상 증권사';

-- 회원이 마이페이지에서 등록한 계좌
CREATE TABLE IF NOT EXISTS account (
    account_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '계좌 번호',
    account_no VARCHAR(50) NOT NULL COMMENT '사용자 계좌번호',
    member_id VARCHAR(50) NOT NULL COMMENT '계좌 소유 회원',
    brokerage_id BIGINT UNSIGNED NOT NULL COMMENT '선택한 증권사',
    owner_name VARCHAR(50) NOT NULL COMMENT '예금주명',
    balance BIGINT UNSIGNED NOT NULL DEFAULT 10000000 COMMENT '현금 잔고',
    create_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '계좌 생성일시',
    linked_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '회원 연결일시',
    CONSTRAINT pk_account PRIMARY KEY (account_id),
    CONSTRAINT uq_account_no UNIQUE (account_no),
    CONSTRAINT uq_account_member_brokerage UNIQUE (member_id, brokerage_id),
    CONSTRAINT fk_members_to_account
        FOREIGN KEY (member_id) REFERENCES members (member_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_brokerage_to_account
        FOREIGN KEY (brokerage_id) REFERENCES brokerage (brokerage_id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='가상 증권 계좌';

-- 가상 증권사 기본 선택지 3개
INSERT IGNORE INTO brokerage (brokerage_name, fee_rate)
VALUES ('스톡증권', 0.0001500),
       ('허브증권', 0.0001200),
       ('KH투자증권', 0.0001000);