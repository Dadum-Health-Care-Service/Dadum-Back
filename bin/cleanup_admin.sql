-- 기존 admin 계정 완전 삭제
-- 1. admin 계정의 auth 정보 삭제
DELETE FROM auth WHERE user_id IN (
    SELECT users_id FROM users WHERE email = 'admin@mog.com'
);

-- 2. admin 계정 삭제
DELETE FROM users WHERE email = 'admin@mog.com';

-- 3. 확인
SELECT 'Admin 계정이 삭제되었습니다.' as result;
