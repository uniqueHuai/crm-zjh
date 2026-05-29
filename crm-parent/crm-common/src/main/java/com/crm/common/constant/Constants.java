package com.crm.common.constant;

public interface Constants {

    /* ---------- 系统常量 ---------- */
    String BASE_PACKAGE = "com.crm";

    /* ---------- Token ---------- */
    String TOKEN_HEADER = "Authorization";
    String TOKEN_PREFIX = "Bearer ";
    String LOGIN_USER_KEY = "login_user";
    String LOGIN_USER_ID_KEY = "login_user_id";

    /* ---------- 缓存前缀 ---------- */
    String CACHE_PREFIX = "crm:";
    String CACHE_USER = CACHE_PREFIX + "user:";
    String CACHE_MENU = CACHE_PREFIX + "menu:";
    String CACHE_ROLE = CACHE_PREFIX + "role:";
    String CACHE_DICT = CACHE_PREFIX + "dict:";
    String CACHE_CONFIG = CACHE_PREFIX + "config:";
    String CACHE_CAPTCHA = CACHE_PREFIX + "captcha:";
    String CACHE_TOKEN = CACHE_PREFIX + "token:";

    /* ---------- 通用状态 ---------- */
    int STATUS_NORMAL = 1;
    int STATUS_DISABLED = 0;
    int STATUS_DRAFT = 0;
    int STATUS_PUBLISHED = 1;

    /* ---------- 删除标志 ---------- */
    String DELETED = "deleted";
    String NOT_DELETED = "not_deleted";

    /* ---------- 时间格式 ---------- */
    String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    String DATE_FORMAT = "yyyy-MM-dd";

    /* ---------- 数据范围 ---------- */
    int DATA_SCOPE_SELF = 1;
    int DATA_SCOPE_DEPT = 2;
    int DATA_SCOPE_DEPT_AND_CHILD = 3;
    int DATA_SCOPE_ALL = 4;
    int DATA_SCOPE_CUSTOM = 5;
}
