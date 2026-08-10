-- stockhub DB 생성
CREATE DATABASE IF NOT EXISTS stockhub
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- stockhub DB 선택
USE stockhub;

-- MySQL과 데이터를 주고 받는 인코딩 설정
SET NAMES utf8mb4;

-- 안전 업데이트 모드 비활성화
SET SQL_SAFE_UPDATES = 0;

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
    member_role VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '회원 권한(USER/ADMIN)',
	rank_badge    TINYINT UNSIGNED NULL DEFAULT NULL COMMENT '랭킹 배지(1: 1위, 2: 2위, 3: 3위, NULL: 일반 회원)',
    member_status VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE' COMMENT '회원 상태(ACTIVE/RESTRICTED)',
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

-- 기존 member 테이블에도 이용 상태 컬럼이 없을 때만 추가
SET @add_member_status_sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE member ADD COLUMN member_status VARCHAR(20) NOT NULL DEFAULT ''ACTIVE'' COMMENT ''회원 상태(ACTIVE/RESTRICTED)'' AFTER member_role',
        'SELECT 1'
    )
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'member'
      AND column_name = 'member_status'
);
PREPARE add_member_status_stmt FROM @add_member_status_sql;
EXECUTE add_member_status_stmt;
DEALLOCATE PREPARE add_member_status_stmt;

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
    is_stock_public  BOOLEAN     NOT NULL DEFAULT FALSE COMMENT '내 주식 공개 여부',
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
-- 이 섹션은 대부분 "외부/파트너 소유" 데이터다(증권사가 갖고 있다고 가정하는 정보).
-- 우리(웹사이트) 소유인 테이블은 그때그때 별도로 표시한다(예: account_link).

-- 주식 (※ 외부/파트너 소유 영역)
CREATE TABLE IF NOT EXISTS stock (
    stock_code     VARCHAR(20)      NOT NULL COMMENT '종목 코드(PK, 예: NVDA)',
    stock_name     VARCHAR(100)     NOT NULL COMMENT '종목 이름',
    exchange       VARCHAR(20)      NULL COMMENT '거래소 코드(NASDAQ/NYSE 등). 국내 종목은 NULL — 트레이딩뷰 무료 위젯이 KRX 데이터를 지원하지 않아 검색 대상에서 제외',
    description    TEXT             NULL COMMENT '기업 정보 및 설명',
    listing_date   DATE             NULL COMMENT '상장일',
    stock_value    BIGINT UNSIGNED  NULL COMMENT '시가총액',
    stock_total    BIGINT UNSIGNED  NULL COMMENT '상장 주식 수',
    current_price  BIGINT UNSIGNED  NOT NULL DEFAULT 0 COMMENT '현재가',
    news           VARCHAR(500)     NULL COMMENT '대표 뉴스 또는 뉴스 요약',

    CONSTRAINT PK_STOCK PRIMARY KEY (stock_code),
    INDEX IDX_STOCK_NAME (stock_name),
    INDEX IDX_STOCK_EXCHANGE (exchange)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='주식 종목';

-- 기존 stock 테이블에도 거래소 컬럼이 없을 때만 추가
SET @add_stock_exchange_sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE stock ADD COLUMN exchange VARCHAR(20) NULL COMMENT ''거래소 코드(NASDAQ/NYSE 등, 국내 종목은 NULL)'' AFTER stock_name, ADD INDEX IDX_STOCK_EXCHANGE (exchange)',
        'SELECT 1'
    )
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'stock'
      AND column_name = 'exchange'
);
PREPARE add_stock_exchange_stmt FROM @add_stock_exchange_sql;
EXECUTE add_stock_exchange_stmt;
DEALLOCATE PREPARE add_stock_exchange_stmt;

-- 종목 검색 자동완성 대상 해외 종목 시드 (기존 8개 upsert + 신규 약 47개, 총 55개)
-- 국내 종목(NAVER/SK하이닉스/삼성전자/카카오)은 건드리지 않으므로 exchange가 계속 NULL로 남아 검색 대상에서 자동 제외됨
INSERT INTO `stock` (`stock_code`, `stock_name`, `exchange`)
VALUES
    ('AAPL', 'Apple', 'NASDAQ'),
    ('MSFT', 'Microsoft', 'NASDAQ'),
    ('NVDA', 'NVIDIA', 'NASDAQ'),
    ('TSLA', 'Tesla', 'NASDAQ'),
    ('GOOGL', 'Alphabet', 'NASDAQ'),
    ('AMZN', 'Amazon', 'NASDAQ'),
    ('META', 'Meta Platforms', 'NASDAQ'),
    ('NFLX', 'Netflix', 'NASDAQ'),
    ('AVGO', 'Broadcom', 'NASDAQ'),
    ('COST', 'Costco', 'NASDAQ'),
    ('PEP', 'PepsiCo', 'NASDAQ'),
    ('ADBE', 'Adobe', 'NASDAQ'),
    ('CSCO', 'Cisco', 'NASDAQ'),
    ('INTC', 'Intel', 'NASDAQ'),
    ('AMD', 'AMD', 'NASDAQ'),
    ('QCOM', 'Qualcomm', 'NASDAQ'),
    ('TXN', 'Texas Instruments', 'NASDAQ'),
    ('INTU', 'Intuit', 'NASDAQ'),
    ('AMAT', 'Applied Materials', 'NASDAQ'),
    ('BKNG', 'Booking Holdings', 'NASDAQ'),
    ('SBUX', 'Starbucks', 'NASDAQ'),
    ('GILD', 'Gilead Sciences', 'NASDAQ'),
    ('MDLZ', 'Mondelez', 'NASDAQ'),
    ('ADP', 'ADP', 'NASDAQ'),
    ('ISRG', 'Intuitive Surgical', 'NASDAQ'),
    ('REGN', 'Regeneron', 'NASDAQ'),
    ('VRTX', 'Vertex Pharmaceuticals', 'NASDAQ'),
    ('PANW', 'Palo Alto Networks', 'NASDAQ'),
    ('PYPL', 'PayPal', 'NASDAQ'),
    ('MU', 'Micron Technology', 'NASDAQ'),
    ('JPM', 'JPMorgan Chase', 'NYSE'),
    ('V', 'Visa', 'NYSE'),
    ('MA', 'Mastercard', 'NYSE'),
    ('JNJ', 'Johnson & Johnson', 'NYSE'),
    ('WMT', 'Walmart', 'NYSE'),
    ('PG', 'Procter & Gamble', 'NYSE'),
    ('HD', 'Home Depot', 'NYSE'),
    ('XOM', 'Exxon Mobil', 'NYSE'),
    ('BAC', 'Bank of America', 'NYSE'),
    ('KO', 'Coca-Cola', 'NYSE'),
    ('DIS', 'Disney', 'NYSE'),
    ('CVX', 'Chevron', 'NYSE'),
    ('ABBV', 'AbbVie', 'NYSE'),
    ('MRK', 'Merck', 'NYSE'),
    ('PFE', 'Pfizer', 'NYSE'),
    ('CRM', 'Salesforce', 'NYSE'),
    ('ORCL', 'Oracle', 'NYSE'),
    ('MCD', 'McDonald''s', 'NYSE'),
    ('NKE', 'Nike', 'NYSE'),
    ('WFC', 'Wells Fargo', 'NYSE'),
    ('T', 'AT&T', 'NYSE'),
    ('VZ', 'Verizon', 'NYSE'),
    ('UNH', 'UnitedHealth Group', 'NYSE'),
    ('IBM', 'IBM', 'NYSE'),
    ('GE', 'General Electric', 'NYSE'),
    ('CAT', 'Caterpillar', 'NYSE'),
    ('BA', 'Boeing', 'NYSE')
ON DUPLICATE KEY UPDATE
    `stock_name` = VALUES(`stock_name`),
    `exchange` = VALUES(`exchange`);

