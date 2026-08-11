이 문서는 가상 증권사에서 매수/매도/입금/출금/상품가입/상품환매 같은 일을 처리했을 때, 그 결과가 우리 DB에 어떻게 반영되는지를 Java 서비스 코드와 완전히 동일한 계산식으로 재현한 raw SQL 모음입니다.
각 블록은 그대로 복사해서 앞부분의 SET @변수 = 값; 줄만 채운 뒤, 블록 전체를 한 번에 실행하면 됩니다.
mysql 세션 변수(@변수)를 이용해서 수수료·가중평균 단가 같은 계산을 SQL이 대신 해주기 때문에, 사람이 직접 암산해서 넣을 값은 계좌ID·종목/상품·수량 정도뿐입니다.
모든 블록은 START TRANSACTION; ~ COMMIT;으로 감싸져 있습니다.
잔고나 보유수량이 부족해서 중간 어느 문장에서 에러가 나더라도(예: balance는 unsigned라 음수가 되면 자동으로 에러가 남) 값만 고쳐서 처음부터 다시 실행하면 됩니다.
실행 후에는 항상 대시보드(/member/dashboard)를 새로고침해서 반영을 확인하세요.
총자산/총손익/목표 진행률은 전부 라이브로 다시 계산되기 때문에 서버 재시작이 필요 없습니다
(단, stock.current_price처럼 여러 계좌가 공유하는 값을 바꾸면 그 종목을 보유한 다른 계좌에도 영향을 줍니다).

0. 참고 데이터
   증권사 (brokerage)
   brokerage_id	이름	수수료율
   1	스톡증권	0.00015
   2	허브증권	0.00012
   3	KH투자증권	0.00010
   종목 (stock, 시세가 있는 것만)
   stock_code	이름	현재가
   000660	SK하이닉스	200,000
   005930	삼성전자	82,400
   035420	NAVER	198,500
   035720	카카오	44,650
   AAPL	Apple	270,000
   AMZN	Amazon	260,000
   GOOGL	Alphabet	280,000
   META	Meta Platforms	880,000
   MSFT	Microsoft	600,000
   NFLX	Netflix	1,350,000
   NVDA	NVIDIA	240,000
   TSLA	Tesla	420,000
   (그 외 종목은 current_price = 0으로만 등록돼 있어 거래 시뮬레이션엔 안 맞습니다. 최신값은 SELECT stock_code, stock_name, current_price FROM stock WHERE current_price > 0;로 확인하세요.)

금융상품 (financial_product)
product_id	이름	종류	기준가(nav)
21	스톡 안정형 국공채펀드	BOND	10,120.00
22	스톡 글로벌 성장주 펀드	FUND	12,480.00
23	스톡 조기상환 ELS 1호	ELS	10,000.00
24	허브 머니마켓펀드	FUND	10,005.00
25	허브 우량회사채펀드	BOND	10,850.00
26	허브 지수연동 ELS 2호	ELS	10,000.00
27	KH 배당주 펀드	FUND	11,230.00
28	KH 국공채펀드	BOND	10,060.00
29	KH 리자드형 ELS 3호	ELS	10,000.00
30	KH 테크섹터 펀드	FUND	15,320.00
내 계좌 상태 확인용 조회
SET @account_id = 33; -- 확인하고 싶은 계좌ID로 교체

SELECT account_id, account_no, owner_name, brokerage_id, balance FROM account WHERE account_id = @account_id;
SELECT * FROM holding WHERE account_id = @account_id;
SELECT * FROM product_holding WHERE account_id = @account_id;
1. 매수 (BUY)
   바뀌는 테이블: holding(신규/가중평균 갱신) · account.balance(차감) · trade(이력 기록)

START TRANSACTION;

-- ===== 채울 값 =====
SET @account_id = 33;
SET @stock_code = _utf8mb4'005930' COLLATE utf8mb4_unicode_ci;
SET @quantity   = 1;
-- ===================

