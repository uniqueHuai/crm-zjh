package com.crm.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.common.exception.BizException;
import com.crm.mall.entity.*;
import com.crm.mall.mapper.*;
import com.crm.mall.service.ICrmCartService;
import com.crm.mall.service.ICrmDistributionService;
import com.crm.mall.service.ICrmOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrmOrderServiceImpl extends ServiceImpl<CrmOrderMapper, CrmOrder> implements ICrmOrderService {

    private final CrmOrderMapper orderMapper;
    private final CrmOrderItemMapper orderItemMapper;
    private final CrmPaymentMapper paymentMapper;
    private final CrmCouponUserMapper couponUserMapper;
    private final ICrmCartService cartService;
    private final ICrmDistributionService distributionService;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public IPage<CrmOrder> selectPageWithCondition(Page<CrmOrder> page, Long customerId, String status,
                                                     String startDate, String endDate, String keywords) {
        IPage<CrmOrder> result = orderMapper.selectPage(page, new LambdaQueryWrapper<CrmOrder>()
                .eq(customerId != null, CrmOrder::getCustomerId, customerId)
                .eq(status != null, CrmOrder::getStatus, status)
                .ge(startDate != null, CrmOrder::getCreatedAt, startDate)
                .le(endDate != null, CrmOrder::getCreatedAt, endDate + " 23:59:59")
                .and(keywords != null && !keywords.isEmpty(), w -> w
                        .like(CrmOrder::getOrderNo, keywords)
                        .or()
                        .apply("EXISTS (SELECT 1 FROM crm_customer c WHERE c.id = customer_id AND c.name LIKE {0})",
                                "%" + keywords + "%"))
                .orderByDesc(CrmOrder::getCreatedAt));
        populateCustomerNames(result.getRecords());
        return result;
    }

    private void populateCustomerNames(List<CrmOrder> orders) {
        List<Long> ids = orders.stream().map(CrmOrder::getCustomerId).filter(Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) return;
        String placeholders = ids.stream().map(i -> "?").collect(Collectors.joining(","));
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, name FROM crm_customer WHERE id IN (" + placeholders + ")", ids.toArray());
        Map<Long, String> nameMap = rows.stream()
                .collect(Collectors.toMap(r -> ((Number) r.get("id")).longValue(), r -> (String) r.get("name")));
        orders.forEach(o -> o.setUserName(nameMap.getOrDefault(o.getCustomerId(), "客户#" + o.getCustomerId())));
    }

    @Override
    public CrmOrder selectWithItems(Long id) {
        CrmOrder order = getById(id);
        if (order != null) {
            order.setItems(orderItemMapper.selectList(
                    new LambdaQueryWrapper<CrmOrderItem>()
                            .eq(CrmOrderItem::getOrderId, id)));
        }
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Long id, Long customerId, String status) {
        CrmOrder order = getById(id);
        if (order == null) throw new BizException(404, "订单不存在");
        if (customerId != null && !customerId.equals(order.getCustomerId())) {
            throw new BizException(403, "无权操作该订单");
        }
        return lambdaUpdate().eq(CrmOrder::getId, id)
                .set(CrmOrder::getStatus, status)
                .update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CrmOrder createOrder(Long customerId, List<Map<String, Object>> skuItems,
                                 Long addressId, Long couponId, String remark) {
        if (skuItems == null || skuItems.isEmpty()) {
            throw new BizException(400, "下单商品不能为空");
        }

        // 1. resolve address
        String consignee = "", phone = "", address = "";
        if (addressId != null) {
            try {
                Map<String, Object> addr = jdbcTemplate.queryForMap(
                        "SELECT receiver_name, receiver_phone, province, city, district, detail_address FROM mp_address WHERE id = ? AND deleted_at IS NULL",
                        addressId);
                consignee = (String) addr.get("receiver_name");
                phone = (String) addr.get("receiver_phone");
                String p = nvl(addr.get("province"));
                String c = nvl(addr.get("city"));
                String d = nvl(addr.get("district"));
                String det = nvl(addr.get("detail_address"));
                address = p + c + d + det;
            } catch (Exception e) {
                throw new BizException(404, "收货地址不存在");
            }
        }

        // 2. resolve skus, validate stock, calculate amounts
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (Map<String, Object> item : skuItems) {
            Long skuId = Long.valueOf(item.get("skuId").toString());
            Map<String, Object> sku = jdbcTemplate.queryForMap(
                    "SELECT s.*, p.name product_name, p.cover_image FROM mall_sku s JOIN sale_product p ON s.product_id = p.id WHERE s.id = ?",
                    skuId);
            int qty = Integer.parseInt(item.get("quantity").toString());
            int stock = ((Number) sku.get("stock")).intValue();
            if (stock < qty) {
                throw new BizException(400, "商品「" + sku.get("product_name") + "」库存不足");
            }
            BigDecimal price = (BigDecimal) sku.get("price");
            BigDecimal subtotal = price.multiply(BigDecimal.valueOf(qty));
            item.put("_price", price);
            item.put("_productName", sku.get("product_name"));
            item.put("_productId", sku.get("product_id"));
            item.put("_specs", sku.get("specs"));
            item.put("_coverImage", sku.get("cover_image"));
            item.put("_subtotal", subtotal);
            item.put("_stock", stock);
            totalAmount = totalAmount.add(subtotal);
        }

        // 3. apply coupon discount
        BigDecimal discountAmount = BigDecimal.ZERO;
        CrmCouponUser usedCoupon = null;
        if (couponId != null) {
            List<CrmCouponUser> coupons = couponUserMapper.selectList(
                    new LambdaQueryWrapper<CrmCouponUser>()
                            .eq(CrmCouponUser::getCouponId, couponId)
                            .eq(CrmCouponUser::getUserId, customerId)
                            .eq(CrmCouponUser::getStatus, "unused"));
            if (!coupons.isEmpty()) {
                usedCoupon = coupons.get(0);
                try {
                    Map<String, Object> couponDef = jdbcTemplate.queryForMap(
                            "SELECT type, value, condition_amount FROM mall_coupon_define WHERE id = ?", couponId);
                    String type = (String) couponDef.get("type");
                    BigDecimal value = (BigDecimal) couponDef.get("value");
                    BigDecimal condition = (BigDecimal) couponDef.get("condition_amount");
                    if (condition != null && condition.compareTo(BigDecimal.ZERO) > 0
                            && totalAmount.compareTo(condition) < 0) {
                        usedCoupon = null;
                    } else {
                        if ("full_reduce".equals(type)) {
                            discountAmount = value;
                        } else if ("discount".equals(type) && value != null) {
                            discountAmount = totalAmount.multiply(BigDecimal.ONE.subtract(value));
                        }
                    }
                } catch (Exception ignored) {}
            }
        }

        // 4. deduct stock
        for (Map<String, Object> item : skuItems) {
            Long skuId = Long.valueOf(item.get("skuId").toString());
            int qty = Integer.parseInt(item.get("quantity").toString());
            int affected = jdbcTemplate.update(
                    "UPDATE mall_sku SET frozen_stock = frozen_stock + ?, stock = stock - ? WHERE id = ? AND stock >= ?",
                    qty, qty, skuId, qty);
            if (affected == 0) {
                throw new BizException(400, "库存扣减失败");
            }
        }

        // 5. create order
        String orderNo = "ORD" + System.currentTimeMillis();
        CrmOrder order = new CrmOrder();
        order.setOrderNo(orderNo);
        order.setCustomerId(customerId);
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(discountAmount);
        order.setPayAmount(totalAmount.subtract(discountAmount).max(BigDecimal.ZERO));
        order.setStatus("pending");
        order.setConsignee(consignee);
        order.setPhone(phone);
        order.setAddress(address);
        order.setRemark(remark);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        save(order);

        // 6. create order items
        for (Map<String, Object> item : skuItems) {
            CrmOrderItem oi = new CrmOrderItem();
            oi.setOrderId(order.getId());
            oi.setProductId(((Number) item.get("_productId")).longValue());
            oi.setSkuId(Long.valueOf(item.get("skuId").toString()));
            oi.setProductName((String) item.get("_productName"));
            oi.setSkuAttrs(item.get("_specs"));
            oi.setQuantity(Integer.parseInt(item.get("quantity").toString()));
            oi.setPrice((BigDecimal) item.get("_price"));
            oi.setSubtotal((BigDecimal) item.get("_subtotal"));
            orderItemMapper.insert(oi);
        }

        // 7. mark coupon as used
        if (usedCoupon != null) {
            usedCoupon.setStatus("used");
            usedCoupon.setOrderId(order.getId());
            usedCoupon.setUsedAt(LocalDateTime.now());
            couponUserMapper.updateById(usedCoupon);
        }

        // 8. clear cart
        List<Long> skuIds = skuItems.stream()
                .map(i -> Long.valueOf(i.get("skuId").toString()))
                .toList();
        cartService.clearByCustomerId(customerId, skuIds);

        order.setItems(orderItemMapper.selectList(
                new LambdaQueryWrapper<CrmOrderItem>()
                        .eq(CrmOrderItem::getOrderId, order.getId())));
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean payOrder(Long orderId, Long customerId, String paymentMethod) {
        CrmOrder order = getById(orderId);
        if (order == null) throw new BizException(404, "订单不存在");
        if (customerId != null && !customerId.equals(order.getCustomerId())) {
            throw new BizException(403, "无权操作该订单");
        }
        if (!"pending".equals(order.getStatus())) {
            throw new BizException(400, "订单状态不允许支付");
        }

        CrmPayment payment = new CrmPayment();
        payment.setOrderId(orderId);
        payment.setPaymentNo("PAY" + System.currentTimeMillis());
        payment.setPaymentMethod(paymentMethod != null ? paymentMethod : "offline");
        payment.setAmount(order.getPayAmount());
        payment.setStatus("success");
        payment.setPaidAt(LocalDateTime.now());
        payment.setCreatedAt(LocalDateTime.now());
        paymentMapper.insert(payment);

        lambdaUpdate().eq(CrmOrder::getId, orderId)
                .set(CrmOrder::getStatus, "paid")
                .set(CrmOrder::getPaymentMethod, paymentMethod != null ? paymentMethod : "offline")
                .set(CrmOrder::getPaidAt, LocalDateTime.now())
                .update();

        jdbcTemplate.update("""
            UPDATE crm_customer SET
                total_consumption = COALESCE(total_consumption, 0) + ?,
                order_count = COALESCE(order_count, 0) + 1
            WHERE id = ?
            """, order.getPayAmount(), order.getCustomerId());

        // calculate distribution commission
        try {
            distributionService.calculateCommission(orderId, order.getCustomerId(), order.getPayAmount());
        } catch (Exception e) {
            log.warn("commission calculation failed", e);
        }

        return true;
    }

    private String nvl(Object v) {
        return v != null ? v.toString() : "";
    }
}
