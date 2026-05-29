-- ============================================================================
-- CRM 系统数据库初始化脚本
-- 数据库：PostgreSQL 15+
-- 编码：UTF-8
-- ============================================================================

-- 创建数据库（需要超级用户权限，实际运行时可能需要手动创建）
-- CREATE DATABASE crm_db WITH ENCODING 'UTF8' LC_COLLATE 'zh_CN.UTF-8' LC_CTYPE 'zh_CN.UTF-8';

-- ============================================================================
-- 第一部分：系统管理域（sys_*）
-- ============================================================================

-- 1.1 部门表
CREATE TABLE IF NOT EXISTS sys_dept (
    id              BIGSERIAL PRIMARY KEY,
    parent_id       BIGINT NOT NULL DEFAULT 0,
    ancestors       VARCHAR(500) NOT NULL DEFAULT '' COMMENT '祖级列表（逗号分隔）',
    name            VARCHAR(100) NOT NULL,
    sort_order      INT NOT NULL DEFAULT 0,
    leader_id       BIGINT COMMENT '部门负责人',
    phone           VARCHAR(20),
    email           VARCHAR(100),
    status          SMALLINT NOT NULL DEFAULT 1,
    created_by      BIGINT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by      BIGINT,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ
);
COMMENT ON TABLE sys_dept IS '部门表';
COMMENT ON COLUMN sys_dept.status IS '状态：0-禁用 1-启用';

-- 1.2 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id              BIGSERIAL PRIMARY KEY,
    username        VARCHAR(100) NOT NULL,
    password        VARCHAR(255) NOT NULL,
    salt            VARCHAR(64),
    real_name       VARCHAR(100) NOT NULL,
    nickname        VARCHAR(100),
    avatar          VARCHAR(500),
    phone           VARCHAR(20),
    email           VARCHAR(100),
    gender          SMALLINT DEFAULT 0 COMMENT '0-未知 1-男 2-女',
    dept_id         BIGINT,
    post            VARCHAR(100) COMMENT '岗位',
    status          SMALLINT NOT NULL DEFAULT 1,
    last_login_ip   VARCHAR(50),
    last_login_at   TIMESTAMPTZ,
    pwd_error_count INT NOT NULL DEFAULT 0,
    pwd_updated_at  TIMESTAMPTZ,
    created_by      BIGINT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by      BIGINT,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ
);
COMMENT ON TABLE sys_user IS '系统用户表';
COMMENT ON COLUMN sys_user.status IS '状态：0-禁用 1-启用';

-- 1.3 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    role_code       VARCHAR(100) NOT NULL UNIQUE,
    data_scope      SMALLINT NOT NULL DEFAULT 1 COMMENT '1-本人 2-本部门 3-本部门及下属 4-全部 5-自定义',
    status          SMALLINT NOT NULL DEFAULT 1,
    sort_order      INT NOT NULL DEFAULT 0,
    remark          VARCHAR(500),
    created_by      BIGINT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by      BIGINT,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ
);
COMMENT ON TABLE sys_role IS '角色表';

-- 1.4 用户角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    role_id     BIGINT NOT NULL,
    UNIQUE (user_id, role_id)
);
COMMENT ON TABLE sys_user_role IS '用户角色关联表';

-- 1.5 菜单表（目录 / 菜单 / 按钮）
CREATE TABLE IF NOT EXISTS sys_menu (
    id              BIGSERIAL PRIMARY KEY,
    parent_id       BIGINT NOT NULL DEFAULT 0,
    name            VARCHAR(100) NOT NULL,
    menu_type       CHAR(1) NOT NULL COMMENT 'M-目录 C-菜单 F-按钮',
    icon            VARCHAR(100),
    route_path      VARCHAR(200),
    component       VARCHAR(200),
    permission_code VARCHAR(200) COMMENT '按钮权限标识',
    query_param     VARCHAR(200) COMMENT '路由参数',
    is_visible      BOOLEAN NOT NULL DEFAULT TRUE,
    is_frame        BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否外链',
    sort_order      INT NOT NULL DEFAULT 0,
    status          SMALLINT NOT NULL DEFAULT 1,
    remark          VARCHAR(500),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ
);
COMMENT ON TABLE sys_menu IS '菜单权限表';

-- 1.6 角色菜单关联表
CREATE TABLE IF NOT EXISTS sys_role_menu (
    id          BIGSERIAL PRIMARY KEY,
    role_id     BIGINT NOT NULL,
    menu_id     BIGINT NOT NULL,
    UNIQUE (role_id, menu_id)
);
COMMENT ON TABLE sys_role_menu IS '角色菜单关联表';

-- 1.7 角色数据范围-部门关联表（data_scope=5 时使用）
CREATE TABLE IF NOT EXISTS sys_role_dept (
    id          BIGSERIAL PRIMARY KEY,
    role_id     BIGINT NOT NULL,
    dept_id     BIGINT NOT NULL,
    UNIQUE (role_id, dept_id)
);
COMMENT ON TABLE sys_role_dept IS '角色数据范围-部门关联表';

-- 1.8 字典类型表
CREATE TABLE IF NOT EXISTS sys_dict_type (
    id              BIGSERIAL PRIMARY KEY,
    type_code       VARCHAR(100) NOT NULL UNIQUE,
    type_name       VARCHAR(100) NOT NULL,
    remark          VARCHAR(500),
    status          SMALLINT NOT NULL DEFAULT 1,
    created_by      BIGINT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ
);
COMMENT ON TABLE sys_dict_type IS '字典类型表';

-- 1.9 字典数据项表
CREATE TABLE IF NOT EXISTS sys_dict_item (
    id              BIGSERIAL PRIMARY KEY,
    type_code       VARCHAR(100) NOT NULL,
    item_code       VARCHAR(100) NOT NULL,
    item_value      VARCHAR(500) NOT NULL,
    sort_order      INT NOT NULL DEFAULT 0,
    css_class       VARCHAR(100),
    is_default      BOOLEAN NOT NULL DEFAULT FALSE,
    status          SMALLINT NOT NULL DEFAULT 1,
    remark          VARCHAR(500),
    created_by      BIGINT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ,
    UNIQUE (type_code, item_code)
);
COMMENT ON TABLE sys_dict_item IS '字典数据项表';

