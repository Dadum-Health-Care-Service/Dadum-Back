-- 모임 일정 관련 컬럼 추가 (데이터 보존 방법)

-- 1단계: NULL 허용으로 컬럼 추가
ALTER TABLE gatherings 
ADD schedule_type VARCHAR2(20);

ALTER TABLE gatherings 
ADD schedule_details VARCHAR2(200);

-- 2단계: 기존 데이터에 기본값 설정
UPDATE gatherings 
SET schedule_type = 'ONE_TIME' 
WHERE schedule_type IS NULL;

-- 3단계: NOT NULL 제약조건 추가
ALTER TABLE gatherings 
MODIFY schedule_type VARCHAR2(20) NOT NULL;

-- 4단계: CHECK 제약조건 추가
ALTER TABLE gatherings 
ADD CONSTRAINT chk_schedule_type 
CHECK (schedule_type IN ('ONE_TIME', 'WEEKLY', 'MONTHLY', 'CUSTOM'));

-- 컬럼 코멘트 추가
COMMENT ON COLUMN gatherings.schedule_type IS '일정 유형 (ONE_TIME, WEEKLY, MONTHLY, CUSTOM)';
COMMENT ON COLUMN gatherings.schedule_details IS '일정 상세 정보';
