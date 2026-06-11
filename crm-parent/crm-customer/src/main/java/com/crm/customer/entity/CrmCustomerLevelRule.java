package com.crm.customer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("crm_customer_level_rule")
public class CrmCustomerLevelRule implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long levelId;

    private String ruleType;

    private String conditionField;

    private String conditionOperator;

    private BigDecimal conditionValue;

    private Integer periodDays;

    private String evaluateCycle;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