SET @price    = (SELECT current_price FROM stock WHERE stock_code = @stock_code);
SET @fee_rate = (SELECT b.fee_rate FROM account a JOIN brokerage b ON b.brokerage_id = a.brokerage_id WHERE a.account_id = @account_id);
SET @amount     = @price * @quantity;
SET @fee        = ROUND(@amount * @fee_rate);
SET @total_cost = @amount + @fee;

INSERT INTO holding (account_id, stock_code, quantity, avg_price)
VALUES (@account_id, @stock_code, @quantity, @total_cost DIV @quantity)
ON DUPLICATE KEY UPDATE
avg_price = (quantity * avg_price + @total_cost) DIV (quantity + @quantity),
quantity  = quantity + @quantity;

-- 잔고가 부족하면 balance가 unsigned라 여기서 에러가 나고, 위 INSERT까지 통째로 취소됩니다
UPDATE account SET balance = balance - @total_cost WHERE account_id = @account_id;

INSERT INTO trade (account_id, stock_code, trade_type, quantity, price, fee)
VALUES (@account_id, @stock_code, 'BUY', @quantity, @price, @fee);

COMMIT;
2. 매도 (SELL)
   바뀌는 테이블: holding(수량 감소, 0이면 삭제) · account.balance(증가) · trade(이력 기록)

START TRANSACTION;

-- ===== 채울 값 =====
SET @account_id = 33;
SET @stock_code = _utf8mb4'035720' COLLATE utf8mb4_unicode_ci; -- 보유 중인 종목이어야 함
SET @quantity   = 1;        -- 보유수량 이하여야 함
-- ===================

SET @price    = (SELECT current_price FROM stock WHERE stock_code = @stock_code);
SET @fee_rate = (SELECT b.fee_rate FROM account a JOIN brokerage b ON b.brokerage_id = a.brokerage_id WHERE a.account_id = @account_id);
SET @amount   = @price * @quantity;
SET @fee      = ROUND(@amount * @fee_rate);
SET @proceeds = @amount - @fee;

UPDATE holding SET quantity = quantity - @quantity
WHERE account_id = @account_id AND stock_code = @stock_code;
-- 보유수량보다 많이 팔면 quantity가 unsigned라 여기서 에러가 나고 전부 취소됩니다

-- 전량 매도로 0주가 됐으면 보유내역 자체를 삭제 (실제 서비스 로직과 동일)
DELETE FROM holding WHERE account_id = @account_id AND stock_code = @stock_code AND quantity = 0;

UPDATE account SET balance = balance + @proceeds WHERE account_id = @account_id;

INSERT INTO trade (account_id, stock_code, trade_type, quantity, price, fee)
VALUES (@account_id, @stock_code, 'SELL', @quantity, @price, @fee);

COMMIT;
3. 금융상품 가입 (SUBSCRIBE)
   바뀌는 테이블: product_holding(신규/가중평균 갱신) · account.balance(차감) · product_transaction(이력 기록)

START TRANSACTION;

-- ===== 채울 값 =====
SET @account_id = 33;
SET @product_id = 30;
SET @quantity   = 2.0000; -- 좌수 (소수 가능)
-- ===================

SET @nav    = (SELECT nav FROM financial_product WHERE product_id = @product_id);
SET @amount = ROUND(@nav * @quantity);

INSERT INTO product_holding (account_id, product_id, quantity, avg_nav, purchase_amount)
VALUES (@account_id, @product_id, @quantity, @nav, @amount)
ON DUPLICATE KEY UPDATE
avg_nav         = ROUND((quantity * avg_nav + @quantity * @nav) / (quantity + @quantity), 2),
purchase_amount = purchase_amount + @amount,
quantity        = quantity + @quantity;

UPDATE account SET balance = balance - @amount WHERE account_id = @account_id;

INSERT INTO product_transaction (account_id, product_id, transaction_type, quantity, nav, amount)
VALUES (@account_id, @product_id, 'SUBSCRIBE', @quantity, @nav, @amount);

