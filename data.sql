-- ============================================================
-- Schema: Manajemen Inventaris / Stok Barang
-- Proyek PAM 2026 - Sri Diva Siagian
-- ============================================================

CREATE TABLE IF NOT EXISTS users (
                                     id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100)  NOT NULL,
    username    VARCHAR(50)   NOT NULL UNIQUE,
    password    VARCHAR(255)  NOT NULL,
    photo       VARCHAR(255)  NULL,
    about       TEXT          NULL,
    created_at  TIMESTAMP     NOT NULL,
    updated_at  TIMESTAMP     NOT NULL
    );

CREATE TABLE IF NOT EXISTS refresh_tokens (
                                              id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID  NOT NULL,
    refresh_token TEXT  NOT NULL,
    auth_token    TEXT  NOT NULL,
    created_at    TIMESTAMP NOT NULL
    );

CREATE TABLE IF NOT EXISTS products (
                                        id          UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID           NOT NULL,
    name        VARCHAR(150)   NOT NULL,
    description TEXT           NOT NULL,
    category    VARCHAR(100)   NOT NULL,
    unit        VARCHAR(50)    NOT NULL,        -- satuan: pcs, kg, liter, box, dll
    price       DECIMAL(15,2)  NOT NULL DEFAULT 0,
    stock       INT            NOT NULL DEFAULT 0,
    min_stock   INT            NOT NULL DEFAULT 0, -- batas minimum stok (alert)
    image       TEXT           NULL,
    created_at  TIMESTAMP      NOT NULL,
    updated_at  TIMESTAMP      NOT NULL,

    CONSTRAINT fk_products_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
    );

-- Index untuk mempercepat query pencarian dan filter
CREATE INDEX IF NOT EXISTS idx_products_user_id  ON products(user_id);
CREATE INDEX IF NOT EXISTS idx_products_category ON products(category);
CREATE INDEX IF NOT EXISTS idx_products_name     ON products(name);