-- stockhub DB 생성
CREATE DATABASE IF NOT EXISTS stockhub
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- stockhub DB 선택
USE stockhub;

-- MySQL과 데이터를 주고 받는 인코딩 설정
SET NAMES utf8mb4;

-- -------------------- 1. 회원 및 인증 --------------------

-- 회원
CREATE TABLE IF NOT EXISTS member (
    member_id     VARCHAR(50)  NOT NULL COMMENT '로그인 아이디(PK)',
    member_pwd    VARCHAR(255) NOT NULL COMMENT '암호화된 비밀번호',
    member_name   VARCHAR(50)  NOT NULL COMMENT '이름',
    nickname      VARCHAR(50)  NOT NULL COMMENT '닉네임',
    email         VARCHAR(100) NOT NULL COMMENT '이메일',
    profile       VARCHAR(300) NOT NULL DEFAULT '/images/common_member.png'
        COMMENT '프로필 이미지 저장 경로',
    member_role   VARCHAR(20)  NOT NULL DEFAULT 'USER' COMMENT '회원 권한(USER/ADMIN)',
    create_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '가입일시',

    CONSTRAINT PK_MEMBER PRIMARY KEY (member_id),
    CONSTRAINT UQ_MEMBER_NICKNAME UNIQUE (nickname),
    CONSTRAINT UQ_MEMBER_EMAIL UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='회원';

-- 기존 member 테이블에도 권한 컬럼이 없을 때만 추가
SET @add_member_role_sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE member ADD COLUMN member_role VARCHAR(20) NOT NULL DEFAULT ''USER'' COMMENT ''회원 권한(USER/ADMIN)'' AFTER profile',
        'SELECT 1'
    )
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'member'
      AND column_name = 'member_role'
);
PREPARE add_member_role_stmt FROM @add_member_role_sql;
EXECUTE add_member_role_stmt;
DEALLOCATE PREPARE add_member_role_stmt;

-- 기존 회원의 빈 프로필도 공통 기본 이미지로 변경
UPDATE member
SET profile = '/images/common_member.png'
WHERE profile IS NULL
   OR TRIM(profile) = '';

-- 기존 member 테이블에도 기본 프로필 경로가 자동 저장되도록 설정
ALTER TABLE member
    MODIFY COLUMN profile VARCHAR(300) NOT NULL
        DEFAULT '/images/common_member.png'
        COMMENT '프로필 이미지 저장 경로';

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

-- -------------------- 2. 종목, 증권사, 계좌 및 거래 --------------------

