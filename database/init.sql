-- ============================================================================
-- CRM 系统数据库初始化脚本
-- 数据库：PostgreSQL 15+
-- 编码：UTF-8
-- ============================================================================

-- 创建数据库（需要超级用户权限，实际运行时可能需要手动创建）
-- CREATE DATABASE crm_db WITH ENCODING 'UTF8' LC_COLLATE 'zh_CN.UTF-8' LC_CTYPE 'zh_CN.UTF-8';

-- 启用扩展
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- ============================================================================
-- 第一部分：系统管理域（sys_*）
-- ============================================================================

-- 1.1 部门表
CREATE TABLE IF NOT EXISTS sys_dept (
    id              BIGSERIAL PRIMARY KEY,
    parent_id       BIGINT NOT NULL DEFAULT 0,
    ancestors       VARCHAR(500) NOT NULL DEFAULT '',
    name            VARCHAR(100) NOT NULL,
    sort_order      INT NOT NULL DEFAULT 0,
    leader_id       BIGINT,
    phone           VARCHAR(20),
    email           VARCHAR(100),
    status          SMALLINT NOT NULL DEFAULT 1,
    created_by      BIGINT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by      BIGINT,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ
);
COMMENT ON COLUMN sys_dept.ancestors IS '祖级列表（逗号分隔）';
COMMENT ON COLUMN sys_dept.leader_id IS '部门负责人';
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
    gender          SMALLINT DEFAULT 0,
    dept_id         BIGINT,
    post            VARCHAR(100),
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
COMMENT ON COLUMN sys_user.gender IS '0-未知 1-男 2-女';
COMMENT ON COLUMN sys_user.post IS '岗位';
COMMENT ON TABLE sys_user IS '系统用户表';
COMMENT ON COLUMN sys_user.status IS '状态：0-禁用 1-启用';

-- 1.3 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    role_code       VARCHAR(100) NOT NULL UNIQUE,
    data_scope      SMALLINT NOT NULL DEFAULT 1,
    status          SMALLINT NOT NULL DEFAULT 1,
    sort_order      INT NOT NULL DEFAULT 0,
    remark          VARCHAR(500),
    created_by      BIGINT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by      BIGINT,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ
);
COMMENT ON COLUMN sys_role.data_scope IS '1-本人 2-本部门 3-本部门及下属 4-全部 5-自定义';
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
    menu_type       CHAR(1) NOT NULL,
    icon            VARCHAR(100),
    route_path      VARCHAR(200),
    component       VARCHAR(200),
    permission_code VARCHAR(200),
    query_param     VARCHAR(200),
    is_visible      BOOLEAN NOT NULL DEFAULT TRUE,
    is_frame        BOOLEAN NOT NULL DEFAULT FALSE,
    sort_order      INT NOT NULL DEFAULT 0,
    status          SMALLINT NOT NULL DEFAULT 1,
    remark          VARCHAR(500),
    created_by      BIGINT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by      BIGINT,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ
);
COMMENT ON COLUMN sys_menu.menu_type IS 'M-目录 C-菜单 F-按钮';
COMMENT ON COLUMN sys_menu.permission_code IS '按钮权限标识';
COMMENT ON COLUMN sys_menu.query_param IS '路由参数';
COMMENT ON COLUMN sys_menu.is_frame IS '是否外链';
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
    updated_by      BIGINT,
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
    updated_by      BIGINT,
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
    config_type     SMALLINT NOT NULL DEFAULT 0,
    is_public       BOOLEAN NOT NULL DEFAULT FALSE,
    remark          VARCHAR(500),
    created_by      BIGINT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by      BIGINT,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ
);
COMMENT ON COLUMN sys_config.config_type IS '0-系统内置 1-自定义';
COMMENT ON COLUMN sys_config.is_public IS '是否前端可见';
COMMENT ON TABLE sys_config IS '系统配置表';

