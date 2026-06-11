package com.crm.mall.controller;

import com.crm.common.model.R;
import com.crm.mall.entity.CrmActivity;
import com.crm.mall.entity.CrmOrder;
import com.crm.mall.mp.MpContext;
import com.crm.mall.service.ICrmActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "小程序端活动")
@RestController
@RequestMapping("/app/activities")
@RequiredArgsConstructor
public class CrmAppActivityController {

    private final ICrmActivityService activityService;

    @Operation(summary = "进行中的活动列表")
    @GetMapping
    public R<List<CrmActivity>> activeList(@RequestParam(required = false) String type) {
        return R.ok(activityService.selectActiveActivities(type));
    }

    @Operation(summary = "活动商品列表")
    @GetMapping("/{id}/products")
    public R<List<Map<String, Object>>> products(@PathVariable Long id) {
        return R.ok(activityService.selectActivityProducts(id));
    }

    @Operation(summary = "秒杀下单")
    @PostMapping("/seckill")
    public R<CrmOrder> seckill(@RequestBody Map<String, Object> body) {
        Long activityId = Long.valueOf(body.get("activityId").toString());
        Long skuId = Long.valueOf(body.get("skuId").toString());
        Integer quantity = body.containsKey("quantity") ? Integer.valueOf(body.get("quantity").toString()) : 1;
        Long addressId = body.containsKey("addressId") ? Long.valueOf(body.get("addressId").toString()) : null;
        CrmOrder order = activityService.createSeckillOrder(MpContext.getCustomerId(), activityId, skuId, quantity, addressId);
        return R.ok(order);
    }

    @Operation(summary = "拼团下单")
    @PostMapping("/group")
    public R<CrmOrder> groupBuy(@RequestBody Map<String, Object> body) {
        Long activityId = Long.valueOf(body.get("activityId").toString());
        Long skuId = Long.valueOf(body.get("skuId").toString());
        Long addressId = body.containsKey("addressId") ? Long.valueOf(body.get("addressId").toString()) : null;
        String groupId = (String) body.get("groupId");
        CrmOrder order = activityService.createGroupBuyOrder(MpContext.getCustomerId(), activityId, skuId, addressId, groupId);
        return R.ok(order);
    }
}
