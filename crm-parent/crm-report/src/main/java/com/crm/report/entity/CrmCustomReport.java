package com.crm.report.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.crm.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("report_custom_report")
public class CrmCustomReport extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String name;

    private String dataSource;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> dimensions;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> metrics;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Object> filters;

    private String chartType;

    private String schedule;

    @TableField(exist = false)
    private List<Long> recipientIds;
}
