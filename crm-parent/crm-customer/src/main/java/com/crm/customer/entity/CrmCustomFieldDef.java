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
@TableName("crm_custom_field_def")
public class CrmCustomFieldDef extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String entityType;

    private String fieldKey;

    private String fieldName;

    private String fieldType;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object options;

    private Boolean isRequired;

    private Integer sortOrder;

    private Integer status;
}
