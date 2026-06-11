-- ============================================================================
-- 小程序商城 补充数据库脚本
-- ============================================================================

-- 0. 商品分类表（商城专用）
CREATE TABLE IF NOT EXISTS sale_product_category (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    parent_id       BIGINT,
    icon            VARCHAR(500),
    sort_order      INT DEFAULT 0,
    status          SMALLINT NOT NULL DEFAULT 1,
    created_by      BIGINT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by      BIGINT,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ
);
COMMENT ON TABLE sale_product_category IS '商城-商品分类表';
CREATE INDEX IF NOT EXISTS idx_sale_category_parent ON sale_product_category (parent_id);

-- 0.1 商品表（商城专用）
CREATE TABLE IF NOT EXISTS sale_product (
    id              BIGSERIAL PRIMARY KEY,
    category_id     BIGINT,
    name            VARCHAR(200) NOT NULL,
    description     TEXT,
    cover_image     VARCHAR(500),
    images          JSONB,
    standard_price  DECIMAL(18,2) DEFAULT 0,
    cost_price      DECIMAL(18,2),
    unit            VARCHAR(20),
    sort_order      INT DEFAULT 0,
    status          SMALLINT NOT NULL DEFAULT 1,
    ext_json        JSONB,
    created_by      BIGINT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by      BIGINT,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ
);
COMMENT ON TABLE sale_product IS '商城-商品表';
CREATE INDEX IF NOT EXISTS idx_sale_product_category ON sale_product (category_id);
CREATE INDEX IF NOT EXISTS idx_sale_product_name ON sale_product USING gin (name gin_trgm_ops);

