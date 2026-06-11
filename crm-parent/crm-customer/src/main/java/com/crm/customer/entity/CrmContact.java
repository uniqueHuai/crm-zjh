package com.crm.customer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.crm.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("crm_contact")
public class CrmContact extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long customerId;

    @TableField(exist = false)
    private String customerName;

    private String name;

    private String phone;

    private String wechatUnionid;

    private String email;

    private String position;

    private String department;

    private Boolean isDecisionMaker;

    private Boolean isPrimary;

    private LocalDate birthday;

    private Integer gender;

    private String remark;

    private String extJson;

    private Integer sortOrder;
}
