package com.crm.customer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.crm.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("crm_customer")
public class CrmCustomer extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String name;

    private String phone;

    private String wechatUnionid;

    private String wechatOpenid;

    private String company;

    private String position;

    private LocalDate birthday;

    private Integer gender;

    private String email;

    private String province;

    private String city;

    private String district;

    private String address;

    private String sourceChannel;

    private Long levelId;

    private Long ownerId;

    private LocalDateTime lastContactAt;

    private BigDecimal totalConsumption;

    private Integer orderCount;

    private String remark;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object extJson;

    private Integer status;

    @TableField(exist = false)
    private String levelName;

    @TableField(exist = false)
    private String ownerName;

    @TableField(exist = false)
    private String sourceChannelName;

    @TableField(exist = false)
    private List<Long> tagIds;

    @TableField(exist = false)
    private List<CrmTag> tags;
}
