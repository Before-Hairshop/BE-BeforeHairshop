-- 유저 생성 (디자이너 7명)
insert into member (designer_flag, email, premium_flag, provider, username, role, nickname, status)
values (1, 'aaaa@naver.com', 0, 'GOOGLE', 'google_1', 'ROLE_DESIGNER', '홍길동', 1);
insert into member (designer_flag, email, premium_flag, provider, username, role, nickname, status)
values (1, 'bbbb@naver.com', 0, 'GOOGLE', 'google_2', 'ROLE_USER', '김길동', 1);
insert into member (designer_flag, email, premium_flag, provider, username, role, nickname, status)
values (1, 'bbbb@naver.com', 0, 'GOOGLE', 'google_3', 'ROLE_USER', '정길동', 1);
insert into member (designer_flag, email, premium_flag, provider, username, role, nickname, status)
values (1, 'bbbb@naver.com', 0, 'GOOGLE', 'google_4', 'ROLE_USER', '이길동', 1);
insert into member (designer_flag, email, premium_flag, provider, username, role, nickname, status)
values (1, 'bbbb@naver.com', 0, 'GOOGLE', 'google_5', 'ROLE_USER', '박길동', 1);
insert into member (designer_flag, email, premium_flag, provider, username, role, nickname, status)
values (1, 'bbbb@naver.com', 0, 'GOOGLE', 'google_6', 'ROLE_USER', '유길동', 1);
insert into member (designer_flag, email, premium_flag, provider, username, role, nickname, status)
values (1, 'bbbb@naver.com', 0, 'GOOGLE', 'google_7', 'ROLE_USER', '창길동', 1);
insert into member (designer_flag, email, premium_flag, provider, username, role, nickname, status)
values (1, 'bbbb@naver.com', 0, 'GOOGLE', 'google_8', 'ROLE_USER', '선길동', 1);

-- 선릉 6명, 건대 2명
insert into hair_designer_profile (hair_designer_id, name, description, hair_shop_name, zip_code, zip_address, latitude, longitude, detail_address, phone_number, status)
values (1, '홍길동', '펌 장인이에요', '<홍>살롱', 06211, '선릉', 37.5028101, 127.0447469, '건물 1층', '010-1111-2222', 1);
insert into hair_designer_profile (hair_designer_id, name, description, hair_shop_name, zip_code, zip_address, latitude, longitude, detail_address, phone_number, status)
values (2, '김길동', '펌 장인이에요', '<김>살롱', 06211, '선릉', 37.5056988, 127.0474095, '건물 1층', '010-1111-2222', 1);
insert into hair_designer_profile (hair_designer_id, name, description, hair_shop_name, zip_code, zip_address, latitude, longitude, detail_address, phone_number, status)
values (3, '정길동', '펌 장인이에요', '<정>살롱', 06211, '선릉', 37.505564, 127.0491607, '건물 1층', '010-1111-2222', 1);
insert into hair_designer_profile (hair_designer_id, name, description, hair_shop_name, zip_code, zip_address, latitude, longitude, detail_address, phone_number, status)
values (4, '이길동', '펌 장인이에요', '<이>살롱', 06211, '선릉', 37.5059789, 127.0501095, '건물 1층', '010-1111-2222', 1);
insert into hair_designer_profile (hair_designer_id, name, description, hair_shop_name, zip_code, zip_address, latitude, longitude, detail_address, phone_number, status)
values (5, '박길동', '펌 장인이에요', '<박>살롱', 06211, '선릉', 37.5028112, 127.0447116, '건물 1층', '010-1111-2222', 1);
insert into hair_designer_profile (hair_designer_id, name, description, hair_shop_name, zip_code, zip_address, latitude, longitude, detail_address, phone_number, status)
values (6, '유길동', '펌 장인이에요', '<유>살롱', 06211, '건대입구', 37.540372, 127.069276, '건물 1층', '010-1111-2222', 1);
insert into hair_designer_profile (hair_designer_id, name, description, hair_shop_name, zip_code, zip_address, latitude, longitude, detail_address, phone_number, status)
values (7, '창길동', '펌 장인이에요', '<창>살롱', 06211, '건대입구', 37.540372, 127.069276, '건물 1층', '010-1111-2222', 1);
insert into hair_designer_profile (hair_designer_id, name, description, hair_shop_name, zip_code, zip_address, latitude, longitude, detail_address, phone_number, status)
values (8, '선길동', '펌 장인이에요', '<선>살롱', 06211, '선릉', 37.5032909, 127.0498323, '건물 1층', '010-1111-2222', 1);

-- -- 유저 생성 (일반 3명)
-- insert into member (designer_flag, email, premium_flag, provider, username, role, nickname, status)
-- values (0, 'bbbb@naver.com', 0, 'GOOGLE', 'google_6', 'ROLE_USER', '보규', 1);
-- insert into member (designer_flag, email, premium_flag, provider, username, role, nickname, status)
-- values (0, 'bbbb@naver.com', 0, 'GOOGLE', 'google_7', 'ROLE_USER', '나영', 1);
-- insert into member (designer_flag, email, premium_flag, provider, username, role, nickname, status)
-- values (0, 'bbbb@naver.com', 0, 'GOOGLE', 'google_8', 'ROLE_USER', '경모', 1);


