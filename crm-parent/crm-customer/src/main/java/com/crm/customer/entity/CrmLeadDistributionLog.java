package com.crm.customer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("crm_lead_distribution_log")
public class CrmLeadDistributionLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long ruleId;

    private Long leadId;

    private Long fromOwnerId;

    private Long toOwnerId;

    private String assignType;

    private LocalDateTime createdAt;
}