-- 시가총액 상위 종목 추가 시드 (위 55개와 합쳐 약 100개 종목). 웹 검색으로 확인한 티커만 반영했고,
-- 비상장/실제 거래 안 되는 종목(예: SpaceX)이나 미국 상장 여부가 불확실한 종목은 제외함
INSERT INTO `stock` (`stock_code`, `stock_name`, `exchange`)
VALUES
    ('TSM', 'Taiwan Semiconductor', 'NYSE'),
    ('BRK.B', 'Berkshire Hathaway', 'NYSE'),
    ('LLY', 'Eli Lilly', 'NYSE'),
    ('PLTR', 'Palantir Technologies', 'NASDAQ'),
    ('LRCX', 'Lam Research', 'NASDAQ'),
    ('HSBC', 'HSBC Holdings', 'NYSE'),
    ('MS', 'Morgan Stanley', 'NYSE'),
    ('GS', 'Goldman Sachs', 'NYSE'),
    ('ARM', 'Arm Holdings', 'NASDAQ'),
    ('RTX', 'RTX Corporation', 'NYSE'),
    ('NVS', 'Novartis', 'NYSE'),
    ('PM', 'Philip Morris International', 'NYSE'),
    ('RY', 'Royal Bank of Canada', 'NYSE'),
    ('DELL', 'Dell Technologies', 'NYSE'),
    ('BABA', 'Alibaba Group', 'NYSE'),
    ('GEV', 'GE Vernova', 'NYSE'),
    ('KLAC', 'KLA Corporation', 'NASDAQ'),
    ('MUFG', 'Mitsubishi UFJ Financial Group', 'NYSE'),
    ('AZN', 'AstraZeneca', 'NASDAQ'),
    ('SHEL', 'Shell', 'NYSE'),
    ('ANET', 'Arista Networks', 'NYSE'),
    ('SAP', 'SAP', 'NYSE'),
    ('AXP', 'American Express', 'NYSE'),
    ('C', 'Citigroup', 'NYSE'),
    ('BHP', 'BHP Group', 'NYSE'),
    ('LIN', 'Linde', 'NASDAQ'),
    ('TM', 'Toyota Motor', 'NYSE'),
    ('AMGN', 'Amgen', 'NASDAQ'),
    ('TMO', 'Thermo Fisher Scientific', 'NYSE'),
    ('CRWD', 'CrowdStrike', 'NASDAQ'),
    ('SAN', 'Banco Santander', 'NYSE'),
    ('NVO', 'Novo Nordisk', 'NYSE'),
    ('APH', 'Amphenol', 'NYSE'),
    ('TD', 'Toronto-Dominion Bank', 'NYSE'),
    ('SHOP', 'Shopify', 'NYSE'),
    ('MRVL', 'Marvell Technology', 'NASDAQ'),
    ('TTE', 'TotalEnergies', 'NYSE'),
    ('TMUS', 'T-Mobile US', 'NASDAQ'),
    ('ADI', 'Analog Devices', 'NASDAQ'),
    ('ABT', 'Abbott Laboratories', 'NYSE'),
    ('SCHW', 'Charles Schwab', 'NYSE'),
    ('BLK', 'BlackRock', 'NYSE'),
    ('STX', 'Seagate Technology', 'NASDAQ'),
    ('SNDK', 'Sandisk', 'NASDAQ'),
    ('TJX', 'TJX Companies', 'NYSE'),
    ('NEE', 'NextEra Energy', 'NYSE'),
    ('ETN', 'Eaton', 'NYSE'),
    ('UNP', 'Union Pacific', 'NYSE'),
    ('RIO', 'Rio Tinto', 'NYSE'),
    ('WELL', 'Welltower', 'NYSE'),
    ('BX', 'Blackstone', 'NYSE'),
    ('DE', 'Deere & Company', 'NYSE'),
    ('SCCO', 'Southern Copper', 'NYSE'),
    ('BUD', 'Anheuser-Busch InBev', 'NYSE'),
    ('ASML', 'ASML Holding', 'NASDAQ')
ON DUPLICATE KEY UPDATE
    `stock_name` = VALUES(`stock_name`),
    `exchange` = VALUES(`exchange`);

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

