package com.crm.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.crm.common.exception.BizException;
import com.crm.mall.entity.CrmCartItem;
import com.crm.mall.entity.CrmProductSku;
import com.crm.mall.mapper.CrmCartItemMapper;
import com.crm.mall.mapper.CrmProductSkuMapper;
import com.crm.mall.service.ICrmCartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CrmCartServiceImpl implements ICrmCartService {

    private final CrmCartItemMapper cartItemMapper;
    private final CrmProductSkuMapper skuMapper;

    @Override
    public List<CrmCartItem> listByCustomerId(Long customerId) {
        List<Map<String, Object>> rows = cartItemMapper.selectCartDetail(customerId);
        return rows.stream().map(this::toCartItem).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addItem(Long customerId, Long skuId, Integer quantity) {
        // check existing
        LambdaQueryWrapper<CrmCartItem> wrapper = new LambdaQueryWrapper<CrmCartItem>()
                .eq(CrmCartItem::getCustomerId, customerId)
                .eq(CrmCartItem::getSkuId, skuId);
        CrmCartItem existing = cartItemMapper.selectOne(wrapper);
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + quantity);
            existing.setUpdatedAt(LocalDateTime.now());
            return cartItemMapper.updateById(existing) > 0;
        }

        // need product_id from sku
        CrmProductSku sku = skuMapper.selectById(skuId);
        if (sku == null) {
            throw new BizException(404, "SKU不存在");
        }

        CrmCartItem item = new CrmCartItem();
        item.setCustomerId(customerId);
        item.setProductId(sku.getProductId());
        item.setSkuId(skuId);
        item.setQuantity(quantity);
        item.setSelected(true);
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());
        return cartItemMapper.insert(item) > 0;
    }

    @Override
    public boolean updateQuantity(Long id, Long customerId, Integer quantity) {
        return cartItemMapper.update(null,
                new LambdaUpdateWrapper<CrmCartItem>()
                        .eq(CrmCartItem::getId, id)
                        .eq(CrmCartItem::getCustomerId, customerId)
                        .set(CrmCartItem::getQuantity, quantity)
                        .set(CrmCartItem::getUpdatedAt, LocalDateTime.now())) > 0;
    }

    @Override
    public boolean toggleSelected(Long id, Long customerId, Boolean selected) {
        return cartItemMapper.update(null,
                new LambdaUpdateWrapper<CrmCartItem>()
                        .eq(CrmCartItem::getId, id)
                        .eq(CrmCartItem::getCustomerId, customerId)
                        .set(CrmCartItem::getSelected, selected)) > 0;
    }

    @Override
    public boolean deleteItem(Long id, Long customerId) {
        return cartItemMapper.delete(
                new LambdaQueryWrapper<CrmCartItem>()
                        .eq(CrmCartItem::getId, id)
                        .eq(CrmCartItem::getCustomerId, customerId)) > 0;
    }

    @Override
    public void clearByCustomerId(Long customerId, List<Long> skuIds) {
        if (skuIds != null && !skuIds.isEmpty()) {
            cartItemMapper.delete(
                    new LambdaQueryWrapper<CrmCartItem>()
                            .eq(CrmCartItem::getCustomerId, customerId)
                            .in(CrmCartItem::getSkuId, skuIds));
        }
    }

    private CrmCartItem toCartItem(Map<String, Object> row) {
        CrmCartItem item = new CrmCartItem();
        item.setId(toLong(row.get("id")));
        item.setCustomerId(toLong(row.get("customer_id")));
        item.setProductId(toLong(row.get("product_id")));
        item.setSkuId(toLong(row.get("sku_id")));
        item.setQuantity(toInt(row.get("quantity")));
        item.setSelected(Boolean.TRUE.equals(row.get("selected")));
        item.setProductName((String) row.get("product_name"));
        item.setProductImage((String) row.get("product_image"));
        item.setPrice((BigDecimal) row.get("price"));
        item.setSkuSpecs(row.get("sku_specs") != null ? row.get("sku_specs").toString() : null);
        item.setStock(toInt(row.get("stock")));
        return item;
    }

    private Long toLong(Object v) { return v != null ? ((Number) v).longValue() : null; }
    private Integer toInt(Object v) { return v != null ? ((Number) v).intValue() : null; }
}
