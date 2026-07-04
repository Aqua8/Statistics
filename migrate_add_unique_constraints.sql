-- 기존 DB에 적용할 마이그레이션: 집계 테이블 UNIQUE 제약 추가
-- 이미 중복 데이터가 있는 경우 ALTER TABLE이 실패합니다.
-- 먼저 아래 쿼리로 중복 여부를 확인하세요:
--   SELECT project_id, stat_date, COUNT(*) FROM daily_stats GROUP BY project_id, stat_date HAVING COUNT(*) > 1;
--   SELECT project_id, stat_date, LEFT(page_url, 255), COUNT(*) FROM page_stats GROUP BY 1,2,3 HAVING COUNT(*) > 1;
--   SELECT project_id, stat_date, LEFT(referrer, 255), COUNT(*) FROM referrer_stats GROUP BY 1,2,3 HAVING COUNT(*) > 1;

ALTER TABLE daily_stats
    ADD UNIQUE KEY uk_project_stat_date (project_id, stat_date);

ALTER TABLE page_stats
    ADD UNIQUE KEY uk_project_page_stat_date (project_id, stat_date, page_url(255));

ALTER TABLE referrer_stats
    ADD UNIQUE KEY uk_project_referrer_stat_date (project_id, stat_date, referrer(255));
