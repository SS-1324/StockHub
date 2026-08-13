-- 커뮤니티의 내부 key는 기존 게시글과 연결되어 있으므로 free/tip/profit/review를 유지한다.
-- 화면에는 각각 자유/종목토론/정보공유/반성일지로 표시한다.
--
-- 아래 UPDATE는 이전 수정 과정에서 새 key로 저장된 글이 있는 환경만 기존 key로 복구한다.
-- 현재 key가 이미 free/tip/profit/review인 게시글은 변경하지 않으며 게시글 자체도 삭제하지 않는다.
START TRANSACTION;

UPDATE board
SET category = 'tip'
WHERE category = 'discussion';

UPDATE board
SET category = 'profit'
WHERE category = 'info';

UPDATE board
SET category = 'review'
WHERE category IN ('journal', 'reflection');

COMMIT;
