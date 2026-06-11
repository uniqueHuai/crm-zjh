package com.crm.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.crm.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mall_order")
public class CrmOrder extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String orderNo;

    @TableField(exist = false)
    private Long userId;

    private Long customerId;

    private BigDecimal totalAmount;

    private BigDecimal discountAmount;

    @TableField("final_amount")
    private BigDecimal payAmount;

    private String status;

    private String paymentMethod;

    private LocalDateTime paidAt;

    @TableField("receiver_name")
    private String consignee;

    @TableField("receiver_phone")
    private String phone;

    @TableField(exist = false)
    private String province;

    @TableField(exist = false)
    private String city;

    @TableField(exist = false)
    private String district;

    @TableField("receiver_address")
    private String address;

    @TableField("customer_remark")
    private String remark;

    @TableField(exist = false)
    private List<CrmOrderItem> items;

    @TableField(exist = false)
    private String userName;
}
