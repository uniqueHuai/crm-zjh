package com.crm.sales.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@TableName("crm_payment_plan")
public class CrmPaymentPlan implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long contractId;

    private Integer stage;

    private String stageName;

    private BigDecimal expectedAmount;

    private BigDecimal actualAmount;

    private LocalDate expectedDate;

    private LocalDate paidDate;

    private String status;

    private String paymentMethod;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> voucherUrls;

    private String remark;
}
