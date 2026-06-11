package com.crm.customer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.crm.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("crm_lead")
public class CrmLead extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String name;

    private String phone;

    private String wechatUnionid;

    private String wechatOpenid;

    private String company;

    private String position;

    private String province;

    private String city;

    private String industry;

    private String sourceChannel;

    private String status;

    private Long ownerId;

    private String assignType;

    private LocalDateTime assignedAt;

    private LocalDateTime poolReturnAt;

    private Long convertCustomerId;

    private Long convertOpportunityId;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object extJson;

    private String remark;

    @TableField(exist = false)
    private String ownerName;

    @TableField(exist = false)
    private String sourceChannelName;

    @TableField(exist = false)
    private String statusName;
}
