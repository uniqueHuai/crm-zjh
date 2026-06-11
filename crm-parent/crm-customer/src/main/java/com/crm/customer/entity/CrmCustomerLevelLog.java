package com.crm.customer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("crm_customer_level_log")
public class CrmCustomerLevelLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long customerId;

    private Long oldLevelId;

    private Long newLevelId;

    private String changeType;

    private String reason;

    private Long operatorId;

    private LocalDateTime createdAt;
}
