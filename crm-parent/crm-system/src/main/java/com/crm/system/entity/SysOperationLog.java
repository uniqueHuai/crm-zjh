package com.crm.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@TableName("sys_operation_log")
public class SysOperationLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String module;

    private String action;

    private Long operatorId;

    private String operatorName;

    private String targetType;

    private Long targetId;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> detail;

    private String requestUrl;

    private String requestMethod;

    private String requestParams;

    private String ip;

    private String userAgent;

    private Integer durationMs;

    private Integer resultCode;

    private String errorMsg;

    private LocalDateTime createdAt;
}
