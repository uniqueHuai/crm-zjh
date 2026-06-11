package com.crm.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.crm.mall.entity.CrmCartItem;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface CrmCartItemMapper extends BaseMapper<CrmCartItem> {

    @Select("""
        SELECT ci.*, p.name product_name, p.cover_image product_image, s.price, s.specs sku_specs, s.stock
        FROM mall_cart_item ci
        JOIN sale_product p ON ci.product_id = p.id
        LEFT JOIN mall_sku s ON ci.sku_id = s.id
        WHERE ci.customer_id = #{customerId}
        ORDER BY ci.updated_at DESC
        """)
    List<Map<String, Object>> selectCartDetail(Long customerId);
}
