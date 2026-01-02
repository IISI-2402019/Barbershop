-- 如果資料表不存在，則建立 Stylists 表
CREATE TABLE IF NOT EXISTS stylists (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    specialty VARCHAR(255),
    avatar_url VARCHAR(255)
);

-- 如果資料表不存在，則建立 Services 表
CREATE TABLE IF NOT EXISTS services (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    duration_hours DOUBLE PRECISION,
    price NUMERIC(38, 2),
    is_price_starting_from BOOLEAN DEFAULT FALSE NOT NULL
);

-- 確保欄位存在 (針對舊資料表)
ALTER TABLE services ADD COLUMN IF NOT EXISTS is_price_starting_from BOOLEAN DEFAULT FALSE NOT NULL;

-- 如果資料表不存在，則建立 Users 表
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    line_user_id VARCHAR(255) NOT NULL UNIQUE,
    display_name VARCHAR(255),
    real_name VARCHAR(255),
    phone VARCHAR(50),
    role VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 如果資料表不存在，則建立 Appointments 表
CREATE TABLE IF NOT EXISTS appointments (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES users(id),
    stylist_id BIGINT NOT NULL REFERENCES stylists(id),
    service_id BIGINT NOT NULL REFERENCES services(id),
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 如果資料表不存在，則建立 Schedules 表
CREATE TABLE IF NOT EXISTS schedules (
    id BIGSERIAL PRIMARY KEY,
    stylist_id BIGINT REFERENCES stylists(id),
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    is_all_day BOOLEAN DEFAULT FALSE NOT NULL,
    reason VARCHAR(255)
);

-- 修改 stylist_id 為可空 (針對舊資料表)
ALTER TABLE schedules ALTER COLUMN stylist_id DROP NOT NULL;
