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
    price NUMERIC(38, 2)
);

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
