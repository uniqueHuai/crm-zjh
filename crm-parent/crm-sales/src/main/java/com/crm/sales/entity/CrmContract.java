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
import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("crm_contract")
public class CrmContract extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String contractNo;

    private Long customerId;

    private Long opportunityId;

    private Long quotationId;

    private Long templateId;

    private String title;

    private BigDecimal totalAmount;

    private String paymentTerms;

    private LocalDate validFrom;

    private LocalDate validUntil;

    private String signerName;

    private String signerPhone;

    private String remark;

    private String status;

    private String signType;

    private String platform;

    private String signUrl;

    private LocalDateTime signedAt;

    @TableField(exist = false)
    private List<CrmContractItem> items;

    @TableField(exist = false)
    private String customerName;
}
