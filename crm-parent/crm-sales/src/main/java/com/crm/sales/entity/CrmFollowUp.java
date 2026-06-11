package com.crm.sales.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.crm.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("crm_follow_up")
public class CrmFollowUp extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long customerId;

    private Long contactId;

    private Long opportunityId;

    private String type;

    private String content;

    private String voiceUrl;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object imageUrls;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object location;

    private String nextPlan;

    private LocalDate nextPlanDate;

    private Boolean isImportant;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object tags;

    private Long creatorId;

    @TableField(exist = false)
    private String creatorName;
}
