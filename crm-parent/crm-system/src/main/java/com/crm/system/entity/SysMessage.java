package com.crm.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("sys_message")
public class SysMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long receiverId;

    @TableField(exist = false)
    private List<Long> receiverIds;

    private String channel;

    private String title;

    private String content;

    private String bizType;

    private Long bizId;

    private Boolean isRead;

    private LocalDateTime readAt;

    private String priority;

    @TableField(exist = false)
    private String creatorName;

    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