-- 가상 계좌 (※ 외부/파트너 소유 영역 — 증권사가 갖고 있는 데이터. 우리 회원 정보는 여기 두지 않는다. 연동 관계는 account_link 참고)
CREATE TABLE IF NOT EXISTS account (
    account_id    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '가상 계좌 번호(PK)',
    account_no    VARCHAR(50)     NOT NULL COMMENT '사용자에게 표시할 계좌번호',
    brokerage_id  BIGINT UNSIGNED NOT NULL COMMENT '계좌를 개설한 증권사(FK)',
    owner_name    VARCHAR(50)     NOT NULL COMMENT '예금주명',
	balance BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '현금 잔고(기본 0원)',
    create_at     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '계좌 개설일시',
	return_rate             DECIMAL(9, 4)   NOT NULL DEFAULT 0.0000 COMMENT '현재 수익률(%)',
	profit_amount           BIGINT           NOT NULL DEFAULT 0 COMMENT '현재 수익금(원)',
	holding_stock_quantity  BIGINT UNSIGNED  NOT NULL DEFAULT 0 COMMENT '총 보유 주식 수량',

    CONSTRAINT PK_ACCOUNT PRIMARY KEY (account_id),
    CONSTRAINT UQ_ACCOUNT_NO UNIQUE (account_no),
    CONSTRAINT FK_BROKERAGE_TO_ACCOUNT
        FOREIGN KEY (brokerage_id) REFERENCES brokerage (brokerage_id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='가상 증권 계좌';

-- 회원-계좌 연동 (※ 우리(웹사이트) 소유 영역 — "어느 회원이 어느 증권사 계좌를 검증하고 연동했는가"만 담는다.
-- account_no/owner_name으로 본인확인 후 여기 한 행이 생기며, 그 전까지 계좌는 어떤 회원과도 연결되지 않은 상태다.
-- 회원 탈퇴시에도 이 테이블만 정리하고 account/holding/trade 등 증권사측 데이터는 그대로 보존한다.
CREATE TABLE IF NOT EXISTS account_link (
    link_id     BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '연동 번호(PK)',
    member_id   VARCHAR(50)     NOT NULL COMMENT '연동한 회원(FK)',
    account_id  BIGINT UNSIGNED NOT NULL COMMENT '연동된 계좌(FK)',
    linked_at   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '연동 일시',

    CONSTRAINT PK_ACCOUNT_LINK PRIMARY KEY (link_id),
    CONSTRAINT UQ_ACCOUNT_LINK_ACCOUNT UNIQUE (account_id),
    CONSTRAINT UQ_ACCOUNT_LINK_MEMBER_ACCOUNT UNIQUE (member_id, account_id),
    CONSTRAINT FK_MEMBER_TO_ACCOUNT_LINK
        FOREIGN KEY (member_id) REFERENCES member (member_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT FK_ACCOUNT_TO_ACCOUNT_LINK
        FOREIGN KEY (account_id) REFERENCES account (account_id)
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='회원-계좌 연동(우리 사이트 소유)';

-- 보유 종목 (※ 외부/파트너 소유 영역)
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

-- 거래 체결 이력 (※ 외부/파트너 소유 영역)
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

-- 종목별 상승/하락 의견 투표 ("살까?팔까?" 실시간 투표)
CREATE TABLE IF NOT EXISTS stock_vote (
    vote_id     BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '투표 번호(PK)',
    stock_code  VARCHAR(20)     NOT NULL COMMENT '투표 대상 종목(FK)',
    member_id   VARCHAR(50)     NOT NULL COMMENT '투표한 회원(FK)',
    vote_type   ENUM('UP', 'DOWN') NOT NULL COMMENT '상승/하락 의견',
    voted_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP COMMENT '투표(의견 변경 포함) 일시',

    CONSTRAINT PK_STOCK_VOTE PRIMARY KEY (vote_id),
    CONSTRAINT UQ_STOCK_VOTE_MEMBER UNIQUE (stock_code, member_id),
    CONSTRAINT FK_STOCK_TO_STOCK_VOTE
        FOREIGN KEY (stock_code) REFERENCES stock (stock_code)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT FK_MEMBER_TO_STOCK_VOTE
        FOREIGN KEY (member_id) REFERENCES member (member_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    INDEX IDX_STOCK_VOTE_STOCK (stock_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='종목별 상승/하락 의견 투표';

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
    return_rate_snapshot      DECIMAL(9, 4)   NOT NULL COMMENT '수익률(%)',
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

-- 증권사 전용 상품 (※ 외부/파트너 소유 영역)
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

-- 계좌별 금융상품 보유내역 (holding의 상품 버전) (※ 외부/파트너 소유 영역)
CREATE TABLE IF NOT EXISTS product_holding (
    product_holding_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '상품보유 번호(PK)',
    account_id      BIGINT UNSIGNED NOT NULL COMMENT '가상 계좌(FK)',
    product_id      BIGINT UNSIGNED NOT NULL COMMENT '금융상품(FK)',
    quantity        DECIMAL(18, 4)  NOT NULL DEFAULT 0 COMMENT '보유 좌수(펀드/채권은 소수 단위 가능)',
    avg_nav         DECIMAL(18, 2)  NOT NULL DEFAULT 0 COMMENT '평균 매입 기준가',
    purchase_amount BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '누적 매입원금',
    update_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '최종 갱신일시',

    CONSTRAINT PK_PRODUCT_HOLDING PRIMARY KEY (product_holding_id),
    CONSTRAINT UQ_PRODUCT_HOLDING_ACCOUNT_PRODUCT UNIQUE (account_id, product_id),
    CONSTRAINT FK_ACCOUNT_TO_PRODUCT_HOLDING
        FOREIGN KEY (account_id) REFERENCES account (account_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT FK_PRODUCT_TO_PRODUCT_HOLDING
        FOREIGN KEY (product_id) REFERENCES financial_product (product_id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='계좌별 금융상품 보유내역';

-- 금융상품 가입·환매 원장 (trade의 상품 버전) (※ 외부/파트너 소유 영역)
CREATE TABLE IF NOT EXISTS product_transaction (
    transaction_id   BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '상품거래내역 번호(PK)',
    account_id       BIGINT UNSIGNED NOT NULL COMMENT '가상 계좌(FK)',
    product_id       BIGINT UNSIGNED NOT NULL COMMENT '금융상품(FK)',
    transaction_type ENUM('SUBSCRIBE', 'REDEEM') NOT NULL COMMENT '가입/환매 구분',
    quantity         DECIMAL(18, 4)  NOT NULL COMMENT '거래 좌수',
    nav              DECIMAL(18, 2)  NOT NULL COMMENT '거래 시점 기준가',
    amount           BIGINT UNSIGNED NOT NULL COMMENT '거래금액',
    transaction_at   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '거래일시',

    CONSTRAINT PK_PRODUCT_TRANSACTION PRIMARY KEY (transaction_id),
    CONSTRAINT CK_PRODUCT_TX_QUANTITY CHECK (quantity > 0),
    CONSTRAINT FK_ACCOUNT_TO_PRODUCT_TRANSACTION
        FOREIGN KEY (account_id) REFERENCES account (account_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT FK_PRODUCT_TO_PRODUCT_TRANSACTION
        FOREIGN KEY (product_id) REFERENCES financial_product (product_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    INDEX IDX_PRODUCT_TX_ACCOUNT_DATE (account_id, transaction_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='금융상품 가입·환매 원장';

-- 계좌 입출금 원장 (※ 외부/파트너 소유 영역)
CREATE TABLE IF NOT EXISTS cash_transaction (
    cash_transaction_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '입출금내역 번호(PK)',
    account_id          BIGINT UNSIGNED NOT NULL COMMENT '가상 계좌(FK)',
    transaction_type    ENUM('DEPOSIT', 'WITHDRAWAL') NOT NULL COMMENT '입금/출금 구분',
    amount              BIGINT UNSIGNED NOT NULL COMMENT '입출금액',
    balance_after       BIGINT UNSIGNED NOT NULL COMMENT '처리 후 잔고 스냅샷(명세서 조회용)',
    memo                VARCHAR(200)    NULL COMMENT '메모(예: 초기 입금, 생활비 출금)',
    transaction_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '입출금일시',

    CONSTRAINT PK_CASH_TRANSACTION PRIMARY KEY (cash_transaction_id),
    CONSTRAINT CK_CASH_TX_AMOUNT CHECK (amount > 0),
    CONSTRAINT FK_ACCOUNT_TO_CASH_TRANSACTION
        FOREIGN KEY (account_id) REFERENCES account (account_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    INDEX IDX_CASH_TX_ACCOUNT_DATE (account_id, transaction_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='계좌 입출금 원장';

-- 계좌별 일자별 총자산 스냅샷 (※ 외부/파트너 소유 영역 - 기간별 손익 계산용)
CREATE TABLE IF NOT EXISTS asset_snapshot (
    snapshot_id   BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '스냅샷 번호(PK)',
    account_id    BIGINT UNSIGNED NOT NULL COMMENT '가상 계좌(FK)',
    snapshot_date DATE            NOT NULL COMMENT '스냅샷 기준일',
    total_asset   BIGINT          NOT NULL COMMENT '그 날짜 기준 총자산(현금잔고+평가금액)',
    create_at     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',

    CONSTRAINT PK_ASSET_SNAPSHOT PRIMARY KEY (snapshot_id),
    CONSTRAINT UQ_ASSET_SNAPSHOT_ACCOUNT_DATE UNIQUE (account_id, snapshot_date),
    CONSTRAINT FK_ACCOUNT_TO_ASSET_SNAPSHOT
        FOREIGN KEY (account_id) REFERENCES account (account_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    INDEX IDX_ASSET_SNAPSHOT_ACCOUNT_DATE (account_id, snapshot_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='계좌별 일자별 총자산 스냅샷(기간별 손익 계산용)';

-- 회원 목표 (※ 우리(웹사이트) 소유 영역 - 대시보드 도달률 표시용, 증권사와 무관한 우리 사이트 자체 기능)
CREATE TABLE IF NOT EXISTS goal (
    goal_id      BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '목표 번호(PK)',
    member_id    VARCHAR(50)     NOT NULL COMMENT '목표를 설정한 회원(FK)',
    goal_type    ENUM('RETURN_RATE', 'PROFIT_AMOUNT') NOT NULL COMMENT '목표 종류(수익률 % / 수익금 원)',
    title        VARCHAR(100)    NOT NULL COMMENT '목표 이름(예: 이번 달 +5%, 30만원 모으기)',
    target_value DECIMAL(18, 2)  NOT NULL COMMENT '목표치(수익률=%, 수익금=원)',
    target_date  DATE            NULL COMMENT '목표 기한(없으면 무기한)',
    create_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '목표 설정일시',

    CONSTRAINT PK_GOAL PRIMARY KEY (goal_id),
    CONSTRAINT CK_GOAL_TARGET CHECK (target_value > 0),
    CONSTRAINT FK_MEMBER_TO_GOAL
        FOREIGN KEY (member_id) REFERENCES member (member_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    INDEX IDX_GOAL_MEMBER_DATE (member_id, create_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='회원 목표(대시보드 도달률 표시용)';

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
    is_hidden   BOOLEAN         NOT NULL DEFAULT FALSE COMMENT '관리자 숨김 여부',
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

-- 기존 board 테이블에도 관리자 숨김 컬럼이 없을 때만 추가
SET @add_board_hidden_sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE board ADD COLUMN is_hidden BOOLEAN NOT NULL DEFAULT FALSE COMMENT ''관리자 숨김 여부'' AFTER like_count',
        'SELECT 1'
    )
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'board'
      AND column_name = 'is_hidden'
);
PREPARE add_board_hidden_stmt FROM @add_board_hidden_sql;
EXECUTE add_board_hidden_stmt;
DEALLOCATE PREPARE add_board_hidden_stmt;

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
    is_hidden          BOOLEAN         NOT NULL DEFAULT FALSE COMMENT '관리자 숨김 여부',
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

-- 기존 board_comment 테이블에도 관리자 숨김 컬럼이 없을 때만 추가
SET @add_comment_hidden_sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE board_comment ADD COLUMN is_hidden BOOLEAN NOT NULL DEFAULT FALSE COMMENT ''관리자 숨김 여부'' AFTER content',
        'SELECT 1'
    )
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'board_comment'
      AND column_name = 'is_hidden'
);
PREPARE add_comment_hidden_stmt FROM @add_comment_hidden_sql;
EXECUTE add_comment_hidden_stmt;
DEALLOCATE PREPARE add_comment_hidden_stmt;

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

-- 관리자 작업 이력
CREATE TABLE IF NOT EXISTS admin_log (
    log_id       BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '관리 이력 번호(PK)',
    admin_id     VARCHAR(50)     NULL COMMENT '작업 관리자(FK, 탈퇴 시 NULL)',
    action_type  VARCHAR(30)     NOT NULL COMMENT '작업 유형',
    target_type  VARCHAR(30)     NOT NULL COMMENT '대상 유형',
    target_id    VARCHAR(100)    NULL COMMENT '대상 식별값',
    detail       VARCHAR(500)    NULL COMMENT '작업 상세 내용',
    create_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '작업 일시',

    CONSTRAINT PK_ADMIN_LOG PRIMARY KEY (log_id),
    CONSTRAINT FK_MEMBER_TO_ADMIN_LOG
        FOREIGN KEY (admin_id) REFERENCES member (member_id)
        ON UPDATE CASCADE ON DELETE SET NULL,
    INDEX IDX_ADMIN_LOG_DATE (create_at),
    INDEX IDX_ADMIN_LOG_ADMIN_DATE (admin_id, create_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='관리자 작업 이력';

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

START TRANSACTION;

-- ============================================================
-- 매매와 투자 행동 / category = 'trading'
-- 41개
-- ============================================================
INSERT INTO `glossary` (`term`, `definition`, `category`)
VALUES
    ('평균매입단가', '같은 주식을 여러 번 다른 가격에 샀을 때, 내가 주식 한 개를 평균적으로 얼마에 샀는지 보여주는 가격이에요.', 'trading'),
    ('상승장', '주식 가격이 계속 오르거나, 앞으로 오를 것이라고 기대하는 시장이에요.', 'trading'),
    ('하락장', '주식 가격이 계속 내리거나, 앞으로 내릴 것이라고 예상하는 시장이에요.', 'trading'),
    ('반등', '내려가던 주식 가격이 다시 올라가는 움직임이에요. 잠깐 올랐다가 다시 내려갈 수도 있기 때문에 계속 오를 것이라는 뜻은 아니에요.', 'trading'),
    ('익절', '내가 산 가격보다 더 비싼 가격에 주식을 팔아서 이익을 확정하는 일이에요. ‘이익 실현’을 줄여서 부르는 말이에요.', 'trading'),
    ('손절', '손실이 더 커지는 것을 막기 위해 내가 산 가격보다 낮은 가격에 주식을 파는 일이에요.', 'trading'),
    ('단타', '주식을 산 뒤 몇 분, 몇 시간 또는 며칠 안에 빠르게 파는 거래 방법이에요. 짧은 시간 동안 생기는 가격 차이로 돈을 벌려고 하는 방식이에요.', 'trading'),
    ('스윙', '주식을 산 뒤 며칠에서 몇 주, 길게는 몇 달 동안 가지고 있다가 파는 거래 방법이에요. 단타보다 긴 가격 움직임을 이용하는 방식이에요.', 'trading'),
    ('장기 투자', '좋은 회사의 주식을 오랫동안 가지고 있는 투자 방법이에요. 보통 몇 년 이상 회사가 성장하기를 기다리는 방식이에요.', 'trading'),
    ('개미털기', '주식 가격이 갑자기 크게 떨어져 개인 투자자들이 겁을 먹고 주식을 팔았는데, 이후 가격이 다시 오르는 상황을 표현하는 말이에요. 실제로 누군가 일부러 가격을 내렸다는 뜻으로 단정할 수는 없어요.', 'trading'),
    ('수급', '개인, 외국인, 기관처럼 여러 투자자가 주식을 얼마나 사고팔고 있는지 보여주는 돈의 흐름이에요.', 'trading'),
    ('유동성', '주식을 사고 싶을 때 쉽게 사고, 팔고 싶을 때 쉽게 팔 수 있는 정도예요. 사고파는 사람이 많을수록 유동성이 높다고 해요.', 'trading'),
    ('상승세', '주식 가격이 계속해서 올라가는 흐름이에요.', 'trading'),
    ('하락세', '주식 가격이 계속해서 내려가는 흐름이에요.', 'trading'),
    ('횡보', '주식 가격이 크게 오르거나 내리지 않고 비슷한 가격에서 움직이는 상태예요.', 'trading'),
    ('추세', '주식 가격이 일정한 방향으로 움직이는 전체적인 흐름이에요. 상승 추세, 하락 추세, 횡보 추세가 있어요.', 'trading'),
    ('분할매수', '주식을 한 번에 모두 사지 않고 여러 번 나누어 사는 방법이에요.', 'trading'),
    ('분할매도', '가지고 있는 주식을 한 번에 모두 팔지 않고 여러 번 나누어 파는 방법이에요.', 'trading'),
    ('변동성', '주식 가격이 얼마나 크고 빠르게 오르내리는지를 나타내는 말이에요. 변동성이 크면 돈을 많이 벌 수도 있지만 많이 잃을 수도 있어요.', 'trading'),
    ('목표가', '투자자가 주식이 앞으로 오를 것이라고 예상해 정해둔 가격이에요.', 'trading'),
    ('손절가', '손실이 더 커지는 것을 막기 위해 주식을 팔기로 미리 정해둔 가격이에요.', 'trading'),
    ('익절가', '원하는 이익을 얻었을 때 주식을 팔기로 미리 정해둔 가격이에요.', 'trading'),
    ('물타기', '내가 산 주식의 가격이 내려갔을 때 주식을 더 사서 평균 매입가격을 낮추는 방법이에요. 가격이 계속 내려가면 손실이 더 커질 수 있어요.', 'trading'),
    ('불타기', '내가 산 주식의 가격이 오른 뒤 주식을 더 사는 방법이에요. 가격이 계속 오르면 수익이 커질 수 있지만, 갑자기 내려가면 손실이 커질 수도 있어요.', 'trading'),
    ('몰빵', '가지고 있는 돈 대부분을 한 종목에 투자하는 행동이에요. 큰 수익을 얻을 수도 있지만 큰 손실이 생길 위험도 매우 높아요.', 'trading'),
    ('존버', '주식 가격이 내려가도 팔지 않고 오를 때까지 오랫동안 기다리는 행동을 뜻하는 은어예요.', 'trading'),
    ('손절라인', '손실이 더 커지기 전에 주식을 팔기로 미리 정해둔 가격이에요.', 'trading'),
    ('익절라인', '원하는 이익을 얻었을 때 주식을 팔기로 미리 정해둔 가격이에요.', 'trading'),
    ('추격매수', '주식 가격이 빠르게 오르는 것을 보고 뒤늦게 따라 사는 행동이에요. 너무 높은 가격에 살 위험이 있어요.', 'trading'),
    ('패닉셀', '주식 가격이 갑자기 크게 떨어질 때 겁을 먹고 급하게 파는 행동이에요.', 'trading'),
    ('뇌동매매', '자신의 계획 없이 다른 사람의 말이나 갑작스러운 가격 움직임만 보고 따라 거래하는 행동이에요.', 'trading'),
    ('물리다', '주식 가격이 많이 오른 상태에서 샀는데, 이후 가격이 내려가 손실을 보고 있는 상황이에요.', 'trading'),
    ('본전', '투자한 돈만큼 다시 돌아와서 이익도 손실도 없는 상태예요.', 'trading'),
    ('본절', '손실을 보던 주식이 다시 내가 산 가격 근처까지 왔을 때 팔아서 손실 없이 거래를 끝내는 일이에요.', 'trading'),
    ('리스크 관리', '한 번의 거래나 전체 투자에서 감당할 수 있는 손실 범위를 미리 정하고 지키는 일이에요.', 'trading'),
    ('손익비', '예상하는 손실과 기대하는 수익의 크기를 비교한 비율이에요. 예를 들어 1을 잃을 위험을 감수하고 2를 벌려고 한다면 손익비를 1대 2라고 표현해요.', 'trading'),
    ('포지션 사이징', '손절 가격과 감당 가능한 손실액을 기준으로 한 번에 몇 주를 살지 정하는 과정이에요.', 'trading'),
    ('분산투자', '한 종목이나 한 자산에 돈을 모두 넣지 않고 여러 종목이나 자산에 나누어 투자하는 방법이에요.', 'trading'),
    ('추세추종', '이미 확인된 상승 또는 하락 흐름과 같은 방향으로 거래하려는 방법이에요. 추세가 끝나면 손실이 생길 수 있어요.', 'trading'),
    ('역추세 매매', '현재 이어지고 있는 가격 흐름이 곧 반대로 바뀔 것이라고 예상하고 기존 추세와 반대 방향으로 거래하는 방법이에요.', 'trading'),
    ('매매 원칙', '언제 사고팔지, 얼마를 투자할지, 어디에서 손절할지처럼 거래 전에 정해 두는 개인적인 기준이에요.', 'trading')
ON DUPLICATE KEY UPDATE
    `definition` = VALUES(`definition`),
    `category` = VALUES(`category`);

-- ============================================================
-- 투자자·자금·손익 관리 / category = 'risk-management'
-- 31개
-- ============================================================
INSERT INTO `glossary` (`term`, `definition`, `category`)
VALUES
    ('개인 투자자', '회사나 금융기관이 아니라 자신의 돈으로 직접 주식에 투자하는 일반 사람을 말해요. 흔히들 ‘개미’라고도 불러요.', 'risk-management'),
    ('기관 투자자', '연기금, 보험사, 자산운용사, 증권사처럼 아주 큰돈을 전문적으로 투자하는 회사를 말해요.', 'risk-management'),
    ('외국인 투자자·외인', '우리나라 주식시장에 투자하는 외국 사람이나 외국 회사를 말해요.', 'risk-management'),
    ('신용거래', '증권사에서 돈을 빌려 주식을 사는 거래예요. 빌린 돈에는 이자가 붙고, 손실이 커지면 증권사가 주식을 강제로 팔 수도 있어요.', 'risk-management'),
    ('미수거래', '주식을 살 때 필요한 돈을 모두 내지 않고 일부만 먼저 낸 뒤, 나머지 돈을 며칠 안에 내는 거래예요. 제때 돈을 내지 못하면 주식이 강제로 팔릴 수 있어요.', 'risk-management'),
    ('예수금', '증권계좌에 들어 있는 현금이에요. 다만 이미 주식을 사는 데 사용될 예정인 돈이 있으면 화면에 보이는 금액을 전부 사용할 수 없을 수도 있어요.', 'risk-management'),
    ('증거금', '큰 금액의 거래를 할 때 약속을 지킬 수 있다는 것을 보여주기 위해 미리 맡겨두는 돈이에요.', 'risk-management'),
    ('담보', '돈을 빌렸을 때 갚지 못할 경우를 대비해 맡겨두는 돈이나 주식이에요.', 'risk-management'),
    ('청산', '가지고 있던 주식이나 거래를 팔아서 끝내는 일이에요.', 'risk-management'),
    ('강제청산', '손실이 너무 커져서 거래소나 증권사가 투자자의 거래를 강제로 끝내는 일이에요.', 'risk-management'),
    ('반대매매', '빌린 돈을 갚지 못하거나 계좌의 돈이 부족할 때 증권사가 투자자의 주식을 강제로 파는 일이에요.', 'risk-management'),
    ('마진콜', '손실이 커졌으니 계좌에 돈을 더 넣으라고 증권사나 거래소가 알리는 일이에요. 돈을 넣지 않으면 강제청산될 수 있어요.', 'risk-management'),
    ('보유수량', '현재 내가 가지고 있는 주식의 개수예요.', 'risk-management'),
    ('주문가능금액', '현재 증권계좌에서 새 주식을 사는 데 사용할 수 있는 돈이에요.', 'risk-management'),
    ('평가금액', '내가 가진 주식을 현재 가격으로 모두 계산했을 때의 금액이에요.', 'risk-management'),
    ('평가손익', '지금 주식을 판다고 생각했을 때 예상되는 이익이나 손실이에요. 아직 실제로 판 것은 아니기 때문에 금액이 계속 바뀔 수 있어요.', 'risk-management'),
    ('실현손익', '주식을 실제로 팔아서 확정된 이익이나 손실이에요.', 'risk-management'),
    ('미실현손익', '아직 팔지 않은 주식에서 현재 나타나고 있는 이익이나 손실이에요. 평가손익이라고도 해요.', 'risk-management'),
    ('수익률', '투자한 돈에 비해 얼마를 벌거나 잃었는지 퍼센트로 보여주는 숫자예요.', 'risk-management'),
    ('수수료', '주식을 사고팔 때 증권사나 거래소에 내는 이용료예요.', 'risk-management'),
    ('세금', '주식을 팔거나 배당금을 받을 때 나라에 내야 할 수 있는 돈이에요. 국가와 상품에 따라 달라요.', 'risk-management'),
    ('손익분기점', '수수료와 세금까지 생각했을 때 이익도 손실도 없는 가격이에요.', 'risk-management'),
    ('순매수', '일정한 기간 동안 판 금액보다 산 금액이 더 많은 상태예요.', 'risk-management'),
    ('순매도', '일정한 기간 동안 산 금액보다 판 금액이 더 많은 상태예요.', 'risk-management'),
    ('가용자금', '현재 투자나 주문에 실제로 사용할 수 있는 돈이에요. 계좌에 보이는 전체 금액과 다를 수 있어요.', 'risk-management'),
    ('총자산', '현금, 보유 주식의 평가금액 등 계좌 안에 있는 자산을 모두 합친 금액이에요.', 'risk-management'),
    ('투자원금', '투자를 시작하거나 특정 거래에 실제로 넣은 기준 금액이에요.', 'risk-management'),
    ('담보비율', '빌린 돈과 비교해 계좌에 담보로 인정되는 자산이 어느 정도 있는지 보여주는 비율이에요.', 'risk-management'),
    ('유지증거금', '선물이나 신용 거래 등의 포지션을 계속 유지하기 위해 계좌에 남아 있어야 하는 최소한의 증거금이에요.', 'risk-management'),
    ('최대손실액', '한 번의 거래나 일정한 기간 동안 잃어도 된다고 미리 정한 가장 큰 손실 금액이에요.', 'risk-management'),
    ('출금가능금액', '결제 예정 금액이나 증거금 등을 제외하고 현재 계좌에서 실제로 출금할 수 있는 돈이에요.', 'risk-management')
ON DUPLICATE KEY UPDATE
    `definition` = VALUES(`definition`),
    `category` = VALUES(`category`);

-- ============================================================
-- 상품과 포지션 / category = 'position'
-- 19개
-- ============================================================
INSERT INTO `glossary` (`term`, `definition`, `category`)
VALUES
    ('레버리지', '내가 가진 돈보다 더 큰 금액으로 투자하는 방법이에요. 돈을 많이 벌 수도 있지만, 반대로 손실도 더 크게 생길 수 있어요.', 'position'),
    ('인버스', '주가지수나 특정 자산의 가격과 반대 방향으로 움직이도록 만든 상품이에요. 기준이 되는 가격이 내려가면 인버스 상품은 오를 수 있고, 기준 가격이 오르면 인버스 상품은 내릴 수 있어요.', 'position'),
    ('ETF', '여러 회사의 주식이나 채권 등을 하나의 꾸러미처럼 묶어 만든 상품이에요. 일반 주식처럼 쉽게 사고팔 수 있어요.', 'position'),
    ('롱', '주식이나 자산의 가격이 오를 것이라고 생각하고 투자하는 상태예요. 가격이 오르면 이익이 생겨요.', 'position'),
    ('숏', '주식이나 자산의 가격이 내릴 것이라고 생각하고 투자하는 상태예요. 가격이 내리면 이익이 생길 수 있어요.', 'position'),
    ('공매도', '주식을 다른 사람에게 빌려서 먼저 판 뒤, 가격이 내려가면 더 싼 가격에 다시 사서 갚는 거래예요. 예상과 달리 가격이 오르면 손실이 생겨요.', 'position'),
    ('현물', '실제 주식이나 자산을 직접 사서 가지고 있는 거래예요.', 'position'),
    ('선물', '미래의 정해진 날짜나 조건에 자산을 사고팔기로 약속하는 거래예요. 실제 주식보다 가격 움직임에 따른 손익이 크게 나타날 수 있어요.', 'position'),
    ('옵션', '미래에 정해진 가격으로 자산을 사거나 팔 수 있는 권리를 거래하는 상품이에요. 구조가 복잡하고 큰 손실이 생길 수 있어요.', 'position'),
    ('채권', '정부나 회사에 돈을 빌려주고, 정해진 이자와 원금을 돌려받는 투자 상품이에요.', 'position'),
    ('주식', '회사의 일부를 작은 단위로 나눈 권리예요. 주식을 가진 사람은 회사의 주주가 돼요.', 'position'),
    ('보통주', '일반적으로 거래되는 주식으로, 주주총회에서 의결권을 가질 수 있고 회사 상황에 따라 배당을 받을 수 있어요.', 'position'),
    ('우선주', '보통주보다 배당이나 남은 재산 분배에서 우선권을 주는 대신 의결권이 없거나 제한될 수 있는 주식이에요.', 'position'),
    ('펀드', '여러 투자자의 돈을 모아 전문가가 주식, 채권 등에 나누어 투자하는 상품이에요.', 'position'),
    ('ETN', '증권회사가 특정 지수나 자산의 움직임에 따라 수익을 지급하기로 약속한 상장 상품이에요. 발행한 증권회사의 신용 위험도 확인해야 해요.', 'position'),
    ('리츠(REITs)', '여러 투자자의 돈을 모아 부동산이나 부동산 관련 자산에 투자하고 임대료 등의 수익을 나누는 상품이에요.', 'position'),
    ('파생상품', '주식, 지수, 원자재 같은 기초자산의 가격 변화에 따라 가치가 달라지는 상품이에요. 선물과 옵션 등이 포함돼요.', 'position'),
    ('콜 옵션', '정해진 기간 안에 기초자산을 미리 정한 가격으로 살 수 있는 권리예요.', 'position'),
    ('풋 옵션', '정해진 기간 안에 기초자산을 미리 정한 가격으로 팔 수 있는 권리예요.', 'position')
ON DUPLICATE KEY UPDATE
    `definition` = VALUES(`definition`),
    `category` = VALUES(`category`);

-- ============================================================
-- 시장·지수·주문·거래 제도 / category = 'market'
-- 45개
-- ============================================================
INSERT INTO `glossary` (`term`, `definition`, `category`)
VALUES
    ('코스피(KOSPI)', '우리나라의 비교적 크고 유명한 회사들이 많이 모여 있는 주식시장이에요. 이 시장의 전체적인 움직임을 보여주는 숫자도 코스피라고 불러요.', 'market'),
    ('코스닥(KOSDAQ)', '우리나라의 중소기업, 벤처기업, 기술기업이 많이 모여 있는 주식시장이에요. 이 시장의 전체적인 움직임을 보여주는 숫자도 코스닥이라고 불러요.', 'market'),
    ('나스닥(Nasdaq)', '미국에 있는 주식시장이에요. 애플, 마이크로소프트, 엔비디아처럼 기술 관련 회사가 많이 포함되어 있어요.', 'market'),
    ('나스닥 종합지수', '나스닥 시장에 등록된 많은 회사의 주가 움직임을 한눈에 보여주는 숫자예요.', 'market'),
    ('나스닥 100', '나스닥 시장에 등록된 회사 중 금융회사를 제외한 큰 회사 100개의 주가 움직임을 보여주는 숫자예요.', 'market'),
    ('S&P 500', '미국을 대표하는 큰 회사 약 500개의 주가 움직임을 보여주는 숫자예요. 미국 주식시장 전체의 분위기를 살펴볼 때 자주 사용해요.', 'market'),
    ('다우존스 지수', '미국을 대표하는 큰 회사 30개의 주가 움직임을 보여주는 숫자예요.', 'market'),
    ('주가지수', '여러 회사의 주가 움직임을 하나의 숫자로 나타낸 값이에요. 시장 전체가 오르는지 내리는지 쉽게 확인할 수 있어요.', 'market'),
    ('사이드카', '주식시장이 너무 빠르게 움직일 때 컴퓨터가 자동으로 내는 큰 주문을 5분 동안 멈추는 제도예요. 시장이 갑자기 크게 흔들리는 것을 줄이기 위해 사용해요.', 'market'),
    ('서킷브레이커', '주식시장 전체가 매우 크게 떨어질 때 모든 거래를 잠시 멈추는 제도예요. 투자자들이 놀란 상태에서 급하게 거래하는 것을 막기 위한 장치예요.', 'market'),
    ('VI(변동성 완화장치)', '한 종목의 가격이 너무 빠르게 오르거나 내릴 때 거래 속도를 잠시 늦추는 장치예요. 주문을 잠시 모은 뒤 하나의 가격으로 거래해요.', 'market'),
    ('매수', '주식이나 금융상품을 사는 일이에요.', 'market'),
    ('매도', '가지고 있는 주식이나 금융상품을 파는 일이에요.', 'market'),
    ('주문수량', '사고 싶거나 팔고 싶은 주식의 개수예요.', 'market'),
    ('호가', '주식을 얼마에 몇 개 사고 싶은지 또는 팔고 싶은지 나타내는 주문이에요.', 'market'),
    ('매수호가', '주식을 사려는 사람이 제시한 가격과 수량이에요.', 'market'),
    ('매도호가', '주식을 팔려는 사람이 제시한 가격과 수량이에요.', 'market'),
    ('호가창', '사람들이 어떤 가격에 주식을 사고팔려고 하는지 보여주는 화면이에요.', 'market'),
    ('시장가 주문', '가격을 직접 정하지 않고 현재 바로 거래할 수 있는 가격으로 주문하는 방법이에요. 빠르게 거래되지만 생각보다 비싼 가격에 사거나 싼 가격에 팔릴 수 있어요.', 'market'),
    ('지정가 주문', '내가 원하는 가격을 직접 정해서 주문하는 방법이에요. 주식 가격이 내가 정한 가격에 오지 않으면 거래되지 않을 수 있어요.', 'market'),
    ('예약주문', '시장이 열리기 전이나 정해진 시간에 미리 주문을 넣어두는 방법이에요.', 'market'),
    ('조건부 주문', '내가 정한 가격에 도달했을 때 자동으로 주문이 나가도록 설정하는 방법이에요.', 'market'),
    ('체결', '주식을 사려는 주문과 팔려는 주문이 만나 실제 거래가 끝난 상태예요.', 'market'),
    ('미체결', '주문을 넣었지만 아직 사고파는 거래가 끝나지 않은 상태예요.', 'market'),
    ('부분체결', '주문한 주식 중 일부만 거래되고 나머지는 아직 거래되지 않은 상태예요.', 'market'),
    ('주문 취소', '아직 거래되지 않은 주문을 없애는 일이에요.', 'market'),
    ('정정 주문', '아직 거래되지 않은 주문의 가격이나 수량을 바꾸는 일이에요.', 'market'),
    ('거래량', '일정한 시간 동안 사고팔린 주식의 총개수예요.', 'market'),
    ('거래대금', '일정한 시간 동안 주식을 사고파는 데 사용된 돈을 모두 합친 금액이에요.', 'market'),
    ('스프레드', '가장 비싸게 사겠다는 가격과 가장 싸게 팔겠다는 가격의 차이예요. 차이가 작을수록 보통 주식을 사고팔기 쉬워요.', 'market'),
    ('슬리피지', '내가 예상한 가격과 실제로 거래된 가격 사이에 차이가 생기는 일이에요. 주가가 빠르게 움직일 때 자주 생겨요.', 'market'),
    ('상한가', '우리나라 주식시장에서 하루 동안 오를 수 있는 가장 높은 가격이에요. 전일 종가 대비 +30%일 때 가격이 멈춰요.', 'market'),
    ('하한가', '우리나라 주식시장에서 하루 동안 내릴 수 있는 가장 낮은 가격이에요. 전일 종가 대비 -30%일 때 가격이 멈춰요.', 'market'),
    ('거래정지', '특별한 문제가 생겨 해당 주식을 잠시 사고팔 수 없게 된 상태예요.', 'market'),
    ('상장', '회사의 주식을 주식시장에서 누구나 사고팔 수 있도록 등록하는 일이에요.', 'market'),
    ('상장폐지', '해당 주식을 주식시장에서 더 이상 정상적으로 거래할 수 없게 되는 일이에요. 큰 손실이 생길 수 있으므로 주의해야 해요.', 'market'),
    ('정규장', '거래소가 정한 일반적인 주식 거래 시간이에요. 국내의 경우에는 오전 9시부터 오후 3시 반까지, 미국의 경우에는 한국 시간 기준 오후 11시 30분부터 오전 6시까지예요. 썸머타임이 적용된 경우에는 오후 10시 30분부터 오전 6시까지예요.', 'market'),
    ('시간외거래', '정규장이 시작되기 전이나 끝난 뒤에 정해진 방식으로 주식을 거래하는 제도예요.', 'market'),
    ('프리마켓', '미국 주식시장의 정규장이 열리기 전에 이루어지는 거래 시간이에요. 정규장보다 거래량이 적을 수 있어요.', 'market'),
    ('애프터마켓', '미국 주식시장의 정규장이 끝난 뒤에 이루어지는 거래 시간이에요. 가격 변동과 스프레드가 커질 수 있어요.', 'market'),
    ('동시호가', '일정 시간 동안 들어온 주문을 모은 뒤 하나의 가격을 정해 한꺼번에 체결하는 방식이에요.', 'market'),
    ('IPO·기업공개', '회사가 주식을 일반 투자자가 거래할 수 있도록 공개하고 주식시장에 상장하는 절차예요.', 'market'),
    ('스톱 주문', '시장 가격이 미리 정한 가격에 도달하면 시장가 주문으로 바뀌어 제출되는 주문 방식이에요.', 'market'),
    ('스톱리밋 주문', '시장 가격이 정한 기준에 도달하면 지정가 주문으로 바뀌는 방식이에요. 지정 가격에서 거래 상대가 없으면 체결되지 않을 수 있어요.', 'market'),
    ('호가단위', '주문 가격을 올리거나 내릴 수 있는 최소 가격 간격이에요. 주가와 시장에 따라 간격이 달라질 수 있어요.', 'market')
ON DUPLICATE KEY UPDATE
    `definition` = VALUES(`definition`),
    `category` = VALUES(`category`);

-- ============================================================
-- 종목 정보와 기업 분석 / category = 'fundamental'
-- 50개
-- ============================================================
INSERT INTO `glossary` (`term`, `definition`, `category`)
VALUES
    ('실적 발표·실발', '회사가 일정 기간 동안 얼마를 벌고 얼마를 썼는지 공개하는 일이에요. 실발은 실적 발표를 줄여서 부르는 말이에요.', 'fundamental'),
    ('시가', '그날 주식시장이 열린 뒤 처음으로 거래된 가격이에요.', 'fundamental'),
    ('고가', '그날 또는 선택한 기간 동안 가장 높았던 가격이에요.', 'fundamental'),
    ('저가', '그날 또는 선택한 기간 동안 가장 낮았던 가격이에요.', 'fundamental'),
    ('종가', '그날 정해진 주식 거래 시간이 끝날 때 마지막으로 거래된 가격이에요.', 'fundamental'),
    ('현재가', '지금 가장 최근에 거래된 주식 가격이에요.', 'fundamental'),
    ('전일 종가', '바로 전 거래일에 시장이 끝날 때의 주식 가격이에요.', 'fundamental'),
    ('전일 대비', '현재 가격이 전날 마지막 가격보다 얼마나 올랐거나 내렸는지 보여주는 값이에요.', 'fundamental'),
    ('등락률', '현재 가격이 전날 마지막 가격보다 몇 퍼센트 올랐거나 내렸는지 보여주는 숫자예요.', 'fundamental'),
    ('시가총액', '현재 주가에 시장에 나온 전체 주식 수를 곱한 금액이에요. 회사가 주식시장에서 어느 정도 크기로 평가받는지 보여줘요.', 'fundamental'),
    ('52주 최고가', '최근 1년 동안 기록한 가장 높은 주식 가격이에요.', 'fundamental'),
    ('52주 최저가', '최근 1년 동안 기록한 가장 낮은 주식 가격이에요.', 'fundamental'),
    ('공시', '회사가 투자자에게 중요한 소식을 공식적으로 알리는 일이에요. 회사의 실적, 큰 계약, 대표 변경 등의 내용이 포함될 수 있어요.', 'fundamental'),
    ('실적', '회사가 일정한 기간 동안 물건이나 서비스를 얼마나 팔았고, 얼마를 벌었는지 보여주는 결과예요.', 'fundamental'),
    ('컨센서스', '여러 증권사 전문가가 예상한 회사의 실적이나 주가 전망을 모아 평균처럼 나타낸 값이에요.', 'fundamental'),
    ('어닝 서프라이즈', '회사가 발표한 실적이 사람들이 예상한 것보다 훨씬 좋게 나온 경우예요.', 'fundamental'),
    ('어닝 쇼크', '회사가 발표한 실적이 사람들이 예상한 것보다 훨씬 나쁘게 나온 경우예요.', 'fundamental'),
    ('목표주가', '증권사 전문가가 회사의 상태를 살펴본 뒤 앞으로 적당하다고 예상한 주식 가격이에요. 반드시 그 가격까지 오른다는 뜻은 아니에요.', 'fundamental'),
    ('매출액', '회사가 물건이나 서비스를 팔아서 받은 돈을 모두 합친 금액이에요. 회사가 실제로 남긴 이익과는 달라요.', 'fundamental'),
    ('영업이익', '회사가 주된 사업을 통해 벌어들인 돈에서 사업에 사용한 비용을 뺀 금액이에요.', 'fundamental'),
    ('순이익', '회사가 벌어들인 돈에서 비용, 이자, 세금 등을 모두 빼고 마지막에 남은 돈이에요.', 'fundamental'),
    ('영업손실', '회사가 주된 사업으로 번 돈보다 사용한 돈이 더 많은 상태예요.', 'fundamental'),
    ('적자', '회사가 번 돈보다 쓴 돈이 많아 손실이 난 상태예요.', 'fundamental'),
    ('흑자', '회사가 쓴 돈보다 번 돈이 많아 이익이 난 상태예요.', 'fundamental'),
    ('EPS·주당순이익', '회사가 번 순이익을 전체 주식 수로 나눈 값이에요. 주식 한 개가 얼마만큼의 이익을 만들어냈는지 보여줘요.', 'fundamental'),
    ('PER·주가수익비율', '현재 주가가 회사가 벌어들이는 이익에 비해 몇 배 정도인지 보여주는 숫자예요. 같은 업종의 회사끼리 비교할 때 주로 사용해요.', 'fundamental'),
    ('PBR·주가순자산비율', '현재 주가가 회사가 가진 재산과 비교해 어느 정도로 평가받고 있는지 보여주는 숫자예요.', 'fundamental'),
    ('ROE·자기자본이익률', '회사가 주주에게 받은 돈을 이용해 이익을 얼마나 잘 만들었는지 보여주는 숫자예요.', 'fundamental'),
    ('부채', '회사가 다른 사람이나 은행에 갚아야 하는 돈이에요.', 'fundamental'),
    ('부채비율', '회사가 가진 자기 돈과 비교해 빚이 얼마나 많은지 보여주는 숫자예요.', 'fundamental'),
    ('성장률', '회사의 매출이나 이익이 이전보다 얼마나 늘거나 줄었는지 보여주는 숫자예요.', 'fundamental'),
    ('배당', '회사가 번 돈의 일부를 주식을 가진 사람들에게 나누어 주는 일이에요. 현금이나 주식으로 받을 수 있어요.', 'fundamental'),
    ('배당금', '회사가 주주에게 실제로 나누어 주는 돈이에요.', 'fundamental'),
    ('배당수익률', '현재 주가와 비교했을 때 1년 동안 받을 수 있는 배당금이 어느 정도인지 퍼센트로 보여주는 숫자예요.', 'fundamental'),
    ('배당락', '배당을 받을 수 있는 날짜가 지나면서 주가가 배당금만큼 낮아질 수 있는 현상이에요.', 'fundamental'),
    ('배당기준일', '이 날짜에 주식을 가지고 있는 사람을 기준으로 배당받을 사람을 정하는 날이에요.', 'fundamental'),
    ('테마주', '특정한 뉴스, 정책, 산업 또는 인물과 관련이 있다고 여겨져 함께 움직이는 주식이에요. 실제 회사 실적과 관계없이 크게 오르내릴 수 있어요.', 'fundamental'),
    ('대장주', '같은 업종이나 주제에 속한 주식 중에서 가장 크거나 가격 움직임을 이끄는 대표 종목이에요.', 'fundamental'),
    ('잡주', '회사의 상태가 좋지 않거나 가격이 특별한 이유 없이 크게 움직이는 작은 종목을 낮춰 부르는 말이에요. 공식 용어는 아니에요.', 'fundamental'),
    ('동전주', '주식 한 개의 가격이 매우 싼 종목을 부르는 말이에요. 가격이 싸다고 해서 회사의 가치도 싼 것은 아니에요.', 'fundamental'),
    ('재무제표', '회사의 자산, 부채, 매출, 이익, 현금 흐름 등을 정해진 형식으로 정리한 보고서예요.', 'fundamental'),
    ('영업이익률', '매출액 중에서 회사의 주된 사업으로 남긴 영업이익이 차지하는 비율이에요.', 'fundamental'),
    ('순이익률', '매출액 중에서 비용과 세금 등을 모두 제외한 순이익이 차지하는 비율이에요.', 'fundamental'),
    ('자기자본', '회사의 전체 자산에서 갚아야 할 부채를 뺀 금액으로, 주주에게 돌아가는 몫에 가까운 개념이에요.', 'fundamental'),
    ('자산', '회사가 가지고 있거나 앞으로 경제적 가치가 생길 것으로 기대되는 현금, 건물, 설비, 재고 등의 재산이에요.', 'fundamental'),
    ('유상증자', '회사가 새 주식을 발행해 투자자에게 돈을 받고 자본을 늘리는 일이에요. 주식 수가 늘어나 기존 주주의 지분 가치가 낮아질 수 있어요.', 'fundamental'),
    ('무상증자', '회사가 가진 자본의 일부를 옮겨 새 주식을 발행하고 기존 주주에게 돈을 받지 않고 나누어 주는 일이에요.', 'fundamental'),
    ('액면분할', '주식 한 주의 액면가를 낮추면서 주식 수를 늘리는 일이에요. 회사 전체 가치가 바로 늘어나는 것은 아니에요.', 'fundamental'),
    ('자사주', '회사가 자기 회사의 주식을 직접 사서 보유하고 있는 주식이에요.', 'fundamental'),
    ('시가배당률', '현재 주가와 비교해 한 해에 지급되는 배당금이 어느 정도인지 보여주는 비율이에요.', 'fundamental')
ON DUPLICATE KEY UPDATE
    `definition` = VALUES(`definition`),
    `category` = VALUES(`category`);

-- ============================================================
-- 차트와 기술적 분석 / category = 'chart'
-- 37개
-- ============================================================
INSERT INTO `glossary` (`term`, `definition`, `category`)
VALUES
    ('차트', '시간이 지나면서 주식 가격이 어떻게 움직였는지 그림으로 보여주는 화면이에요.', 'chart'),
    ('캔들(봉)', '일정한 시간 동안 주식 가격이 어디에서 시작하고, 가장 높거나 낮게 움직인 뒤, 어디에서 끝났는지 보여주는 모양이에요.', 'chart'),
    ('양봉', '정해진 시간 동안 시작 가격보다 마지막 가격이 더 높게 끝난 캔들이에요. 일반적으로 가격이 올랐다는 뜻이에요.', 'chart'),
    ('음봉', '정해진 시간 동안 시작 가격보다 마지막 가격이 더 낮게 끝난 캔들이에요. 일반적으로 가격이 내렸다는 뜻이에요.', 'chart'),
    ('윗꼬리', '가격이 한때 위로 올랐지만 다시 내려온 흔적이에요. 높은 가격에서 주식을 팔려는 사람이 많았을 수 있어요.', 'chart'),
    ('아랫꼬리', '가격이 한때 아래로 내려갔지만 다시 올라온 흔적이에요. 낮은 가격에서 주식을 사려는 사람이 많았을 수 있어요.', 'chart'),
    ('지지선', '주식 가격이 내려오다가 다시 올라갈 가능성이 있다고 보는 가격 구간이에요. 그 가격에서 주식을 사려는 사람이 많을 수 있어요.', 'chart'),
    ('저항선', '주식 가격이 올라가다가 다시 내려갈 가능성이 있다고 보는 가격 구간이에요. 그 가격에서 주식을 팔려는 사람이 많을 수 있어요.', 'chart'),
    ('돌파', '주식 가격이 중요한 가격대나 선을 넘어서는 움직임이에요.', 'chart'),
    ('이탈', '주식 가격이 지지선이나 중요한 가격 아래로 내려가는 움직임이에요.', 'chart'),
    ('조정', '가격이 계속 오르거나 내리는 중간에 잠시 반대 방향으로 움직이는 일이에요.', 'chart'),
    ('눌림목', '주가가 오르는 중간에 잠시 내려온 구간이에요. 이후 다시 오를 것이라고 생각하고 매수하는 사람이 있을 수 있어요.', 'chart'),
    ('갭', '전날 거래된 가격과 떨어진 위치에서 다음 날 거래가 시작돼 차트 사이에 빈 공간이 생기는 일이에요.', 'chart'),
    ('갭 상승', '전날 마지막 가격보다 훨씬 높은 가격에서 다음 날 거래가 시작되는 일이에요.', 'chart'),
    ('갭 하락', '전날 마지막 가격보다 훨씬 낮은 가격에서 다음 날 거래가 시작되는 일이에요.', 'chart'),
    ('매물대', '과거에 많은 주식이 사고팔린 가격 구간이에요. 그 가격에서 다시 사고팔려는 사람이 많아질 수 있어요.', 'chart'),
    ('이동평균선·이평선', '일정 기간의 평균 주가를 선으로 이어 만든 것이에요. 주가가 전체적으로 오르는지 내리는지 살펴볼 때 사용해요.', 'chart'),
    ('거래량 증가', '이전보다 사고팔린 주식의 수가 많아진 상태예요. 많은 사람이 해당 종목에 관심을 보이고 있다는 뜻일 수 있어요.', 'chart'),
    ('거래량 감소', '이전보다 사고팔린 주식의 수가 줄어든 상태예요. 해당 종목에 대한 관심이 줄었다는 뜻일 수 있어요.', 'chart'),
    ('신고가', '일정한 기간 동안 주가가 가장 높은 가격을 새롭게 기록한 상태예요.', 'chart'),
    ('신저가', '일정한 기간 동안 주가가 가장 낮은 가격을 새롭게 기록한 상태예요.', 'chart'),
    ('박스권', '주가가 일정한 높은 가격과 낮은 가격 사이에서 반복해서 움직이는 상태예요.', 'chart'),
    ('추세 전환', '계속 오르던 주가가 내리기 시작하거나, 계속 내리던 주가가 오르기 시작하는 변화예요.', 'chart'),
    ('추세선', '차트에서 주요 고점이나 저점을 이어 가격이 움직이는 방향을 살펴보기 위해 그은 선이에요.', 'chart'),
    ('상승 추세선', '점점 높아지는 저점들을 이어 만든 선으로, 가격이 상승 흐름을 유지하는지 살펴볼 때 사용해요.', 'chart'),
    ('하락 추세선', '점점 낮아지는 고점들을 이어 만든 선으로, 가격이 하락 흐름을 유지하는지 살펴볼 때 사용해요.', 'chart'),
    ('골든크로스', '짧은 기간의 이동평균선이 긴 기간의 이동평균선을 아래에서 위로 넘어가는 현상이에요. 상승 신호로 보기도 하지만 항상 오르는 것은 아니에요.', 'chart'),
    ('데드크로스', '짧은 기간의 이동평균선이 긴 기간의 이동평균선을 위에서 아래로 내려가는 현상이에요. 하락 신호로 보기도 하지만 항상 내리는 것은 아니에요.', 'chart'),
    ('정배열', '짧은 기간 이동평균선이 위에 있고 긴 기간 이동평균선이 아래에 있는 등 이동평균선이 상승 흐름에 맞게 배열된 상태예요.', 'chart'),
    ('역배열', '긴 기간 이동평균선이 위에 있고 짧은 기간 이동평균선이 아래에 있는 등 이동평균선이 하락 흐름에 맞게 배열된 상태예요.', 'chart'),
    ('RSI', '최근 가격의 상승과 하락 강도를 0부터 100 사이의 숫자로 나타내는 보조지표예요. 높은 값이나 낮은 값만으로 바로 매매를 결정하면 안 돼요.', 'chart'),
    ('과매수', '가격이 짧은 기간에 많이 올라 매수세가 지나치게 강해 보이는 상태를 말해요. 곧바로 하락한다는 뜻은 아니에요.', 'chart'),
    ('과매도', '가격이 짧은 기간에 많이 내려 매도세가 지나치게 강해 보이는 상태를 말해요. 곧바로 상승한다는 뜻은 아니에요.', 'chart'),
    ('볼린저 밴드', '이동평균선을 중심으로 가격 변동 정도를 반영한 위쪽과 아래쪽 밴드를 표시하는 보조지표예요.', 'chart'),
    ('거래량 이동평균', '일정 기간의 평균 거래량을 선으로 나타낸 것으로, 현재 거래량이 평소보다 많은지 적은지 비교할 때 사용해요.', 'chart'),
    ('리테스트', '가격이 중요한 지지선이나 저항선을 넘어선 뒤 다시 그 가격대로 돌아와 지지나 저항이 바뀌었는지 확인하는 움직임이에요.', 'chart'),
    ('지지/저항 전환', '과거의 저항 구간을 돌파한 뒤 지지 구간이 되거나, 과거의 지지 구간을 이탈한 뒤 저항 구간이 되는 현상이에요.', 'chart')
ON DUPLICATE KEY UPDATE
    `definition` = VALUES(`definition`),
    `category` = VALUES(`category`);

COMMIT;

SHOW TABLES;
