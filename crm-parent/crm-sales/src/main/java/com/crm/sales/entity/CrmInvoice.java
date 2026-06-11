package com.crm.sales.entity;

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
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("crm_invoice")
public class CrmInvoice extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long contractId;

    private Long customerId;

    private String title;

    private String taxId;

    private String invoiceType;

    private BigDecimal amount;

    private String content;

    private String receiveEmail;

    private String remark;

    private String status;

    private String invoiceNo;

    private String invoiceFileUrl;

    private LocalDate issueDate;

    private String expressCompany;

    private String expressNo;

    private LocalDate shipDate;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> cancelAttachmentIds;
}
