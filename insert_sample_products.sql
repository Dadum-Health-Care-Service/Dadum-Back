-- 샘플 상품 데이터 삽입 스크립트
-- 기존 상품 데이터가 있다면 삭제
DELETE FROM products WHERE seller_id = 1;

-- 샘플 상품 데이터 삽입
INSERT INTO products (seller_id, product_name, description, price, stock, category, image_url, image_data, is_active, created_at, updated_at) VALUES
(1, '프리미엄 요가매트', '고급 실리콘 소재의 안전한 요가매트', 45000, 50, 'equipment', 'https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?w=500&h=500&fit=crop', NULL, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

(1, '스마트 웨이트', '블루투스 연결 가능한 스마트 웨이트', 120000, 30, 'equipment', 'https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?w=500&h=500&fit=crop', NULL, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

(1, '운동복 세트', '편안하고 스타일리시한 운동복', 89000, 25, 'clothing', 'https://images.unsplash.com/photo-1591195853828-11db59a44f6b?w=500&h=500&fit=crop', NULL, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

(1, '프로틴 파우더', '고품질 단백질 보충제', 65000, 40, 'supplement', 'https://images.unsplash.com/photo-1593095948071-474c5cc2989d?w=500&h=500&fit=crop', NULL, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

(1, '테스트 상품', '100원 테스트용 상품입니다 (아임포트 최소 금액)', 100, 100, 'equipment', 'https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?w=500&h=500&fit=crop', NULL, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

(1, '덤벨 세트', '가정용 덤벨 세트 10kg', 150000, 20, 'equipment', 'https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?w=500&h=500&fit=crop', NULL, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

(1, '운동화', '편안한 운동화', 120000, 35, 'clothing', 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=500&h=500&fit=crop', NULL, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

(1, '비타민', '종합 비타민', 35000, 60, 'supplement', 'https://images.unsplash.com/photo-1559757148-5c350d0d3c56?w=500&h=500&fit=crop', NULL, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 삽입된 데이터 확인
SELECT * FROM products WHERE seller_id = 1 AND is_active = true;