-- 1.11 操作日志表
CREATE TABLE IF NOT EXISTS sys_operation_log (
    id              BIGSERIAL PRIMARY KEY,
    module          VARCHAR(50) NOT NULL,
    action          VARCHAR(50) NOT NULL,
    operator_id     BIGINT,
    operator_name   VARCHAR(100),
    target_type     VARCHAR(100),
    target_id       BIGINT,
    detail          JSONB,
    request_url     VARCHAR(500),
    request_method  VARCHAR(10),
    request_params  TEXT,
    ip              VARCHAR(50),
    user_agent      VARCHAR(500),
    duration_ms     INT,
    result_code     INT DEFAULT 200,
    error_msg       TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
COMMENT ON COLUMN sys_operation_log.module IS 'system/customer/sales/mall';
COMMENT ON COLUMN sys_operation_log.action IS 'create/update/delete/import/export/...';
COMMENT ON COLUMN sys_operation_log.target_type IS '操作对象类型';
COMMENT ON COLUMN sys_operation_log.target_id IS '操作对象ID';
COMMENT ON COLUMN sys_operation_log.detail IS '变更详情';
COMMENT ON COLUMN sys_operation_log.duration_ms IS '请求耗时';
COMMENT ON TABLE sys_operation_log IS '操作日志表';
CREATE INDEX IF NOT EXISTS idx_operation_log_module ON sys_operation_log (module);
CREATE INDEX IF NOT EXISTS idx_operation_log_action ON sys_operation_log (action);
CREATE INDEX IF NOT EXISTS idx_operation_log_operator ON sys_operation_log (operator_id);
CREATE INDEX IF NOT EXISTS idx_operation_log_target ON sys_operation_log (target_type, target_id);
CREATE INDEX IF NOT EXISTS idx_operation_log_created ON sys_operation_log (created_at);

-- 1.12 消息表
CREATE TABLE IF NOT EXISTS sys_message (
    id              BIGSERIAL PRIMARY KEY,
    receiver_id     BIGINT NOT NULL,
    channel         VARCHAR(50) NOT NULL,
    title           VARCHAR(200) NOT NULL,
    content         TEXT,
    biz_type        VARCHAR(50),
    biz_id          BIGINT,
    is_read         BOOLEAN NOT NULL DEFAULT FALSE,
    read_at         TIMESTAMPTZ,
    priority        VARCHAR(20) NOT NULL DEFAULT 'normal',
    created_by      BIGINT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by      BIGINT,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ
);
COMMENT ON COLUMN sys_message.channel IS '站内信/企微/短信/小程序订阅';
COMMENT ON COLUMN sys_message.biz_type IS '业务类型：lead_assign/approval/system';
COMMENT ON COLUMN sys_message.biz_id IS '业务ID';
COMMENT ON TABLE sys_message IS '消息表';
CREATE INDEX IF NOT EXISTS idx_message_receiver ON sys_message (receiver_id, is_read);
CREATE INDEX IF NOT EXISTS idx_message_created ON sys_message (created_at);

-- 1.13 文件表
CREATE TABLE IF NOT EXISTS sys_file (
    id              BIGSERIAL PRIMARY KEY,
    file_name       VARCHAR(255) NOT NULL,
    original_name   VARCHAR(255) NOT NULL,
    file_size       BIGINT NOT NULL,
    file_type       VARCHAR(100),
    file_url        VARCHAR(500) NOT NULL,
    thumbnail_url   VARCHAR(500),
    storage_type    VARCHAR(50) NOT NULL DEFAULT 'minio',
    biz_type        VARCHAR(50),
    biz_id          BIGINT,
    md5_hash        VARCHAR(64),
    is_public       BOOLEAN NOT NULL DEFAULT FALSE,
    created_by      BIGINT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by      BIGINT,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ
);
COMMENT ON COLUMN sys_file.storage_type IS 'minio/oss/local';
COMMENT ON COLUMN sys_file.biz_type IS 'avatar/contract/attachment/product';
COMMENT ON COLUMN sys_file.biz_id IS '业务ID';
COMMENT ON TABLE sys_file IS '文件表';
CREATE INDEX IF NOT EXISTS idx_file_biz ON sys_file (biz_type, biz_id);
CREATE INDEX IF NOT EXISTS idx_file_md5 ON sys_file (md5_hash);

-- 1.14 API密钥表（开放接口）
CREATE TABLE IF NOT EXISTS sys_api_key (
    id              BIGSERIAL PRIMARY KEY,
    app_name        VARCHAR(100) NOT NULL,
    api_key         VARCHAR(100) NOT NULL UNIQUE,
    api_secret      VARCHAR(255) NOT NULL,
    permissions     JSONB,
    ip_whitelist    JSONB,
    expire_at       TIMESTAMPTZ,
    last_used_at    TIMESTAMPTZ,
    status          SMALLINT NOT NULL DEFAULT 1,
    created_by      BIGINT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by      BIGINT,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ
);
COMMENT ON COLUMN sys_api_key.api_secret IS '仅创建和重置时明文显示';
COMMENT ON COLUMN sys_api_key.permissions IS '权限列表 ["customer:read","order:read"]';
COMMENT ON COLUMN sys_api_key.ip_whitelist IS 'IP白名单';
COMMENT ON TABLE sys_api_key IS 'API密钥表';

-- 1.15 系统公告表
CREATE TABLE IF NOT EXISTS sys_notice (
    id              BIGSERIAL PRIMARY KEY,
    title           VARCHAR(200) NOT NULL,
    content         TEXT NOT NULL,
    notice_type     SMALLINT NOT NULL DEFAULT 1,
    status          SMALLINT NOT NULL DEFAULT 0,
    publish_at      TIMESTAMPTZ,
    created_by      BIGINT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by      BIGINT,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ
);
COMMENT ON COLUMN sys_notice.notice_type IS '1-公告 2-通知 3-提醒';
COMMENT ON COLUMN sys_notice.status IS '0-草稿 1-已发布';
COMMENT ON TABLE sys_notice IS '系统公告表';

-- 1.16 登录日志表
CREATE TABLE IF NOT EXISTS sys_login_log (
    id              BIGSERIAL PRIMARY KEY,
    username        VARCHAR(100) NOT NULL,
    login_ip        VARCHAR(50),
    login_location  VARCHAR(255),
    browser         VARCHAR(100),
    os              VARCHAR(100),
    status          SMALLINT NOT NULL DEFAULT 1,
    fail_reason     VARCHAR(200),
    login_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
COMMENT ON COLUMN sys_login_log.status IS '0-失败 1-成功';
COMMENT ON TABLE sys_login_log IS '登录日志表';
CREATE INDEX IF NOT EXISTS idx_login_log_username ON sys_login_log (username);
CREATE INDEX IF NOT EXISTS idx_login_log_at ON sys_login_log (login_at);

-- ============================================================================
-- 第二部分：客户管理域（crm_*）
-- ============================================================================

-- 2.1 客户等级表
CREATE TABLE IF NOT EXISTS crm_customer_level (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(50) NOT NULL,
    icon            VARCHAR(200),
    min_amount      DECIMAL(18,2) DEFAULT 0,
    max_amount      DECIMAL(18,2) DEFAULT 999999999.99,
    min_order_count INT DEFAULT 0,
    benefits        JSONB,
    sort_order      INT NOT NULL DEFAULT 0,
    status          SMALLINT NOT NULL DEFAULT 1,
    remark          VARCHAR(500),
    created_by      BIGINT,
    updated_by      BIGINT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ
);
COMMENT ON COLUMN crm_customer_level.min_amount IS '年消费下限';
COMMENT ON COLUMN crm_customer_level.max_amount IS '年消费上限';
COMMENT ON COLUMN crm_customer_level.benefits IS '权益配置 {"discount":0.95,"freeShipping":true}';
COMMENT ON TABLE crm_customer_level IS '客户等级表';

-- 2.2 升降级规则表
CREATE TABLE IF NOT EXISTS crm_customer_level_rule (
    id              BIGSERIAL PRIMARY KEY,
    level_id        BIGINT NOT NULL,
    rule_type       VARCHAR(20) NOT NULL,
    condition_field VARCHAR(50) NOT NULL,
    condition_operator VARCHAR(20) NOT NULL,
    condition_value DECIMAL(18,2) NOT NULL,
    period_days     INT,
    evaluate_cycle  VARCHAR(20) NOT NULL DEFAULT 'monthly',
    status          SMALLINT NOT NULL DEFAULT 1,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
COMMENT ON COLUMN crm_customer_level_rule.rule_type IS 'upgrade/downgrade';
COMMENT ON COLUMN crm_customer_level_rule.condition_field IS 'amount/order_count/no_order_days';
COMMENT ON COLUMN crm_customer_level_rule.condition_operator IS 'gte/lte';
COMMENT ON COLUMN crm_customer_level_rule.period_days IS '评估周期天数';
COMMENT ON COLUMN crm_customer_level_rule.evaluate_cycle IS 'daily/weekly/monthly';
COMMENT ON TABLE crm_customer_level_rule IS '客户升降级规则表';
CREATE INDEX IF NOT EXISTS idx_level_rule_level ON crm_customer_level_rule (level_id);

-- 2.3 标签表
CREATE TABLE IF NOT EXISTS crm_tag (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(50) NOT NULL,
    color           VARCHAR(20),
    type            VARCHAR(20) NOT NULL,
    remark          VARCHAR(500),
    status          SMALLINT NOT NULL DEFAULT 1,
    created_by      BIGINT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by      BIGINT,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ
);
COMMENT ON COLUMN crm_tag.type IS 'manual-手动 auto-自动';
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
    gender              SMALLINT DEFAULT 0,
    email               VARCHAR(100),
    province            VARCHAR(50),
    city                VARCHAR(50),
    district            VARCHAR(50),
    address             VARCHAR(500),
    source_channel      VARCHAR(50),
    level_id            BIGINT,
    owner_id            BIGINT,
    last_contact_at     TIMESTAMPTZ,
    total_consumption   DECIMAL(18,2) DEFAULT 0,
    order_count         INT DEFAULT 0,
    remark              TEXT,
    ext_json            JSONB,
    status              SMALLINT NOT NULL DEFAULT 1,
    created_by          BIGINT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by          BIGINT,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMPTZ
);
COMMENT ON COLUMN crm_customer.gender IS '0-未知 1-男 2-女';
COMMENT ON COLUMN crm_customer.source_channel IS '来源渠道';
COMMENT ON COLUMN crm_customer.owner_id IS '负责人';
COMMENT ON COLUMN crm_customer.last_contact_at IS '最后联系时间';
COMMENT ON COLUMN crm_customer.total_consumption IS '累计消费';
COMMENT ON COLUMN crm_customer.order_count IS '订单数';
COMMENT ON COLUMN crm_customer.ext_json IS '扩展字段（自定义字段动态映射）';
COMMENT ON TABLE crm_customer IS '客户表';
COMMENT ON COLUMN crm_customer.ext_json IS '扩展字段，存储动态自定义字段 {fieldKey: value}';
CREATE INDEX IF NOT EXISTS idx_customer_phone ON crm_customer (phone);
CREATE UNIQUE INDEX IF NOT EXISTS idx_customer_wechat_openid ON crm_customer (wechat_openid);
CREATE INDEX IF NOT EXISTS idx_customer_wechat ON crm_customer (wechat_unionid);
CREATE INDEX IF NOT EXISTS idx_customer_owner ON crm_customer (owner_id);
CREATE INDEX IF NOT EXISTS idx_customer_level ON crm_customer (level_id);
CREATE INDEX IF NOT EXISTS idx_customer_source ON crm_customer (source_channel);
CREATE INDEX IF NOT EXISTS idx_customer_created ON crm_customer (created_at);
CREATE INDEX IF NOT EXISTS idx_customer_name ON crm_customer USING gin (name gin_trgm_ops);

-- 2.5 客户标签关系表
CREATE TABLE IF NOT EXISTS crm_customer_tag (
    id              BIGSERIAL PRIMARY KEY,
    customer_id     BIGINT NOT NULL,
    tag_id          BIGINT NOT NULL,
    tag_type        VARCHAR(20) NOT NULL DEFAULT 'auto',
    created_by      BIGINT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (customer_id, tag_id)
);
COMMENT ON COLUMN crm_customer_tag.tag_type IS 'manual/auto';
COMMENT ON TABLE crm_customer_tag IS '客户标签关系表';
CREATE INDEX IF NOT EXISTS idx_customer_tag_customer ON crm_customer_tag (customer_id);
CREATE INDEX IF NOT EXISTS idx_customer_tag_tag ON crm_customer_tag (tag_id);

-- 2.6 客户等级变更日志
CREATE TABLE IF NOT EXISTS crm_customer_level_log (
    id              BIGSERIAL PRIMARY KEY,
    customer_id     BIGINT NOT NULL,
    old_level_id    BIGINT,
    new_level_id    BIGINT NOT NULL,
    change_type     VARCHAR(20) NOT NULL,
    reason          VARCHAR(500),
    operator_id     BIGINT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
COMMENT ON COLUMN crm_customer_level_log.change_type IS 'auto_upgrade/auto_downgrade/manual';
COMMENT ON TABLE crm_customer_level_log IS '客户等级变更日志表';
CREATE INDEX IF NOT EXISTS idx_level_log_customer ON crm_customer_level_log (customer_id);

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
    updated_by          BIGINT,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMPTZ
);
COMMENT ON TABLE crm_contact IS '联系人表';
CREATE INDEX IF NOT EXISTS idx_contact_customer ON crm_contact (customer_id);

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
    source_channel      VARCHAR(50) NOT NULL,
    status              VARCHAR(20) NOT NULL DEFAULT 'pending',
    owner_id            BIGINT,
    assign_type         VARCHAR(20),
    assigned_at         TIMESTAMPTZ,
    pool_return_at      TIMESTAMPTZ,
    convert_customer_id BIGINT,
    convert_opportunity_id BIGINT,
    ext_json            JSONB,
    remark              TEXT,
    created_by          BIGINT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by          BIGINT,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMPTZ
);
COMMENT ON COLUMN crm_lead.source_channel IS '来源渠道';
COMMENT ON COLUMN crm_lead.status IS 'pending-待跟进 following-跟进中 converted-已转换 invalid-无效 merged-已合并';
COMMENT ON COLUMN crm_lead.owner_id IS '负责人';
COMMENT ON COLUMN crm_lead.assign_type IS 'auto-自动分配 manual-手动指派';
COMMENT ON COLUMN crm_lead.pool_return_at IS '回池时间';
COMMENT ON COLUMN crm_lead.convert_customer_id IS '转换后的客户ID';
COMMENT ON COLUMN crm_lead.convert_opportunity_id IS '转换后的商机ID';
COMMENT ON TABLE crm_lead IS '线索表';
CREATE INDEX IF NOT EXISTS idx_lead_phone ON crm_lead (phone);
CREATE INDEX IF NOT EXISTS idx_lead_status ON crm_lead (status);
CREATE INDEX IF NOT EXISTS idx_lead_owner ON crm_lead (owner_id);
CREATE INDEX IF NOT EXISTS idx_lead_source ON crm_lead (source_channel);
CREATE INDEX IF NOT EXISTS idx_lead_created ON crm_lead (created_at);
CREATE INDEX IF NOT EXISTS idx_lead_pool_return ON crm_lead (pool_return_at);

-- 2.9 线索分配规则表
CREATE TABLE IF NOT EXISTS crm_lead_distribution_rule (
    id                  BIGSERIAL PRIMARY KEY,
    name                VARCHAR(100) NOT NULL,
    priority            INT NOT NULL DEFAULT 0,
    conditions          JSONB NOT NULL,
    strategy            VARCHAR(30) NOT NULL,
    strategy_config     JSONB,
    target_type         VARCHAR(20) NOT NULL,
    target_id           BIGINT NOT NULL,
    time_ranges         JSONB,
    max_daily_per_person INT DEFAULT 50,
    status              SMALLINT NOT NULL DEFAULT 1,
    created_by          BIGINT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by          BIGINT,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMPTZ
);
COMMENT ON COLUMN crm_lead_distribution_rule.conditions IS '匹配条件 {"logic":"and","rules":[...]}';
COMMENT ON COLUMN crm_lead_distribution_rule.strategy IS 'round_robin/weight/idle_longest/load_balanced';
COMMENT ON COLUMN crm_lead_distribution_rule.strategy_config IS '策略配置 {"weight":{"user1":1,"user2":2}}';
COMMENT ON COLUMN crm_lead_distribution_rule.target_type IS 'user/role/dept';
COMMENT ON COLUMN crm_lead_distribution_rule.time_ranges IS '生效时间段 [{"weekday":"1-5","startTime":"09:00","endTime":"18:00"}]';
COMMENT ON TABLE crm_lead_distribution_rule IS '线索分配规则表';

-- 2.10 线索分配日志表
CREATE TABLE IF NOT EXISTS crm_lead_distribution_log (
    id              BIGSERIAL PRIMARY KEY,
    rule_id         BIGINT,
    lead_id         BIGINT NOT NULL,
    from_owner_id   BIGINT,
    to_owner_id     BIGINT NOT NULL,
    assign_type     VARCHAR(20) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
COMMENT ON COLUMN crm_lead_distribution_log.assign_type IS 'auto/manual/pool_return';
COMMENT ON TABLE crm_lead_distribution_log IS '线索分配日志表';
CREATE INDEX IF NOT EXISTS idx_dist_log_lead ON crm_lead_distribution_log (lead_id);
CREATE INDEX IF NOT EXISTS idx_dist_log_owner ON crm_lead_distribution_log (to_owner_id);

-- 2.11 自动标签规则表
CREATE TABLE IF NOT EXISTS crm_auto_tag_rule (
    id              BIGSERIAL PRIMARY KEY,
    tag_id          BIGINT NOT NULL,
    rule_name       VARCHAR(100) NOT NULL,
    conditions      JSONB NOT NULL,
    schedule        VARCHAR(50),
    last_execute_at TIMESTAMPTZ,
    status          SMALLINT NOT NULL DEFAULT 1,
    created_by      BIGINT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by      BIGINT,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ
);
COMMENT ON COLUMN crm_auto_tag_rule.conditions IS '匹配条件 {"logic":"and","rules":[...]}';
COMMENT ON COLUMN crm_auto_tag_rule.schedule IS 'cron 表达式';
COMMENT ON TABLE crm_auto_tag_rule IS '自动标签规则表';

-- 2.12 客户分群表
CREATE TABLE IF NOT EXISTS crm_segment (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    conditions      JSONB NOT NULL,
    is_dynamic      BOOLEAN NOT NULL DEFAULT TRUE,
    member_count    INT NOT NULL DEFAULT 0,
    last_refresh_at TIMESTAMPTZ,
    status          SMALLINT NOT NULL DEFAULT 1,
    created_by      BIGINT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by      BIGINT,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ
);
COMMENT ON COLUMN crm_segment.conditions IS '筛选条件 {"logic":"and","rules":[...]}';
COMMENT ON COLUMN crm_segment.is_dynamic IS '是否动态分群';
COMMENT ON TABLE crm_segment IS '客户分群表';

-- 2.13 分群成员表
CREATE TABLE IF NOT EXISTS crm_segment_member (
    id              BIGSERIAL PRIMARY KEY,
    segment_id      BIGINT NOT NULL,
    customer_id     BIGINT NOT NULL,
    join_type       VARCHAR(20) NOT NULL DEFAULT 'auto',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (segment_id, customer_id)
);
COMMENT ON COLUMN crm_segment_member.join_type IS 'auto-自动匹配 manual-手动添加';
COMMENT ON TABLE crm_segment_member IS '分群成员表';
CREATE INDEX IF NOT EXISTS idx_segment_member_segment ON crm_segment_member (segment_id);

-- 2.14 客户操作日志表（业务级，区别于系统操作日志）
CREATE TABLE IF NOT EXISTS crm_activity_log (
    id              BIGSERIAL PRIMARY KEY,
    customer_id     BIGINT NOT NULL,
    contact_id      BIGINT,
    action          VARCHAR(50) NOT NULL,
    detail          JSONB,
    operator_id     BIGINT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
COMMENT ON COLUMN crm_activity_log.action IS 'create/update/transfer/tag/level_change/merge';
COMMENT ON COLUMN crm_activity_log.detail IS '操作详情';
COMMENT ON TABLE crm_activity_log IS '客户操作日志表';
CREATE INDEX IF NOT EXISTS idx_activity_customer ON crm_activity_log (customer_id);
CREATE INDEX IF NOT EXISTS idx_activity_created ON crm_activity_log (created_at);

-- 2.15 自定义字段定义表（客户扩展字段元数据）
CREATE TABLE IF NOT EXISTS crm_custom_field_def (
    id              BIGSERIAL PRIMARY KEY,
    entity_type     VARCHAR(50) NOT NULL,
    field_key       VARCHAR(100) NOT NULL,
    field_name      VARCHAR(100) NOT NULL,
    field_type      VARCHAR(30) NOT NULL,
    options         JSONB,
    is_required     BOOLEAN NOT NULL DEFAULT FALSE,
    sort_order      INT NOT NULL DEFAULT 0,
    status          SMALLINT NOT NULL DEFAULT 1,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ,
    UNIQUE (entity_type, field_key)
);
COMMENT ON COLUMN crm_custom_field_def.entity_type IS 'customer/lead/contact';
COMMENT ON COLUMN crm_custom_field_def.field_type IS 'text/number/date/select/multi_select/boolean';
COMMENT ON COLUMN crm_custom_field_def.options IS '选项列表（select类型）[{"label":"A","value":"a"}]';
COMMENT ON TABLE crm_custom_field_def IS '自定义字段定义表';

-- ============================================================================
-- 第三部分：销售管理域（crm_*）
-- ============================================================================

-- 3.1 商机阶段定义表
CREATE TABLE IF NOT EXISTS crm_opportunity_stage (
    id                  BIGSERIAL PRIMARY KEY,
    name                VARCHAR(100) NOT NULL,
    sort_order          INT NOT NULL DEFAULT 0,
    probability         INT DEFAULT 0,
    category            VARCHAR(20) NOT NULL DEFAULT 'open',
    status              SMALLINT NOT NULL DEFAULT 1,
    created_by          BIGINT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by          BIGINT,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMPTZ
);
COMMENT ON TABLE crm_opportunity_stage IS '商机阶段定义表';
COMMENT ON COLUMN crm_opportunity_stage.category IS 'open-开放 win-赢单 lose-输单';

-- 3.2 商机表
CREATE TABLE IF NOT EXISTS crm_opportunity (
    id                  BIGSERIAL PRIMARY KEY,
    customer_id         BIGINT NOT NULL,
    contact_id          BIGINT,
    name                VARCHAR(200) NOT NULL,
    expected_amount     DECIMAL(18,2),
    expected_close_date DATE,
    stage_id            BIGINT,
    budget              DECIMAL(18,2),
    decision_maker      VARCHAR(100),
    competition         TEXT,
    pain_points         TEXT,
    requirements        TEXT,
    solution            TEXT,
    participant_ids     JSONB,
    owner_id            BIGINT,
    final_amount        DECIMAL(18,2),
    lose_reason         VARCHAR(500),
    lose_reason_detail  TEXT,
    competitor          VARCHAR(200),
    contract_id         BIGINT,
    remark              TEXT,
    created_by          BIGINT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by          BIGINT,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMPTZ
);
COMMENT ON TABLE crm_opportunity IS '商机表';
CREATE INDEX IF NOT EXISTS idx_opportunity_customer ON crm_opportunity (customer_id);
CREATE INDEX IF NOT EXISTS idx_opportunity_stage ON crm_opportunity (stage_id);
CREATE INDEX IF NOT EXISTS idx_opportunity_owner ON crm_opportunity (owner_id);

-- 3.3 商机参与人表
CREATE TABLE IF NOT EXISTS crm_opportunity_participant (
    id              BIGSERIAL PRIMARY KEY,
    opportunity_id  BIGINT NOT NULL,
    user_id         BIGINT NOT NULL
);
COMMENT ON TABLE crm_opportunity_participant IS '商机参与人表';
CREATE INDEX IF NOT EXISTS idx_opp_part_opp ON crm_opportunity_participant (opportunity_id);

-- 3.4 商机阶段变更日志
CREATE TABLE IF NOT EXISTS crm_opportunity_stage_log (
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
COMMENT ON TABLE crm_opportunity_stage_log IS '商机阶段变更日志表';
CREATE INDEX IF NOT EXISTS idx_opp_stage_log_opp ON crm_opportunity_stage_log (opportunity_id);

-- 3.5 产品分类表
CREATE TABLE IF NOT EXISTS crm_product_category (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    parent_id   BIGINT,
    sort_order  INT NOT NULL DEFAULT 0,
    status      SMALLINT NOT NULL DEFAULT 1,
    created_by  BIGINT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by  BIGINT,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at  TIMESTAMPTZ
);
COMMENT ON TABLE crm_product_category IS '产品分类表';

-- 3.6 产品表
CREATE TABLE IF NOT EXISTS crm_sales_product (
    id              BIGSERIAL PRIMARY KEY,
    category_id     BIGINT,
    name            VARCHAR(200) NOT NULL,
    unit            VARCHAR(20),
    standard_price  DECIMAL(18,2) DEFAULT 0,
    cost_price      DECIMAL(18,2),
    description     TEXT,
    specifications  VARCHAR(500),
    status          SMALLINT NOT NULL DEFAULT 1,
    created_by      BIGINT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by      BIGINT,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ
);
COMMENT ON TABLE crm_sales_product IS '产品表';
CREATE INDEX IF NOT EXISTS idx_product_category ON crm_sales_product (category_id);

-- 3.7 报价单表
CREATE TABLE IF NOT EXISTS crm_quotation (
    id              BIGSERIAL PRIMARY KEY,
    quotation_no    VARCHAR(50),
    customer_id     BIGINT NOT NULL,
    opportunity_id  BIGINT,
    contact_id      BIGINT,
    valid_until     DATE,
    payment_terms   TEXT,
    delivery_terms  TEXT,
    remark          TEXT,
    total_amount    DECIMAL(18,2) DEFAULT 0,
    discount_amount DECIMAL(18,2) DEFAULT 0,
    final_amount    DECIMAL(18,2) DEFAULT 0,
    status          VARCHAR(30) NOT NULL DEFAULT 'draft',
    creator_id      BIGINT,
    created_by      BIGINT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by      BIGINT,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ
);
COMMENT ON TABLE crm_quotation IS '报价单表';
COMMENT ON COLUMN crm_quotation.status IS 'draft/pending_approval/approved/rejected/voided';
CREATE INDEX IF NOT EXISTS idx_quotation_customer ON crm_quotation (customer_id);
CREATE INDEX IF NOT EXISTS idx_quotation_opportunity ON crm_quotation (opportunity_id);

-- 3.8 报价单项表
CREATE TABLE IF NOT EXISTS crm_quotation_item (
    id              BIGSERIAL PRIMARY KEY,
    quotation_id    BIGINT NOT NULL,
    product_id      BIGINT,
    product_name    VARCHAR(200) NOT NULL,
    quantity        INT NOT NULL DEFAULT 1,
    unit_price      DECIMAL(18,2) DEFAULT 0,
    discount_rate   DECIMAL(5,4) DEFAULT 1,
    subtotal        DECIMAL(18,2) DEFAULT 0,
    remark          TEXT
);
COMMENT ON TABLE crm_quotation_item IS '报价单项表';
CREATE INDEX IF NOT EXISTS idx_quotation_item_quote ON crm_quotation_item (quotation_id);

-- 3.9 合同表
CREATE TABLE IF NOT EXISTS crm_contract (
    id              BIGSERIAL PRIMARY KEY,
    contract_no     VARCHAR(50),
    customer_id     BIGINT NOT NULL,
    opportunity_id  BIGINT,
    quotation_id    BIGINT,
    template_id     BIGINT,
    title           VARCHAR(200) NOT NULL,
    total_amount    DECIMAL(18,2) DEFAULT 0,
    payment_terms   TEXT,
    valid_from      DATE,
    valid_until     DATE,
    signer_name     VARCHAR(100),
    signer_phone    VARCHAR(20),
    remark          TEXT,
    status          VARCHAR(30) NOT NULL DEFAULT 'draft',
    sign_type       VARCHAR(30),
    platform        VARCHAR(50),
    sign_url        VARCHAR(500),
    signed_at       TIMESTAMPTZ,
    created_by      BIGINT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by      BIGINT,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ
);
COMMENT ON TABLE crm_contract IS '合同表';
COMMENT ON COLUMN crm_contract.status IS 'draft/pending_sign/signed/cancelled/expired';
CREATE INDEX IF NOT EXISTS idx_contract_customer ON crm_contract (customer_id);
CREATE INDEX IF NOT EXISTS idx_contract_opportunity ON crm_contract (opportunity_id);

-- 3.10 合同项表
CREATE TABLE IF NOT EXISTS crm_contract_item (
    id              BIGSERIAL PRIMARY KEY,
    contract_id     BIGINT NOT NULL,
    product_name    VARCHAR(200) NOT NULL,
    quantity        INT NOT NULL DEFAULT 1,
    unit_price      DECIMAL(18,2) DEFAULT 0,
    subtotal        DECIMAL(18,2) DEFAULT 0
);
COMMENT ON TABLE crm_contract_item IS '合同项表';
CREATE INDEX IF NOT EXISTS idx_contract_item_contract ON crm_contract_item (contract_id);

-- 3.11 合同模板表
CREATE TABLE IF NOT EXISTS crm_contract_template (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(200) NOT NULL,
    file_url    VARCHAR(500),
    fields      JSONB,
    status      SMALLINT NOT NULL DEFAULT 1,
    created_by  BIGINT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by  BIGINT,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at  TIMESTAMPTZ
);
COMMENT ON TABLE crm_contract_template IS '合同模板表';

-- 3.12 回款计划表
CREATE TABLE IF NOT EXISTS crm_payment_plan (
    id              BIGSERIAL PRIMARY KEY,
    contract_id     BIGINT NOT NULL,
    stage           INT,
    stage_name      VARCHAR(100),
    expected_amount DECIMAL(18,2) DEFAULT 0,
    actual_amount   DECIMAL(18,2),
    expected_date   DATE,
    paid_date       DATE,
    status          VARCHAR(20) NOT NULL DEFAULT 'pending',
    payment_method  VARCHAR(50),
    voucher_urls    JSONB,
    remark          TEXT
);
COMMENT ON TABLE crm_payment_plan IS '回款计划表';
COMMENT ON COLUMN crm_payment_plan.status IS 'pending/partial/settled/overdue';
CREATE INDEX IF NOT EXISTS idx_payment_contract ON crm_payment_plan (contract_id);

-- 3.13 发票表
CREATE TABLE IF NOT EXISTS crm_invoice (
    id                  BIGSERIAL PRIMARY KEY,
    contract_id         BIGINT,
    customer_id         BIGINT,
    title               VARCHAR(200),
    tax_id              VARCHAR(50),
    invoice_type        VARCHAR(20),
    amount              DECIMAL(18,2),
    content             TEXT,
    receive_email       VARCHAR(100),
    remark              TEXT,
    status              VARCHAR(20) NOT NULL DEFAULT 'pending',
    invoice_no          VARCHAR(50),
    invoice_file_url    VARCHAR(500),
    issue_date          DATE,
    express_company     VARCHAR(100),
    express_no          VARCHAR(50),
    ship_date           DATE,
    cancel_attachment_ids JSONB,
    created_by          BIGINT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by          BIGINT,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMPTZ
);
COMMENT ON TABLE crm_invoice IS '发票表';
COMMENT ON COLUMN crm_invoice.status IS 'pending/issued/shipped/confirmed/cancelled';
CREATE INDEX IF NOT EXISTS idx_invoice_contract ON crm_invoice (contract_id);
CREATE INDEX IF NOT EXISTS idx_invoice_customer ON crm_invoice (customer_id);

-- 3.14 跟进记录表
CREATE TABLE IF NOT EXISTS crm_follow_up (
    id              BIGSERIAL PRIMARY KEY,
    customer_id     BIGINT NOT NULL,
    contact_id      BIGINT,
    opportunity_id  BIGINT,
    type            VARCHAR(20) NOT NULL DEFAULT 'call',
    content         TEXT,
    voice_url       VARCHAR(500),
    image_urls      JSONB,
    location        JSONB,
    next_plan       TEXT,
    next_plan_date  DATE,
    is_important    BOOLEAN DEFAULT FALSE,
    tags            JSONB,
    creator_id      BIGINT,
    created_by      BIGINT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by      BIGINT,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ
);
COMMENT ON TABLE crm_follow_up IS '跟进记录表';
COMMENT ON COLUMN crm_follow_up.type IS 'call/meeting/visit/mail/other';
CREATE INDEX IF NOT EXISTS idx_followup_customer ON crm_follow_up (customer_id);
CREATE INDEX IF NOT EXISTS idx_followup_opportunity ON crm_follow_up (opportunity_id);
CREATE INDEX IF NOT EXISTS idx_followup_creator ON crm_follow_up (creator_id);

-- 3.15 拜访日程表
CREATE TABLE IF NOT EXISTS crm_appointment (
    id                  BIGSERIAL PRIMARY KEY,
    customer_id         BIGINT NOT NULL,
    contact_id          BIGINT,
    title               VARCHAR(200) NOT NULL,
    description         TEXT,
    appointment_date    DATE,
    start_time          TIME,
    end_time            TIME,
    location            VARCHAR(500),
    longitude           DECIMAL(10,7),
    latitude            DECIMAL(10,7),
    type                VARCHAR(20) NOT NULL DEFAULT 'visit',
    remind_before       INT DEFAULT 30,
    participant_ids     JSONB,
    status              VARCHAR(20) NOT NULL DEFAULT 'pending',
    check_in_time       TIMESTAMPTZ,
    check_in_location   JSONB,
    photo_urls          JSONB,
    summary             TEXT,
    next_step           TEXT,
    follow_up_id        BIGINT,
    cancel_reason       TEXT,
    owner_id            BIGINT,
    created_by          BIGINT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by          BIGINT,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMPTZ
);
COMMENT ON TABLE crm_appointment IS '拜访日程表';
COMMENT ON COLUMN crm_appointment.type IS 'visit/call/meeting/other';
COMMENT ON COLUMN crm_appointment.status IS 'pending/completed/cancelled';
CREATE INDEX IF NOT EXISTS idx_appointment_customer ON crm_appointment (customer_id);
CREATE INDEX IF NOT EXISTS idx_appointment_owner ON crm_appointment (owner_id);

-- ============================================================================
-- 第四部分：商城交易域（mall_*）
-- ============================================================================

-- 4.1 SKU表（商城多规格）
CREATE TABLE IF NOT EXISTS mall_sku (
    id              BIGSERIAL PRIMARY KEY,
    product_id      BIGINT NOT NULL,
    specs           JSONB,
    price           DECIMAL(18,2) NOT NULL,
    stock           INT NOT NULL DEFAULT 0,
    frozen_stock    INT NOT NULL DEFAULT 0,
    sku_code        VARCHAR(100),
    cover_image     VARCHAR(500),
    status          SMALLINT NOT NULL DEFAULT 1,
    created_by      BIGINT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by      BIGINT,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ
);
COMMENT ON COLUMN mall_sku.specs IS '规格属性 [{"key":"颜色","value":"黑色"},{"key":"尺寸","value":"L"}]';
COMMENT ON COLUMN mall_sku.frozen_stock IS '冻结库存';
COMMENT ON COLUMN mall_sku.sku_code IS 'SKU编码';
COMMENT ON TABLE mall_sku IS 'SKU表（商城多规格）';
CREATE INDEX IF NOT EXISTS idx_sku_product ON mall_sku (product_id);
CREATE INDEX IF NOT EXISTS idx_sku_code ON mall_sku (sku_code);

-- 4.2 商城订单表
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
    status              VARCHAR(30) NOT NULL DEFAULT 'pending',
    receiver_name       VARCHAR(100),
    receiver_phone      VARCHAR(20),
    receiver_address    VARCHAR(500),
    express_company     VARCHAR(100),
    express_no          VARCHAR(100),
    customer_remark     TEXT,
    paid_at             TIMESTAMPTZ,
    shipped_at          TIMESTAMPTZ,
    completed_at        TIMESTAMPTZ,
    pickup_code         VARCHAR(20),
    pickup_at           TIMESTAMPTZ,
    created_by          BIGINT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by          BIGINT,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMPTZ
);
COMMENT ON COLUMN mall_order.status IS 'pending-待支付 paid-已支付 shipped-已发货 completed-已完成 cancelled-已取消 refunding-退款中';
COMMENT ON COLUMN mall_order.pickup_code IS '自提核销码';
COMMENT ON TABLE mall_order IS '商城订单表';
CREATE INDEX IF NOT EXISTS idx_order_customer ON mall_order (customer_id);
CREATE INDEX IF NOT EXISTS idx_order_no ON mall_order (order_no);
CREATE INDEX IF NOT EXISTS idx_order_status ON mall_order (status);
CREATE INDEX IF NOT EXISTS idx_order_created ON mall_order (created_at);

-- 4.3 订单明细表
CREATE TABLE IF NOT EXISTS mall_order_item (
    id              BIGSERIAL PRIMARY KEY,
    order_id        BIGINT NOT NULL,
    product_id      BIGINT NOT NULL,
    sku_id          BIGINT,
    product_name    VARCHAR(200) NOT NULL,
    sku_specs       JSONB,
    quantity        INT NOT NULL,
    unit_price      DECIMAL(18,2) NOT NULL,
    subtotal        DECIMAL(18,2) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
COMMENT ON COLUMN mall_order_item.sku_specs IS '购买时的规格';
COMMENT ON TABLE mall_order_item IS '订单明细表';
CREATE INDEX IF NOT EXISTS idx_order_item_order ON mall_order_item (order_id);

-- 4.4 购物车表
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
CREATE INDEX IF NOT EXISTS idx_cart_customer ON mall_cart_item (customer_id);

-- 4.5 优惠券定义表
CREATE TABLE IF NOT EXISTS mall_coupon_define (
    id                  BIGSERIAL PRIMARY KEY,
    name                VARCHAR(100) NOT NULL,
    type                VARCHAR(30) NOT NULL,
    value               DECIMAL(18,2) NOT NULL,
    condition_amount    DECIMAL(18,2) DEFAULT 0,
    total_count         INT NOT NULL,
    used_count          INT DEFAULT 0,
    per_user_limit      INT DEFAULT 1,
    valid_from          TIMESTAMPTZ NOT NULL,
    valid_until         TIMESTAMPTZ NOT NULL,
    scope               VARCHAR(20) NOT NULL DEFAULT 'all',
    product_ids         JSONB,
    channels            JSONB,
    status              SMALLINT NOT NULL DEFAULT 0,
    created_by          BIGINT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by          BIGINT,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMPTZ
);
COMMENT ON COLUMN mall_coupon_define.type IS 'full_reduce-满减 discount-折扣 new_user-新人专享';
COMMENT ON COLUMN mall_coupon_define.value IS '面值或折扣率';
COMMENT ON COLUMN mall_coupon_define.condition_amount IS '满减条件（0-无门槛）';
COMMENT ON COLUMN mall_coupon_define.total_count IS '发行总量';
COMMENT ON COLUMN mall_coupon_define.scope IS 'all-全场指定 specific-指定商品';
COMMENT ON COLUMN mall_coupon_define.product_ids IS '指定商品ID列表';
COMMENT ON COLUMN mall_coupon_define.channels IS '可用渠道';
COMMENT ON COLUMN mall_coupon_define.status IS '0-未发布 1-已发布 2-已结束';
COMMENT ON TABLE mall_coupon_define IS '优惠券定义表';
CREATE INDEX IF NOT EXISTS idx_coupon_type ON mall_coupon_define (type);
CREATE INDEX IF NOT EXISTS idx_coupon_valid ON mall_coupon_define (valid_from, valid_until);

-- 4.6 用户优惠券表
CREATE TABLE IF NOT EXISTS mall_coupon (
    id              BIGSERIAL PRIMARY KEY,
    define_id       BIGINT NOT NULL,
    customer_id     BIGINT NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'unused',
    used_at         TIMESTAMPTZ,
    order_id        BIGINT,
    received_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
COMMENT ON COLUMN mall_coupon.status IS 'unused-未使用 used-已使用 expired-已过期';
COMMENT ON TABLE mall_coupon IS '用户优惠券表';
CREATE INDEX IF NOT EXISTS idx_coupon_user ON mall_coupon (customer_id, status);
CREATE INDEX IF NOT EXISTS idx_coupon_define ON mall_coupon (define_id);

-- 4.7 支付记录表
CREATE TABLE IF NOT EXISTS mall_payment (
    id                  BIGSERIAL PRIMARY KEY,
    payment_no          VARCHAR(100) NOT NULL UNIQUE,
    order_id            BIGINT NOT NULL,
    order_type          VARCHAR(20) NOT NULL DEFAULT 'mall_order',
    customer_id         BIGINT NOT NULL,
    payment_method      VARCHAR(30) NOT NULL,
    amount              DECIMAL(18,2) NOT NULL,
    status              VARCHAR(20) NOT NULL DEFAULT 'pending',
    transaction_id      VARCHAR(100),
    open_id             VARCHAR(100),
    prepay_id           VARCHAR(100),
    pay_params          JSONB,
    paid_at             TIMESTAMPTZ,
    created_by          BIGINT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by          BIGINT,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMPTZ
);
COMMENT ON COLUMN mall_payment.order_type IS 'mall_order-商城订单';
COMMENT ON COLUMN mall_payment.payment_method IS 'wechat-微信支付 alipay-支付宝';
COMMENT ON COLUMN mall_payment.status IS 'pending-待支付 success-已成功 fail-已失败 refunding-退款中 refunded-已退款';
COMMENT ON COLUMN mall_payment.transaction_id IS '微信/支付宝交易号';
COMMENT ON COLUMN mall_payment.pay_params IS '调起支付参数';
COMMENT ON TABLE mall_payment IS '支付记录表';
CREATE INDEX IF NOT EXISTS idx_payment_order ON mall_payment (order_id);
CREATE INDEX IF NOT EXISTS idx_payment_no ON mall_payment (payment_no);
CREATE INDEX IF NOT EXISTS idx_mall_payment_status ON mall_payment (status);

-- 4.8 退款记录表
CREATE TABLE IF NOT EXISTS mall_refund (
    id                  BIGSERIAL PRIMARY KEY,
    refund_no           VARCHAR(100) NOT NULL UNIQUE,
    order_id            BIGINT NOT NULL,
    payment_id          BIGINT,
    customer_id         BIGINT NOT NULL,
    refund_amount       DECIMAL(18,2) NOT NULL,
    reason              VARCHAR(500) NOT NULL,
    status              VARCHAR(30) NOT NULL DEFAULT 'pending',
    review_comment      VARCHAR(500),
    transaction_refund_id VARCHAR(100),
    completed_at        TIMESTAMPTZ,
    created_by          BIGINT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by          BIGINT,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMPTZ
);
COMMENT ON COLUMN mall_refund.status IS 'pending-待审核 approved-已通过 rejected-已驳回 completed-已退款';
COMMENT ON COLUMN mall_refund.transaction_refund_id IS '微信退款单号';
COMMENT ON TABLE mall_refund IS '退款记录表';
CREATE INDEX IF NOT EXISTS idx_refund_order ON mall_refund (order_id);

-- 4.9 营销活动表
CREATE TABLE IF NOT EXISTS mall_activity (
    id                  BIGSERIAL PRIMARY KEY,
    name                VARCHAR(100) NOT NULL,
    type                VARCHAR(30) NOT NULL,
    start_time          TIMESTAMPTZ NOT NULL,
    end_time            TIMESTAMPTZ NOT NULL,
    rules               JSONB,
    status              SMALLINT NOT NULL DEFAULT 0,
    created_by          BIGINT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by          BIGINT,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMPTZ
);
COMMENT ON COLUMN mall_activity.type IS 'seckill-秒杀 group-拼团 cut-砍价 points-积分兑换';
COMMENT ON COLUMN mall_activity.rules IS '活动规则';
COMMENT ON COLUMN mall_activity.status IS '0-草稿 1-已发布 2-已结束';
COMMENT ON TABLE mall_activity IS '营销活动表';
CREATE INDEX IF NOT EXISTS idx_activity_type ON mall_activity (type);
CREATE INDEX IF NOT EXISTS idx_activity_time ON mall_activity (start_time, end_time);

-- 4.10 活动商品关联表
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
CREATE INDEX IF NOT EXISTS idx_activity_product_activity ON mall_activity_product (activity_id);

-- 4.11 分销关系表
CREATE TABLE IF NOT EXISTS mall_distribution (
    id              BIGSERIAL PRIMARY KEY,
    referrer_id     BIGINT NOT NULL,
    customer_id     BIGINT NOT NULL,
    order_id        BIGINT,
    commission_rate DECIMAL(5,4),
    commission_amount DECIMAL(18,2) DEFAULT 0,
    status          VARCHAR(20) NOT NULL DEFAULT 'pending',
    settled_at      TIMESTAMPTZ,
    created_by      BIGINT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by      BIGINT,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ
);
COMMENT ON COLUMN mall_distribution.referrer_id IS '推荐人客户ID';
COMMENT ON COLUMN mall_distribution.customer_id IS '被推荐人客户ID';
COMMENT ON COLUMN mall_distribution.commission_rate IS '佣金比例';
COMMENT ON COLUMN mall_distribution.status IS 'pending-待结算 settled-已结算 cancelled-已取消';
COMMENT ON TABLE mall_distribution IS '分销关系表';
CREATE INDEX IF NOT EXISTS idx_distribution_referrer ON mall_distribution (referrer_id);
CREATE INDEX IF NOT EXISTS idx_distribution_customer ON mall_distribution (customer_id);

-- 4.12 小程序页面模板表
CREATE TABLE IF NOT EXISTS mall_page_template (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    page_type       VARCHAR(30) NOT NULL DEFAULT 'homepage',
    preview_image   VARCHAR(500),
    page_config     TEXT,
    status          SMALLINT NOT NULL DEFAULT 0,
    created_by      BIGINT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by      BIGINT,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ
);
COMMENT ON COLUMN mall_page_template.page_type IS 'homepage-首页 category-分类页 product_list-商品列表 personal-个人中心';
COMMENT ON COLUMN mall_page_template.page_config IS 'JSON配置，定义页面模块布局和组件参数';
COMMENT ON COLUMN mall_page_template.status IS '0-草稿 1-已发布';
COMMENT ON TABLE mall_page_template IS '小程序页面模板表';
CREATE INDEX IF NOT EXISTS idx_page_template_type ON mall_page_template (page_type);
CREATE INDEX IF NOT EXISTS idx_page_template_status ON mall_page_template (status);

-- ============================================================================
-- 第五部分：办公协同域（coll_* / wecom_*）
-- ============================================================================

-- 5.1 审批流程定义表
CREATE TABLE IF NOT EXISTS coll_approval_define (
    id                  BIGSERIAL PRIMARY KEY,
    name                VARCHAR(100) NOT NULL,
    biz_type            VARCHAR(50) NOT NULL,
    trigger_condition   JSONB,
    status              SMALLINT NOT NULL DEFAULT 0,
    remark              VARCHAR(500),
    created_by          BIGINT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by          BIGINT,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMPTZ
);
COMMENT ON COLUMN coll_approval_define.biz_type IS 'quotation/contract/order/refund';
COMMENT ON COLUMN coll_approval_define.trigger_condition IS '触发条件 {"field":"discountRate","operator":"lt","value":0.9}';
COMMENT ON COLUMN coll_approval_define.status IS '0-草稿 1-已启用 2-已停用';
COMMENT ON TABLE coll_approval_define IS '审批流程定义表';
CREATE INDEX IF NOT EXISTS idx_approval_define_biz ON coll_approval_define (biz_type);

-- 5.2 审批流程步骤表
CREATE TABLE IF NOT EXISTS coll_approval_define_step (
    id              BIGSERIAL PRIMARY KEY,
    define_id       BIGINT NOT NULL,
    step_id         INT NOT NULL,
    step_name       VARCHAR(100) NOT NULL,
    approver_type   VARCHAR(30) NOT NULL,
    approver_ids    JSONB NOT NULL,
    step_type       VARCHAR(30) NOT NULL DEFAULT 'approve_or_reject',
    sort_order      INT NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
COMMENT ON COLUMN coll_approval_define_step.approver_type IS 'user-指定用户 role-角色 dept_leader-部门负责人 upstream_leader-上级 self_choose-自选';
COMMENT ON COLUMN coll_approval_define_step.approver_ids IS '审批人ID列表';
COMMENT ON COLUMN coll_approval_define_step.step_type IS 'approve_or_reject/countersign';
COMMENT ON TABLE coll_approval_define_step IS '审批流程步骤表';
CREATE INDEX IF NOT EXISTS idx_approval_step_define ON coll_approval_define_step (define_id);

-- 5.3 审批实例表
CREATE TABLE IF NOT EXISTS coll_approval_instance (
    id                  BIGSERIAL PRIMARY KEY,
    define_id           BIGINT NOT NULL,
    biz_type            VARCHAR(50) NOT NULL,
    biz_id              BIGINT NOT NULL,
    form_data           JSONB,
    status              VARCHAR(30) NOT NULL DEFAULT 'pending',
    current_step_id     INT,
    applicant_id        BIGINT NOT NULL,
    applicant_name      VARCHAR(100),
    completed_at        TIMESTAMPTZ,
    created_by          BIGINT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by          BIGINT,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMPTZ
);
COMMENT ON COLUMN coll_approval_instance.biz_id IS '关联业务ID';
COMMENT ON COLUMN coll_approval_instance.form_data IS '审批表单数据';
COMMENT ON COLUMN coll_approval_instance.status IS 'pending-待审批 approved-已通过 rejected-已驳回 recalled-已撤销';
COMMENT ON TABLE coll_approval_instance IS '审批实例表';
CREATE INDEX IF NOT EXISTS idx_approval_inst_define ON coll_approval_instance (define_id);
CREATE INDEX IF NOT EXISTS idx_approval_inst_biz ON coll_approval_instance (biz_type, biz_id);
CREATE INDEX IF NOT EXISTS idx_approval_inst_applicant ON coll_approval_instance (applicant_id);
CREATE INDEX IF NOT EXISTS idx_approval_inst_status ON coll_approval_instance (status);

-- 5.4 审批节点处理记录表
CREATE TABLE IF NOT EXISTS coll_approval_node_record (
    id              BIGSERIAL PRIMARY KEY,
    instance_id     BIGINT NOT NULL,
    step_id         INT NOT NULL,
    step_name       VARCHAR(100),
    approver_id     BIGINT NOT NULL,
    approver_name   VARCHAR(100),
    action          VARCHAR(20) NOT NULL,
    comment         TEXT,
    signature_image VARCHAR(500),
    acted_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
COMMENT ON COLUMN coll_approval_node_record.action IS 'approve-通过 reject-驳回 transfer-转交';
COMMENT ON TABLE coll_approval_node_record IS '审批节点处理记录表';
CREATE INDEX IF NOT EXISTS idx_approval_record_instance ON coll_approval_node_record (instance_id);
CREATE INDEX IF NOT EXISTS idx_approval_record_approver ON coll_approval_node_record (approver_id);

-- 5.5 服务工单表
CREATE TABLE IF NOT EXISTS coll_service_ticket (
    id                  BIGSERIAL PRIMARY KEY,
    ticket_no           VARCHAR(100) NOT NULL UNIQUE,
    customer_id         BIGINT NOT NULL,
    type                VARCHAR(30) NOT NULL,
    title               VARCHAR(200) NOT NULL,
    description         TEXT,
    priority            VARCHAR(20) NOT NULL DEFAULT 'normal',
    source              VARCHAR(30),
    attachment_ids      JSONB,
    assignee_id         BIGINT,
    status              VARCHAR(30) NOT NULL DEFAULT 'pending',
    customer_rating     SMALLINT,
    customer_feedback   TEXT,
    closed_at           TIMESTAMPTZ,
    ext_json            JSONB,
    created_by          BIGINT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by          BIGINT,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMPTZ
);
COMMENT ON COLUMN coll_service_ticket.type IS 'repair-报修 install-安装 complaint-投诉 other';
COMMENT ON COLUMN coll_service_ticket.priority IS 'low/normal/urgent/critical';
COMMENT ON COLUMN coll_service_ticket.source IS 'phone/wechat/mall/manual';
COMMENT ON COLUMN coll_service_ticket.attachment_ids IS '附件文件ID列表';
COMMENT ON COLUMN coll_service_ticket.assignee_id IS '处理人';
COMMENT ON COLUMN coll_service_ticket.status IS 'pending-待处理 assigned-已指派 accepted-已接单 in_progress-处理中 completed-已完成 closed-已关闭';
COMMENT ON COLUMN coll_service_ticket.customer_rating IS '客户评分 1-5';
COMMENT ON TABLE coll_service_ticket IS '服务工单表';
CREATE INDEX IF NOT EXISTS idx_ticket_customer ON coll_service_ticket (customer_id);
CREATE INDEX IF NOT EXISTS idx_ticket_assignee ON coll_service_ticket (assignee_id);
CREATE INDEX IF NOT EXISTS idx_ticket_status ON coll_service_ticket (status);
CREATE INDEX IF NOT EXISTS idx_ticket_type ON coll_service_ticket (type);
CREATE INDEX IF NOT EXISTS idx_ticket_created ON coll_service_ticket (created_at);

-- 5.6 工单操作记录表
CREATE TABLE IF NOT EXISTS coll_ticket_operation (
    id              BIGSERIAL PRIMARY KEY,
    ticket_id       BIGINT NOT NULL,
    action          VARCHAR(30) NOT NULL,
    operator_id     BIGINT NOT NULL,
    operator_name   VARCHAR(100),
    detail          JSONB,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
COMMENT ON COLUMN coll_ticket_operation.action IS 'assign/accept/start/complete/close/rate/transfer';
COMMENT ON COLUMN coll_ticket_operation.detail IS '操作详情';
COMMENT ON TABLE coll_ticket_operation IS '工单操作记录表';
CREATE INDEX IF NOT EXISTS idx_ticket_op_ticket ON coll_ticket_operation (ticket_id);

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
    msg_type        VARCHAR(30) NOT NULL,
    title           VARCHAR(200),
    content         TEXT,
    url             VARCHAR(500),
    biz_type        VARCHAR(50),
    biz_id          BIGINT,
    status          SMALLINT NOT NULL DEFAULT 1,
    fail_reason     TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
COMMENT ON COLUMN wecom_message_log.msg_type IS 'text/textcard/image';
COMMENT ON COLUMN wecom_message_log.status IS '0-失败 1-成功';
COMMENT ON TABLE wecom_message_log IS '企业微信消息推送日志表';
CREATE INDEX IF NOT EXISTS idx_wecom_msg_biz ON wecom_message_log (biz_type, biz_id);

-- 5.9 退货退款申请表
CREATE TABLE IF NOT EXISTS coll_refund_request (
    id                  BIGSERIAL PRIMARY KEY,
    refund_no           VARCHAR(100) NOT NULL UNIQUE,
    order_id            BIGINT NOT NULL,
    customer_id         BIGINT NOT NULL,
    refund_amount       DECIMAL(18,2) NOT NULL,
    refund_type         VARCHAR(20) NOT NULL,
    reason              VARCHAR(500) NOT NULL,
    description         TEXT,
    attachment_ids      JSONB,
    status              VARCHAR(30) NOT NULL DEFAULT 'pending',
    review_comment      VARCHAR(500),
    express_company     VARCHAR(100),
    express_no          VARCHAR(100),
    completed_at        TIMESTAMPTZ,
    created_by          BIGINT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by          BIGINT,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMPTZ
);
COMMENT ON COLUMN coll_refund_request.refund_type IS 'only_refund-仅退款 refund_return-退货退款';
COMMENT ON COLUMN coll_refund_request.status IS 'pending-待审核 approved-已通过 rejected-已驳回 ship_back-待寄回 received-已收货 completed-已完成';
COMMENT ON TABLE coll_refund_request IS '退货退款申请表';
CREATE INDEX IF NOT EXISTS idx_refund_req_order ON coll_refund_request (order_id);
CREATE INDEX IF NOT EXISTS idx_refund_req_status ON coll_refund_request (status);

-- ============================================================================
-- 第六部分：数据分析域（report_*）
-- ============================================================================

-- 6.1 自定义报表表
CREATE TABLE IF NOT EXISTS report_custom_report (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    data_source     VARCHAR(50) NOT NULL,
    dimensions      JSONB NOT NULL,
    metrics         JSONB NOT NULL,
    filters         JSONB,
    chart_type      VARCHAR(30) DEFAULT 'bar',
    schedule        VARCHAR(50),
    status          SMALLINT NOT NULL DEFAULT 1,
    created_by      BIGINT NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by      BIGINT,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ
);
COMMENT ON COLUMN report_custom_report.data_source IS 'opportunity/customer/order/follow_up';
COMMENT ON COLUMN report_custom_report.dimensions IS '维度列表 ["dept_name","owner_name","date"]';
COMMENT ON COLUMN report_custom_report.metrics IS '指标列表 ["count","sum_amount"]';
COMMENT ON COLUMN report_custom_report.filters IS '筛选条件';
COMMENT ON COLUMN report_custom_report.chart_type IS 'bar/line/pie/table';
COMMENT ON COLUMN report_custom_report.schedule IS '自动生成cron';
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
    layout          JSONB NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
COMMENT ON COLUMN report_dashboard_layout.layout IS '看板布局 [{"cardType":"sales_funnel","position":1,"width":6,"height":2}]';
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

ALTER TABLE crm_opportunity ADD CONSTRAINT fk_opp_customer FOREIGN KEY (customer_id) REFERENCES crm_customer(id);
ALTER TABLE crm_opportunity ADD CONSTRAINT fk_opp_stage FOREIGN KEY (stage_id) REFERENCES crm_opportunity_stage(id);
ALTER TABLE crm_opportunity ADD CONSTRAINT fk_opp_owner FOREIGN KEY (owner_id) REFERENCES sys_user(id);
ALTER TABLE crm_opportunity_participant ADD CONSTRAINT fk_opp_part_opp FOREIGN KEY (opportunity_id) REFERENCES crm_opportunity(id) ON DELETE CASCADE;
ALTER TABLE crm_opportunity_stage_log ADD CONSTRAINT fk_opp_stage_log_opp FOREIGN KEY (opportunity_id) REFERENCES crm_opportunity(id) ON DELETE CASCADE;
ALTER TABLE crm_follow_up ADD CONSTRAINT fk_fu_customer FOREIGN KEY (customer_id) REFERENCES crm_customer(id);
ALTER TABLE crm_follow_up ADD CONSTRAINT fk_fu_creator FOREIGN KEY (created_by) REFERENCES sys_user(id);
ALTER TABLE crm_appointment ADD CONSTRAINT fk_appt_customer FOREIGN KEY (customer_id) REFERENCES crm_customer(id);
ALTER TABLE crm_appointment ADD CONSTRAINT fk_appt_owner FOREIGN KEY (owner_id) REFERENCES sys_user(id);
ALTER TABLE crm_quotation ADD CONSTRAINT fk_quote_customer FOREIGN KEY (customer_id) REFERENCES crm_customer(id);
ALTER TABLE crm_quotation_item ADD CONSTRAINT fk_quote_item_quote FOREIGN KEY (quotation_id) REFERENCES crm_quotation(id) ON DELETE CASCADE;
ALTER TABLE crm_contract ADD CONSTRAINT fk_contract_customer FOREIGN KEY (customer_id) REFERENCES crm_customer(id);
ALTER TABLE crm_contract_item ADD CONSTRAINT fk_contract_item_contract FOREIGN KEY (contract_id) REFERENCES crm_contract(id) ON DELETE CASCADE;
ALTER TABLE crm_payment_plan ADD CONSTRAINT fk_payment_contract FOREIGN KEY (contract_id) REFERENCES crm_contract(id) ON DELETE CASCADE;
ALTER TABLE crm_invoice ADD CONSTRAINT fk_invoice_contract FOREIGN KEY (contract_id) REFERENCES crm_contract(id);
ALTER TABLE crm_sales_product ADD CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES crm_product_category(id);

ALTER TABLE mall_sku ADD CONSTRAINT fk_sku_product FOREIGN KEY (product_id) REFERENCES crm_sales_product(id) ON DELETE CASCADE;
ALTER TABLE mall_order ADD CONSTRAINT fk_order_customer FOREIGN KEY (customer_id) REFERENCES crm_customer(id);
ALTER TABLE mall_order_item ADD CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES mall_order(id) ON DELETE CASCADE;
ALTER TABLE mall_coupon ADD CONSTRAINT fk_coupon_define FOREIGN KEY (define_id) REFERENCES mall_coupon_define(id);
ALTER TABLE mall_coupon ADD CONSTRAINT fk_coupon_customer FOREIGN KEY (customer_id) REFERENCES crm_customer(id);
ALTER TABLE mall_payment ADD CONSTRAINT fk_payment_order FOREIGN KEY (order_id) REFERENCES mall_order(id);
ALTER TABLE mall_refund ADD CONSTRAINT fk_refund_order FOREIGN KEY (order_id) REFERENCES mall_order(id);
ALTER TABLE mall_activity_product ADD CONSTRAINT fk_act_product_activity FOREIGN KEY (activity_id) REFERENCES mall_activity(id) ON DELETE CASCADE;
ALTER TABLE mall_activity_product ADD CONSTRAINT fk_act_product_product FOREIGN KEY (product_id) REFERENCES crm_sales_product(id);

ALTER TABLE coll_approval_define_step ADD CONSTRAINT fk_approval_step_define FOREIGN KEY (define_id) REFERENCES coll_approval_define(id) ON DELETE CASCADE;

-- 兼容已有数据库：coll_approval_instance 补充缺失列
ALTER TABLE coll_approval_instance ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ;
ALTER TABLE coll_approval_instance ADD COLUMN IF NOT EXISTS created_by BIGINT;
ALTER TABLE coll_approval_instance ADD COLUMN IF NOT EXISTS updated_by BIGINT;

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
-- CREATE INDEX IF NOT EXISTS idx_customer_name_gin ON crm_customer USING gin (name gin_trgm_ops);

-- JSONB 索引入门（按需添加）
-- CREATE INDEX IF NOT EXISTS idx_lead_ext ON crm_lead USING gin (ext_json);

-- 复合索引：常用列表查询
CREATE INDEX IF NOT EXISTS idx_fu_customer_created ON crm_follow_up (customer_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_opp_owner_stage ON crm_opportunity (owner_id, stage_id);
CREATE INDEX IF NOT EXISTS idx_lead_owner_status ON crm_lead (owner_id, status);

-- ============================================================================
-- 第九部分：初始化数据
-- ============================================================================

-- 9.1 默认管理员
INSERT INTO sys_dept (id, parent_id, name, sort_order, status) VALUES (1, 0, '总公司', 1, 1);

INSERT INTO sys_user (id, username, password, real_name, dept_id, status)
VALUES (1, 'admin', '$2a$10$YDrNOBsC0ad27KQBjuU1OekZLZQousMiQkerg5kzUJFN58LSO2/qm', '系统管理员', 1, 1);

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
(31, 21, '营销活动', 'C', '/mall/activity', 'mall/activity/index', 4, 1),
(36, 21, '小程序页面', 'C', '/mall/page', 'mall/page/index', 5, 1),
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
('lead_source', 'offline', '线下活动', 3),
('lead_source', 'manual_input', '手工录入', 4),
('lead_source', 'referral', '朋友推荐', 5),
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
INSERT INTO crm_opportunity_stage (id, name, sort_order, probability, category) VALUES
(1, '初步接触', 1, 10, 'open'),
(2, '需求分析', 2, 25, 'open'),
(3, '方案报价', 3, 50, 'open'),
(4, '商务谈判', 4, 75, 'open'),
(5, '赢单', 5, 100, 'win'),
(6, '输单', 6, 0, 'lose');

-- 9.8 默认产品分类
INSERT INTO crm_product_category (id, name, parent_id, sort_order, status, created_by) VALUES
(1, '软件产品', NULL, 1, 1, 1),
(2, '硬件设备', NULL, 2, 1, 1),
(3, '技术服务', NULL, 3, 1, 1);

-- 9.9 默认产品
INSERT INTO crm_sales_product (id, category_id, name, unit, standard_price, status, created_by) VALUES
(1, 1, 'CRM标准版', '套', 50000.00, 1, 1),
(2, 1, 'CRM企业版', '套', 150000.00, 1, 1),
(3, 2, '服务器', '台', 30000.00, 1, 1),
(4, 3, '实施服务', '人/天', 3000.00, 1, 1),
(5, 3, '培训服务', '人/天', 2000.00, 1, 1);

-- 9.10 默认客户等级
INSERT INTO crm_customer_level (id, name, min_amount, max_amount, min_order_count, sort_order) VALUES
(1, '普通客户', 0, 9999.99, 0, 1),
(2, '银卡客户', 10000, 49999.99, 2, 2),
(3, '金卡客户', 50000, 199999.99, 5, 3),
(4, '钻石客户', 200000, 99999999.99, 10, 4);

-- 9.11 按钮权限（与后端 @PreAuthorize 对应）
INSERT INTO sys_menu (id, parent_id, name, menu_type, permission_code, sort_order, status) VALUES

-- 系统管理 - 用户管理
(1001, 2, '用户列表', 'F', 'system:user:list', 1, 1),
(1002, 2, '用户查询', 'F', 'system:user:query', 2, 1),
(1003, 2, '新增用户', 'F', 'system:user:create', 3, 1),
(1004, 2, '编辑用户', 'F', 'system:user:edit', 4, 1),
(1005, 2, '删除用户', 'F', 'system:user:delete', 5, 1),
(1006, 2, '导入用户', 'F', 'system:user:import', 6, 1),
(1007, 2, '导出用户', 'F', 'system:user:export', 7, 1),

-- 系统管理 - 角色管理
(1008, 3, '角色列表', 'F', 'system:role:list', 1, 1),
(1009, 3, '新增角色', 'F', 'system:role:create', 2, 1),
(1010, 3, '角色查询', 'F', 'system:role:query', 3, 1),
(1011, 3, '编辑角色', 'F', 'system:role:edit', 4, 1),
(1012, 3, '删除角色', 'F', 'system:role:delete', 5, 1),

-- 系统管理 - 菜单管理
(1013, 4, '菜单列表', 'F', 'system:menu:list', 1, 1),
(1014, 4, '新增菜单', 'F', 'system:menu:create', 2, 1),
(1015, 4, '编辑菜单', 'F', 'system:menu:edit', 3, 1),
(1016, 4, '删除菜单', 'F', 'system:menu:delete', 4, 1),

-- 系统管理 - 部门管理
(1017, 5, '部门列表', 'F', 'system:dept:list', 1, 1),
(1018, 5, '新增部门', 'F', 'system:dept:create', 2, 1),
(1019, 5, '编辑部门', 'F', 'system:dept:edit', 3, 1),
(1020, 5, '删除部门', 'F', 'system:dept:delete', 4, 1),

-- 系统管理 - 字典管理
(1021, 6, '字典列表', 'F', 'system:dict:list', 1, 1),
(1022, 6, '新增字典', 'F', 'system:dict:create', 2, 1),
(1023, 6, '字典查询', 'F', 'system:dict:query', 3, 1),
(1024, 6, '编辑字典', 'F', 'system:dict:edit', 4, 1),
(1025, 6, '删除字典', 'F', 'system:dict:delete', 5, 1),

-- 系统管理 - 配置管理
(1026, 7, '配置列表', 'F', 'system:config:list', 1, 1),
(1027, 7, '配置查询', 'F', 'system:config:query', 2, 1),
(1028, 7, '编辑配置', 'F', 'system:config:edit', 3, 1),

-- 系统管理 - 操作日志
(1029, 8, '日志列表', 'F', 'system:log:list', 1, 1),
(1030, 8, '日志查询', 'F', 'system:log:query', 2, 1),
(1031, 8, '删除日志', 'F', 'system:log:delete', 3, 1),
(1032, 8, '导出日志', 'F', 'system:log:export', 4, 1),

-- 系统管理 - API密钥
(1033, 1, 'API密钥列表', 'F', 'system:api-key:list', 1, 1),
(1034, 1, '创建API密钥', 'F', 'system:api-key:create', 2, 1),
(1035, 1, '编辑API密钥', 'F', 'system:api-key:edit', 3, 1),
(1036, 1, '删除API密钥', 'F', 'system:api-key:delete', 4, 1),

-- 系统管理 - 消息与文件
(1037, 1, '发送消息', 'F', 'system:message:send', 1, 1),
(1038, 1, '删除文件', 'F', 'system:file:delete', 2, 1),

-- 客户管理 - 线索管理
(1039, 10, '线索列表', 'F', 'customer:lead:list', 1, 1),
(1040, 10, '新增线索', 'F', 'customer:lead:create', 2, 1),
(1041, 10, '线索查询', 'F', 'customer:lead:query', 3, 1),
(1042, 10, '编辑线索', 'F', 'customer:lead:edit', 4, 1),
(1043, 10, '删除线索', 'F', 'customer:lead:delete', 5, 1),
(1044, 10, '导入线索', 'F', 'customer:lead:import', 6, 1),
(1045, 10, '转换线索', 'F', 'customer:lead:convert', 7, 1),
(1046, 10, '分配线索', 'F', 'customer:lead:distribute', 8, 1),

-- 客户管理 - 客户列表
(1047, 11, '客户列表', 'F', 'customer:customer:list', 1, 1),
(1048, 11, '新增客户', 'F', 'customer:customer:create', 2, 1),
(1049, 11, '客户查询', 'F', 'customer:customer:query', 3, 1),
(1050, 11, '编辑客户', 'F', 'customer:customer:edit', 4, 1),
(1051, 11, '删除客户', 'F', 'customer:customer:delete', 5, 1),
(1052, 11, '导入客户', 'F', 'customer:customer:import', 6, 1),
(1053, 11, '导出客户', 'F', 'customer:customer:export', 7, 1),

-- 客户管理 - 标签管理
(1054, 12, '标签列表', 'F', 'customer:tag:list', 1, 1),
(1055, 12, '新增标签', 'F', 'customer:tag:create', 2, 1),
(1056, 12, '编辑标签', 'F', 'customer:tag:edit', 3, 1),
(1057, 12, '删除标签', 'F', 'customer:tag:delete', 4, 1),
(1058, 12, '自动打标规则', 'F', 'customer:tag:auto-rule', 5, 1),

-- 客户管理 - 客户分群
(1059, 13, '分群列表', 'F', 'customer:segment:list', 1, 1),
(1060, 13, '新增分群', 'F', 'customer:segment:create', 2, 1),
(1061, 13, '编辑分群', 'F', 'customer:segment:edit', 3, 1),
(1062, 13, '删除分群', 'F', 'customer:segment:delete', 4, 1),
(1063, 13, '分群查询', 'F', 'customer:segment:query', 5, 1),
(1064, 13, '分群营销', 'F', 'customer:segment:campaign', 6, 1),

-- 客户管理 - 联系人
(1065, 14, '联系人列表', 'F', 'customer:contact:list', 1, 1),
(1066, 14, '新增联系人', 'F', 'customer:contact:create', 2, 1),
(1067, 14, '编辑联系人', 'F', 'customer:contact:edit', 3, 1),
(1068, 14, '删除联系人', 'F', 'customer:contact:delete', 4, 1),

-- 客户管理 - 分配规则
(1069, 9, '分配规则列表', 'F', 'customer:distribution:list', 1, 1),
(1070, 9, '新增分配规则', 'F', 'customer:distribution:create', 2, 1),
(1071, 9, '编辑分配规则', 'F', 'customer:distribution:edit', 3, 1),
(1072, 9, '删除分配规则', 'F', 'customer:distribution:delete', 4, 1),
(1073, 9, '执行分配', 'F', 'customer:distribution:execute', 5, 1),
(1074, 9, '分配规则查询', 'F', 'customer:distribution:query', 6, 1),

-- 客户管理 - 客户等级
(1075, 9, '等级列表', 'F', 'customer:level:list', 7, 1),
(1076, 9, '新增等级', 'F', 'customer:level:create', 8, 1),
(1077, 9, '编辑等级', 'F', 'customer:level:edit', 9, 1),
(1078, 9, '删除等级', 'F', 'customer:level:delete', 10, 1),
(1079, 9, '等级查询', 'F', 'customer:level:query', 11, 1),
(1080, 9, '等级评估', 'F', 'customer:level:evaluate', 12, 1),

-- 销售管理 - 商机看板
(1081, 16, '商机列表', 'F', 'sales:opportunity:list', 1, 1),
(1082, 16, '新增商机', 'F', 'sales:opportunity:create', 2, 1),
(1083, 16, '商机查询', 'F', 'sales:opportunity:query', 3, 1),
(1084, 16, '编辑商机', 'F', 'sales:opportunity:edit', 4, 1),
(1085, 16, '删除商机', 'F', 'sales:opportunity:delete', 5, 1),
(1086, 16, '阶段列表', 'F', 'sales:stage:list', 6, 1),
(1087, 16, '新增阶段', 'F', 'sales:stage:create', 7, 1),
(1088, 16, '编辑阶段', 'F', 'sales:stage:edit', 8, 1),
(1089, 16, '删除阶段', 'F', 'sales:stage:delete', 9, 1),

-- 销售管理 - 跟进记录
(1090, 17, '跟进列表', 'F', 'sales:followup:list', 1, 1),
(1091, 17, '新增跟进', 'F', 'sales:followup:create', 2, 1),
(1092, 17, '跟进查询', 'F', 'sales:followup:query', 3, 1),
(1093, 17, '编辑跟进', 'F', 'sales:followup:edit', 4, 1),
(1094, 17, '删除跟进', 'F', 'sales:followup:delete', 5, 1),

-- 销售管理 - 报价管理
(1095, 18, '报价列表', 'F', 'sales:quotation:list', 1, 1),
(1096, 18, '新增报价', 'F', 'sales:quotation:create', 2, 1),
(1097, 18, '报价查询', 'F', 'sales:quotation:query', 3, 1),
(1098, 18, '编辑报价', 'F', 'sales:quotation:edit', 4, 1),
(1099, 18, '删除报价', 'F', 'sales:quotation:delete', 5, 1),
(1100, 18, '报价审批', 'F', 'sales:quotation:approve', 6, 1),
(1101, 18, '导出报价', 'F', 'sales:quotation:export', 7, 1),
(1102, 18, '产品列表', 'F', 'sales:product:list', 8, 1),
(1103, 18, '新增产品', 'F', 'sales:product:create', 9, 1),
(1104, 18, '编辑产品', 'F', 'sales:product:edit', 10, 1),
(1105, 18, '删除产品', 'F', 'sales:product:delete', 11, 1),
(1106, 18, '产品分类列表', 'F', 'sales:category:list', 12, 1),
(1107, 18, '新增产品分类', 'F', 'sales:category:create', 13, 1),
(1108, 18, '编辑产品分类', 'F', 'sales:category:edit', 14, 1),
(1109, 18, '删除产品分类', 'F', 'sales:category:delete', 15, 1),

-- 销售管理 - 合同管理
(1110, 19, '合同列表', 'F', 'sales:contract:list', 1, 1),
(1111, 19, '新增合同', 'F', 'sales:contract:create', 2, 1),
(1112, 19, '合同查询', 'F', 'sales:contract:query', 3, 1),
(1113, 19, '编辑合同', 'F', 'sales:contract:edit', 4, 1),
(1114, 19, '删除合同', 'F', 'sales:contract:delete', 5, 1),
(1115, 19, '回款计划列表', 'F', 'sales:payment:list', 6, 1),
(1116, 19, '新增回款计划', 'F', 'sales:payment:create', 7, 1),
(1117, 19, '编辑回款计划', 'F', 'sales:payment:edit', 8, 1),
(1118, 19, '删除回款计划', 'F', 'sales:payment:delete', 9, 1),
(1119, 19, '合同模板列表', 'F', 'sales:template:list', 10, 1),
(1120, 19, '新增合同模板', 'F', 'sales:template:create', 11, 1),
(1121, 19, '编辑合同模板', 'F', 'sales:template:edit', 12, 1),
(1122, 19, '删除合同模板', 'F', 'sales:template:delete', 13, 1),

-- 销售管理 - 回款与发票
(1123, 20, '发票列表', 'F', 'sales:invoice:list', 1, 1),
(1124, 20, '新增发票', 'F', 'sales:invoice:create', 2, 1),
(1125, 20, '开票', 'F', 'sales:invoice:issue', 3, 1),
(1126, 20, '发货', 'F', 'sales:invoice:ship', 4, 1),
(1127, 20, '确认', 'F', 'sales:invoice:confirm', 5, 1),
(1128, 20, '取消', 'F', 'sales:invoice:cancel', 6, 1),
(1129, 20, '发票查询', 'F', 'sales:invoice:query', 7, 1),

-- 销售管理 - 日程
(1130, 15, '日程列表', 'F', 'sales:appointment:list', 1, 1),
(1131, 15, '新增日程', 'F', 'sales:appointment:create', 2, 1),
(1132, 15, '编辑日程', 'F', 'sales:appointment:edit', 3, 1),
(1133, 15, '删除日程', 'F', 'sales:appointment:delete', 4, 1),

-- 商城管理 - 商品管理
(1134, 22, '商品列表', 'F', 'mall:product:list', 1, 1),
(1135, 22, '新增商品', 'F', 'mall:product:create', 2, 1),
(1136, 22, '商品查询', 'F', 'mall:product:query', 3, 1),
(1137, 22, '编辑商品', 'F', 'mall:product:edit', 4, 1),
(1138, 22, '删除商品', 'F', 'mall:product:delete', 5, 1),
(1139, 22, '导入商品', 'F', 'mall:product:import', 6, 1),
(1140, 22, '导出商品', 'F', 'mall:product:export', 7, 1),
(1141, 22, '分类列表', 'F', 'mall:category:list', 8, 1),
(1142, 22, '新增分类', 'F', 'mall:category:create', 9, 1),
(1143, 22, '编辑分类', 'F', 'mall:category:edit', 10, 1),
(1144, 22, '删除分类', 'F', 'mall:category:delete', 11, 1),

-- 商城管理 - 订单管理
(1145, 23, '订单列表', 'F', 'mall:order:list', 1, 1),
(1146, 23, '订单查询', 'F', 'mall:order:query', 2, 1),
(1147, 23, '编辑订单', 'F', 'mall:order:edit', 3, 1),

-- 商城管理 - 优惠券
(1148, 24, '优惠券列表', 'F', 'mall:coupon:list', 1, 1),
(1149, 24, '新增优惠券', 'F', 'mall:coupon:create', 2, 1),
(1150, 24, '编辑优惠券', 'F', 'mall:coupon:edit', 3, 1),
(1151, 24, '删除优惠券', 'F', 'mall:coupon:delete', 4, 1),
(1152, 24, '发放优惠券', 'F', 'mall:coupon:distribute', 5, 1),

-- 商城管理 - 分销
(1153, 21, '分销列表', 'F', 'mall:distribution:list', 1, 1),
(1154, 21, '编辑分销', 'F', 'mall:distribution:edit', 2, 1),

-- 商城管理 - 营销活动
(1192, 31, '活动列表', 'F', 'mall:activity:list', 1, 1),
(1193, 31, '新增活动', 'F', 'mall:activity:create', 2, 1),
(1194, 31, '活动查询', 'F', 'mall:activity:query', 3, 1),
(1195, 31, '编辑活动', 'F', 'mall:activity:edit', 4, 1),
(1196, 31, '删除活动', 'F', 'mall:activity:delete', 5, 1),

-- 商城管理 - 小程序页面
(1203, 36, '页面列表', 'F', 'mall:page-template:list', 1, 1),
(1204, 36, '新增页面', 'F', 'mall:page-template:create', 2, 1),
(1205, 36, '页面查询', 'F', 'mall:page-template:query', 3, 1),
(1206, 36, '编辑页面', 'F', 'mall:page-template:edit', 4, 1),
(1207, 36, '删除页面', 'F', 'mall:page-template:delete', 5, 1),

-- 数据分析 - 核心看板
(1155, 26, '看板查询', 'F', 'report:dashboard:query', 1, 1),
(1156, 26, '看板编辑', 'F', 'report:dashboard:edit', 2, 1),

-- 数据分析 - 自定义报表
(1157, 27, '报表列表', 'F', 'report:custom-report:list', 1, 1),
(1158, 27, '新增报表', 'F', 'report:custom-report:create', 2, 1),
(1159, 27, '报表查询', 'F', 'report:custom-report:query', 3, 1),
(1160, 27, '导出报表', 'F', 'report:custom-report:export', 4, 1),
(1161, 27, '编辑报表', 'F', 'report:custom-report:edit', 5, 1),
(1162, 27, '删除报表', 'F', 'report:custom-report:delete', 6, 1),

-- 协同办公 - 审批管理
(1163, 29, '审批定义列表', 'F', 'collaboration:approval-define:list', 1, 1),
(1164, 29, '新增审批定义', 'F', 'collaboration:approval-define:create', 2, 1),
(1165, 29, '审批定义查询', 'F', 'collaboration:approval-define:query', 3, 1),
(1166, 29, '编辑审批定义', 'F', 'collaboration:approval-define:edit', 4, 1),
(1167, 29, '删除审批定义', 'F', 'collaboration:approval-define:delete', 5, 1),
(1168, 29, '审批实例列表', 'F', 'collaboration:approval-instance:list', 6, 1),
(1169, 29, '发起审批', 'F', 'collaboration:approval-instance:create', 7, 1),
(1170, 29, '审批查询', 'F', 'collaboration:approval-instance:query', 8, 1),
(1171, 29, '审批通过', 'F', 'collaboration:approval-instance:approve', 9, 1),
(1172, 29, '审批拒绝', 'F', 'collaboration:approval-instance:reject', 10, 1),
(1173, 29, '撤回审批', 'F', 'collaboration:approval-instance:recall', 11, 1),

-- 协同办公 - 服务工单
(1174, 30, '工单列表', 'F', 'collaboration:ticket:list', 1, 1),
(1175, 30, '创建工单', 'F', 'collaboration:ticket:create', 2, 1),
(1176, 30, '工单查询', 'F', 'collaboration:ticket:query', 3, 1),
(1177, 30, '编辑工单', 'F', 'collaboration:ticket:edit', 4, 1),
(1178, 30, '删除工单', 'F', 'collaboration:ticket:delete', 5, 1),
(1179, 30, '分配工单', 'F', 'collaboration:ticket:assign', 6, 1),
(1180, 30, '接单', 'F', 'collaboration:ticket:accept', 7, 1),
(1181, 30, '开始处理', 'F', 'collaboration:ticket:start', 8, 1),
(1182, 30, '完成工单', 'F', 'collaboration:ticket:complete', 9, 1),
(1183, 30, '评价工单', 'F', 'collaboration:ticket:rate', 10, 1),
(1184, 30, '退款列表', 'F', 'collaboration:refund:list', 11, 1),
(1185, 30, '退款查询', 'F', 'collaboration:refund:query', 12, 1),
(1186, 30, '退款审批', 'F', 'collaboration:refund:approve', 13, 1),
(1187, 30, '退款驳回', 'F', 'collaboration:refund:reject', 14, 1),
(1188, 30, '退款确认', 'F', 'collaboration:refund:receive', 15, 1),
(1189, 30, '退款完成', 'F', 'collaboration:refund:complete', 16, 1),

-- 协同办公 - 企微
(1190, 28, '绑定企微', 'F', 'collaboration:wecom:bind', 1, 1),
(1191, 28, '发送消息', 'F', 'collaboration:wecom:send', 2, 1);

-- 9.12 管理员角色权限绑定（关联所有按钮权限）
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 1001),(1, 1002),(1, 1003),(1, 1004),(1, 1005),(1, 1006),(1, 1007),
(1, 1008),(1, 1009),(1, 1010),(1, 1011),(1, 1012),
(1, 1013),(1, 1014),(1, 1015),(1, 1016),
(1, 1017),(1, 1018),(1, 1019),(1, 1020),
(1, 1021),(1, 1022),(1, 1023),(1, 1024),(1, 1025),
(1, 1026),(1, 1027),(1, 1028),
(1, 1029),(1, 1030),(1, 1031),(1, 1032),
(1, 1033),(1, 1034),(1, 1035),(1, 1036),
(1, 1037),(1, 1038),
(1, 1039),(1, 1040),(1, 1041),(1, 1042),(1, 1043),(1, 1044),(1, 1045),(1, 1046),
(1, 1047),(1, 1048),(1, 1049),(1, 1050),(1, 1051),(1, 1052),(1, 1053),
(1, 1054),(1, 1055),(1, 1056),(1, 1057),(1, 1058),
(1, 1059),(1, 1060),(1, 1061),(1, 1062),(1, 1063),(1, 1064),
(1, 1065),(1, 1066),(1, 1067),(1, 1068),
(1, 1069),(1, 1070),(1, 1071),(1, 1072),(1, 1073),(1, 1074),
(1, 1075),(1, 1076),(1, 1077),(1, 1078),(1, 1079),(1, 1080),
(1, 1081),(1, 1082),(1, 1083),(1, 1084),(1, 1085),
(1, 1086),(1, 1087),(1, 1088),(1, 1089),
(1, 1090),(1, 1091),(1, 1092),(1, 1093),(1, 1094),
(1, 1095),(1, 1096),(1, 1097),(1, 1098),(1, 1099),(1, 1100),(1, 1101),
(1, 1102),(1, 1103),(1, 1104),(1, 1105),
(1, 1106),(1, 1107),(1, 1108),(1, 1109),
(1, 1110),(1, 1111),(1, 1112),(1, 1113),(1, 1114),
(1, 1115),(1, 1116),(1, 1117),(1, 1118),
(1, 1119),(1, 1120),(1, 1121),(1, 1122),
(1, 1123),(1, 1124),(1, 1125),(1, 1126),(1, 1127),(1, 1128),(1, 1129),
(1, 1130),(1, 1131),(1, 1132),(1, 1133),
(1, 1134),(1, 1135),(1, 1136),(1, 1137),(1, 1138),(1, 1139),(1, 1140),
(1, 1141),(1, 1142),(1, 1143),(1, 1144),
(1, 1145),(1, 1146),(1, 1147),
(1, 1148),(1, 1149),(1, 1150),(1, 1151),(1, 1152),
(1, 1153),(1, 1154),
(1, 1155),(1, 1156),
(1, 1157),(1, 1158),(1, 1159),(1, 1160),(1, 1161),(1, 1162),
(1, 1163),(1, 1164),(1, 1165),(1, 1166),(1, 1167),
(1, 1168),(1, 1169),(1, 1170),(1, 1171),(1, 1172),(1, 1173),
(1, 1174),(1, 1175),(1, 1176),(1, 1177),(1, 1178),(1, 1179),(1, 1180),(1, 1181),(1, 1182),(1, 1183),
(1, 1184),(1, 1185),(1, 1186),(1, 1187),(1, 1188),(1, 1189),
(1, 1190),(1, 1191),
(1, 1192),(1, 1193),(1, 1194),(1, 1195),(1, 1196),
(1, 1203),(1, 1204),(1, 1205),(1, 1206),(1, 1207);

-- 绑定 小程序页面 菜单到管理员角色
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 36);

-- 迁移：为已有数据库补充 updated_by 列
ALTER TABLE crm_customer_level ADD COLUMN IF NOT EXISTS updated_by BIGINT;

-- ============================================================================
-- 第十部分：业务数据初始化（种子数据）
-- ============================================================================

-- 10.1 客户示例数据（owner_id=1 为 admin 用户）
INSERT INTO crm_customer (id, name, phone, company, position, province, city, source_channel, level_id, owner_id, last_contact_at, total_consumption, order_count, created_by, created_at) VALUES
(1, '华为技术有限公司', '13800138001', '华为技术有限公司', '企业客户', '广东省', '深圳市', 'offline', 3, 1, NOW() - INTERVAL '2 days', 158000.00, 12, 1, NOW() - INTERVAL '5 days'),
(2, '阿里巴巴集团', '13800138002', '阿里巴巴集团', '企业客户', '浙江省', '杭州市', 'referral', 3, 1, NOW() - INTERVAL '5 days', 220000.00, 18, 1, NOW() - INTERVAL '3 days'),
(3, '腾讯科技有限公司', '13800138003', '腾讯科技有限公司', '企业客户', '广东省', '深圳市', 'offline', 2, 1, NOW() - INTERVAL '30 days', 45000.00, 5, 1, NOW() - INTERVAL '60 days'),
(4, '字节跳动', '13800138004', '字节跳动', '企业客户', '北京市', '北京市', 'ad_landing', 2, 1, NOW() - INTERVAL '15 days', 32000.00, 3, 1, NOW() - INTERVAL '10 days'),
(5, '小米科技有限责任公司', '13800138005', '小米科技有限责任公司', '企业客户', '北京市', '北京市', 'referral', 1, 1, NOW() - INTERVAL '60 days', 8500.00, 2, 1, NOW() - INTERVAL '140 days'),
(6, '比亚迪股份有限公司', '13800138006', '比亚迪股份有限公司', '企业客户', '广东省', '深圳市', 'offline', 2, 1, NOW() - INTERVAL '7 days', 28000.00, 4, 1, NOW() - INTERVAL '130 days'),
(7, '京东集团', '13800138007', '京东集团', '企业客户', '北京市', '北京市', 'ad_landing', 1, 1, NOW() - INTERVAL '45 days', 5600.00, 1, 1, NOW() - INTERVAL '120 days'),
(8, '网易集团', '13800138008', '网易集团', '企业客户', '广东省', '广州市', 'referral', 4, 1, NOW() - INTERVAL '3 days', 350000.00, 25, 1, NOW() - INTERVAL '110 days');

-- 10.2 联系人示例数据
INSERT INTO crm_contact (customer_id, name, phone, email, position, department, is_decision_maker, is_primary, birthday, remark, created_by, created_at) VALUES
(1, '王建国', '138****8001', 'wangjg@huawei.com', 'CTO', '技术部', TRUE, TRUE, '1985-06-15', '技术决策者，对数据安全要求高', 1, NOW()),
(1, '陈静', '134****8006', 'chenj@huawei.com', '财务总监', '财务部', TRUE, FALSE, '1980-12-05', '预算审批关键人', 1, NOW()),
(2, '李明芳', '139****8002', 'limf@alibaba.com', '采购总监', '采购部', TRUE, TRUE, '1982-03-22', '负责IT类采购', 1, NOW()),
(2, '孙鹏', '133****8007', 'sunp@alibaba.com', '项目经理', '项目部', FALSE, FALSE, '1991-05-18', '项目执行层面对接', 1, NOW()),
(3, '张伟', '137****8003', 'zhangw@tencent.com', '技术经理', '技术部', FALSE, TRUE, '1990-11-08', '技术对接人', 1, NOW()),
(3, '周婷', '132****8008', 'zhout@tencent.com', '市场总监', '市场部', TRUE, FALSE, '1986-08-25', '市场合作决策人', 1, NOW()),
(4, '赵晓雪', '136****8004', 'zhaoxx@bytedance.com', '运营总监', '运营部', TRUE, TRUE, '1988-07-30', NULL, 1, NOW()),
(5, '刘洋', '135****8005', 'liuy@xiaomi.com', '产品经理', '产品部', FALSE, TRUE, '1992-09-12', '产品需求对接', 1, NOW()),
(6, '张明', '137****8010', 'zhangm@byd.com', 'IT总监', '信息部', TRUE, TRUE, '1983-04-20', '信息化建设负责人', 1, NOW()),
(7, '李华', '138****8011', 'lihua@jd.com', '运营经理', '运营部', FALSE, TRUE, '1993-11-01', NULL, 1, NOW()),
(8, '王芳', '139****8012', 'wangf@163.com', '副总裁', '总裁办', TRUE, TRUE, '1979-08-15', '高层决策人', 1, NOW());

-- 10.3 标签示例数据
INSERT INTO crm_tag (id, name, color, type, status, created_by, created_at) VALUES
(1, '高活跃', '#1890ff', 'auto', 1, 1, NOW()),
(2, '沉睡客户', '#faad14', 'auto', 1, 1, NOW()),
(3, '价格敏感', '#ff4d4f', 'auto', 1, 1, NOW()),
(4, '大客户', '#722ed1', 'auto', 1, 1, NOW()),
(5, 'VIP', '#eb2f96', 'manual', 1, 1, NOW()),
(6, '意向强烈', '#52c41a', 'manual', 1, 1, NOW()),
(7, '需要跟进', '#fa8c16', 'manual', 1, 1, NOW()),
(8, '已合作', '#13c2c2', 'manual', 1, 1, NOW());

-- 10.4 客户-标签关系示例
INSERT INTO crm_customer_tag (customer_id, tag_id, tag_type, created_by) VALUES
(1, 1, 'auto', 1), (1, 4, 'auto', 1), (1, 8, 'manual', 1),
(2, 1, 'auto', 1), (2, 5, 'manual', 1), (2, 8, 'manual', 1),
(3, 1, 'auto', 1), (3, 8, 'manual', 1),
(4, 6, 'manual', 1), (4, 7, 'manual', 1),
(5, 2, 'auto', 1), (5, 7, 'manual', 1),
(6, 1, 'auto', 1), (6, 8, 'manual', 1),
(7, 7, 'manual', 1),
(8, 1, 'auto', 1), (8, 4, 'auto', 1), (8, 5, 'manual', 1);

-- 10.5 客户分群示例数据
INSERT INTO crm_segment (id, name, conditions, is_dynamic, member_count, status, created_by, created_at) VALUES
(1, '高价值客户', '{"logic":"and","rules":[{"field":"total_consumption","operator":"gte","value":100000}]}', TRUE, 2, 1, 1, NOW()),
(2, '沉睡客户唤醒', '{"logic":"and","rules":[{"field":"last_contact_at","operator":"lte","value":-90},{"field":"total_consumption","operator":"gte","value":10000}]}', TRUE, 1, 1, 1, NOW()),
(3, '待跟进客户', '{"logic":"and","rules":[{"field":"last_contact_at","operator":"lte","value":-7}]}', TRUE, 3, 1, 1, NOW()),
(4, '本月新注册', '{"logic":"and","rules":[{"field":"created_at","operator":"gte","value":"first_day_of_month"}]}', TRUE, 0, 0, 1, NOW()),
(5, '深圳区域客户', '{"logic":"and","rules":[{"field":"province","operator":"eq","value":"广东省"},{"field":"city","operator":"eq","value":"深圳市"}]}', TRUE, 3, 1, 1, NOW());

-- 10.6 分群成员示例
INSERT INTO crm_segment_member (segment_id, customer_id, join_type) VALUES
(1, 1, 'auto'), (1, 8, 'auto'),
(2, 5, 'auto'),
(3, 3, 'auto'), (3, 5, 'auto'), (3, 7, 'auto'),
(5, 1, 'auto'), (5, 3, 'auto'), (5, 6, 'auto');

-- 10.7 线索示例数据
INSERT INTO crm_lead (id, name, phone, company, position, province, city, industry, source_channel, status, owner_id, remark, created_by, created_at) VALUES
(1, '中兴通讯', '13900010001', '中兴通讯股份有限公司', 'IT总监', '广东省', '深圳市', '通信', 'ad_landing', 'pending', 1, '官网留言咨询CRM系统', 1, NOW() - INTERVAL '3 days'),
(2, '美团', '13900010002', '北京三快在线科技有限公司', '技术VP', '北京市', '北京市', '互联网', 'referral', 'following', 1, '朋友推荐，需尽快联系', 1, NOW() - INTERVAL '7 days'),
(3, '中国平安', '13900010003', '中国平安保险集团', '采购经理', '广东省', '深圳市', '金融', 'mini_program', 'pending', 1, '小程序提交咨询', 1, NOW() - INTERVAL '1 days'),
(4, '格力电器', '13900010004', '珠海格力电器股份有限公司', '信息部长', '广东省', '珠海市', '制造', 'offline', 'following', 1, '行业展会收集的名片', 1, NOW() - INTERVAL '14 days'),
(5, '科大讯飞', '13900010005', '科大讯飞股份有限公司', '运营总监', '安徽省', '合肥市', 'AI', 'manual_input', 'following', 1, '主动来电咨询', 1, NOW() - INTERVAL '10 days'),
(6, '滴滴出行', '13900010006', '滴滴出行科技有限公司', 'CTO', '北京市', '北京市', '互联网', 'ad_landing', 'pending', 1, NULL, 1, NOW() - INTERVAL '2 days');

-- 10.8 商机示例数据
INSERT INTO crm_opportunity (id, customer_id, name, expected_amount, expected_close_date, stage_id, owner_id, pain_points, requirements, created_by, created_at) VALUES
(1, 1, '华为-CRM企业版采购', 150000.00, NOW() + INTERVAL '60 days', 3, 1, '现有系统无法满足销售管理需求，数据孤岛严重', '需要与企业微信集成，支持定制化报表', 1, NOW() - INTERVAL '30 days'),
(2, 2, '阿里-CRM标准版续费升级', 50000.00, NOW() + INTERVAL '30 days', 4, 1, '标准版功能不够用，需增加高级分析能力', '升级至企业版，增加API接口', 1, NOW() - INTERVAL '20 days'),
(3, 6, '比亚迪-实施服务项目', 90000.00, NOW() + INTERVAL '45 days', 2, 1, '新系统上线需要专业实施团队支持', '全流程实施+培训服务', 1, NOW() - INTERVAL '15 days'),
(4, 4, '字节跳动-产品咨询', 30000.00, NOW() + INTERVAL '90 days', 1, 1, '团队快速扩张，急需规范化客户管理', '先进行POC测试验证', 1, NOW() - INTERVAL '5 days');

-- 10.9 跟进记录示例数据
INSERT INTO crm_follow_up (id, customer_id, type, content, next_plan, next_plan_date, is_important, creator_id, created_by, created_at) VALUES
(1, 1, 'visit', '拜访华为CTO王建国，演示CRM企业版功能，客户对数据安全模块很感兴趣', '准备安全架构方案，下周二次演示', NOW() + INTERVAL '7 days', TRUE, 1, 1, NOW() - INTERVAL '10 days'),
(2, 1, 'call', '电话沟通了解具体需求，确认需要对接企业微信', '发送需求确认函', NOW() + INTERVAL '2 days', FALSE, 1, 1, NOW() - INTERVAL '3 days'),
(3, 2, 'visit', '拜访阿里采购总监李明芳，沟通续费升级方案', '准备对比方案', NOW() + INTERVAL '5 days', TRUE, 1, 1, NOW() - INTERVAL '8 days'),
(4, 3, 'online', '腾讯张伟在线咨询产品技术细节', '发送技术白皮书', NOW() + INTERVAL '3 days', FALSE, 1, 1, NOW() - INTERVAL '15 days'),
(5, 6, 'visit', '拜访比亚迪IT总监，现场调研现有系统使用情况', '输出调研报告', NOW() + INTERVAL '14 days', TRUE, 1, 1, NOW() - INTERVAL '30 days');

-- 10.10 系统消息示例数据
INSERT INTO sys_message (receiver_id, channel, title, content, biz_type, is_read, created_by, created_at) VALUES
(1, 'system', '欢迎使用CRM系统', '感谢您使用CRM系统，请及时完善个人资料。', 'system', TRUE, 1, NOW() - INTERVAL '180 days'),
(1, 'system', '审批待办提醒', '您有一条报销审批待处理，请前往审批中心处理。', 'approval', FALSE, 1, NOW() - INTERVAL '2 days'),
(1, 'system', '合同到期提醒', '合同「CRM企业版维护合同」将于30天后到期，请及时处理续签。', 'system', FALSE, 1, NOW() - INTERVAL '1 days'),
(1, 'system', '商机更新提醒', '商机「华为-CRM企业版采购」已进入方案报价阶段。', 'system', TRUE, 1, NOW() - INTERVAL '7 days'),
(1, 'system', '系统升级通知', '系统将于本周六凌晨2:00-4:00进行版本升级，届时将暂停服务。', 'system', FALSE, 1, NOW()),
(1, 'system', '客户生日提醒', '客户「小米科技」张国强的生日即将到来，建议发送祝福邮件。', 'system', FALSE, 1, NOW() + INTERVAL '28 days');

-- 10.11 系统公告示例数据
INSERT INTO sys_notice (id, title, content, notice_type, status, publish_at, created_by, created_at) VALUES
(1, 'CRM系统正式上线通知', '<p>各位同事，CRM系统已完成开发和测试，正式上线运行。如有问题请联系系统管理员。</p><p><b>主要功能：</b></p><ul><li>客户管理：线索、客户、联系人统一管理</li><li>销售管理：商机看板、跟进记录、报价合同</li><li>商城管理：商品、订单、优惠券</li><li>协同办公：审批管理、服务工单</li></ul>', 1, 1, NOW() - INTERVAL '180 days', 1, NOW() - INTERVAL '180 days'),
(2, '2024年Q2销售目标公告', '<p>Q2销售目标已下达，请各位销售经理查收。本季度重点开拓华南和华东市场。</p><p>整体目标：签约金额800万，回款600万。</p>', 1, 1, NOW() - INTERVAL '90 days', 1, NOW() - INTERVAL '90 days'),
(3, '关于启用审批流程的通知', '<p>自下周一起，所有报价单和合同审批需通过线上审批流程提交，纸质审批单将不再受理。</p>', 2, 1, NOW() - INTERVAL '30 days', 1, NOW() - INTERVAL '30 days');

-- 10.12 审批流程定义示例
INSERT INTO coll_approval_define (id, name, biz_type, status, remark, created_by, created_at) VALUES
(1, '报价审批', 'quotation', 1, '超过标准折扣的报价需审批', 1, NOW() - INTERVAL '60 days'),
(2, '合同审批', 'contract', 1, '合同签署前需法务及总经理审批', 1, NOW() - INTERVAL '60 days'),
(3, '费用报销', 'expense', 1, '差旅及日常费用报销', 1, NOW() - INTERVAL '60 days'),
(4, '退款审批', 'refund', 0, '商城订单退款审核', 1, NOW() - INTERVAL '30 days');

INSERT INTO coll_approval_define_step (define_id, step_id, step_name, approver_type, approver_ids, sort_order) VALUES
(1, 1, '销售经理审批', 'role', '[2]', 1),
(1, 2, '财务审批', 'role', '[4]', 2),
(2, 1, '法务审批', 'user', '[1]', 1),
(2, 2, '总经理审批', 'user', '[1]', 2),
(3, 1, '部门经理审批', 'user', '[1]', 1),
(3, 2, '财务审批', 'role', '[4]', 2),
(4, 1, '客服审核', 'user', '[1]', 1),
(4, 2, '财务确认', 'role', '[4]', 2);

-- 10.13 审批实例示例数据
INSERT INTO coll_approval_instance (id, define_id, biz_type, biz_id, form_data, status, applicant_id, applicant_name, created_by, created_at) VALUES
(1, 3, 'expense', 1, '{"title":"Q2差旅报销","amount":12800.00,"remark":"Q2拜访客户差旅费用","reason":"Q2出差拜访深圳客户产生的交通及住宿费"}', 'approved', 1, '系统管理员', 1, NOW() - INTERVAL '20 days'),
(2, 3, 'expense', 2, '{"title":"采购办公设备","amount":5600.00,"remark":"申请采购办公桌椅","reason":"部门新增员工需要补充办公设备"}', 'pending', 1, '系统管理员', 1, NOW() - INTERVAL '3 days'),
(3, 3, 'expense', 3, '{"title":"市场活动费用","amount":25000.00,"remark":"Q2行业展会参展费用","reason":"参加深圳电子展的展位费及物料费"}', 'pending', 1, '系统管理员', 1, NOW() - INTERVAL '1 days'),
(4, 1, 'quotation', 1, '{"title":"华为企业版报价","amount":150000.00,"remark":"含首年实施服务费"}', 'approved', 1, '系统管理员', 1, NOW() - INTERVAL '15 days');

-- 10.14 服务工单示例数据
INSERT INTO coll_service_ticket (id, ticket_no, customer_id, type, title, description, priority, source, assignee_id, status, created_by, created_at) VALUES
(1, 'TK-20250001', 1, 'install', 'CRM系统安装部署', '华为采购的CRM企业版需要上门安装部署，请安排工程师对接。', 'urgent', 'manual', 1, 'in_progress', 1, NOW() - INTERVAL '5 days'),
(2, 'TK-20250002', 2, 'complaint', '系统响应速度慢', '近期系统操作响应缓慢，页面加载时间较长，影响工作效率。', 'high', 'phone', 1, 'assigned', 1, NOW() - INTERVAL '2 days'),
(3, 'TK-20250003', 3, 'repair', '数据导出功能异常', '导出Excel时部分数据丢失，请求尽快修复。', 'urgent', 'wechat', NULL, 'pending', 1, NOW() - INTERVAL '1 days'),
(4, 'TK-20250004', 4, 'other', '新增用户权限配置', '需要为三个新员工配置CRM系统权限，附上权限清单。', 'low', 'manual', 1, 'completed', 1, NOW() - INTERVAL '10 days'),
(5, 'TK-20250005', 6, 'install', '服务器部署支持', '新采购的服务器需要部署CRM系统及数据库环境。', 'medium', 'manual', NULL, 'pending', 1, NOW());

-- 10.15 商城商品SKU示例数据
INSERT INTO mall_sku (product_id, specs, price, stock, sku_code, status, created_by, created_at) VALUES
(1, '[{"key":"版本","value":"标准版"},{"key":"年限","value":"1年"}]', 50000.00, 100, 'CRM-STD-1Y', 1, 1, NOW()),
(2, '[{"key":"版本","value":"企业版"},{"key":"年限","value":"1年"}]', 150000.00, 50, 'CRM-ENT-1Y', 1, 1, NOW()),
(4, '[{"key":"服务类型","value":"实施服务"},{"key":"天数","value":"1天"}]', 3000.00, 999, 'SVC-IMP-1D', 1, 1, NOW()),
(5, '[{"key":"服务类型","value":"培训服务"},{"key":"天数","value":"1天"}]', 2000.00, 999, 'SVC-TRN-1D', 1, 1, NOW());

-- 10.16 跟进日程示例
INSERT INTO crm_appointment (customer_id, title, description, appointment_date, start_time, end_time, location, type, status, owner_id, created_by, created_at) VALUES
(1, '华为-方案演示', '向华为CTO演示企业版安全方案', CURRENT_DATE + 3, '10:00:00', '11:30:00', '深圳市南山区华为基地', 'visit', 'pending', 1, 1, NOW()),
(2, '阿里-合同谈判', '续费合同条款沟通', CURRENT_DATE + 5, '14:00:00', '16:00:00', '杭州市余杭区阿里巴巴西溪园区', 'visit', 'pending', 1, 1, NOW()),
(6, '比亚迪-调研汇报', '提交实施调研报告', CURRENT_DATE + 7, '09:30:00', '11:00:00', '深圳市坪山区比亚迪总部', 'visit', 'pending', 1, 1, NOW()),
(4, '阿里-线上沟通', '确认升级需求细节', CURRENT_DATE + 1, '15:00:00', '15:30:00', NULL, 'call', 'pending', 1, 1, NOW());

-- 10.17 看板布局示例
INSERT INTO report_dashboard_layout (user_id, layout) VALUES
(1, '[{"cardType":"stats","position":1,"width":12,"height":1},{"cardType":"sales_funnel","position":2,"width":6,"height":2},{"cardType":"customer_analysis","position":3,"width":6,"height":2},{"cardType":"lead_trend","position":4,"width":12,"height":2},{"cardType":"recent_follow_up","position":5,"width":6,"height":2},{"cardType":"top_performers","position":6,"width":6,"height":2}]');

-- ============================================================================
-- 完成
-- ============================================================================

-- ============================================================================
-- 第十一部分：AI 智能体域（ai_*）
-- ============================================================================

-- 启用 pgvector 扩展（需超级用户权限）
CREATE EXTENSION IF NOT EXISTS vector;

-- 11.1 知识库表
CREATE TABLE IF NOT EXISTS ai_knowledge_base (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(200) NOT NULL,
    description     TEXT,
    type            VARCHAR(50) NOT NULL DEFAULT 'faq',
    status          VARCHAR(20) NOT NULL DEFAULT 'enabled',
    created_by      BIGINT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by      BIGINT,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ
);
COMMENT ON COLUMN ai_knowledge_base.type IS 'faq-常见问题 product-商品信息 policy-售后政策 manual-操作手册';
COMMENT ON TABLE ai_knowledge_base IS 'AI 知识库表';

-- 11.2 知识库文档块表（含向量）
CREATE TABLE IF NOT EXISTS ai_knowledge_chunk (
    id              BIGSERIAL PRIMARY KEY,
    kb_id           BIGINT NOT NULL REFERENCES ai_knowledge_base(id) ON DELETE CASCADE,
    title           VARCHAR(500),
    content         TEXT NOT NULL,
    chunk_index     INT,
    embedding       VECTOR(1024),
    metadata        JSONB,
    token_count     INT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
COMMENT ON COLUMN ai_knowledge_chunk.content IS '分块内容';
COMMENT ON COLUMN ai_knowledge_chunk.embedding IS '向量嵌入（1024维）';
COMMENT ON COLUMN ai_knowledge_chunk.metadata IS '元数据 {"source":"","tags":[]}';
COMMENT ON TABLE ai_knowledge_chunk IS '知识库文档块表';
CREATE INDEX IF NOT EXISTS idx_chunk_kb_id ON ai_knowledge_chunk(kb_id);
CREATE INDEX IF NOT EXISTS idx_chunk_embedding ON ai_knowledge_chunk USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100);

-- 11.3 AI 对话表
CREATE TABLE IF NOT EXISTS ai_conversation (
    id              BIGSERIAL PRIMARY KEY,
    agent_type      VARCHAR(30) NOT NULL,
    title           VARCHAR(500),
    user_id         BIGINT,
    customer_id     BIGINT,
    status          VARCHAR(20) NOT NULL DEFAULT 'active',
    message_count   INT NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
COMMENT ON COLUMN ai_conversation.agent_type IS 'customer_service/sales_assistant/butler';
COMMENT ON COLUMN ai_conversation.user_id IS '内部系统用户ID';
COMMENT ON COLUMN ai_conversation.customer_id IS '小程序客户ID';
COMMENT ON COLUMN ai_conversation.status IS 'active-进行中 closed-已关闭';
COMMENT ON TABLE ai_conversation IS 'AI 对话表';
CREATE INDEX IF NOT EXISTS idx_conv_agent ON ai_conversation(agent_type);
CREATE INDEX IF NOT EXISTS idx_conv_user ON ai_conversation(user_id);
CREATE INDEX IF NOT EXISTS idx_conv_customer ON ai_conversation(customer_id);
CREATE INDEX IF NOT EXISTS idx_conv_updated ON ai_conversation(updated_at DESC);

-- 11.4 AI 消息表
CREATE TABLE IF NOT EXISTS ai_message (
    id              BIGSERIAL PRIMARY KEY,
    conversation_id BIGINT NOT NULL REFERENCES ai_conversation(id) ON DELETE CASCADE,
    role            VARCHAR(20) NOT NULL,
    content         TEXT,
    tool_calls      JSONB,
    tokens_in       INT DEFAULT 0,
    tokens_out      INT DEFAULT 0,
    latency_ms      INT DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
COMMENT ON COLUMN ai_message.role IS 'user/assistant/system/tool';
COMMENT ON COLUMN ai_message.tool_calls IS 'Function Calling 调用记录';
COMMENT ON TABLE ai_message IS 'AI 消息表';
CREATE INDEX IF NOT EXISTS idx_msg_conv ON ai_message(conversation_id);
CREATE INDEX IF NOT EXISTS idx_msg_created ON ai_message(created_at);

-- 11.5 AI Agent 配置表
CREATE TABLE IF NOT EXISTS ai_agent_config (
    id              BIGSERIAL PRIMARY KEY,
    agent_type      VARCHAR(30) NOT NULL UNIQUE,
    llm_provider    VARCHAR(50) NOT NULL DEFAULT 'siliconflow',
    model_name      VARCHAR(100) NOT NULL DEFAULT 'deepseek-ai/DeepSeek-V4-Flash',
    system_prompt   TEXT NOT NULL,
    temperature     DECIMAL(3,2) DEFAULT 0.7,
    max_tokens      INT DEFAULT 4096,
    top_p           DECIMAL(3,2) DEFAULT 0.9,
    tools_enabled   JSONB,
    status          VARCHAR(20) NOT NULL DEFAULT 'enabled',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
COMMENT ON COLUMN ai_agent_config.tools_enabled IS '启用的工具列表 ["get_my_customers","get_sales_summary"]';
COMMENT ON TABLE ai_agent_config IS 'AI Agent 配置表';

-- 11.6 默认 Agent 配置
INSERT INTO ai_agent_config (agent_type, system_prompt, tools_enabled) VALUES
('customer_service', '你是 CRM 智能客服助手"小C"，基于知识库回答客户问题。\n\n规则：\n1. 只回答知识库范围内的问题，不知道就说"这个问题我需要转接人工客服"\n2. 回答要简洁、友好、专业\n3. 涉及订单/售后问题时，引导用户提供订单号\n4. 不能透露任何公司内部信息\n5. 不能编造商品信息或政策条款', '[]'),
('sales_assistant', '你是 CRM 销售助手，帮助销售人员分析客户和业绩。\n\n你可以：\n1. 查询你负责的客户列表和详情\n2. 分析你的销售业绩和趋势\n3. 分析产品销量情况\n4. 给出客户跟进建议和拓客策略\n\n规则：\n1. 数据回答要有具体数字支撑\n2. 分析结果要给出可操作的建议\n3. 可以对比历史数据说明趋势\n4. 涉及客户隐私时注意数据脱敏', '["get_my_customers","get_my_sales_summary","get_product_ranking","get_customer_analysis","get_pipeline_analysis"]'),
('butler', '你是企业管理智能助手，帮助管理者掌握公司运营全貌。\n\n你可以：\n1. 分析公司整体销售业绩和趋势\n2. 分析各部门/团队绩效\n3. 分析产品线和客户结构\n4. 生成经营分析报告\n\n规则：\n1. 数据必须准确，注明数据截止时间\n2. 分析要有同比/环比对比\n3. 发现问题时要指出具体原因和改进建议', '["get_company_overview","get_sales_trend","get_department_performance","get_customer_structure","get_product_analysis"]');

-- 11.7 AI 相关菜单（父菜单ID=31，按钮从 1197 开始）
INSERT INTO sys_menu (id, parent_id, name, menu_type, route_path, component, sort_order, status) VALUES
(32, 0, '智能AI', 'M', '/ai', 'Layout', 60, 1),
(33, 32, '销售助手', 'C', '/ai/sales-assistant', 'ai/sales-assistant/index', 1, 1),
(34, 32, '智能管家', 'C', '/ai/butler', 'ai/butler/index', 2, 1),
(35, 32, '知识库管理', 'C', '/ai/knowledge-base', 'ai/knowledge-base/index', 3, 1);

INSERT INTO sys_menu (id, parent_id, name, menu_type, permission_code, sort_order, status) VALUES
(1197, 33, '销售助手聊天', 'F', 'ai:assistant:chat', 1, 1),
(1198, 34, '智能管家聊天', 'F', 'ai:butler:chat', 2, 1),
(1199, 35, '知识库列表', 'F', 'ai:knowledge:list', 1, 1),
(1200, 35, '新增知识库', 'F', 'ai:knowledge:create', 2, 1),
(1201, 35, '编辑知识库', 'F', 'ai:knowledge:edit', 3, 1),
(1202, 35, '删除知识库', 'F', 'ai:knowledge:delete', 4, 1);

-- 绑定 AI 权限到管理员角色
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 32), (1, 33), (1, 34), (1, 35),
(1, 1197), (1, 1198), (1, 1199), (1, 1200), (1, 1201), (1, 1202);

-- ============================================================================
-- 完成
-- ============================================================================
