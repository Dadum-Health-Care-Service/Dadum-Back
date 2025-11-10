-- Admin 계정 생성 (평문 비밀번호 사용)
-- 기존 admin 계정이 있다면 삭제
DELETE FROM auth WHERE user_id IN (SELECT users_id FROM users WHERE email = 'admin@mog.com');
DELETE FROM users WHERE email = 'admin@mog.com';

-- admin 계정 생성
INSERT INTO users (users_name, nick_name, email, phone_num, role, reg_date, update_date)
VALUES ('관리자', 'admin', 'admin@mog.com', '01000000000', 'ADMIN', NOW(), NOW());

-- admin 계정의 auth 정보 생성 (평문 비밀번호: admin1234)
INSERT INTO auth (user_id, password)
SELECT users_id, 'admin1234'
FROM users 
WHERE email = 'admin@mog.com';

-- 생성된 계정 확인
SELECT u.users_id, u.email, u.role, a.auth_id, a.password 
FROM users u 
LEFT JOIN auth a ON u.users_id = a.user_id 
WHERE u.email = 'admin@mog.com';
