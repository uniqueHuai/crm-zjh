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
@TableName("crm_opportunity")
public class CrmOpportunity extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long customerId;

    private Long contactId;

    private String name;

    private BigDecimal expectedAmount;

    private LocalDate expectedCloseDate;

    private Long stageId;

    private BigDecimal budget;

    private String decisionMaker;

    private String competition;

    private String painPoints;

    private String requirements;

    private String solution;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> participantIds;

    private Long ownerId;

    private BigDecimal finalAmount;

    @TableField(exist = false)
    private Integer probability;

    private String loseReason;

    private String loseReasonDetail;

    private String competitor;

    private Long contractId;

    private String remark;

    @TableField(exist = false)
    private String customerName;

    @TableField(exist = false)
    private String contactName;

    @TableField(exist = false)
    private String stageName;

    @TableField(exist = false)
    private String ownerName;
}
