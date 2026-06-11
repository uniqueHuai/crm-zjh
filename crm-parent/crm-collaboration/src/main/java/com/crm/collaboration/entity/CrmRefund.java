package com.crm.collaboration.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.crm.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("coll_refund_request")
public class CrmRefund extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long orderId;

    private Long customerId;

    private BigDecimal amount;

    private String reason;

    private String status;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> imageUrls;

    private String rejectReason;

    private String expressCompany;

    private String expressNo;

    @TableField(exist = false)
    private String customerName;
}
