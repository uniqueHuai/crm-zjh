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
-- 第四部分：商城交易域（mall_* / sale_* / mp_*）
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

-- 4.13 商城-商品分类表（小程序商城专用，区别于 crm_product_category）
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

-- 4.14 商城-商品表（小程序商城专用，区别于 crm_sales_product）
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

-- 4.15 小程序会话表
CREATE TABLE IF NOT EXISTS mp_session (
    id              BIGSERIAL PRIMARY KEY,
    customer_id     BIGINT NOT NULL,
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

-- 4.16 小程序收货地址表
CREATE TABLE IF NOT EXISTS mp_address (
    id              BIGSERIAL PRIMARY KEY,
    customer_id     BIGINT NOT NULL,
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

-- 4.17 分销商表
CREATE TABLE IF NOT EXISTS crm_distributor (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    parent_id       BIGINT,
    level           VARCHAR(20) DEFAULT 'basic',
    total_commission DECIMAL(18,2) DEFAULT 0,
    withdrawable    DECIMAL(18,2) DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
COMMENT ON TABLE crm_distributor IS '分销商表';
COMMENT ON COLUMN crm_distributor.level IS 'basic/silver/gold';
CREATE INDEX IF NOT EXISTS idx_distributor_user ON crm_distributor (user_id);
CREATE INDEX IF NOT EXISTS idx_distributor_parent ON crm_distributor (parent_id);

-- 4.18 佣金记录表
CREATE TABLE IF NOT EXISTS crm_commission (
    id              BIGSERIAL PRIMARY KEY,
    distributor_id  BIGINT NOT NULL,
    order_id        BIGINT NOT NULL,
    amount          DECIMAL(18,2) NOT NULL,
    rate            DECIMAL(5,4),
    status          VARCHAR(20) NOT NULL DEFAULT 'pending',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
COMMENT ON TABLE crm_commission IS '佣金记录表';
COMMENT ON COLUMN crm_commission.status IS 'pending-待结算 settled-已结算 cancelled-已取消';
CREATE INDEX IF NOT EXISTS idx_commission_distributor ON crm_commission (distributor_id);
CREATE INDEX IF NOT EXISTS idx_commission_order ON crm_commission (order_id);

-- 4.19 拼团记录表
CREATE TABLE IF NOT EXISTS mp_group_buy (
    id              BIGSERIAL PRIMARY KEY,
    activity_id     BIGINT NOT NULL,
    product_id      BIGINT NOT NULL,
    sku_id          BIGINT,
    leader_id       BIGINT NOT NULL,
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

-- 4.20 拼团成员表
CREATE TABLE IF NOT EXISTS mp_group_buy_member (
    id              BIGSERIAL PRIMARY KEY,
    group_id        BIGINT NOT NULL,
    customer_id     BIGINT NOT NULL,
    order_id        BIGINT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
COMMENT ON TABLE mp_group_buy_member IS '拼团成员表';
CREATE INDEX IF NOT EXISTS idx_mp_group_member_group ON mp_group_buy_member (group_id);

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

-- 小程序模块外键约束
ALTER TABLE mp_session ADD CONSTRAINT fk_mp_session_customer FOREIGN KEY (customer_id) REFERENCES crm_customer(id) ON DELETE CASCADE;
ALTER TABLE mp_address ADD CONSTRAINT fk_mp_address_customer FOREIGN KEY (customer_id) REFERENCES crm_customer(id) ON DELETE CASCADE;
ALTER TABLE crm_distributor ADD CONSTRAINT fk_distributor_user FOREIGN KEY (user_id) REFERENCES crm_customer(id) ON DELETE CASCADE;
ALTER TABLE crm_distributor ADD CONSTRAINT fk_distributor_parent FOREIGN KEY (parent_id) REFERENCES crm_distributor(id);
ALTER TABLE crm_commission ADD CONSTRAINT fk_commission_distributor FOREIGN KEY (distributor_id) REFERENCES crm_distributor(id) ON DELETE CASCADE;
ALTER TABLE crm_commission ADD CONSTRAINT fk_commission_order FOREIGN KEY (order_id) REFERENCES mall_order(id) ON DELETE CASCADE;
ALTER TABLE mp_group_buy ADD CONSTRAINT fk_group_buy_activity FOREIGN KEY (activity_id) REFERENCES mall_activity(id);
ALTER TABLE mp_group_buy ADD CONSTRAINT fk_group_buy_product FOREIGN KEY (product_id) REFERENCES sale_product(id);
ALTER TABLE mp_group_buy ADD CONSTRAINT fk_group_buy_sku FOREIGN KEY (sku_id) REFERENCES mall_sku(id);
ALTER TABLE mp_group_buy ADD CONSTRAINT fk_group_buy_leader FOREIGN KEY (leader_id) REFERENCES crm_customer(id);
ALTER TABLE mp_group_buy_member ADD CONSTRAINT fk_group_member_group FOREIGN KEY (group_id) REFERENCES mp_group_buy(id) ON DELETE CASCADE;
ALTER TABLE mp_group_buy_member ADD CONSTRAINT fk_group_member_customer FOREIGN KEY (customer_id) REFERENCES crm_customer(id);
ALTER TABLE mp_group_buy_member ADD CONSTRAINT fk_group_member_order FOREIGN KEY (order_id) REFERENCES mall_order(id);

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

ALTER TABLE crm_customer_level ADD COLUMN IF NOT EXISTS updated_by BIGINT;

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


-- ====================================================================
-- 完成
-- ====================================================================