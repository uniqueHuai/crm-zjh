package com.crm.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.crm.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mall_coupon_define")
public class CrmCoupon extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String name;

    private String type;

    private BigDecimal value;

    private BigDecimal conditionAmount;

    private Integer totalCount;

    @TableField(exist = false)
    private Integer receivedCount;

    @TableField("used_count")
    private Integer usedCount;

    @TableField("per_user_limit")
    private Integer perLimit;

    @TableField(exist = false)
    private Integer validDays;

    @TableField("valid_from")
    private LocalDate validStart;

    @TableField("valid_until")
    private LocalDate validEnd;

    private Integer status;

    @TableField(exist = false)
    private String remark;
}
