package com.crm.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("mall_coupon")
public class CrmCouponUser implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("define_id")
    private Long couponId;

    @TableField("customer_id")
    private Long userId;

    private Long orderId;

    private String status;

    private LocalDateTime receivedAt;

    private LocalDateTime usedAt;

    @TableField(exist = false)
    private String couponName;

    @TableField(exist = false)
    private String couponType;

    @TableField(exist = false)
    private java.math.BigDecimal couponValue;
}
