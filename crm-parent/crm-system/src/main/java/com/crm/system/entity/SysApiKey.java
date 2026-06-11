package com.crm.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.crm.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_api_key")
public class SysApiKey extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String appName;

    private String apiKey;

    private String apiSecret;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> permissions;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> ipWhitelist;

    private LocalDateTime expireAt;

    private LocalDateTime lastUsedAt;

    private Integer status;
}
