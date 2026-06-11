package com.crm.collaboration.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.crm.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("coll_service_ticket")
public class CrmTicket extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long customerId;

    private String ticketNo;

    private String type;

    private String title;

    private String description;

    private String priority;

    private String source;

    @TableField(value = "attachment_ids", typeHandler = JacksonTypeHandler.class)
    private List<Long> attachments;

    private Long assigneeId;

    private String status;

    @TableField("customer_rating")
    private Integer rating;

    @TableField(exist = false)
    private String customerName;

    @TableField(exist = false)
    private String assigneeName;
}
