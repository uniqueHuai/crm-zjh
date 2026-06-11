package com.crm.customer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.crm.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("crm_lead_distribution_rule")
public class CrmLeadDistributionRule extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String name;

    private Integer priority;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object conditions;

    private String strategy;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object strategyConfig;

    private String targetType;

    private Long targetId;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object timeRanges;

    private Integer maxDailyPerPerson;

    private Integer status;
}
