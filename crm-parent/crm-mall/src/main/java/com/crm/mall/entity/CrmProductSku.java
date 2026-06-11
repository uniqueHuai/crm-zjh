package com.crm.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@TableName("mall_sku")
public class CrmProductSku implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long productId;

    @TableField(value = "specs", typeHandler = JacksonTypeHandler.class)
    private Object attrs;

    private BigDecimal price;

    @TableField(exist = false)
    private BigDecimal originalPrice;

    private Integer stock;

    @TableField("cover_image")
    private String skuImage;

    @TableField(exist = false)
    private Integer sortOrder;

    private Integer status;
}
