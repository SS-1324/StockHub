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
