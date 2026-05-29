package com.crm.common.model;

import com.crm.common.enums.ResultCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class R<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private int code;
    private String message;
    private T data;
    private String traceId;
    private Long timestamp;

    private R() {
        this.timestamp = System.currentTimeMillis();
    }

    private R(int code, String message, T data) {
        this();
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> R<T> ok() {
        return new R<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null);
    }

    public static <T> R<T> ok(T data) {
        return new R<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    public static <T> R<T> ok(String message, T data) {
        return new R<>(ResultCode.SUCCESS.getCode(), message, data);
    }

    public static <T> R<T> failed(ResultCode resultCode) {
        return new R<>(resultCode.getCode(), resultCode.getMessage(), null);
    }

    public static <T> R<T> failed(ResultCode resultCode, String message) {
        return new R<>(resultCode.getCode(), message, null);
    }

    public static <T> R<T> failed(int code, String message) {
        return new R<>(code, message, null);
    }

    public static <T> R<T> failed(String message) {
        return new R<>(ResultCode.BIZ_ERROR.getCode(), message, null);
    }

    public R<T> traceId(String traceId) {
        this.traceId = traceId;
        return this;
    }
}
