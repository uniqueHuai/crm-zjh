package com.crm.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "success"),

    BAD_REQUEST(400, "bad_request"),
    INVALID_PARAM(400001, "invalid_param"),
    DUPLICATE_DATA(400002, "duplicate_data"),
    ILLEGAL_STATE(400003, "illegal_state"),

    UNAUTHORIZED(401, "unauthorized"),
    TOKEN_EXPIRED(401001, "token_expired"),
    INVALID_TOKEN(401002, "invalid_token"),

    FORBIDDEN(403, "forbidden"),
    DATA_SCOPE_FORBIDDEN(403001, "data_scope_forbidden"),

    NOT_FOUND(404, "not_found"),

    CONFLICT(409, "conflict"),
    STATUS_CONFLICT(409001, "status_conflict"),
    REF_CONFLICT(409002, "ref_conflict"),

    TOO_MANY_REQUESTS(429, "too_many_requests"),

    INTERNAL_ERROR(500, "internal_error"),
    BIZ_ERROR(500001, "biz_error"),
    FILE_TOO_LARGE(500002, "file_too_large"),
    ;

    private final int code;
    private final String message;
}