-- 1.10 系统配置表
CREATE TABLE IF NOT EXISTS sys_config (
    id              BIGSERIAL PRIMARY KEY,
    config_key      VARCHAR(100) NOT NULL UNIQUE,
    config_name     VARCHAR(100) NOT NULL,
    config_value    TEXT NOT NULL,
    config_type     SMALLINT NOT NULL DEFAULT 0 COMMENT '0-系统内置 1-自定义',
    is_public       BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否前端可见',
    remark          VARCHAR(500),
    created_by      BIGINT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
COMMENT ON TABLE sys_config IS '系统配置表';

-- 1.11 操作日志表
CREATE TABLE IF NOT EXISTS sys_operation_log (
    id              BIGSERIAL PRIMARY KEY,
    module          VARCHAR(50) NOT NULL COMMENT 'system/customer/sales/mall',
    action          VARCHAR(50) NOT NULL COMMENT 'create/update/delete/import/export/...',
    operator_id     BIGINT,
    operator_name   VARCHAR(100),
    target_type     VARCHAR(100) COMMENT '操作对象类型',
    target_id       BIGINT COMMENT '操作对象ID',
    detail          JSONB COMMENT '变更详情',
    request_url     VARCHAR(500),
    request_method  VARCHAR(10),
    request_params  TEXT,
    ip              VARCHAR(50),
    user_agent      VARCHAR(500),
    duration_ms     INT COMMENT '请求耗时',
    result_code     INT DEFAULT 200,
    error_msg       TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
COMMENT ON TABLE sys_operation_log IS '操作日志表';
CREATE INDEX idx_operation_log_module ON sys_operation_log (module);
CREATE INDEX idx_operation_log_action ON sys_operation_log (action);
CREATE INDEX idx_operation_log_operator ON sys_operation_log (operator_id);
CREATE INDEX idx_operation_log_target ON sys_operation_log (target_type, target_id);
CREATE INDEX idx_operation_log_created ON sys_operation_log (created_at);

-- 1.12 消息表
CREATE TABLE IF NOT EXISTS sys_message (
    id              BIGSERIAL PRIMARY KEY,
    receiver_id     BIGINT NOT NULL,
    channel         VARCHAR(50) NOT NULL COMMENT '站内信/企微/短信/小程序订阅',
    title           VARCHAR(200) NOT NULL,
    content         TEXT,
    biz_type        VARCHAR(50) COMMENT '业务类型：lead_assign/approval/system',
    biz_id          BIGINT COMMENT '业务ID',
    is_read         BOOLEAN NOT NULL DEFAULT FALSE,
    read_at         TIMESTAMPTZ,
    priority        VARCHAR(20) NOT NULL DEFAULT 'normal',
    created_by      BIGINT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
COMMENT ON TABLE sys_message IS '消息表';
CREATE INDEX idx_message_receiver ON sys_message (receiver_id, is_read);
CREATE INDEX idx_message_created ON sys_message (created_at);

-- 1.13 文件表
CREATE TABLE IF NOT EXISTS sys_file (
    id              BIGSERIAL PRIMARY KEY,
    file_name       VARCHAR(255) NOT NULL,
    original_name   VARCHAR(255) NOT NULL,
    file_size       BIGINT NOT NULL,
    file_type       VARCHAR(100),
    file_url        VARCHAR(500) NOT NULL,
    thumbnail_url   VARCHAR(500),
    storage_type    VARCHAR(50) NOT NULL DEFAULT 'minio' COMMENT 'minio/oss/local',
    biz_type        VARCHAR(50) COMMENT 'avatar/contract/attachment/product',
    biz_id          BIGINT COMMENT '业务ID',
    md5_hash        VARCHAR(64),
    is_public       BOOLEAN NOT NULL DEFAULT FALSE,
    created_by      BIGINT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ
);
COMMENT ON TABLE sys_file IS '文件表';
CREATE INDEX idx_file_biz ON sys_file (biz_type, biz_id);
CREATE INDEX idx_file_md5 ON sys_file (md5_hash);

-- 1.14 API密钥表（开放接口）
CREATE TABLE IF NOT EXISTS sys_api_key (
    id              BIGSERIAL PRIMARY KEY,
    app_name        VARCHAR(100) NOT NULL,
    api_key         VARCHAR(100) NOT NULL UNIQUE,
    api_secret      VARCHAR(255) NOT NULL COMMENT '仅创建和重置时明文显示',
    permissions     JSONB COMMENT '权限列表 ["customer:read","order:read"]',
    ip_whitelist    JSONB COMMENT 'IP白名单',
    expire_at       TIMESTAMPTZ,
    last_used_at    TIMESTAMPTZ,
    status          SMALLINT NOT NULL DEFAULT 1,
    created_by      BIGINT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ
);
COMMENT ON TABLE sys_api_key IS 'API密钥表';

-- 1.15 系统公告表
CREATE TABLE IF NOT EXISTS sys_notice (
    id              BIGSERIAL PRIMARY KEY,
    title           VARCHAR(200) NOT NULL,
    content         TEXT NOT NULL,
    notice_type     SMALLINT NOT NULL DEFAULT 1 COMMENT '1-公告 2-通知 3-提醒',
    status          SMALLINT NOT NULL DEFAULT 0 COMMENT '0-草稿 1-已发布',
    publish_at      TIMESTAMPTZ,
    created_by      BIGINT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ
);
COMMENT ON TABLE sys_notice IS '系统公告表';

-- 1.16 登录日志表
CREATE TABLE IF NOT EXISTS sys_login_log (
    id              BIGSERIAL PRIMARY KEY,
    username        VARCHAR(100) NOT NULL,
    login_ip        VARCHAR(50),
    login_location  VARCHAR(255),
    browser         VARCHAR(100),
    os              VARCHAR(100),
    status          SMALLINT NOT NULL DEFAULT 1 COMMENT '0-失败 1-成功',
    fail_reason     VARCHAR(200),
    login_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
COMMENT ON TABLE sys_login_log IS '登录日志表';
CREATE INDEX idx_login_log_username ON sys_login_log (username);
CREATE INDEX idx_login_log_at ON sys_login_log (login_at);

-- ============================================================================
-- 第二部分：客户管理域（crm_*）
-- ============================================================================

-- 2.1 客户等级表
CREATE TABLE IF NOT EXISTS crm_customer_level (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(50) NOT NULL,
    icon            VARCHAR(200),
    min_amount      DECIMAL(18,2) DEFAULT 0 COMMENT '年消费下限',
    max_amount      DECIMAL(18,2) DEFAULT 999999999.99 COMMENT '年消费上限',
    min_order_count INT DEFAULT 0,
    benefits        JSONB COMMENT '权益配置 {"discount":0.95,"freeShipping":true}',
    sort_order      INT NOT NULL DEFAULT 0,
    status          SMALLINT NOT NULL DEFAULT 1,
    remark          VARCHAR(500),
    created_by      BIGINT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ
);
COMMENT ON TABLE crm_customer_level IS '客户等级表';

-- 2.2 升降级规则表
CREATE TABLE IF NOT EXISTS crm_customer_level_rule (
    id              BIGSERIAL PRIMARY KEY,
    level_id        BIGINT NOT NULL,
    rule_type       VARCHAR(20) NOT NULL COMMENT 'upgrade/downgrade',
    condition_field VARCHAR(50) NOT NULL COMMENT 'amount/order_count/no_order_days',
    condition_operator VARCHAR(20) NOT NULL COMMENT 'gte/lte',
    condition_value DECIMAL(18,2) NOT NULL,
    period_days     INT COMMENT '评估周期天数',
    evaluate_cycle  VARCHAR(20) NOT NULL DEFAULT 'monthly' COMMENT 'daily/weekly/monthly',
    status          SMALLINT NOT NULL DEFAULT 1,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
COMMENT ON TABLE crm_customer_level_rule IS '客户升降级规则表';
CREATE INDEX idx_level_rule_level ON crm_customer_level_rule (level_id);

-- 2.3 标签表
CREATE TABLE IF NOT EXISTS crm_tag (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(50) NOT NULL,
    color           VARCHAR(20),
    type            VARCHAR(20) NOT NULL COMMENT 'manual-手动 auto-自动',
    remark          VARCHAR(500),
    status          SMALLINT NOT NULL DEFAULT 1,
    created_by      BIGINT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ
);
COMMENT ON TABLE crm_tag IS '标签表';

-- 2.4 客户表
CREATE TABLE IF NOT EXISTS crm_customer (
    id                  BIGSERIAL PRIMARY KEY,
    name                VARCHAR(100) NOT NULL,
    phone               VARCHAR(20),
    wechat_unionid      VARCHAR(100),
    wechat_openid       VARCHAR(100),
    company             VARCHAR(200),
    position            VARCHAR(100),
    birthday            DATE,
    gender              SMALLINT DEFAULT 0 COMMENT '0-未知 1-男 2-女',
    email               VARCHAR(100),
    province            VARCHAR(50),
    city                VARCHAR(50),
    district            VARCHAR(50),
    address             VARCHAR(500),
    source_channel      VARCHAR(50) COMMENT '来源渠道',
    level_id            BIGINT,
    owner_id            BIGINT COMMENT '负责人',
    last_contact_at     TIMESTAMPTZ COMMENT '最后联系时间',
    total_consumption   DECIMAL(18,2) DEFAULT 0 COMMENT '累计消费',
    order_count         INT DEFAULT 0 COMMENT '订单数',
    remark              TEXT,
    ext_json            JSONB COMMENT '扩展字段（自定义字段动态映射）',
    status              SMALLINT NOT NULL DEFAULT 1,
    created_by          BIGINT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by          BIGINT,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMPTZ
);
COMMENT ON TABLE crm_customer IS '客户表';
COMMENT ON COLUMN crm_customer.ext_json IS '扩展字段，存储动态自定义字段 {fieldKey: value}';
CREATE INDEX idx_customer_phone ON crm_customer (phone);
CREATE INDEX idx_customer_wechat ON crm_customer (wechat_unionid);
CREATE INDEX idx_customer_owner ON crm_customer (owner_id);
CREATE INDEX idx_customer_level ON crm_customer (level_id);
CREATE INDEX idx_customer_source ON crm_customer (source_channel);
CREATE INDEX idx_customer_created ON crm_customer (created_at);
CREATE INDEX idx_customer_name ON crm_customer USING gin (name gin_trgm_ops);

-- 2.5 客户标签关系表
CREATE TABLE IF NOT EXISTS crm_customer_tag (
    id              BIGSERIAL PRIMARY KEY,
    customer_id     BIGINT NOT NULL,
    tag_id          BIGINT NOT NULL,
    tag_type        VARCHAR(20) NOT NULL DEFAULT 'auto' COMMENT 'manual/auto',
    created_by      BIGINT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (customer_id, tag_id)
);
COMMENT ON TABLE crm_customer_tag IS '客户标签关系表';
CREATE INDEX idx_customer_tag_customer ON crm_customer_tag (customer_id);
CREATE INDEX idx_customer_tag_tag ON crm_customer_tag (tag_id);

-- 2.6 客户等级变更日志
CREATE TABLE IF NOT EXISTS crm_customer_level_log (
    id              BIGSERIAL PRIMARY KEY,
    customer_id     BIGINT NOT NULL,
    old_level_id    BIGINT,
    new_level_id    BIGINT NOT NULL,
    change_type     VARCHAR(20) NOT NULL COMMENT 'auto_upgrade/auto_downgrade/manual',
    reason          VARCHAR(500),
    operator_id     BIGINT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
COMMENT ON TABLE crm_customer_level_log IS '客户等级变更日志表';
CREATE INDEX idx_level_log_customer ON crm_customer_level_log (customer_id);

-- 2.7 联系人表
CREATE TABLE IF NOT EXISTS crm_contact (
    id                  BIGSERIAL PRIMARY KEY,
    customer_id         BIGINT NOT NULL,
    name                VARCHAR(100) NOT NULL,
    phone               VARCHAR(20),
    wechat_unionid      VARCHAR(100),
    email               VARCHAR(100),
    position            VARCHAR(100),
    department          VARCHAR(100),
    is_decision_maker   BOOLEAN NOT NULL DEFAULT FALSE,
    is_primary          BOOLEAN NOT NULL DEFAULT FALSE,
    birthday            DATE,
    gender              SMALLINT DEFAULT 0,
    remark              TEXT,
    ext_json            JSONB,
    sort_order          INT NOT NULL DEFAULT 0,
    created_by          BIGINT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMPTZ
);
COMMENT ON TABLE crm_contact IS '联系人表';
CREATE INDEX idx_contact_customer ON crm_contact (customer_id);

-- 2.8 线索表
CREATE TABLE IF NOT EXISTS crm_lead (
    id                  BIGSERIAL PRIMARY KEY,
    name                VARCHAR(100) NOT NULL,
    phone               VARCHAR(20),
    wechat_unionid      VARCHAR(100),
    wechat_openid       VARCHAR(100),
    company             VARCHAR(200),
    position            VARCHAR(100),
    province            VARCHAR(50),
    city                VARCHAR(50),
    industry            VARCHAR(50),
    source_channel      VARCHAR(50) NOT NULL COMMENT '来源渠道',
    status              VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT 'pending-待跟进 following-跟进中 converted-已转换 invalid-无效 merged-已合并',
    owner_id            BIGINT COMMENT '负责人',
    assign_type         VARCHAR(20) COMMENT 'auto-自动分配 manual-手动指派',
    assigned_at         TIMESTAMPTZ,
    pool_return_at      TIMESTAMPTZ COMMENT '回池时间',
    convert_customer_id BIGINT COMMENT '转换后的客户ID',
    convert_opportunity_id BIGINT COMMENT '转换后的商机ID',
    ext_json            JSONB,
    remark              TEXT,
    created_by          BIGINT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMPTZ
);
COMMENT ON TABLE crm_lead IS '线索表';
CREATE INDEX idx_lead_phone ON crm_lead (phone);
CREATE INDEX idx_lead_status ON crm_lead (status);
CREATE INDEX idx_lead_owner ON crm_lead (owner_id);
CREATE INDEX idx_lead_source ON crm_lead (source_channel);
CREATE INDEX idx_lead_created ON crm_lead (created_at);
CREATE INDEX idx_lead_pool_return ON crm_lead (pool_return_at);

-- 2.9 线索分配规则表
CREATE TABLE IF NOT EXISTS crm_lead_distribution_rule (
    id                  BIGSERIAL PRIMARY KEY,
    name                VARCHAR(100) NOT NULL,
    priority            INT NOT NULL DEFAULT 0,
    conditions          JSONB NOT NULL COMMENT '匹配条件 {"logic":"and","rules":[...]}',
    strategy            VARCHAR(30) NOT NULL COMMENT 'round_robin/weight/idle_longest/load_balanced',
    strategy_config     JSONB COMMENT '策略配置 {"weight":{"user1":1,"user2":2}}',
    target_type         VARCHAR(20) NOT NULL COMMENT 'user/role/dept',
    target_id           BIGINT NOT NULL,
    time_ranges         JSONB COMMENT '生效时间段 [{"weekday":"1-5","startTime":"09:00","endTime":"18:00"}]',
    max_daily_per_person INT DEFAULT 50,
    status              SMALLINT NOT NULL DEFAULT 1,
    created_by          BIGINT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMPTZ
);
COMMENT ON TABLE crm_lead_distribution_rule IS '线索分配规则表';

-- 2.10 线索分配日志表
CREATE TABLE IF NOT EXISTS crm_lead_distribution_log (
    id              BIGSERIAL PRIMARY KEY,
    rule_id         BIGINT,
    lead_id         BIGINT NOT NULL,
    from_owner_id   BIGINT,
    to_owner_id     BIGINT NOT NULL,
    assign_type     VARCHAR(20) NOT NULL COMMENT 'auto/manual/pool_return',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
COMMENT ON TABLE crm_lead_distribution_log IS '线索分配日志表';
CREATE INDEX idx_dist_log_lead ON crm_lead_distribution_log (lead_id);
CREATE INDEX idx_dist_log_owner ON crm_lead_distribution_log (to_owner_id);

-- 2.11 自动标签规则表
CREATE TABLE IF NOT EXISTS crm_auto_tag_rule (
    id              BIGSERIAL PRIMARY KEY,
    tag_id          BIGINT NOT NULL,
    rule_name       VARCHAR(100) NOT NULL,
    conditions      JSONB NOT NULL COMMENT '匹配条件 {"logic":"and","rules":[...]}',
    schedule        VARCHAR(50) COMMENT 'cron 表达式',
    last_execute_at TIMESTAMPTZ,
    status          SMALLINT NOT NULL DEFAULT 1,
    created_by      BIGINT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ
);
COMMENT ON TABLE crm_auto_tag_rule IS '自动标签规则表';

-- 2.12 客户分群表
CREATE TABLE IF NOT EXISTS crm_segment (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    conditions      JSONB NOT NULL COMMENT '筛选条件 {"logic":"and","rules":[...]}',
    is_dynamic      BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否动态分群',
    member_count    INT NOT NULL DEFAULT 0,
    last_refresh_at TIMESTAMPTZ,
    status          SMALLINT NOT NULL DEFAULT 1,
    created_by      BIGINT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ
);
COMMENT ON TABLE crm_segment IS '客户分群表';

-- 2.13 分群成员表
CREATE TABLE IF NOT EXISTS crm_segment_member (
    id              BIGSERIAL PRIMARY KEY,
    segment_id      BIGINT NOT NULL,
    customer_id     BIGINT NOT NULL,
    join_type       VARCHAR(20) NOT NULL DEFAULT 'auto' COMMENT 'auto-自动匹配 manual-手动添加',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (segment_id, customer_id)
);
COMMENT ON TABLE crm_segment_member IS '分群成员表';
CREATE INDEX idx_segment_member_segment ON crm_segment_member (segment_id);

-- 2.14 客户操作日志表（业务级，区别于系统操作日志）
CREATE TABLE IF NOT EXISTS crm_activity_log (
    id              BIGSERIAL PRIMARY KEY,
    customer_id     BIGINT NOT NULL,
    contact_id      BIGINT,
    action          VARCHAR(50) NOT NULL COMMENT 'create/update/transfer/tag/level_change/merge',
    detail          JSONB COMMENT '操作详情',
    operator_id     BIGINT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
COMMENT ON TABLE crm_activity_log IS '客户操作日志表';
CREATE INDEX idx_activity_customer ON crm_activity_log (customer_id);
CREATE INDEX idx_activity_created ON crm_activity_log (created_at);

-- 2.15 自定义字段定义表（客户扩展字段元数据）
CREATE TABLE IF NOT EXISTS crm_custom_field_def (
    id              BIGSERIAL PRIMARY KEY,
    entity_type     VARCHAR(50) NOT NULL COMMENT 'customer/lead/contact',
    field_key       VARCHAR(100) NOT NULL,
    field_name      VARCHAR(100) NOT NULL,
    field_type      VARCHAR(30) NOT NULL COMMENT 'text/number/date/select/multi_select/boolean',
    options         JSONB COMMENT '选项列表（select类型）[{"label":"A","value":"a"}]',
    is_required     BOOLEAN NOT NULL DEFAULT FALSE,
    sort_order      INT NOT NULL DEFAULT 0,
    status          SMALLINT NOT NULL DEFAULT 1,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ,
    UNIQUE (entity_type, field_key)
);
COMMENT ON TABLE crm_custom_field_def IS '自定义字段定义表';

-- ============================================================================
-- 第三部分：销售管理域（sale_*）
-- ============================================================================

-- 3.1 产品分类表
CREATE TABLE IF NOT EXISTS sale_product_category (
    id              BIGSERIAL PRIMARY KEY,
    parent_id       BIGINT NOT NULL DEFAULT 0,
    name            VARCHAR(100) NOT NULL,
    icon            VARCHAR(200),
    sort_order      INT NOT NULL DEFAULT 0,
    status          SMALLINT NOT NULL DEFAULT 1,
    created_by      BIGINT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ
);
COMMENT ON TABLE sale_product_category IS '产品分类表';

-- 3.2 产品表（报价单 & 商城共用产品库）
CREATE TABLE IF NOT EXISTS sale_product (
    id                  BIGSERIAL PRIMARY KEY,
    category_id         BIGINT,
    name                VARCHAR(200) NOT NULL,
    unit                VARCHAR(20) COMMENT '个/套/次/年',
    standard_price      DECIMAL(18,2) COMMENT '标准价',
    cost_price          DECIMAL(18,2) COMMENT '成本价',
    member_price        DECIMAL(18,2) COMMENT '会员价',
    channel_price       DECIMAL(18,2) COMMENT '渠道价',
    description         TEXT,
    cover_image         VARCHAR(500),
    images              JSONB COMMENT '商品图片列表',
    status              SMALLINT NOT NULL DEFAULT 0 COMMENT '0-下架 1-上架',
    sort_order          INT DEFAULT 0,
    ext_json            JSONB COMMENT '扩展属性',
    created_by          BIGINT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMPTZ
);
COMMENT ON TABLE sale_product IS '产品表（报价单&商城共用产品库）';
CREATE INDEX idx_product_category ON sale_product (category_id);
CREATE INDEX idx_product_status ON sale_product (status);
CREATE INDEX idx_product_name ON sale_product USING gin (name gin_trgm_ops);

-- 3.3 SKU表（商城多规格）
CREATE TABLE IF NOT EXISTS mall_sku (
    id              BIGSERIAL PRIMARY KEY,
    product_id      BIGINT NOT NULL,
    specs           JSONB COMMENT '规格属性 [{"key":"颜色","value":"黑色"},{"key":"尺寸","value":"L"}]',
    price           DECIMAL(18,2) NOT NULL,
    stock           INT NOT NULL DEFAULT 0,
    frozen_stock    INT NOT NULL DEFAULT 0 COMMENT '冻结库存',
    sku_code        VARCHAR(100) COMMENT 'SKU编码',
    cover_image     VARCHAR(500),
    status          SMALLINT NOT NULL DEFAULT 1,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ
);
COMMENT ON TABLE mall_sku IS 'SKU表（商城多规格）';
CREATE INDEX idx_sku_product ON mall_sku (product_id);
CREATE INDEX idx_sku_code ON mall_sku (sku_code);

-- 3.4 销售阶段定义表
CREATE TABLE IF NOT EXISTS sale_opportunity_stage (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(50) NOT NULL,
    sort_order      INT NOT NULL,
    probability     INT NOT NULL DEFAULT 0 COMMENT '赢单概率 %',
    category        VARCHAR(20) NOT NULL COMMENT 'open-开放阶段 win-赢单 lose-输单',
    status          SMALLINT NOT NULL DEFAULT 1,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ
);
COMMENT ON TABLE sale_opportunity_stage IS '销售阶段定义表';

-- 3.5 商机表
CREATE TABLE IF NOT EXISTS sale_opportunity (
    id                  BIGSERIAL PRIMARY KEY,
    customer_id         BIGINT NOT NULL,
    contact_id          BIGINT,
    name                VARCHAR(200) NOT NULL,
    stage_id            BIGINT NOT NULL,
    expected_amount     DECIMAL(18,2) COMMENT '预计成交金额',
    final_amount        DECIMAL(18,2) COMMENT '最终成交金额',
    expected_close_date DATE COMMENT '预计成交日期',
    budget              DECIMAL(18,2) COMMENT '客户预算',
    decision_maker      VARCHAR(100) COMMENT '决策人',
    competition         TEXT COMMENT '竞争情况',
    pain_points         TEXT COMMENT '客户痛点',
    requirements        TEXT COMMENT '客户需求',
    solution            TEXT COMMENT '解决方案',
    win_probability     INT DEFAULT 0 COMMENT '赢单概率%',
    lose_reason         VARCHAR(500),
    lose_reason_detail  TEXT,
    owner_id            BIGINT NOT NULL,
    source              VARCHAR(50) COMMENT '商机来源',
    remark              TEXT,
    ext_json            JSONB,
    created_by          BIGINT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by          BIGINT,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMPTZ
);
COMMENT ON TABLE sale_opportunity IS '商机表';
CREATE INDEX idx_opp_customer ON sale_opportunity (customer_id);
CREATE INDEX idx_opp_stage ON sale_opportunity (stage_id);
CREATE INDEX idx_opp_owner ON sale_opportunity (owner_id);
CREATE INDEX idx_opp_created ON sale_opportunity (created_at);
CREATE INDEX idx_opp_close_date ON sale_opportunity (expected_close_date);

-- 3.6 商机参与人表
CREATE TABLE IF NOT EXISTS sale_opportunity_participant (
    id              BIGSERIAL PRIMARY KEY,
    opportunity_id  BIGINT NOT NULL,
    user_id         BIGINT NOT NULL,
    role            VARCHAR(50) COMMENT '参与角色',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (opportunity_id, user_id)
);
COMMENT ON TABLE sale_opportunity_participant IS '商机参与人表';
CREATE INDEX idx_opp_participant ON sale_opportunity_participant (opportunity_id);

-- 3.7 商机阶段变更日志
CREATE TABLE IF NOT EXISTS sale_opportunity_stage_log (
    id              BIGSERIAL PRIMARY KEY,
    opportunity_id  BIGINT NOT NULL,
    from_stage_id   BIGINT,
    to_stage_id     BIGINT NOT NULL,
    from_amount     DECIMAL(18,2),
    to_amount       DECIMAL(18,2),
    remark          TEXT,
    operator_id     BIGINT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
COMMENT ON TABLE sale_opportunity_stage_log IS '商机阶段变更日志表';
CREATE INDEX idx_opp_stage_log_opp ON sale_opportunity_stage_log (opportunity_id);

-- 3.8 跟进记录表
CREATE TABLE IF NOT EXISTS sale_follow_up (
    id              BIGSERIAL PRIMARY KEY,
    customer_id     BIGINT NOT NULL,
    contact_id      BIGINT,
    opportunity_id  BIGINT,
    type            VARCHAR(20) NOT NULL COMMENT 'call-电话 visit-拜访 online-线上 chat-聊天 other',
    content         TEXT NOT NULL,
    voice_url       VARCHAR(500) COMMENT '录音文件URL',
    image_urls      JSONB COMMENT '图片列表',
    location        JSONB COMMENT '位置 {"lng":113.95,"lat":22.54,"address":"..."}',
    next_plan       TEXT COMMENT '下一步计划',
    next_plan_date  DATE,
    is_important    BOOLEAN NOT NULL DEFAULT FALSE,
    tags            JSONB COMMENT '跟进标签 ["需求明确","高意向"]',
    creator_id      BIGINT NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ
);
COMMENT ON TABLE sale_follow_up IS '跟进记录表';
CREATE INDEX idx_fu_customer ON sale_follow_up (customer_id);
CREATE INDEX idx_fu_opportunity ON sale_follow_up (opportunity_id);
CREATE INDEX idx_fu_creator ON sale_follow_up (creator_id);
CREATE INDEX idx_fu_created ON sale_follow_up (created_at);
CREATE INDEX idx_fu_type ON sale_follow_up (type);

-- 3.9 拜访计划表（日程）
CREATE TABLE IF NOT EXISTS sale_appointment (
    id              BIGSERIAL PRIMARY KEY,
    customer_id     BIGINT NOT NULL,
    contact_id      BIGINT,
    title           VARCHAR(200) NOT NULL,
    description     TEXT,
    appointment_date DATE NOT NULL,
    start_time      TIME,
    end_time        TIME,
    location        VARCHAR(500),
    longitude       DECIMAL(10,7),
    latitude        DECIMAL(10,7),
    type            VARCHAR(20) NOT NULL DEFAULT 'visit' COMMENT 'visit-拜访 meeting-会议 other',
    remind_before   INT DEFAULT 30 COMMENT '提前N分钟提醒',
    status          VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT 'pending-待进行 checked_in-已签到 completed-已完成 cancelled-已取消',
    checkin_at      TIMESTAMPTZ,
    checkin_photo   JSONB COMMENT '签到照片',
    summary         TEXT COMMENT '拜访总结',
    cancel_reason   VARCHAR(500),
    follow_up_id    BIGINT COMMENT '关联跟进记录',
    owner_id        BIGINT NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ
);
COMMENT ON TABLE sale_appointment IS '拜访计划表（日程）';
CREATE INDEX idx_appt_customer ON sale_appointment (customer_id);
CREATE INDEX idx_appt_owner ON sale_appointment (owner_id);
CREATE INDEX idx_appt_date ON sale_appointment (appointment_date);
CREATE INDEX idx_appt_status ON sale_appointment (status);

-- 3.10 报价单表
CREATE TABLE IF NOT EXISTS sale_quotation (
    id                  BIGSERIAL PRIMARY KEY,
    quotation_no        VARCHAR(100) NOT NULL UNIQUE,
    customer_id         BIGINT NOT NULL,
    opportunity_id      BIGINT,
    contact_id          BIGINT,
    total_amount        DECIMAL(18,2) NOT NULL DEFAULT 0,
    discount_amount     DECIMAL(18,2) DEFAULT 0,
    final_amount        DECIMAL(18,2) NOT NULL DEFAULT 0,
    valid_until         DATE,
    payment_terms       TEXT,
    delivery_terms      TEXT,
    status              VARCHAR(30) NOT NULL DEFAULT 'draft'
                        COMMENT 'draft-草稿 pending_approval-审批中 approved-已通过 rejected-已驳回 void-已作废',
    remark              TEXT,
    created_by          BIGINT NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by          BIGINT,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMPTZ
);
COMMENT ON TABLE sale_quotation IS '报价单表';
CREATE INDEX idx_quote_customer ON sale_quotation (customer_id);
CREATE INDEX idx_quote_opportunity ON sale_quotation (opportunity_id);
CREATE INDEX idx_quote_status ON sale_quotation (status);
CREATE INDEX idx_quote_created ON sale_quotation (created_at);

-- 3.11 报价明细表
CREATE TABLE IF NOT EXISTS sale_quotation_item (
    id              BIGSERIAL PRIMARY KEY,
    quotation_id    BIGINT NOT NULL,
    product_id      BIGINT,
    product_name    VARCHAR(200) NOT NULL,
    sku_id          BIGINT,
    quantity        INT NOT NULL DEFAULT 1,
    unit_price      DECIMAL(18,2) NOT NULL,
    discount_rate   DECIMAL(5,4) DEFAULT 1.0000,
    subtotal        DECIMAL(18,2) NOT NULL,
    remark          VARCHAR(500),
    sort_order      INT DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
COMMENT ON TABLE sale_quotation_item IS '报价明细表';
CREATE INDEX idx_quote_item_quote ON sale_quotation_item (quotation_id);

-- 3.12 合同表
CREATE TABLE IF NOT EXISTS sale_contract (
    id                  BIGSERIAL PRIMARY KEY,
    contract_no         VARCHAR(100) NOT NULL UNIQUE,
    customer_id         BIGINT NOT NULL,
    opportunity_id      BIGINT,
    quotation_id        BIGINT,
    template_id         BIGINT,
    title               VARCHAR(200) NOT NULL,
    total_amount        DECIMAL(18,2) NOT NULL,
    paid_amount         DECIMAL(18,2) DEFAULT 0 COMMENT '已回款金额',
    payment_terms       TEXT,
    valid_from          DATE,
    valid_until         DATE,
    sign_type           VARCHAR(20) COMMENT 'electronic-电子签 manual-线下签署',
    sign_platform       VARCHAR(50) COMMENT 'fadada/esign',
    sign_url            VARCHAR(500),
    sign_status         VARCHAR(30) NOT NULL DEFAULT 'draft'
                        COMMENT 'draft-草稿 pending_sign-待签署 signed-已签署 cancelled-已作废',
    signer_name         VARCHAR(100),
    signer_phone        VARCHAR(20),
    file_url            VARCHAR(500) COMMENT '合同文件',
    remark              TEXT,
    ext_json            JSONB,
    created_by          BIGINT NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by          BIGINT,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMPTZ
);
COMMENT ON TABLE sale_contract IS '合同表';
CREATE INDEX idx_contract_customer ON sale_contract (customer_id);
CREATE INDEX idx_contract_opportunity ON sale_contract (opportunity_id);
CREATE INDEX idx_contract_status ON sale_contract (sign_status);
CREATE INDEX idx_contract_created ON sale_contract (created_at);

-- 3.13 合同明细表
CREATE TABLE IF NOT EXISTS sale_contract_item (
    id              BIGSERIAL PRIMARY KEY,
    contract_id     BIGINT NOT NULL,
    product_name    VARCHAR(200) NOT NULL,
    quantity        INT NOT NULL DEFAULT 1,
    unit_price      DECIMAL(18,2) NOT NULL,
    subtotal        DECIMAL(18,2) NOT NULL,
    remark          VARCHAR(500),
    sort_order      INT DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
COMMENT ON TABLE sale_contract_item IS '合同明细表';
CREATE INDEX idx_contract_item_contract ON sale_contract_item (contract_id);

-- 3.14 合同模板表
CREATE TABLE IF NOT EXISTS sale_contract_template (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    file_url        VARCHAR(500) NOT NULL,
    fields          JSONB COMMENT '模板字段定义 [{"fieldKey":"contractNo","fieldName":"合同编号","fieldType":"text"}]',
    status          SMALLINT NOT NULL DEFAULT 1,
    created_by      BIGINT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ
);
COMMENT ON TABLE sale_contract_template IS '合同模板表';

-- 3.15 回款计划表
CREATE TABLE IF NOT EXISTS sale_payment_plan (
    id                  BIGSERIAL PRIMARY KEY,
    contract_id         BIGINT NOT NULL,
    stage               INT NOT NULL COMMENT '第几期',
    stage_name          VARCHAR(100),
    expected_amount     DECIMAL(18,2) NOT NULL,
    actual_amount       DECIMAL(18,2) DEFAULT 0,
    expected_date       DATE NOT NULL,
    paid_date           DATE,
    payment_method      VARCHAR(50) COMMENT 'bank_transfer/wechat/alipay/cash',
    voucher_urls        JSONB COMMENT '付款凭证',
    status              VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT 'pending-待回款 paid-已回款 overdue-逾期',
    remark              TEXT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
COMMENT ON TABLE sale_payment_plan IS '回款计划表';
CREATE INDEX idx_payment_contract ON sale_payment_plan (contract_id);
CREATE INDEX idx_payment_status ON sale_payment_plan (status);
CREATE INDEX idx_payment_date ON sale_payment_plan (expected_date);

-- 3.16 发票表
CREATE TABLE IF NOT EXISTS sale_invoice (
    id                  BIGSERIAL PRIMARY KEY,
    invoice_no          VARCHAR(100) UNIQUE COMMENT '发票号码',
    contract_id         BIGINT NOT NULL,
    customer_id         BIGINT NOT NULL,
    title               VARCHAR(200) NOT NULL COMMENT '发票抬头',
    tax_id              VARCHAR(50) COMMENT '税号',
    invoice_type        VARCHAR(30) NOT NULL COMMENT 'vat_special-专票 vat_normal-普票 electronic-电子票',
    amount              DECIMAL(18,2) NOT NULL,
    content             VARCHAR(500) COMMENT '开票内容',
    status              VARCHAR(30) NOT NULL DEFAULT 'pending'
                        COMMENT 'pending-待开票 issued-已开票 shipped-已邮寄 confirmed-已签收 cancelled-已作废',
    issue_date          DATE COMMENT '开票日期',
    file_url            VARCHAR(500) COMMENT '发票文件',
    express_company     VARCHAR(100),
    express_no          VARCHAR(100),
    ship_date           DATE,
    receive_email       VARCHAR(100),
    remark              TEXT,
    created_by          BIGINT NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMPTZ
);
COMMENT ON TABLE sale_invoice IS '发票表';
CREATE INDEX idx_invoice_contract ON sale_invoice (contract_id);
CREATE INDEX idx_invoice_customer ON sale_invoice (customer_id);
CREATE INDEX idx_invoice_status ON sale_invoice (status);

-- ============================================================================
-- 第四部分：商城交易域（mall_*）
-- ============================================================================

-- 4.1 商城订单表
CREATE TABLE IF NOT EXISTS mall_order (
    id                  BIGSERIAL PRIMARY KEY,
    order_no            VARCHAR(100) NOT NULL UNIQUE,
    customer_id         BIGINT NOT NULL,
    total_amount        DECIMAL(18,2) NOT NULL,
    discount_amount     DECIMAL(18,2) DEFAULT 0,
    shipping_fee        DECIMAL(18,2) DEFAULT 0,
    final_amount        DECIMAL(18,2) NOT NULL,
    payment_method      VARCHAR(30),
    payment_id          BIGINT,
    status              VARCHAR(30) NOT NULL DEFAULT 'pending'
                        COMMENT 'pending-待支付 paid-已支付 shipped-已发货 completed-已完成 cancelled-已取消 refunding-退款中',
    receiver_name       VARCHAR(100),
    receiver_phone      VARCHAR(20),
    receiver_address    VARCHAR(500),
    express_company     VARCHAR(100),
    express_no          VARCHAR(100),
    customer_remark     TEXT,
    paid_at             TIMESTAMPTZ,
    shipped_at          TIMESTAMPTZ,
    completed_at        TIMESTAMPTZ,
    pickup_code         VARCHAR(20) COMMENT '自提核销码',
    pickup_at           TIMESTAMPTZ,
    created_by          BIGINT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMPTZ
);
COMMENT ON TABLE mall_order IS '商城订单表';
CREATE INDEX idx_order_customer ON mall_order (customer_id);
CREATE INDEX idx_order_no ON mall_order (order_no);
CREATE INDEX idx_order_status ON mall_order (status);
CREATE INDEX idx_order_created ON mall_order (created_at);

-- 4.2 订单明细表
CREATE TABLE IF NOT EXISTS mall_order_item (
    id              BIGSERIAL PRIMARY KEY,
    order_id        BIGINT NOT NULL,
    product_id      BIGINT NOT NULL,
    sku_id          BIGINT,
    product_name    VARCHAR(200) NOT NULL,
    sku_specs       JSONB COMMENT '购买时的规格',
    quantity        INT NOT NULL,
    unit_price      DECIMAL(18,2) NOT NULL,
    subtotal        DECIMAL(18,2) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
COMMENT ON TABLE mall_order_item IS '订单明细表';
CREATE INDEX idx_order_item_order ON mall_order_item (order_id);

-- 4.3 购物车表
CREATE TABLE IF NOT EXISTS mall_cart_item (
    id              BIGSERIAL PRIMARY KEY,
    customer_id     BIGINT NOT NULL,
    product_id      BIGINT NOT NULL,
    sku_id          BIGINT,
    quantity        INT NOT NULL DEFAULT 1,
    selected        BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (customer_id, sku_id)
);
COMMENT ON TABLE mall_cart_item IS '购物车表';
CREATE INDEX idx_cart_customer ON mall_cart_item (customer_id);

-- 4.4 优惠券定义表
CREATE TABLE IF NOT EXISTS mall_coupon_define (
    id                  BIGSERIAL PRIMARY KEY,
    name                VARCHAR(100) NOT NULL,
    type                VARCHAR(30) NOT NULL COMMENT 'full_reduce-满减 discount-折扣 new_user-新人专享',
    value               DECIMAL(18,2) NOT NULL COMMENT '面值或折扣率',
    condition_amount    DECIMAL(18,2) DEFAULT 0 COMMENT '满减条件（0-无门槛）',
    total_count         INT NOT NULL COMMENT '发行总量',
    used_count          INT DEFAULT 0,
    per_user_limit      INT DEFAULT 1,
    valid_from          TIMESTAMPTZ NOT NULL,
    valid_until         TIMESTAMPTZ NOT NULL,
    scope               VARCHAR(20) NOT NULL DEFAULT 'all' COMMENT 'all-全场指定 specific-指定商品',
    product_ids         JSONB COMMENT '指定商品ID列表',
    channels            JSONB COMMENT '可用渠道',
    status              SMALLINT NOT NULL DEFAULT 0 COMMENT '0-未发布 1-已发布 2-已结束',
    created_by          BIGINT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMPTZ
);
COMMENT ON TABLE mall_coupon_define IS '优惠券定义表';
CREATE INDEX idx_coupon_type ON mall_coupon_define (type);
CREATE INDEX idx_coupon_valid ON mall_coupon_define (valid_from, valid_until);

-- 4.5 用户优惠券表
CREATE TABLE IF NOT EXISTS mall_coupon (
    id              BIGSERIAL PRIMARY KEY,
    define_id       BIGINT NOT NULL,
    customer_id     BIGINT NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'unused' COMMENT 'unused-未使用 used-已使用 expired-已过期',
    used_at         TIMESTAMPTZ,
    order_id        BIGINT,
    received_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
COMMENT ON TABLE mall_coupon IS '用户优惠券表';
CREATE INDEX idx_coupon_user ON mall_coupon (customer_id, status);
CREATE INDEX idx_coupon_define ON mall_coupon (define_id);

-- 4.6 支付记录表
CREATE TABLE IF NOT EXISTS mall_payment (
    id                  BIGSERIAL PRIMARY KEY,
    payment_no          VARCHAR(100) NOT NULL UNIQUE,
    order_id            BIGINT NOT NULL,
    order_type          VARCHAR(20) NOT NULL DEFAULT 'mall_order' COMMENT 'mall_order-商城订单',
    customer_id         BIGINT NOT NULL,
    payment_method      VARCHAR(30) NOT NULL COMMENT 'wechat-微信支付 alipay-支付宝',
    amount              DECIMAL(18,2) NOT NULL,
    status              VARCHAR(20) NOT NULL DEFAULT 'pending'
                        COMMENT 'pending-待支付 success-已成功 fail-已失败 refunding-退款中 refunded-已退款',
    transaction_id      VARCHAR(100) COMMENT '微信/支付宝交易号',
    open_id             VARCHAR(100),
    prepay_id           VARCHAR(100),
    pay_params          JSONB COMMENT '调起支付参数',
    paid_at             TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
COMMENT ON TABLE mall_payment IS '支付记录表';
CREATE INDEX idx_payment_order ON mall_payment (order_id);
CREATE INDEX idx_payment_no ON mall_payment (payment_no);
CREATE INDEX idx_payment_status ON mall_payment (status);

-- 4.7 退款记录表
CREATE TABLE IF NOT EXISTS mall_refund (
    id                  BIGSERIAL PRIMARY KEY,
    refund_no           VARCHAR(100) NOT NULL UNIQUE,
    order_id            BIGINT NOT NULL,
    payment_id          BIGINT,
    customer_id         BIGINT NOT NULL,
    refund_amount       DECIMAL(18,2) NOT NULL,
    reason              VARCHAR(500) NOT NULL,
    status              VARCHAR(30) NOT NULL DEFAULT 'pending'
                        COMMENT 'pending-待审核 approved-已通过 rejected-已驳回 completed-已退款',
    review_comment      VARCHAR(500),
    transaction_refund_id VARCHAR(100) COMMENT '微信退款单号',
    completed_at        TIMESTAMPTZ,
    created_by          BIGINT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMPTZ
);
COMMENT ON TABLE mall_refund IS '退款记录表';
CREATE INDEX idx_refund_order ON mall_refund (order_id);

-- 4.8 营销活动表
CREATE TABLE IF NOT EXISTS mall_activity (
    id                  BIGSERIAL PRIMARY KEY,
    name                VARCHAR(100) NOT NULL,
    type                VARCHAR(30) NOT NULL COMMENT 'seckill-秒杀 group-拼团 cut-砍价 points-积分兑换',
    start_time          TIMESTAMPTZ NOT NULL,
    end_time            TIMESTAMPTZ NOT NULL,
    rules               JSONB COMMENT '活动规则',
    status              SMALLINT NOT NULL DEFAULT 0 COMMENT '0-草稿 1-已发布 2-已结束',
    created_by          BIGINT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMPTZ
);
COMMENT ON TABLE mall_activity IS '营销活动表';
CREATE INDEX idx_activity_type ON mall_activity (type);
CREATE INDEX idx_activity_time ON mall_activity (start_time, end_time);

-- 4.9 活动商品关联表
CREATE TABLE IF NOT EXISTS mall_activity_product (
    id              BIGSERIAL PRIMARY KEY,
    activity_id     BIGINT NOT NULL,
    product_id      BIGINT NOT NULL,
    sku_id          BIGINT,
    activity_price  DECIMAL(18,2) NOT NULL,
    total_stock     INT NOT NULL,
    sold_stock      INT DEFAULT 0,
    limit_per_user  INT DEFAULT 1,
    UNIQUE (activity_id, sku_id)
);
COMMENT ON TABLE mall_activity_product IS '活动商品关联表';
CREATE INDEX idx_activity_product_activity ON mall_activity_product (activity_id);

-- 4.10 分销关系表
CREATE TABLE IF NOT EXISTS mall_distribution (
    id              BIGSERIAL PRIMARY KEY,
    referrer_id     BIGINT NOT NULL COMMENT '推荐人客户ID',
    customer_id     BIGINT NOT NULL COMMENT '被推荐人客户ID',
    order_id        BIGINT,
    commission_rate DECIMAL(5,4) COMMENT '佣金比例',
    commission_amount DECIMAL(18,2) DEFAULT 0,
    status          VARCHAR(20) NOT NULL DEFAULT 'pending'
                    COMMENT 'pending-待结算 settled-已结算 cancelled-已取消',
    settled_at      TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
COMMENT ON TABLE mall_distribution IS '分销关系表';
CREATE INDEX idx_distribution_referrer ON mall_distribution (referrer_id);
CREATE INDEX idx_distribution_customer ON mall_distribution (customer_id);

-- ============================================================================
-- 第五部分：办公协同域（coll_* / wecom_*）
-- ============================================================================

-- 5.1 审批流程定义表
CREATE TABLE IF NOT EXISTS coll_approval_define (
    id                  BIGSERIAL PRIMARY KEY,
    name                VARCHAR(100) NOT NULL,
    biz_type            VARCHAR(50) NOT NULL COMMENT 'quotation/contract/order/refund',
    trigger_condition   JSONB COMMENT '触发条件 {"field":"discountRate","operator":"lt","value":0.9}',
    status              SMALLINT NOT NULL DEFAULT 0 COMMENT '0-草稿 1-已启用 2-已停用',
    remark              VARCHAR(500),
    created_by          BIGINT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMPTZ
);
COMMENT ON TABLE coll_approval_define IS '审批流程定义表';
CREATE INDEX idx_approval_define_biz ON coll_approval_define (biz_type);

-- 5.2 审批流程步骤表
CREATE TABLE IF NOT EXISTS coll_approval_define_step (
    id              BIGSERIAL PRIMARY KEY,
    define_id       BIGINT NOT NULL,
    step_id         INT NOT NULL,
    step_name       VARCHAR(100) NOT NULL,
    approver_type   VARCHAR(30) NOT NULL
                    COMMENT 'user-指定用户 role-角色 dept_leader-部门负责人 upstream_leader-上级 self_choose-自选',
    approver_ids    JSONB NOT NULL COMMENT '审批人ID列表',
    step_type       VARCHAR(30) NOT NULL DEFAULT 'approve_or_reject' COMMENT 'approve_or_reject/countersign',
    sort_order      INT NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
COMMENT ON TABLE coll_approval_define_step IS '审批流程步骤表';
CREATE INDEX idx_approval_step_define ON coll_approval_define_step (define_id);

-- 5.3 审批实例表
CREATE TABLE IF NOT EXISTS coll_approval_instance (
    id                  BIGSERIAL PRIMARY KEY,
    define_id           BIGINT NOT NULL,
    biz_type            VARCHAR(50) NOT NULL,
    biz_id              BIGINT NOT NULL COMMENT '关联业务ID',
    form_data           JSONB COMMENT '审批表单数据',
    status              VARCHAR(30) NOT NULL DEFAULT 'pending'
                        COMMENT 'pending-待审批 approved-已通过 rejected-已驳回 recalled-已撤销',
    current_step_id     INT,
    applicant_id        BIGINT NOT NULL,
    applicant_name      VARCHAR(100),
    completed_at        TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
COMMENT ON TABLE coll_approval_instance IS '审批实例表';
CREATE INDEX idx_approval_inst_define ON coll_approval_instance (define_id);
CREATE INDEX idx_approval_inst_biz ON coll_approval_instance (biz_type, biz_id);
CREATE INDEX idx_approval_inst_applicant ON coll_approval_instance (applicant_id);
CREATE INDEX idx_approval_inst_status ON coll_approval_instance (status);

-- 5.4 审批节点处理记录表
CREATE TABLE IF NOT EXISTS coll_approval_node_record (
    id              BIGSERIAL PRIMARY KEY,
    instance_id     BIGINT NOT NULL,
    step_id         INT NOT NULL,
    step_name       VARCHAR(100),
    approver_id     BIGINT NOT NULL,
    approver_name   VARCHAR(100),
    action          VARCHAR(20) NOT NULL COMMENT 'approve-通过 reject-驳回 transfer-转交',
    comment         TEXT,
    signature_image VARCHAR(500),
    acted_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
COMMENT ON TABLE coll_approval_node_record IS '审批节点处理记录表';
CREATE INDEX idx_approval_record_instance ON coll_approval_node_record (instance_id);
CREATE INDEX idx_approval_record_approver ON coll_approval_node_record (approver_id);

-- 5.5 服务工单表
CREATE TABLE IF NOT EXISTS coll_service_ticket (
    id                  BIGSERIAL PRIMARY KEY,
    ticket_no           VARCHAR(100) NOT NULL UNIQUE,
    customer_id         BIGINT NOT NULL,
    type                VARCHAR(30) NOT NULL COMMENT 'repair-报修 install-安装 complaint-投诉 other',
    title               VARCHAR(200) NOT NULL,
    description         TEXT,
    priority            VARCHAR(20) NOT NULL DEFAULT 'normal' COMMENT 'low/normal/urgent/critical',
    source              VARCHAR(30) COMMENT 'phone/wechat/mall/manual',
    attachment_ids      JSONB COMMENT '附件文件ID列表',
    assignee_id         BIGINT COMMENT '处理人',
    status              VARCHAR(30) NOT NULL DEFAULT 'pending'
                        COMMENT 'pending-待处理 assigned-已指派 accepted-已接单 in_progress-处理中 completed-已完成 closed-已关闭',
    customer_rating     SMALLINT COMMENT '客户评分 1-5',
    customer_feedback   TEXT,
    closed_at           TIMESTAMPTZ,
    ext_json            JSONB,
    created_by          BIGINT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMPTZ
);
COMMENT ON TABLE coll_service_ticket IS '服务工单表';
CREATE INDEX idx_ticket_customer ON coll_service_ticket (customer_id);
CREATE INDEX idx_ticket_assignee ON coll_service_ticket (assignee_id);
CREATE INDEX idx_ticket_status ON coll_service_ticket (status);
CREATE INDEX idx_ticket_type ON coll_service_ticket (type);
CREATE INDEX idx_ticket_created ON coll_service_ticket (created_at);

-- 5.6 工单操作记录表
CREATE TABLE IF NOT EXISTS coll_ticket_operation (
    id              BIGSERIAL PRIMARY KEY,
    ticket_id       BIGINT NOT NULL,
    action          VARCHAR(30) NOT NULL COMMENT 'assign/accept/start/complete/close/rate/transfer',
    operator_id     BIGINT NOT NULL,
    operator_name   VARCHAR(100),
    detail          JSONB COMMENT '操作详情',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
COMMENT ON TABLE coll_ticket_operation IS '工单操作记录表';
CREATE INDEX idx_ticket_op_ticket ON coll_ticket_operation (ticket_id);

-- 5.7 企业微信用户绑定表
CREATE TABLE IF NOT EXISTS wecom_user_binding (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL UNIQUE,
    wecom_user_id   VARCHAR(100) NOT NULL UNIQUE,
    wecom_corp_id   VARCHAR(100),
    wecom_agent_id  INT,
    status          SMALLINT NOT NULL DEFAULT 1,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
COMMENT ON TABLE wecom_user_binding IS '企业微信用户绑定表';

-- 5.8 企业微信消息推送日志表
CREATE TABLE IF NOT EXISTS wecom_message_log (
    id              BIGSERIAL PRIMARY KEY,
    wecom_user_ids  JSONB NOT NULL,
    msg_type        VARCHAR(30) NOT NULL COMMENT 'text/textcard/image',
    title           VARCHAR(200),
    content         TEXT,
    url             VARCHAR(500),
    biz_type        VARCHAR(50),
    biz_id          BIGINT,
    status          SMALLINT NOT NULL DEFAULT 1 COMMENT '0-失败 1-成功',
    fail_reason     TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
COMMENT ON TABLE wecom_message_log IS '企业微信消息推送日志表';
CREATE INDEX idx_wecom_msg_biz ON wecom_message_log (biz_type, biz_id);

-- 5.9 退货退款申请表
CREATE TABLE IF NOT EXISTS coll_refund_request (
    id                  BIGSERIAL PRIMARY KEY,
    refund_no           VARCHAR(100) NOT NULL UNIQUE,
    order_id            BIGINT NOT NULL,
    customer_id         BIGINT NOT NULL,
    refund_amount       DECIMAL(18,2) NOT NULL,
    refund_type         VARCHAR(20) NOT NULL COMMENT 'only_refund-仅退款 refund_return-退货退款',
    reason              VARCHAR(500) NOT NULL,
    description         TEXT,
    attachment_ids      JSONB,
    status              VARCHAR(30) NOT NULL DEFAULT 'pending'
                        COMMENT 'pending-待审核 approved-已通过 rejected-已驳回'
                        ' ship_back-待寄回 received-已收货 completed-已完成',
    review_comment      VARCHAR(500),
    express_company     VARCHAR(100),
    express_no          VARCHAR(100),
    completed_at        TIMESTAMPTZ,
    created_by          BIGINT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMPTZ
);
COMMENT ON TABLE coll_refund_request IS '退货退款申请表';
CREATE INDEX idx_refund_req_order ON coll_refund_request (order_id);
CREATE INDEX idx_refund_req_status ON coll_refund_request (status);

-- ============================================================================
-- 第六部分：数据分析域（report_*）
-- ============================================================================

-- 6.1 自定义报表表
CREATE TABLE IF NOT EXISTS report_custom_report (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    data_source     VARCHAR(50) NOT NULL COMMENT 'opportunity/customer/order/follow_up',
    dimensions      JSONB NOT NULL COMMENT '维度列表 ["dept_name","owner_name","date"]',
    metrics         JSONB NOT NULL COMMENT '指标列表 ["count","sum_amount"]',
    filters         JSONB COMMENT '筛选条件',
    chart_type      VARCHAR(30) DEFAULT 'bar' COMMENT 'bar/line/pie/table',
    schedule        VARCHAR(50) COMMENT '自动生成cron',
    status          SMALLINT NOT NULL DEFAULT 1,
    created_by      BIGINT NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ
);
COMMENT ON TABLE report_custom_report IS '自定义报表表';

-- 6.2 报表计划接收人表
CREATE TABLE IF NOT EXISTS report_schedule_recipient (
    id              BIGSERIAL PRIMARY KEY,
    report_id       BIGINT NOT NULL,
    user_id         BIGINT NOT NULL,
    UNIQUE (report_id, user_id)
);
COMMENT ON TABLE report_schedule_recipient IS '报表计划接收人表';

-- 6.3 看板布局表
CREATE TABLE IF NOT EXISTS report_dashboard_layout (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL UNIQUE,
    layout          JSONB NOT NULL COMMENT '看板布局 [{"cardType":"sales_funnel","position":1,"width":6,"height":2}]',
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
COMMENT ON TABLE report_dashboard_layout IS '看板布局表';

-- ============================================================================
-- 第七部分：外键约束
-- ============================================================================

ALTER TABLE sys_user ADD CONSTRAINT fk_user_dept FOREIGN KEY (dept_id) REFERENCES sys_dept(id);
ALTER TABLE sys_user_role ADD CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE;
ALTER TABLE sys_user_role ADD CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE;
ALTER TABLE sys_role_menu ADD CONSTRAINT fk_role_menu_role FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE;
ALTER TABLE sys_role_menu ADD CONSTRAINT fk_role_menu_menu FOREIGN KEY (menu_id) REFERENCES sys_menu(id) ON DELETE CASCADE;
ALTER TABLE sys_role_dept ADD CONSTRAINT fk_role_dept_role FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE;
ALTER TABLE sys_role_dept ADD CONSTRAINT fk_role_dept_dept FOREIGN KEY (dept_id) REFERENCES sys_dept(id) ON DELETE CASCADE;

ALTER TABLE crm_customer ADD CONSTRAINT fk_customer_level FOREIGN KEY (level_id) REFERENCES crm_customer_level(id);
ALTER TABLE crm_customer ADD CONSTRAINT fk_customer_owner FOREIGN KEY (owner_id) REFERENCES sys_user(id);
ALTER TABLE crm_customer_tag ADD CONSTRAINT fk_customer_tag_customer FOREIGN KEY (customer_id) REFERENCES crm_customer(id) ON DELETE CASCADE;
ALTER TABLE crm_customer_tag ADD CONSTRAINT fk_customer_tag_tag FOREIGN KEY (tag_id) REFERENCES crm_tag(id) ON DELETE CASCADE;
ALTER TABLE crm_customer_level_rule ADD CONSTRAINT fk_level_rule_level FOREIGN KEY (level_id) REFERENCES crm_customer_level(id) ON DELETE CASCADE;
ALTER TABLE crm_contact ADD CONSTRAINT fk_contact_customer FOREIGN KEY (customer_id) REFERENCES crm_customer(id) ON DELETE CASCADE;
ALTER TABLE crm_lead ADD CONSTRAINT fk_lead_owner FOREIGN KEY (owner_id) REFERENCES sys_user(id);
ALTER TABLE crm_segment_member ADD CONSTRAINT fk_segment_member_segment FOREIGN KEY (segment_id) REFERENCES crm_segment(id) ON DELETE CASCADE;
ALTER TABLE crm_segment_member ADD CONSTRAINT fk_segment_member_customer FOREIGN KEY (customer_id) REFERENCES crm_customer(id) ON DELETE CASCADE;
ALTER TABLE crm_auto_tag_rule ADD CONSTRAINT fk_auto_tag_rule_tag FOREIGN KEY (tag_id) REFERENCES crm_tag(id) ON DELETE CASCADE;

ALTER TABLE sale_opportunity ADD CONSTRAINT fk_opp_customer FOREIGN KEY (customer_id) REFERENCES crm_customer(id);
ALTER TABLE sale_opportunity ADD CONSTRAINT fk_opp_stage FOREIGN KEY (stage_id) REFERENCES sale_opportunity_stage(id);
ALTER TABLE sale_opportunity ADD CONSTRAINT fk_opp_owner FOREIGN KEY (owner_id) REFERENCES sys_user(id);
ALTER TABLE sale_opportunity_participant ADD CONSTRAINT fk_opp_part_opp FOREIGN KEY (opportunity_id) REFERENCES sale_opportunity(id) ON DELETE CASCADE;
ALTER TABLE sale_opportunity_stage_log ADD CONSTRAINT fk_opp_stage_log_opp FOREIGN KEY (opportunity_id) REFERENCES sale_opportunity(id) ON DELETE CASCADE;
ALTER TABLE sale_follow_up ADD CONSTRAINT fk_fu_customer FOREIGN KEY (customer_id) REFERENCES crm_customer(id);
ALTER TABLE sale_follow_up ADD CONSTRAINT fk_fu_creator FOREIGN KEY (creator_id) REFERENCES sys_user(id);
ALTER TABLE sale_appointment ADD CONSTRAINT fk_appt_customer FOREIGN KEY (customer_id) REFERENCES crm_customer(id);
ALTER TABLE sale_appointment ADD CONSTRAINT fk_appt_owner FOREIGN KEY (owner_id) REFERENCES sys_user(id);
ALTER TABLE sale_quotation ADD CONSTRAINT fk_quote_customer FOREIGN KEY (customer_id) REFERENCES crm_customer(id);
ALTER TABLE sale_quotation_item ADD CONSTRAINT fk_quote_item_quote FOREIGN KEY (quotation_id) REFERENCES sale_quotation(id) ON DELETE CASCADE;
ALTER TABLE sale_contract ADD CONSTRAINT fk_contract_customer FOREIGN KEY (customer_id) REFERENCES crm_customer(id);
ALTER TABLE sale_contract_item ADD CONSTRAINT fk_contract_item_contract FOREIGN KEY (contract_id) REFERENCES sale_contract(id) ON DELETE CASCADE;
ALTER TABLE sale_payment_plan ADD CONSTRAINT fk_payment_contract FOREIGN KEY (contract_id) REFERENCES sale_contract(id) ON DELETE CASCADE;
ALTER TABLE sale_invoice ADD CONSTRAINT fk_invoice_contract FOREIGN KEY (contract_id) REFERENCES sale_contract(id);

ALTER TABLE mall_sku ADD CONSTRAINT fk_sku_product FOREIGN KEY (product_id) REFERENCES sale_product(id) ON DELETE CASCADE;
ALTER TABLE mall_order ADD CONSTRAINT fk_order_customer FOREIGN KEY (customer_id) REFERENCES crm_customer(id);
ALTER TABLE mall_order_item ADD CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES mall_order(id) ON DELETE CASCADE;
ALTER TABLE mall_coupon ADD CONSTRAINT fk_coupon_define FOREIGN KEY (define_id) REFERENCES mall_coupon_define(id);
ALTER TABLE mall_coupon ADD CONSTRAINT fk_coupon_customer FOREIGN KEY (customer_id) REFERENCES crm_customer(id);
ALTER TABLE mall_payment ADD CONSTRAINT fk_payment_order FOREIGN KEY (order_id) REFERENCES mall_order(id);
ALTER TABLE mall_refund ADD CONSTRAINT fk_refund_order FOREIGN KEY (order_id) REFERENCES mall_order(id);
ALTER TABLE mall_activity_product ADD CONSTRAINT fk_act_product_activity FOREIGN KEY (activity_id) REFERENCES mall_activity(id) ON DELETE CASCADE;
ALTER TABLE mall_activity_product ADD CONSTRAINT fk_act_product_product FOREIGN KEY (product_id) REFERENCES sale_product(id);

ALTER TABLE coll_approval_define_step ADD CONSTRAINT fk_approval_step_define FOREIGN KEY (define_id) REFERENCES coll_approval_define(id) ON DELETE CASCADE;
ALTER TABLE coll_approval_instance ADD CONSTRAINT fk_approval_inst_define FOREIGN KEY (define_id) REFERENCES coll_approval_define(id);
ALTER TABLE coll_approval_node_record ADD CONSTRAINT fk_approval_record_instance FOREIGN KEY (instance_id) REFERENCES coll_approval_instance(id) ON DELETE CASCADE;
ALTER TABLE coll_service_ticket ADD CONSTRAINT fk_ticket_customer FOREIGN KEY (customer_id) REFERENCES crm_customer(id);
ALTER TABLE coll_ticket_operation ADD CONSTRAINT fk_ticket_op_ticket FOREIGN KEY (ticket_id) REFERENCES coll_service_ticket(id) ON DELETE CASCADE;
ALTER TABLE coll_refund_request ADD CONSTRAINT fk_refund_req_order FOREIGN KEY (order_id) REFERENCES mall_order(id);
ALTER TABLE wecom_user_binding ADD CONSTRAINT fk_wecom_binding_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE;

ALTER TABLE report_schedule_recipient ADD CONSTRAINT fk_report_recipient_report FOREIGN KEY (report_id) REFERENCES report_custom_report(id) ON DELETE CASCADE;

-- ============================================================================
-- 第八部分：索引补充
-- ============================================================================

-- 全文检索：客户名/产品名模糊搜索（需先创建 extension）
-- CREATE EXTENSION IF NOT EXISTS pg_trgm;
-- CREATE INDEX idx_customer_name_gin ON crm_customer USING gin (name gin_trgm_ops);

-- JSONB 索引入门（按需添加）
-- CREATE INDEX idx_lead_ext ON crm_lead USING gin (ext_json);

-- 复合索引：常用列表查询
CREATE INDEX idx_fu_customer_created ON sale_follow_up (customer_id, created_at DESC);
CREATE INDEX idx_opp_owner_stage ON sale_opportunity (owner_id, stage_id);
CREATE INDEX idx_lead_owner_status ON crm_lead (owner_id, status);

-- ============================================================================
-- 第九部分：初始化数据
-- ============================================================================

-- 9.1 默认管理员
INSERT INTO sys_dept (id, parent_id, name, sort_order, status) VALUES (1, 0, '总公司', 1, 1);

INSERT INTO sys_user (id, username, password, real_name, dept_id, status)
VALUES (1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '系统管理员', 1, 1);

-- 9.2 默认角色
INSERT INTO sys_role (id, name, role_code, data_scope, status) VALUES
(1, '超级管理员', 'admin', 4, 1),
(2, '销售经理', 'sales_manager', 3, 1),
(3, '销售', 'sales', 1, 1),
(4, '财务', 'finance', 4, 1),
(5, '仓管', 'warehouse', 2, 1);

-- 9.3 默认菜单（根目录）
INSERT INTO sys_menu (id, parent_id, name, menu_type, route_path, component, sort_order, status) VALUES
(1, 0, '系统管理', 'M', '/system', 'Layout', 100, 1),
(2, 1, '用户管理', 'C', '/system/user', 'system/user/index', 1, 1),
(3, 1, '角色管理', 'C', '/system/role', 'system/role/index', 2, 1),
(4, 1, '菜单管理', 'C', '/system/menu', 'system/menu/index', 3, 1),
(5, 1, '部门管理', 'C', '/system/dept', 'system/dept/index', 4, 1),
(6, 1, '字典管理', 'C', '/system/dict', 'system/dict/index', 5, 1),
(7, 1, '配置管理', 'C', '/system/config', 'system/config/index', 6, 1),
(8, 1, '操作日志', 'C', '/system/log', 'system/log/index', 7, 1),
(9, 0, '客户管理', 'M', '/customer', 'Layout', 10, 1),
(10, 9, '线索管理', 'C', '/customer/lead', 'customer/lead/index', 1, 1),
(11, 9, '客户列表', 'C', '/customer/list', 'customer/list/index', 2, 1),
(12, 9, '标签管理', 'C', '/customer/tag', 'customer/tag/index', 3, 1),
(13, 9, '客户分群', 'C', '/customer/segment', 'customer/segment/index', 4, 1),
(14, 9, '联系人', 'C', '/customer/contact', 'customer/contact/index', 5, 1),
(15, 0, '销售管理', 'M', '/sales', 'Layout', 20, 1),
(16, 15, '商机看板', 'C', '/sales/pipeline', 'sales/pipeline/index', 1, 1),
(17, 15, '跟进记录', 'C', '/sales/follow-up', 'sales/follow-up/index', 2, 1),
(18, 15, '报价管理', 'C', '/sales/quotation', 'sales/quotation/index', 3, 1),
(19, 15, '合同管理', 'C', '/sales/contract', 'sales/contract/index', 4, 1),
(20, 15, '回款与发票', 'C', '/sales/invoice', 'sales/invoice/index', 5, 1),
(21, 0, '商城管理', 'M', '/mall', 'Layout', 30, 1),
(22, 21, '商品管理', 'C', '/mall/product', 'mall/product/index', 1, 1),
(23, 21, '订单管理', 'C', '/mall/order', 'mall/order/index', 2, 1),
(24, 21, '优惠券', 'C', '/mall/coupon', 'mall/coupon/index', 3, 1),
(25, 0, '数据分析', 'M', '/report', 'Layout', 40, 1),
(26, 25, '核心看板', 'C', '/report/dashboard', 'report/dashboard/index', 1, 1),
(27, 25, '自定义报表', 'C', '/report/custom', 'report/custom/index', 2, 1),
(28, 0, '协同办公', 'M', '/collaboration', 'Layout', 50, 1),
(29, 28, '审批管理', 'C', '/collaboration/approval', 'collaboration/approval/index', 1, 1),
(30, 28, '服务工单', 'C', '/collaboration/ticket', 'collaboration/ticket/index', 2, 1);

-- 9.4 管理员角色绑定
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);

-- 9.5 默认系统配置
INSERT INTO sys_config (config_key, config_name, config_value, config_type, is_public, remark) VALUES
('lead.pool.return.hours', '线索回收时长（小时）', '48', 1, false, '超时未跟进的线索自动回池'),
('lead.duplicate.strategy', '线索查重策略', 'phone', 1, false, 'phone-仅手机号 unionid-微信UnionID both-两者'),
('customer.level.evaluate.cycle', '客户等级评估周期', 'monthly', 1, false, 'daily/weekly/monthly'),
('customer.sleeping.days', '沉睡客户判定天数', '90', 1, false, '超过N天未消费标记为沉睡'),
('appointment.remind.before', '日程提醒提前N分钟', '30', 1, false, '默认提前30分钟提醒'),
('system.captcha.enable', '登录验证码开关', 'true', 1, false, 'true-开启 false-关闭'),
('system.password.error.limit', '密码错误锁定次数', '5', 1, false, '超过次数后账户临时锁定'),
('system.password.error.lock.minutes', '密码错误锁定时间（分钟）', '30', 1, false, '锁定时长');

-- 9.6 默认字典数据
INSERT INTO sys_dict_type (type_code, type_name, status) VALUES
('lead_source', '线索来源', 1),
('lead_status', '线索状态', 1),
('opportunity_stage', '商机阶段', 1),
('customer_level', '客户等级', 1),
('follow_up_type', '跟进类型', 1),
('contract_status', '合同状态', 1),
('invoice_type', '发票类型', 1),
('ticket_type', '工单类型', 1),
('ticket_priority', '工单优先级', 1);

INSERT INTO sys_dict_item (type_code, item_code, item_value, sort_order) VALUES
('lead_source', 'mini_program', '小程序注册', 1),
('lead_source', 'ad_landing', '广告落地页', 2),
('lead_source', 'offline_activity', '线下活动', 3),
('lead_source', 'manual_import', '手工导入', 4),
('lead_source', 'manual_input', '手工录入', 5),
('lead_status', 'pending', '待跟进', 1),
('lead_status', 'following', '跟进中', 2),
('lead_status', 'converted', '已转换', 3),
('lead_status', 'invalid', '无效', 4),
('lead_status', 'merged', '已合并', 5),
('follow_up_type', 'call', '电话', 1),
('follow_up_type', 'visit', '拜访', 2),
('follow_up_type', 'online', '线上', 3),
('follow_up_type', 'chat', '聊天', 4),
('invoice_type', 'vat_special', '增值税专用发票', 1),
('invoice_type', 'vat_normal', '增值税普通发票', 2),
('invoice_type', 'electronic', '电子发票', 3),
('ticket_type', 'repair', '报修', 1),
('ticket_type', 'install', '安装', 2),
('ticket_type', 'complaint', '投诉', 3),
('ticket_priority', 'low', '低', 1),
('ticket_priority', 'normal', '中', 2),
('ticket_priority', 'urgent', '高', 3),
('ticket_priority', 'critical', '紧急', 4);

-- 9.7 默认销售阶段
INSERT INTO sale_opportunity_stage (id, name, sort_order, probability, category) VALUES
(1, '初步接触', 1, 10, 'open'),
(2, '需求分析', 2, 25, 'open'),
(3, '方案报价', 3, 50, 'open'),
(4, '商务谈判', 4, 75, 'open'),
(5, '赢单', 5, 100, 'win'),
(6, '输单', 6, 0, 'lose');

-- 9.8 默认客户等级
INSERT INTO crm_customer_level (id, name, min_amount, max_amount, min_order_count, sort_order) VALUES
(1, '普通客户', 0, 9999.99, 0, 1),
(2, '银卡客户', 10000, 49999.99, 2, 2),
(3, '金卡客户', 50000, 199999.99, 5, 3),
(4, '钻石客户', 200000, 99999999.99, 10, 4);

-- ============================================================================
-- 完成
-- ============================================================================