-- 주식
CREATE TABLE IF NOT EXISTS stock (
    stock_code     VARCHAR(20)      NOT NULL COMMENT '종목 코드(PK, 예: NVDA)',
    stock_name     VARCHAR(100)     NOT NULL COMMENT '종목 이름',
    description    TEXT             NULL COMMENT '기업 정보 및 설명',
    listing_date   DATE             NULL COMMENT '상장일',
    stock_value    BIGINT UNSIGNED  NULL COMMENT '시가총액',
    stock_total    BIGINT UNSIGNED  NULL COMMENT '상장 주식 수',
    current_price  BIGINT UNSIGNED  NOT NULL DEFAULT 0 COMMENT '현재가',
    news           VARCHAR(500)     NULL COMMENT '대표 뉴스 또는 뉴스 요약',

    CONSTRAINT PK_STOCK PRIMARY KEY (stock_code),
    INDEX IDX_STOCK_NAME (stock_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='주식 종목';

-- 증권사(가상)
CREATE TABLE IF NOT EXISTS brokerage (
    brokerage_id    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '증권사 번호(PK)',
    brokerage_name  VARCHAR(50)     NOT NULL COMMENT '증권사 이름',
    fee_rate        DECIMAL(8, 7)   NOT NULL DEFAULT 0.0001500 COMMENT '거래 수수료율',

    CONSTRAINT PK_BROKERAGE PRIMARY KEY (brokerage_id),
    CONSTRAINT UQ_BROKERAGE_NAME UNIQUE (brokerage_name),
    CONSTRAINT CK_BROKERAGE_FEE_RATE CHECK (fee_rate >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='가상 증권사';

-- 마이페이지 증권사 기본 선택지
INSERT IGNORE INTO brokerage (brokerage_name, fee_rate)
VALUES ('스톡증권', 0.0001500),
       ('허브증권', 0.0001200),
       ('KH투자증권', 0.0001000);

-- 가상 계좌
CREATE TABLE IF NOT EXISTS account (
    account_id    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '가상 계좌 번호(PK)',
    account_no    VARCHAR(50)     NOT NULL COMMENT '사용자에게 표시할 계좌번호',
    member_id     VARCHAR(50)     NOT NULL COMMENT '계좌 소유 회원(FK)',
    brokerage_id  BIGINT UNSIGNED NOT NULL COMMENT '계좌를 개설한 증권사(FK)',
    owner_name    VARCHAR(50)     NOT NULL COMMENT '예금주명',
    balance       BIGINT UNSIGNED NOT NULL DEFAULT 10000000 COMMENT '현금 잔고(기본 1천만 원)',
    create_at     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '계좌 개설일시',
    linked_at     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '회원과 연결된 일시',

    CONSTRAINT PK_ACCOUNT PRIMARY KEY (account_id),
    CONSTRAINT UQ_ACCOUNT_NO UNIQUE (account_no),
    CONSTRAINT UQ_ACCOUNT_MEMBER_BROKERAGE UNIQUE (member_id, brokerage_id),
    CONSTRAINT FK_MEMBER_TO_ACCOUNT
        FOREIGN KEY (member_id) REFERENCES member (member_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT FK_BROKERAGE_TO_ACCOUNT
        FOREIGN KEY (brokerage_id) REFERENCES brokerage (brokerage_id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='가상 증권 계좌';

-- 보유 종목
CREATE TABLE IF NOT EXISTS holding (
    holding_id   BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '보유내역 번호(PK)',
    account_id   BIGINT UNSIGNED NOT NULL COMMENT '가상 계좌(FK)',
    stock_code   VARCHAR(20)     NOT NULL COMMENT '종목 코드(FK)',
    quantity     BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '보유수량',
    avg_price    BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '평균매입단가',
    update_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP COMMENT '최종 갱신일시',

    CONSTRAINT PK_HOLDING PRIMARY KEY (holding_id),
    CONSTRAINT UQ_HOLDING_ACCOUNT_STOCK UNIQUE (account_id, stock_code),
    CONSTRAINT FK_ACCOUNT_TO_HOLDING
        FOREIGN KEY (account_id) REFERENCES account (account_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT FK_STOCK_TO_HOLDING
        FOREIGN KEY (stock_code) REFERENCES stock (stock_code)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='계좌별 주식 보유내역';

-- 거래 체결 이력
CREATE TABLE IF NOT EXISTS trade (
    trade_id    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '거래내역 번호(PK)',
    account_id  BIGINT UNSIGNED NOT NULL COMMENT '가상 계좌(FK)',
    stock_code  VARCHAR(20)     NOT NULL COMMENT '종목 코드(FK)',
    trade_type  ENUM('BUY', 'SELL') NOT NULL COMMENT '매수/매도 구분',
    quantity    BIGINT UNSIGNED NOT NULL COMMENT '거래수량',
    price       BIGINT UNSIGNED NOT NULL COMMENT '체결단가',
    fee         BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '체결 당시 거래수수료',
    trade_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '체결일시',

    CONSTRAINT PK_TRADE PRIMARY KEY (trade_id),
    CONSTRAINT CK_TRADE_QUANTITY CHECK (quantity > 0),
    CONSTRAINT CK_TRADE_PRICE CHECK (price > 0),
    CONSTRAINT FK_ACCOUNT_TO_TRADE
        FOREIGN KEY (account_id) REFERENCES account (account_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT FK_STOCK_TO_TRADE
        FOREIGN KEY (stock_code) REFERENCES stock (stock_code)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    INDEX IDX_TRADE_ACCOUNT_DATE (account_id, trade_at),
    INDEX IDX_TRADE_STOCK_DATE (stock_code, trade_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='주식 거래내역';

-- 관심종목
CREATE TABLE IF NOT EXISTS watchlist (
    member_id   VARCHAR(50) NOT NULL COMMENT '회원 아이디(FK)',
    stock_code  VARCHAR(20) NOT NULL COMMENT '관심 종목 코드(FK)',
    watch_at    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '관심 종목 등록일시',

    CONSTRAINT PK_WATCHLIST PRIMARY KEY (member_id, stock_code),
    CONSTRAINT FK_MEMBER_TO_WATCHLIST
        FOREIGN KEY (member_id) REFERENCES member (member_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT FK_STOCK_TO_WATCHLIST
        FOREIGN KEY (stock_code) REFERENCES stock (stock_code)
        ON UPDATE CASCADE ON DELETE CASCADE,
    INDEX IDX_WATCHLIST_STOCK (stock_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='회원별 관심 종목';

-- 주식 채팅
CREATE TABLE IF NOT EXISTS stock_chat (
    chat_id      BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '채팅 번호(PK)',
    stock_code   VARCHAR(20)     NOT NULL COMMENT '채팅 대상 종목(FK)',
    member_id    VARCHAR(50)     NULL COMMENT '작성자(FK, 회원 탈퇴 시 NULL)',
    content      VARCHAR(500)    NOT NULL COMMENT '채팅 내용',
    chart_price  BIGINT UNSIGNED NULL COMMENT '채팅 작성 시점의 종목 가격',
    create_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '채팅 작성일시',

    CONSTRAINT PK_STOCK_CHAT PRIMARY KEY (chat_id),
    CONSTRAINT FK_STOCK_TO_STOCK_CHAT
        FOREIGN KEY (stock_code) REFERENCES stock (stock_code)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT FK_MEMBER_TO_STOCK_CHAT
        FOREIGN KEY (member_id) REFERENCES member (member_id)
        ON UPDATE CASCADE ON DELETE SET NULL,
    INDEX IDX_STOCK_CHAT_STOCK_DATE (stock_code, create_at),
    INDEX IDX_STOCK_CHAT_MEMBER (member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='종목별 실시간 채팅';

-- 시세 변동 기록
CREATE TABLE IF NOT EXISTS stock_price_history (
    history_id   BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '시세 기록 번호(PK)',
    stock_code   VARCHAR(20)     NOT NULL COMMENT '종목 코드(FK)',
    price        BIGINT UNSIGNED NOT NULL COMMENT '해당 시점 가격',
    recorded_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '시세 기록 시각',

    CONSTRAINT PK_STOCK_PRICE_HISTORY PRIMARY KEY (history_id),
    CONSTRAINT UQ_STOCK_PRICE_TIME UNIQUE (stock_code, recorded_at),
    CONSTRAINT FK_STOCK_TO_STOCK_PRICE_HISTORY
        FOREIGN KEY (stock_code) REFERENCES stock (stock_code)
        ON UPDATE CASCADE ON DELETE CASCADE,
    INDEX IDX_STOCK_PRICE_DATE (recorded_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='종목 시세 이력';

-- 랭킹 보드
CREATE TABLE IF NOT EXISTS ranking_board (
    ranking_id       BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '랭킹 레코드 번호(PK)',
    member_id        VARCHAR(50)     NOT NULL COMMENT '대상 회원(FK)',
    stock_code       VARCHAR(20)     NULL COMMENT '종목별 랭킹의 대상 종목(FK), 전체 랭킹이면 NULL',
    rank_date        DATE            NOT NULL COMMENT '랭킹 산정일자',
    rank_position    INT UNSIGNED    NOT NULL COMMENT '순위',
    return_rate      DECIMAL(9, 4)   NOT NULL COMMENT '수익률(%)',
    benefit_received VARCHAR(200)    NULL COMMENT '상위 랭커 혜택 내용',
    create_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '레코드 생성일시',

    CONSTRAINT PK_RANKING_BOARD PRIMARY KEY (ranking_id),
    CONSTRAINT CK_RANKING_POSITION CHECK (rank_position > 0),
    CONSTRAINT FK_MEMBER_TO_RANKING_BOARD
        FOREIGN KEY (member_id) REFERENCES member (member_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT FK_STOCK_TO_RANKING_BOARD
        FOREIGN KEY (stock_code) REFERENCES stock (stock_code)
        ON UPDATE CASCADE ON DELETE CASCADE,
    INDEX IDX_RANKING_DATE_POSITION (rank_date, rank_position),
    INDEX IDX_RANKING_STOCK_DATE (stock_code, rank_date, rank_position),
    INDEX IDX_RANKING_MEMBER_DATE (member_id, rank_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='일자별 수익률 랭킹';

-- 증권사 전용 상품
CREATE TABLE IF NOT EXISTS financial_product (
    product_id     BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '금융상품 번호(PK)',
    brokerage_id   BIGINT UNSIGNED NOT NULL COMMENT '판매 증권사(FK)',
    product_type   ENUM('FUND', 'BOND', 'ELS') NOT NULL COMMENT '상품유형',
    product_name   VARCHAR(200)    NOT NULL COMMENT '상품명',
    description    TEXT            NULL COMMENT '상품 설명',
    nav            DECIMAL(18, 2)  NOT NULL DEFAULT 0 COMMENT '기준가 또는 평가금액',
    maturity_date  DATE            NULL COMMENT '만기일',
    launch_date    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '판매 개시일시',

    CONSTRAINT PK_FINANCIAL_PRODUCT PRIMARY KEY (product_id),
    CONSTRAINT CK_FINANCIAL_PRODUCT_NAV CHECK (nav >= 0),
    CONSTRAINT FK_BROKERAGE_TO_FINANCIAL_PRODUCT
        FOREIGN KEY (brokerage_id) REFERENCES brokerage (brokerage_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    INDEX IDX_FINANCIAL_PRODUCT_BROKERAGE_TYPE (brokerage_id, product_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='증권사별 금융상품';

-- -------------------- 3. 게시판, 댓글 및 회원 관계 --------------------

-- 게시판
CREATE TABLE IF NOT EXISTS board (
    board_id    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '게시글 번호(PK)',
    member_id   VARCHAR(50)     NULL COMMENT '작성자(FK, 회원 탈퇴 시 NULL)',
    category    VARCHAR(50)     NOT NULL COMMENT '카테고리(자유/질문/공지 등)',
    title       VARCHAR(200)    NOT NULL COMMENT '제목',
    content     TEXT            NOT NULL COMMENT '내용',
    count       BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '조회수',
    like_count  BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '좋아요 수 캐시',
    create_at   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '작성일시',
    update_at   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',

    CONSTRAINT PK_BOARD PRIMARY KEY (board_id),
    CONSTRAINT FK_MEMBER_TO_BOARD
        FOREIGN KEY (member_id) REFERENCES member (member_id)
        ON UPDATE CASCADE ON DELETE SET NULL,
    INDEX IDX_BOARD_CATEGORY_DATE (category, create_at),
    INDEX IDX_BOARD_MEMBER_DATE (member_id, create_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='게시글';

-- 회원 문의
CREATE TABLE IF NOT EXISTS inquiry (
    inquiry_id  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '문의 번호(PK)',
    member_id   VARCHAR(50)     NULL COMMENT '문의 회원(FK, 탈퇴 시 NULL)',
    title       VARCHAR(20)     NOT NULL COMMENT '문의 제목(20자 이내)',
    content     VARCHAR(200)    NOT NULL COMMENT '문의 내용(200자 이내)',
    answer      VARCHAR(500)    NULL COMMENT '관리자 답변(500자 이내)',
    status      VARCHAR(20)     NOT NULL DEFAULT 'PENDING' COMMENT '처리 상태',
    answered_by VARCHAR(50)     NULL COMMENT '답변 관리자(FK)',
    answered_at DATETIME        NULL COMMENT '관리자 답변일시',
    create_at   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '문의 접수일시',

    CONSTRAINT PK_INQUIRY PRIMARY KEY (inquiry_id),
    CONSTRAINT FK_MEMBER_TO_INQUIRY
        FOREIGN KEY (member_id) REFERENCES member (member_id)
        ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT FK_MEMBER_TO_INQUIRY_ANSWERED_BY
        FOREIGN KEY (answered_by) REFERENCES member (member_id)
        ON UPDATE CASCADE ON DELETE SET NULL,
    INDEX IDX_INQUIRY_DATE (create_at),
    INDEX IDX_INQUIRY_MEMBER (member_id),
    INDEX IDX_INQUIRY_ANSWERED_BY (answered_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='회원 문의';

-- [통합된 inquiry_answer_migration]
-- 기존 inquiry 테이블에도 관리자 답변 열을 안전하게 추가
SET @add_inquiry_answer_sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE inquiry ADD COLUMN answer VARCHAR(500) NULL COMMENT ''관리자 답변(500자 이내)'' AFTER content',
        'SELECT 1'
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'inquiry'
      AND COLUMN_NAME = 'answer'
);
PREPARE add_inquiry_answer_stmt FROM @add_inquiry_answer_sql;
EXECUTE add_inquiry_answer_stmt;
DEALLOCATE PREPARE add_inquiry_answer_stmt;

SET @add_inquiry_answered_by_sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE inquiry ADD COLUMN answered_by VARCHAR(50) NULL COMMENT ''답변 관리자(FK)'' AFTER status',
        'SELECT 1'
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'inquiry'
      AND COLUMN_NAME = 'answered_by'
);
PREPARE add_inquiry_answered_by_stmt FROM @add_inquiry_answered_by_sql;
EXECUTE add_inquiry_answered_by_stmt;
DEALLOCATE PREPARE add_inquiry_answered_by_stmt;

SET @add_inquiry_answered_at_sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE inquiry ADD COLUMN answered_at DATETIME NULL COMMENT ''관리자 답변일시'' AFTER answered_by',
        'SELECT 1'
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'inquiry'
      AND COLUMN_NAME = 'answered_at'
);
PREPARE add_inquiry_answered_at_stmt FROM @add_inquiry_answered_at_sql;
EXECUTE add_inquiry_answered_at_stmt;
DEALLOCATE PREPARE add_inquiry_answered_at_stmt;

-- 기존 inquiry 테이블에 답변 관리자 인덱스와 FK를 한 번만 추가
SET @add_inquiry_answered_by_index_sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE inquiry ADD INDEX IDX_INQUIRY_ANSWERED_BY (answered_by)',
        'SELECT 1'
    )
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'inquiry'
      AND INDEX_NAME = 'IDX_INQUIRY_ANSWERED_BY'
);
PREPARE add_inquiry_answered_by_index_stmt
    FROM @add_inquiry_answered_by_index_sql;
EXECUTE add_inquiry_answered_by_index_stmt;
DEALLOCATE PREPARE add_inquiry_answered_by_index_stmt;

SET @add_inquiry_answered_by_fk_sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE inquiry ADD CONSTRAINT FK_MEMBER_TO_INQUIRY_ANSWERED_BY FOREIGN KEY (answered_by) REFERENCES member (member_id) ON UPDATE CASCADE ON DELETE SET NULL',
        'SELECT 1'
    )
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE()
      AND TABLE_NAME = 'inquiry'
      AND CONSTRAINT_NAME = 'FK_MEMBER_TO_INQUIRY_ANSWERED_BY'
);
PREPARE add_inquiry_answered_by_fk_stmt
    FROM @add_inquiry_answered_by_fk_sql;
EXECUTE add_inquiry_answered_by_fk_stmt;
DEALLOCATE PREPARE add_inquiry_answered_by_fk_stmt;

-- 게시글 댓글
CREATE TABLE IF NOT EXISTS board_comment (
    comment_id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '댓글 번호(PK)',
    board_id           BIGINT UNSIGNED NOT NULL COMMENT '게시글 번호(FK)',
    member_id          VARCHAR(50)     NULL COMMENT '작성자(FK, 회원 탈퇴 시 NULL)',
    parent_comment_id  BIGINT UNSIGNED NULL COMMENT '부모 댓글 번호(FK), 최상위 댓글이면 NULL',
    content            VARCHAR(1500)   NOT NULL COMMENT '댓글 내용',
    create_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '댓글 작성일시',
    update_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP COMMENT '댓글 수정일시',

    CONSTRAINT PK_BOARD_COMMENT PRIMARY KEY (comment_id),
    CONSTRAINT FK_BOARD_TO_BOARD_COMMENT
        FOREIGN KEY (board_id) REFERENCES board (board_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT FK_MEMBER_TO_BOARD_COMMENT
        FOREIGN KEY (member_id) REFERENCES member (member_id)
        ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT FK_PARENT_TO_BOARD_COMMENT
        FOREIGN KEY (parent_comment_id) REFERENCES board_comment (comment_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    INDEX IDX_BOARD_COMMENT_BOARD_DATE (board_id, create_at),
    INDEX IDX_BOARD_COMMENT_MEMBER (member_id),
    INDEX IDX_BOARD_COMMENT_PARENT (parent_comment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='게시글 댓글 및 대댓글';

-- 게시글 이미지
CREATE TABLE IF NOT EXISTS board_image (
    img_id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '이미지 번호(PK)',
    board_id       BIGINT UNSIGNED NOT NULL COMMENT '게시글 번호(FK)',
    original_name  VARCHAR(255)    NOT NULL COMMENT '업로드 당시 원본 파일명',
    save_name      VARCHAR(255)    NOT NULL COMMENT '서버에 저장된 파일명',
    img_path       VARCHAR(500)    NOT NULL COMMENT '서버 저장 경로',
    img_order      INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '표시 순서(0은 대표 이미지)',
    create_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '업로드일시',

    CONSTRAINT PK_BOARD_IMAGE PRIMARY KEY (img_id),
    CONSTRAINT UQ_BOARD_IMAGE_ORDER UNIQUE (board_id, img_order),
    CONSTRAINT UQ_BOARD_IMAGE_SAVE_NAME UNIQUE (save_name),
    CONSTRAINT FK_BOARD_TO_BOARD_IMAGE
        FOREIGN KEY (board_id) REFERENCES board (board_id)
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='게시글 첨부 이미지';

-- 게시글 좋아요
CREATE TABLE IF NOT EXISTS board_like (
    like_id    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '게시글 좋아요 번호(PK)',
    board_id   BIGINT UNSIGNED NOT NULL COMMENT '대상 게시글(FK)',
    member_id  VARCHAR(50)     NOT NULL COMMENT '좋아요를 누른 회원(FK)',
    like_date  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '좋아요를 누른 일시',

    CONSTRAINT PK_BOARD_LIKE PRIMARY KEY (like_id),
    CONSTRAINT UQ_BOARD_LIKE_MEMBER UNIQUE (board_id, member_id),
    CONSTRAINT FK_BOARD_TO_BOARD_LIKE
        FOREIGN KEY (board_id) REFERENCES board (board_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT FK_MEMBER_TO_BOARD_LIKE
        FOREIGN KEY (member_id) REFERENCES member (member_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    INDEX IDX_BOARD_LIKE_MEMBER (member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='게시글 좋아요';

-- 댓글 좋아요
CREATE TABLE IF NOT EXISTS comment_like (
    comment_like_id  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '댓글 좋아요 번호(PK)',
    comment_id       BIGINT UNSIGNED NOT NULL COMMENT '대상 댓글(FK)',
    member_id        VARCHAR(50)     NOT NULL COMMENT '좋아요를 누른 회원(FK)',
    like_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '좋아요를 누른 일시',

    CONSTRAINT PK_COMMENT_LIKE PRIMARY KEY (comment_like_id),
    CONSTRAINT UQ_COMMENT_LIKE_MEMBER UNIQUE (comment_id, member_id),
    CONSTRAINT FK_BOARD_COMMENT_TO_COMMENT_LIKE
        FOREIGN KEY (comment_id) REFERENCES board_comment (comment_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT FK_MEMBER_TO_COMMENT_LIKE
        FOREIGN KEY (member_id) REFERENCES member (member_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    INDEX IDX_COMMENT_LIKE_MEMBER (member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='댓글 좋아요';

-- 게시글 북마크
CREATE TABLE IF NOT EXISTS board_bookmark (
    bookmark_id  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '북마크 번호(PK)',
    board_id     BIGINT UNSIGNED NOT NULL COMMENT '대상 게시글(FK)',
    member_id    VARCHAR(50)     NOT NULL COMMENT '북마크한 회원(FK)',
    bookmark_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '북마크한 일시',

    CONSTRAINT PK_BOARD_BOOKMARK PRIMARY KEY (bookmark_id),
    CONSTRAINT UQ_BOARD_BOOKMARK_MEMBER UNIQUE (board_id, member_id),
    CONSTRAINT FK_BOARD_TO_BOARD_BOOKMARK
        FOREIGN KEY (board_id) REFERENCES board (board_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT FK_MEMBER_TO_BOARD_BOOKMARK
        FOREIGN KEY (member_id) REFERENCES member (member_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    INDEX IDX_BOARD_BOOKMARK_MEMBER (member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='게시글 북마크';

-- 팔로우
CREATE TABLE IF NOT EXISTS follow (
    follower_id  VARCHAR(50) NOT NULL COMMENT '팔로우를 신청한 회원(FK)',
    followee_id  VARCHAR(50) NOT NULL COMMENT '팔로우를 받은 회원(FK)',
    follow_at    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '팔로우한 일시',

    CONSTRAINT PK_FOLLOW PRIMARY KEY (follower_id, followee_id),
    CONSTRAINT FK_MEMBER_TO_FOLLOWER
        FOREIGN KEY (follower_id) REFERENCES member (member_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT FK_MEMBER_TO_FOLLOWEE
        FOREIGN KEY (followee_id) REFERENCES member (member_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    INDEX IDX_FOLLOW_FOLLOWEE (followee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='회원 팔로우 관계';

-- -------------------- 4. 주식 용어 사전 --------------------

-- 용어사전
CREATE TABLE IF NOT EXISTS glossary (
    term_id     BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '용어 번호(PK)',
    term        VARCHAR(100)    NOT NULL COMMENT '용어명',
    definition  TEXT            NOT NULL COMMENT '용어 정의 및 설명',
    category    VARCHAR(20)     NOT NULL COMMENT '구분(전문용어/은어 등)',

    CONSTRAINT PK_GLOSSARY PRIMARY KEY (term_id),
    CONSTRAINT UQ_GLOSSARY_TERM UNIQUE (term),
    INDEX IDX_GLOSSARY_CATEGORY (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='주식 용어 사전';

-- 관리자계정 생성
-- 아직 관리자에 대한 특별한 권한이 없음
-- INSERT INTO members (member_id, member_pwd, member_name, nickname, email)
-- VALUES ('admin01', 'StockHub1!', '관리자', '스톡허브', 'admin01@kh.co.kr');

SHOW TABLES;