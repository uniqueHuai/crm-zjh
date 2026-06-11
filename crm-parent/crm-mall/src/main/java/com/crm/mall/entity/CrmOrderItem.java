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
@TableName("mall_order_item")
public class CrmOrderItem implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long orderId;

    private Long productId;

    private Long skuId;

    private String productName;

    @TableField(value = "sku_specs", typeHandler = JacksonTypeHandler.class)
    private Object skuAttrs;

    @TableField("unit_price")
    private BigDecimal price;

    private Integer quantity;

    private BigDecimal subtotal;

    @TableField(exist = false)
    private String image;
}
