-- 기존 admin 계정을 SUPER_ADMIN으로 업데이트
UPDATE users 
SET role = 'SUPER_ADMIN' 
WHERE email = 'admin@mog.com' AND role = 'ADMIN';

-- 역할별 사용자 수 확인
SELECT role, COUNT(*) as user_count 
FROM users 
GROUP BY role;

-- 현재 사용자들의 역할 확인
SELECT users_id, users_name, email, role, reg_date 
FROM users 
ORDER BY role, users_id;
