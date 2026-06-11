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
import java.time.LocalTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("crm_appointment")
public class CrmAppointment extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long customerId;

    private Long contactId;

    private String title;

    private String description;

    private LocalDate appointmentDate;

    private LocalTime startTime;

    private LocalTime endTime;

    private String location;

    private BigDecimal longitude;

    private BigDecimal latitude;

    private String type;

    private Integer remindBefore;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object participantIds;

    private String status;

    private java.time.LocalDateTime checkInTime;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object checkInLocation;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object photoUrls;

    private String summary;

    private String nextStep;

    private Long followUpId;

    private String cancelReason;

    private Long ownerId;

    @TableField(exist = false)
    private String customerName;

    @TableField(exist = false)
    private String ownerName;
}
