-- User
INSERT INTO user (user_id, name, created_at, modified_at)
VALUES
(101, '임희은', NOW(), NOW())
     , (102, '김경덕', NOW(), NOW())
     , (103, '이호민', NOW(), NOW())
     , (104, '최재영', NOW(), NOW())
     , (105, '황지수', NOW(), NOW())
     , (106, '설한정', NOW(), NOW())
     , (107, '한동진', NOW(), NOW())
     , (108, '채호연', NOW(), NOW())
     , (109, '이다은', NOW(), NOW())
     , (110, '임기원', NOW(), NOW())
     , (111, '이동현', NOW(), NOW())
     , (112, '김대호', NOW(), NOW())
     , (113, '이지수', NOW(), NOW())
     , (114, '김용철', NOW(), NOW())
     , (115, '박성현', NOW(), NOW())
     , (116, '최현우', NOW(), NOW())
;

-- Coupon
INSERT INTO coupon (coupon_id, coupon_name, discount_type, discount_amount, max_discount_Amount, max_issuance_count, expiry_date, created_at, modified_at)
VALUES
(10001, '10% 할인 쿠폰 (최대 1만원)', 'RATE', 10, 10000, 5, DATE_ADD(NOW(), INTERVAL 7 DAY), NOW(), NOW())
     , (10002, '2000원 할인 쿠폰', 'AMOUNT', 2000, 2000, 20, DATE_ADD(NOW(), INTERVAL 10 DAY), NOW(), NOW())
     , (10003, '유효기간 만료 쿠폰', 'AMOUNT', 1000, 1000, 1, DATE_SUB(NOW(), INTERVAL 1 DAY), NOW(), NOW())
;

-- Coupon_Issuance
INSERT INTO coupon_issuance (issuance_id, user_id, coupon_id, coupon_state, use_date, created_at, modified_at)
VALUES
(10001, 101, 10001, 'USE', NOW(), NOW(), NOW())
     , (10002, 101, 10002, 'UNUSED', NULL, NOW(), NOW())
     , (10003, 101, 10003, 'UNUSED', NULL, NOW(), NOW())
     , (10004, 102, 10001, 'UNUSED', NULL, NOW(), NOW())
     , (10005, 102, 10002, 'USE', NOW(), NOW(), NOW())
     , (10006, 102, 10003, 'UNUSED', NULL, NOW(), NOW())
     , (10007, 103, 10002, 'USE', NOW(), NOW(), NOW())
     , (10008, 103, 10001, 'UNUSED', NULL, NOW(), NOW())
     , (10009, 103, 10003, 'USE', NOW(), NOW(), NOW())
     , (10010, 104, 10001, 'UNUSED', NULL, NOW(), NOW())
     , (10011, 104, 10002, 'USE', NOW(), NOW(), NOW())
     , (10012, 104, 10003, 'UNUSED', NULL, NOW(), NOW())
     , (10013, 105, 10001, 'USE', NOW(), NOW(), NOW())
     , (10014, 105, 10002, 'UNUSED', NULL, NOW(), NOW())
     , (10015, 105, 10003, 'USE', NOW(), NOW(), NOW())
     , (10016, 106, 10001, 'USE', NOW(), NOW(), NOW())
     , (10017, 106, 10002, 'USE', NOW(), NOW(), NOW())
     , (10018, 106, 10003, 'USE', NOW(), NOW(), NOW())
     , (10019, 107, 10001, 'UNUSED', NULL, NOW(), NOW())
     , (10020, 107, 10002, 'USE', NOW(), NOW(), NOW())
     , (10021, 107, 10003, 'UNUSED', NULL, NOW(), NOW())
     , (10022, 108, 10001, 'USE', NOW(), NOW(), NOW())
     , (10023, 108, 10002, 'USE', NOW(), NOW(), NOW())
     , (10024, 108, 10003, 'USE', NOW(), NOW(), NOW())
     , (10025, 109, 10001, 'UNUSED', NULL, NOW(), NOW())
     , (10026, 109, 10002, 'UNUSED', NULL, NOW(), NOW())
     , (10027, 109, 10003, 'UNUSED', NULL, NOW(), NOW())
;


