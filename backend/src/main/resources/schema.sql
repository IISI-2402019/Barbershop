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
