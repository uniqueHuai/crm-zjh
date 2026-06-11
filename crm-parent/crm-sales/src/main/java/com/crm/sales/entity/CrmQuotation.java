package com.crm.sales.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.crm.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("crm_quotation")
public class CrmQuotation extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String quotationNo;

    private Long customerId;

    private Long opportunityId;

    private Long contactId;

    private LocalDate validUntil;

    private String paymentTerms;

    private String deliveryTerms;

    private String remark;

    private BigDecimal totalAmount;

    private BigDecimal discountAmount;

    private BigDecimal finalAmount;

    private String status;

    private Long creatorId;

    @TableField(exist = false)
    private List<CrmQuotationItem> items;

    @TableField(exist = false)
    private String customerName;

    @TableField(exist = false)
    private String opportunityName;

    @TableField(exist = false)
    private String creatorName;
}