-- 1. 小程序会话表
CREATE TABLE IF NOT EXISTS mp_session (
    id              BIGSERIAL PRIMARY KEY,
    customer_id     BIGINT NOT NULL REFERENCES crm_customer(id) ON DELETE CASCADE,
    openid          VARCHAR(100) NOT NULL UNIQUE,
    unionid         VARCHAR(100),
    token           VARCHAR(500),
    token_expire_at TIMESTAMPTZ,
    last_login_at   TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
COMMENT ON TABLE mp_session IS '小程序会话表';
CREATE INDEX IF NOT EXISTS idx_mp_session_customer ON mp_session (customer_id);
CREATE INDEX IF NOT EXISTS idx_mp_session_openid ON mp_session (openid);

-- 2. 收货地址表
CREATE TABLE IF NOT EXISTS mp_address (
    id              BIGSERIAL PRIMARY KEY,
    customer_id     BIGINT NOT NULL REFERENCES crm_customer(id) ON DELETE CASCADE,
    receiver_name   VARCHAR(100) NOT NULL,
    receiver_phone  VARCHAR(20) NOT NULL,
    province        VARCHAR(50),
    city            VARCHAR(50),
    district        VARCHAR(50),
    detail_address  VARCHAR(500) NOT NULL,
    is_default      BOOLEAN NOT NULL DEFAULT FALSE,
    created_by      BIGINT,
    updated_by      BIGINT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ
);
COMMENT ON TABLE mp_address IS '小程序收货地址表';
CREATE INDEX IF NOT EXISTS idx_mp_address_customer ON mp_address (customer_id);

-- 2.1 分销商表
CREATE TABLE IF NOT EXISTS crm_distributor (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES crm_customer(id) ON DELETE CASCADE,
    parent_id       BIGINT REFERENCES crm_distributor(id),
    level           VARCHAR(20) DEFAULT 'basic',
    total_commission DECIMAL(18,2) DEFAULT 0,
    withdrawable    DECIMAL(18,2) DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
COMMENT ON TABLE crm_distributor IS '分销商表';
COMMENT ON COLUMN crm_distributor.level IS 'basic/silver/gold';
CREATE INDEX IF NOT EXISTS idx_distributor_user ON crm_distributor (user_id);
CREATE INDEX IF NOT EXISTS idx_distributor_parent ON crm_distributor (parent_id);

-- 2.2 佣金记录表
CREATE TABLE IF NOT EXISTS crm_commission (
    id              BIGSERIAL PRIMARY KEY,
    distributor_id  BIGINT NOT NULL REFERENCES crm_distributor(id) ON DELETE CASCADE,
    order_id        BIGINT NOT NULL REFERENCES mall_order(id) ON DELETE CASCADE,
    amount          DECIMAL(18,2) NOT NULL,
    rate            DECIMAL(5,4),
    status          VARCHAR(20) NOT NULL DEFAULT 'pending',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
COMMENT ON TABLE crm_commission IS '佣金记录表';
COMMENT ON COLUMN crm_commission.status IS 'pending-待结算 settled-已结算 cancelled-已取消';
CREATE INDEX IF NOT EXISTS idx_commission_distributor ON crm_commission (distributor_id);
CREATE INDEX IF NOT EXISTS idx_commission_order ON crm_commission (order_id);

-- 3. 拼团记录表
CREATE TABLE IF NOT EXISTS mp_group_buy (
    id              BIGSERIAL PRIMARY KEY,
    activity_id     BIGINT NOT NULL REFERENCES mall_activity(id),
    product_id      BIGINT NOT NULL REFERENCES sale_product(id),
    sku_id          BIGINT REFERENCES mall_sku(id),
    leader_id       BIGINT NOT NULL REFERENCES crm_customer(id),
    min_count       INT NOT NULL DEFAULT 2,
    current_count   INT NOT NULL DEFAULT 1,
    start_time      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    end_time        TIMESTAMPTZ NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'pending',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
COMMENT ON COLUMN mp_group_buy.status IS 'pending-拼团中 success-已成团 fail-已失败';
COMMENT ON TABLE mp_group_buy IS '拼团记录表';
CREATE INDEX IF NOT EXISTS idx_mp_group_buy_activity ON mp_group_buy (activity_id);
CREATE INDEX IF NOT EXISTS idx_mp_group_buy_leader ON mp_group_buy (leader_id);

-- 4. 拼团成员表
CREATE TABLE IF NOT EXISTS mp_group_buy_member (
    id              BIGSERIAL PRIMARY KEY,
    group_id        BIGINT NOT NULL REFERENCES mp_group_buy(id) ON DELETE CASCADE,
    customer_id     BIGINT NOT NULL REFERENCES crm_customer(id),
    order_id        BIGINT REFERENCES mall_order(id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
COMMENT ON TABLE mp_group_buy_member IS '拼团成员表';
CREATE INDEX IF NOT EXISTS idx_mp_group_member_group ON mp_group_buy_member (group_id);

-- 5. 商城初始商品分类数据
INSERT INTO sale_product_category (id, name, parent_id, icon, sort_order, status, created_by, created_at) VALUES
(1, '软件产品', NULL, '/icons/software.png', 1, 1, 1, NOW()),
(2, '硬件设备', NULL, '/icons/hardware.png', 2, 1, 1, NOW()),
(3, '技术服务', NULL, '/icons/service.png', 3, 1, 1, NOW()),
(11, 'CRM系统', 1, NULL, 1, 1, 1, NOW()),
(12, '办公软件', 1, NULL, 2, 1, 1, NOW()),
(21, '服务器', 2, NULL, 1, 1, 1, NOW()),
(22, '网络设备', 2, NULL, 2, 1, 1, NOW()),
(31, '实施服务', 3, NULL, 1, 1, 1, NOW()),
(32, '培训服务', 3, NULL, 2, 1, 1, NOW());

-- 6. 商城初始商品数据
INSERT INTO sale_product (id, category_id, name, description, cover_image, standard_price, unit, sort_order, status, created_by, created_at) VALUES
(1, 11, 'CRM标准版', '标准版CRM系统，适合中小团队', '/images/crm-std.png', 50000.00, '套', 1, 1, 1, NOW()),
(2, 11, 'CRM企业版', '企业版CRM系统，适合大型组织', '/images/crm-ent.png', 150000.00, '套', 2, 1, 1, NOW()),
(3, 31, '实施服务', 'CRM系统上门实施部署服务', '/images/impl.png', 3000.00, '人/天', 3, 1, 1, NOW()),
(4, 32, '培训服务', 'CRM系统使用培训服务', '/images/train.png', 2000.00, '人/天', 4, 1, 1, NOW());
