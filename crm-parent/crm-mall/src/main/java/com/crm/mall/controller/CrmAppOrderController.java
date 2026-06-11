package com.crm.mall.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.model.R;
import com.crm.mall.entity.CrmOrder;
import com.crm.mall.mp.MpContext;
import com.crm.mall.service.ICrmOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "小程序端订单")
@RestController
@RequestMapping("/app/orders")
@RequiredArgsConstructor
public class CrmAppOrderController {

    private final ICrmOrderService orderService;

    @Operation(summary = "我的订单列表")
    @GetMapping
    public R<IPage<CrmOrder>> myOrders(@RequestParam(defaultValue = "1") long page,
                                        @RequestParam(defaultValue = "20") long size,
                                        @RequestParam(required = false) String status) {
        return R.ok(orderService.selectPageWithCondition(
                new Page<>(page, size), MpContext.getCustomerId(), status, null, null, null));
    }

    @Operation(summary = "创建订单")
    @PostMapping
    public R<CrmOrder> create(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> skuItems = (List<Map<String, Object>>) body.get("items");
        Long addressId = body.containsKey("addressId") ? Long.valueOf(body.get("addressId").toString()) : null;
        Long couponId = body.containsKey("couponId") ? Long.valueOf(body.get("couponId").toString()) : null;
        String remark = (String) body.get("remark");
        CrmOrder order = orderService.createOrder(MpContext.getCustomerId(), skuItems, addressId, couponId, remark);
        return R.ok(order);
    }

    @Operation(summary = "支付订单（简化-直接标记为已支付）")
    @PostMapping("/{id}/pay")
    public R<Void> pay(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String method = "offline";
        if (body != null) {
            method = body.getOrDefault("method", body.getOrDefault("paymentMethod", "offline"));
        }
        orderService.payOrder(id, MpContext.getCustomerId(), method);
        return R.ok();
    }

    @Operation(summary = "取消订单")
    @PostMapping("/{id}/cancel")
    public R<Void> cancel(@PathVariable Long id) {
        orderService.updateStatus(id, MpContext.getCustomerId(), "cancelled");
        return R.ok();
    }

    @Operation(summary = "确认收货")
    @PostMapping("/{id}/confirm")
    public R<Void> confirm(@PathVariable Long id) {
        orderService.updateStatus(id, MpContext.getCustomerId(), "completed");
        return R.ok();
    }

    @Operation(summary = "订单详情")
    @GetMapping("/{id}")
    public R<CrmOrder> detail(@PathVariable Long id) {
        return R.ok(orderService.selectWithItems(id));
    }
}