COMMIT;
4. 금융상품 환매 (REDEEM)
   바뀌는 테이블: product_holding(좌수 감소, 0이면 삭제) · account.balance(증가) · product_transaction(이력 기록)

START TRANSACTION;

-- ===== 채울 값 =====
SET @account_id = 33;
SET @product_id = 30;
SET @quantity   = 1.0000; -- 보유 좌수 이하여야 함
-- ===================

SET @nav    = (SELECT nav FROM financial_product WHERE product_id = @product_id);
SET @amount = ROUND(@nav * @quantity);

UPDATE product_holding SET
quantity        = quantity - @quantity,
purchase_amount = GREATEST(0, purchase_amount - ROUND(avg_nav * @quantity))
WHERE account_id = @account_id AND product_id = @product_id;

DELETE FROM product_holding WHERE account_id = @account_id AND product_id = @product_id AND quantity = 0;

UPDATE account SET balance = balance + @amount WHERE account_id = @account_id;

INSERT INTO product_transaction (account_id, product_id, transaction_type, quantity, nav, amount)
VALUES (@account_id, @product_id, 'REDEEM', @quantity, @nav, @amount);

COMMIT;
5. 입금 (DEPOSIT)
   바뀌는 테이블: account.balance(증가) · cash_transaction(이력 기록)

START TRANSACTION;

-- ===== 채울 값 =====
SET @account_id = 33;
SET @amount = 100000;
SET @memo   = '용돈 입금';
-- ===================

UPDATE account SET balance = balance + @amount WHERE account_id = @account_id;

INSERT INTO cash_transaction (account_id, transaction_type, amount, balance_after, memo)
VALUES (@account_id, 'DEPOSIT', @amount, (SELECT balance FROM account WHERE account_id = @account_id), @memo);

COMMIT;
6. 출금 (WITHDRAWAL)
   바뀌는 테이블: account.balance(감소) · cash_transaction(이력 기록)

START TRANSACTION;

-- ===== 채울 값 =====
SET @account_id = 33;
SET @amount = 50000; -- 잔고 이하여야 함
SET @memo   = '생활비 출금';
-- ===================

-- 잔고보다 많이 출금하면 balance가 unsigned라 여기서 에러가 나고 전부 취소됩니다
UPDATE account SET balance = balance - @amount WHERE account_id = @account_id;

INSERT INTO cash_transaction (account_id, transaction_type, amount, balance_after, memo)
VALUES (@account_id, 'WITHDRAWAL', @amount, (SELECT balance FROM account WHERE account_id = @account_id), @memo);

COMMIT;
7. (보너스) 시세 변동 — "증권사가 종가를 갱신했다"
   바뀌는 테이블: stock.current_price

이 종목을 보유한 모든 계좌의 평가금액에 영향을 줍니다(개별 계좌 조작이 아님에 주의).

SET @stock_code = _utf8mb4'035720' COLLATE utf8mb4_unicode_ci;
SET @new_price  = 60000;

UPDATE stock SET current_price = @new_price WHERE stock_code = @stock_code;
8. (보너스) 신규 계좌 개설 — "증권사에 새 계좌가 만들어졌다"
   바뀌는 테이블: account(신규 행). 아직 아무 회원과도 연동되지 않은 상태로 생성되며, 대시보드의 "계좌 연동하기"에서 이 계좌번호+예금주명으로 연동해야 실제 회원 것이 됩니다.

-- ===== 채울 값 =====
SET @brokerage_id = 3;
SET @account_no    = CONCAT('NEW-', UPPER(SUBSTRING(MD5(RAND()), 1, 8)));
SET @owner_name    = '테스트유저';
-- ===================

INSERT INTO account (account_no, brokerage_id, owner_name) VALUES (@account_no, @brokerage_id, @owner_name);

SELECT @account_no AS 발급된_계좌번호; -- 이 값을 대시보드 '계좌 연동하기'에 입력