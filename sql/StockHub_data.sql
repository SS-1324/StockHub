-- stockhub DB 생성
CREATE DATABASE IF NOT EXISTS stockhub
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- stockhub DB 선택
USE stockhub;

-- MySQL과 데이터를 주고 받는 인코딩 설정
SET NAMES utf8mb4;

-- -------------------- 회원 및 인증 --------------------

-- 회원
-- 증권사와 계좌번호는 별도 거래 테이블과 연결하지 않고 회원 정보에 직접 저장
CREATE TABLE IF NOT EXISTS member (
    member_id     VARCHAR(50)  NOT NULL COMMENT '로그인 아이디(PK)',
    member_pwd    VARCHAR(255) NOT NULL COMMENT '암호화된 비밀번호',
    member_name   VARCHAR(50)  NOT NULL COMMENT '이름',
    nickname      VARCHAR(50)  NOT NULL COMMENT '닉네임',
    email         VARCHAR(100) NOT NULL COMMENT '이메일',
    profile       VARCHAR(300) NULL COMMENT '프로필 이미지 저장 경로',
    brokerage     VARCHAR(50)  NULL COMMENT '선택한 증권사 이름',
    account_no    VARCHAR(15)  NULL COMMENT '계좌번호(숫자 최대 15자리)',
    create_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '가입일시',

    CONSTRAINT PK_MEMBER PRIMARY KEY (member_id),
    CONSTRAINT UQ_MEMBER_NICKNAME UNIQUE (nickname),
    CONSTRAINT UQ_MEMBER_EMAIL UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='회원';

-- 이미 생성된 member 테이블에도 brokerage 컬럼이 없을 때만 추가
SET @add_brokerage_sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE member ADD COLUMN brokerage VARCHAR(50) NULL COMMENT ''선택한 증권사 이름'' AFTER profile',
        'SELECT 1'
    )
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'member'
      AND column_name = 'brokerage'
);
PREPARE add_brokerage_stmt FROM @add_brokerage_sql;
EXECUTE add_brokerage_stmt;
DEALLOCATE PREPARE add_brokerage_stmt;

-- 이미 생성된 member 테이블에도 account_no 컬럼이 없을 때만 추가
SET @add_account_no_sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE member ADD COLUMN account_no VARCHAR(15) NULL COMMENT ''계좌번호(숫자 최대 15자리)'' AFTER brokerage',
        'SELECT 1'
    )
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'member'
      AND column_name = 'account_no'
);
PREPARE add_account_no_stmt FROM @add_account_no_sql;
EXECUTE add_account_no_stmt;
DEALLOCATE PREPARE add_account_no_stmt;

-- 개인 환경설정
CREATE TABLE IF NOT EXISTS settings (
    member_id          VARCHAR(50) NOT NULL COMMENT '회원 아이디(PK, FK)',
    is_profile_public  BOOLEAN     NOT NULL DEFAULT TRUE COMMENT '프로필 공개 여부',
    is_word_tooltip    BOOLEAN     NOT NULL DEFAULT TRUE COMMENT '주식 용어 툴팁 사용 여부',
    is_light_mode      BOOLEAN     NOT NULL DEFAULT TRUE COMMENT '라이트 모드 사용 여부',
    update_at          DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP
                                    ON UPDATE CURRENT_TIMESTAMP COMMENT '설정 수정일시',

    CONSTRAINT PK_SETTINGS PRIMARY KEY (member_id),
    CONSTRAINT FK_MEMBER_TO_SETTINGS
        FOREIGN KEY (member_id) REFERENCES member (member_id)
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='회원별 환경설정';

-- 이메일 인증
CREATE TABLE IF NOT EXISTS email_verification (
    verification_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '이메일 인증 번호(PK)',
    member_id       VARCHAR(50)     NULL COMMENT '가입 완료 후 연결되는 회원 아이디(FK)',
    email           VARCHAR(100)    NOT NULL COMMENT '인증 대상 이메일',
    code            VARCHAR(10)     NOT NULL COMMENT '이메일 인증 코드',
    is_verified     BOOLEAN         NOT NULL DEFAULT FALSE COMMENT '인증 완료 여부',
    expired_at      DATETIME        NOT NULL COMMENT '인증 코드 만료일시',
    verified_at     DATETIME        NULL COMMENT '인증 완료일시',
    create_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '인증 요청일시',

    CONSTRAINT PK_EMAIL_VERIFICATION PRIMARY KEY (verification_id),
    CONSTRAINT FK_MEMBER_TO_EMAIL_VERIFICATION
        FOREIGN KEY (member_id) REFERENCES member (member_id)
        ON UPDATE CASCADE ON DELETE SET NULL,
    INDEX IDX_EMAIL_VERIFICATION_EMAIL_DATE (email, create_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='이메일 인증';

-- 비밀번호 재설정
CREATE TABLE IF NOT EXISTS password_reset_token (
    token_id    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '비밀번호 재설정 번호(PK)',
    member_id   VARCHAR(50)     NOT NULL COMMENT '회원 아이디(FK)',
    token       VARCHAR(255)    NOT NULL COMMENT '비밀번호 재설정 토큰',
    is_used     BOOLEAN         NOT NULL DEFAULT FALSE COMMENT '토큰 사용 여부',
    expired_at  DATETIME        NOT NULL COMMENT '토큰 만료일시',
    create_at   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '토큰 생성일시',

    CONSTRAINT PK_PASSWORD_RESET_TOKEN PRIMARY KEY (token_id),
    CONSTRAINT UQ_PASSWORD_RESET_TOKEN UNIQUE (token),
    CONSTRAINT FK_MEMBER_TO_PASSWORD_RESET_TOKEN
        FOREIGN KEY (member_id) REFERENCES member (member_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    INDEX IDX_PASSWORD_RESET_MEMBER_DATE (member_id, create_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='비밀번호 재설정 토큰';

-- 거래·보유 종목·차트 관련 테이블은 이 회원 기능 SQL에서 생성하지 않음

-- 관리자계정 생성 예시
-- 비밀번호는 실제 사용 전에 BCrypt로 암호화해야 함
-- INSERT INTO member (member_id, member_pwd, member_name, nickname, email)
-- VALUES ('admin01', '암호화된 비밀번호', '관리자', '스톡허브', 'admin01@kh.co.kr');

-- 테이블 확인
SHOW TABLES;

-- 회원 확인
SELECT
    member_id,
    member_name,
    nickname,
    email,
    brokerage,
    account_no,
    create_at
FROM member
ORDER BY create_at DESC;