-- Product
INSERT INTO product (product_id, product_name, price, created_at, modified_at)
VALUES
(100001, '피카츄', 5000, NOW(), NOW())
     , (100002, '라이츄', 15000, NOW(), NOW())
     , (100003, '파이리', 20000, NOW(), NOW())
     , (100004, '꼬부기', 10000, NOW(), NOW())
     , (100005, '버터플', 8000, NOW(), NOW())
     , (100006, '야도란', 25000, NOW(), NOW())
     , (100007, '피죤', 18500, NOW(), NOW())
     , (100008, '또가스', 50000, NOW(), NOW())
     , (100009, '냐옹', 30000, NOW(), NOW())
     , (100010, '맞아용', 28000, NOW(), NOW())
     , (100011, '파오리', 21000, NOW(), NOW())
     , (100012, '이브이', 11000, NOW(), NOW())
     , (100013, '뮤', 15000, NOW(), NOW())
     , (100014, '뮤츠', 17000, NOW(), NOW())
     , (100015, '잠만보', 10500, NOW(), NOW())
     , (100016, '이상해씨', 30000, NOW(), NOW())
     , (100017, '메타몽', 70000, NOW(), NOW())
     , (100018, '리자몽', 40000, NOW(), NOW())
     , (100019, '거북왕', 32000, NOW(), NOW())
     , (100020, '식스테일', 29000, NOW(), NOW())
     , (100021, '나일테일', 35000, NOW(), NOW())
     , (100022, '치코리타', 19000, NOW(), NOW())
     , (100023, '뚜벅쵸', 80000, NOW(), NOW())
     , (100024, '펄기아', 200000, NOW(), NOW())
     , (100025, '아보', 5000, NOW(), NOW())
     , (100026, '아보크', 7000, NOW(), NOW())
     , (100027, '슬리피', 56000, NOW(), NOW())
     , (100028, '케이시', 35000, NOW(), NOW())
     , (100029, '후딘', 67000, NOW(), NOW())
     , (100030, '따라큐', 34000, NOW(), NOW())
     , (100031, '망나뇽', 99000, NOW(), NOW())
     , (100032, '에브이', 80000, NOW(), NOW())
     , (100033, '물짱이', 16000, NOW(), NOW())
     , (100034, '콘치', 500, NOW(), NOW())
     , (100035, '별가사리', 2000, NOW(), NOW())
     , (100036, '잉어킹', 1500, NOW(), NOW())
     , (100037, '발챙이', 5500, NOW(), NOW())
     , (100038, '갸랴도스', 17000, NOW(), NOW())
     , (100039, '롱스톤', 7000, NOW(), NOW())
     , (100040, '고오스', 8000, NOW(), NOW())
     , (100041, '해피너스', 20000, NOW(), NOW())
     , (100042, '럭키', 7770, NOW(), NOW())
     , (100043, '아라리', 800, NOW(), NOW())
     , (100044, '라프라스', 61000, NOW(), NOW())
     , (100045, '고라파덕', 10000, NOW(), NOW())
     , (100046, '소드라', 4000, NOW(), NOW())
     , (100047, '시드라', 5000, NOW(), NOW())
     , (100048, '이상해꽃', 40000, NOW(), NOW())
     , (100049, '샤미드', 78000, NOW(), NOW())
     , (100050, '쥬피썬더', 90000, NOW(), NOW())
     , (100051, '디그다', 66000, NOW(), NOW())
     , (100052, '닥트리오', 71000, NOW(), NOW())
     , (100053, '마그마', 6000, NOW(), NOW())
     , (100054, '갈모매', 8000, NOW(), NOW())
     , (100055, '루기아', 1000, NOW(), NOW())
     , (100056, '알통몬', 500, NOW(), NOW())
     , (100057, '근육몬', 1000, NOW(), NOW())
     , (100058, '야돈', 8000, NOW(), NOW())
     , (100059, '야도란', 9000, NOW(), NOW())
     , (100060, '피츄', 80000, NOW(), NOW())
;

-- Stock
INSERT INTO stock (stock_id, product_id, quantity, created_at, modified_at)
SELECT product_id, product_id, 10, NOW(), NOW() FROM product
;
