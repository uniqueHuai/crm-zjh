package com.crm.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.crm.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sale_product")
public class CrmProduct extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long categoryId;

    private String name;

    private String description;

    private String coverImage;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> images;

    private BigDecimal standardPrice;

    private BigDecimal costPrice;

    private String unit;

    @TableField(exist = false)
    private String specs;

    @TableField(exist = false)
    private Integer salesVolume;

    private Integer sortOrder;

    private Integer status;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object extJson;

    @TableField(exist = false)
    private List<CrmProductSku> skus;

    @TableField(exist = false)
    private String categoryName;
}
