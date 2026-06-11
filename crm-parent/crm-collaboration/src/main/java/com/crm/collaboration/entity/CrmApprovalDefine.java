package com.crm.collaboration.entity;

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
@TableName("coll_approval_define")
public class CrmApprovalDefine extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String name;

    private String bizType;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object triggerCondition;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Object> steps;

    private Integer status;
}
