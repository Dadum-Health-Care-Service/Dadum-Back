-- 기존 users 테이블에 role 컬럼 추가
ALTER TABLE users ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER';

-- 기존 사용자들의 role을 USER로 설정
UPDATE users SET role = 'USER' WHERE role IS NULL OR role = '';

-- admin 계정 생성 (이미 존재하지 않는 경우)
INSERT INTO users (users_name, nick_name, email, phone_num, role, reg_date, update_date)
SELECT '관리자', 'admin', 'admin@mog.com', '01000000000', 'ADMIN', NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'admin@mog.com'
);

-- admin 계정의 auth 정보 생성 (비밀번호: admin1234)
-- BCrypt로 암호화된 'admin1234' 비밀번호
INSERT INTO auth (user_id, password)
SELECT u.users_id, '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa'
FROM users u
WHERE u.email = 'admin@mog.com'
AND NOT EXISTS (
    SELECT 1 FROM auth a WHERE a.user_id = u.users_id
);

-- role 컬럼에 인덱스 추가 (선택사항)
CREATE INDEX idx_users_role ON users(role);
