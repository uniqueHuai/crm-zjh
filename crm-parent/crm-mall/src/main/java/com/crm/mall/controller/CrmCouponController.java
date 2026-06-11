package com.crm.mall.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.model.R;
import com.crm.framework.aspect.OperationLog;
import com.crm.mall.entity.CrmCoupon;
import com.crm.mall.service.ICrmCouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "优惠券管理", description = "优惠券 CRUD、发放")
@RestController
@RequestMapping("/coupons")
@RequiredArgsConstructor
public class CrmCouponController {

    private final ICrmCouponService couponService;

    @Operation(summary = "优惠券列表")
    @GetMapping
    @PreAuthorize("hasAuthority('mall:coupon:list')")
    public R<IPage<CrmCoupon>> page(@RequestParam(defaultValue = "1") long page,
                                     @RequestParam(defaultValue = "20") long size,
                                     @RequestParam(required = false) String keywords,
                                     @RequestParam(required = false) Integer status) {
        return R.ok(couponService.selectPageWithCondition(new Page<>(page, size), keywords, status));
    }

    @Operation(summary = "创建优惠券")
    @PostMapping
    @PreAuthorize("hasAuthority('mall:coupon:create')")
    @OperationLog(module = "mall", action = "create", description = "创建优惠券")
    public R<Void> create(@RequestBody CrmCoupon coupon) {
        couponService.save(coupon);
        return R.ok();
    }

    @Operation(summary = "更新优惠券")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('mall:coupon:edit')")
    @OperationLog(module = "mall", action = "update", description = "更新优惠券")
    public R<Void> update(@PathVariable Long id, @RequestBody CrmCoupon coupon) {
        coupon.setId(id);
        couponService.updateById(coupon);
        return R.ok();
    }

    @Operation(summary = "删除优惠券")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('mall:coupon:delete')")
    @OperationLog(module = "mall", action = "delete", description = "删除优惠券")
    public R<Void> delete(@PathVariable Long id) {
        couponService.removeById(id);
        return R.ok();
    }

    @Operation(summary = "发放优惠券")
    @PostMapping("/{id}/distribute")
    @PreAuthorize("hasAuthority('mall:coupon:distribute')")
    @OperationLog(module = "mall", action = "distribute", description = "发放优惠券")
    public R<Void> distribute(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Object rawIds = body.get("userIds");
        if (rawIds == null) {
            return R.failed(400, "userIds不能为空");
        }
        @SuppressWarnings("unchecked")
        List<Long> userIds = ((List<Object>) rawIds).stream()
                .map(v -> v instanceof Number ? ((Number) v).longValue() : Long.valueOf(v.toString()))
                .toList();
        couponService.distribute(id, userIds);
        return R.ok();
    }
}
