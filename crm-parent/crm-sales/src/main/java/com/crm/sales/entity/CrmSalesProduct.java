package com.crm.sales.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.crm.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("crm_sales_product")
public class CrmSalesProduct extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long categoryId;

    private String name;

    private String unit;

    private BigDecimal standardPrice;

    private BigDecimal costPrice;

    private String description;

    private String specifications;

    private Integer status;

    @TableField(exist = false)
    private String categoryName;
}
