package com.crm.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.common.exception.BizException;
import com.crm.mall.entity.CrmActivity;
import com.crm.mall.entity.CrmOrder;
import com.crm.mall.entity.CrmOrderItem;
import com.crm.mall.mapper.CrmActivityMapper;
import com.crm.mall.mapper.CrmOrderItemMapper;
import com.crm.mall.mapper.CrmOrderMapper;
import com.crm.mall.service.ICrmActivityService;
import com.crm.mall.service.ICrmOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CrmActivityServiceImpl extends ServiceImpl<CrmActivityMapper, CrmActivity> implements ICrmActivityService {

    private final CrmActivityMapper activityMapper;
    private final CrmOrderMapper orderMapper;
    private final CrmOrderItemMapper orderItemMapper;
    private final ICrmOrderService orderService;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public IPage<CrmActivity> selectPageWithCondition(Page<CrmActivity> page, String keywords, String type, Integer status) {
        return activityMapper.selectPage(page, new LambdaQueryWrapper<CrmActivity>()
                .like(keywords != null && !keywords.isEmpty(), CrmActivity::getName, keywords)
                .eq(type != null, CrmActivity::getType, type)
                .eq(status != null, CrmActivity::getStatus, status)
                .orderByDesc(CrmActivity::getCreatedAt));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Long id, Integer status) {
        CrmActivity activity = getById(id);
        if (activity == null) throw new BizException(404, "活动不存在");
        return lambdaUpdate().eq(CrmActivity::getId, id)
                .set(CrmActivity::getStatus, status)
                .update();
    }

    @Override
    public List<CrmActivity> selectActiveActivities(String type) {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<CrmActivity> wrapper = new LambdaQueryWrapper<CrmActivity>()
                .eq(CrmActivity::getStatus, 1)
                .le(CrmActivity::getStartTime, now)
                .ge(CrmActivity::getEndTime, now)
                .orderByAsc(CrmActivity::getStartTime);
        if (type != null) {
            wrapper.eq(CrmActivity::getType, type);
        }
        return activityMapper.selectList(wrapper);
    }

    @Override
    public List<Map<String, Object>> selectActivityProducts(Long activityId) {
        return jdbcTemplate.queryForList("""
            SELECT ap.id, ap.activity_id, ap.product_id, ap.sku_id, ap.activity_price,
                   ap.total_stock, ap.sold_stock, ap.limit_per_user,
                   p.name product_name, p.cover_image,
                   s.price original_price, s.stock, s.specs
            FROM mall_activity_product ap
            JOIN sale_product p ON ap.product_id = p.id
            LEFT JOIN mall_sku s ON ap.sku_id = s.id
            WHERE ap.activity_id = ?
            """, activityId);
    }

    @Override
    public Map<String, Object> selectActivityProductSku(Long activityId, Long skuId) {
        try {
            return jdbcTemplate.queryForMap("""
                SELECT ap.*, p.name product_name, p.cover_image,
                       s.price original_price, s.stock, s.specs
                FROM mall_activity_product ap
                JOIN sale_product p ON ap.product_id = p.id
                LEFT JOIN mall_sku s ON ap.sku_id = s.id
                WHERE ap.activity_id = ? AND ap.sku_id = ?
                """, activityId, skuId);
        } catch (Exception e) {
            throw new BizException(404, "活动商品不存在");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CrmOrder createSeckillOrder(Long customerId, Long activityId, Long skuId, Integer quantity, Long addressId) {
        if (quantity == null || quantity <= 0) quantity = 1;

        // validate activity
        Map<String, Object> ap = selectActivityProductSku(activityId, skuId);
        int totalStock = ((Number) ap.get("total_stock")).intValue();
        int soldStock = ((Number) ap.get("sold_stock")).intValue();
        if (soldStock + quantity > totalStock) {
            throw new BizException(400, "活动商品库存不足");
        }

        BigDecimal activityPrice = (BigDecimal) ap.get("activity_price");

        // deduct activity stock
        int affected = jdbcTemplate.update(
                "UPDATE mall_activity_product SET sold_stock = sold_stock + ? WHERE activity_id = ? AND sku_id = ? AND total_stock - sold_stock >= ?",
                quantity, activityId, skuId, quantity);
        if (affected == 0) {
            throw new BizException(400, "活动商品库存扣减失败");
        }

        // create order
        String orderNo = "SK" + System.currentTimeMillis();
        CrmOrder order = new CrmOrder();
        order.setOrderNo(orderNo);
        order.setCustomerId(customerId);
        order.setTotalAmount(activityPrice.multiply(BigDecimal.valueOf(quantity)));
        order.setPayAmount(order.getTotalAmount());
        order.setStatus("pending");
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.insert(order);

        // create order item
        CrmOrderItem item = new CrmOrderItem();
        item.setOrderId(order.getId());
        item.setProductId(((Number) ap.get("product_id")).longValue());
        item.setSkuId(skuId);
        item.setProductName((String) ap.get("product_name"));
        item.setSkuAttrs(ap.get("specs"));
        item.setQuantity(quantity);
        item.setPrice(activityPrice);
        item.setSubtotal(order.getTotalAmount());
        orderItemMapper.insert(item);

        return orderService.selectWithItems(order.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CrmOrder createGroupBuyOrder(Long customerId, Long activityId, Long skuId, Long addressId, String groupId) {
        // validate activity
        Map<String, Object> ap = selectActivityProductSku(activityId, skuId);
        int totalStock = ((Number) ap.get("total_stock")).intValue();
        int soldStock = ((Number) ap.get("sold_stock")).intValue();
        if (soldStock + 1 > totalStock) {
            throw new BizException(400, "活动商品库存不足");
        }

        BigDecimal activityPrice = (BigDecimal) ap.get("activity_price");
        String orderNo = "GP" + System.currentTimeMillis();

        // create order
        CrmOrder order = new CrmOrder();
        order.setOrderNo(orderNo);
        order.setCustomerId(customerId);
        order.setTotalAmount(activityPrice);
        order.setPayAmount(activityPrice);
        order.setStatus("pending");
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.insert(order);

        // create order item
        CrmOrderItem item = new CrmOrderItem();
        item.setOrderId(order.getId());
        item.setProductId(((Number) ap.get("product_id")).longValue());
        item.setSkuId(skuId);
        item.setProductName((String) ap.get("product_name"));
        item.setSkuAttrs(ap.get("specs"));
        item.setQuantity(1);
        item.setPrice(activityPrice);
        item.setSubtotal(activityPrice);
        orderItemMapper.insert(item);

        // join or create group
        if (groupId != null && !groupId.isEmpty()) {
            jdbcTemplate.update("INSERT INTO mp_group_buy_member (group_id, customer_id, order_id, created_at) VALUES (?, ?, ?, NOW())",
                    Long.valueOf(groupId), customerId, order.getId());
            jdbcTemplate.update("UPDATE mp_group_buy SET current_count = current_count + 1 WHERE id = ? AND status = 'pending'",
                    Long.valueOf(groupId));
        } else {
            jdbcTemplate.update("""
                INSERT INTO mp_group_buy (activity_id, product_id, sku_id, leader_id, min_count, current_count, start_time, end_time, status)
                VALUES (?, ?, ?, ?, ?, 1, NOW(), NOW() + INTERVAL '24 hours', 'pending')
                """, activityId, ap.get("product_id"), skuId, customerId,
                    ap.get("limit_per_user") != null ? Math.max(2, ((Number) ap.get("limit_per_user")).intValue()) : 2);
        }

        // deduct activity stock
        jdbcTemplate.update("UPDATE mall_activity_product SET sold_stock = sold_stock + 1 WHERE activity_id = ? AND sku_id = ? AND total_stock - sold_stock >= 1",
                activityId, skuId);

        return orderService.selectWithItems(order.getId());
    }
}
