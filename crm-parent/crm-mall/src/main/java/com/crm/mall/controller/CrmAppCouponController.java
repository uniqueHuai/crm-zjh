package com.crm.mall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.model.R;
import com.crm.mall.entity.CrmCoupon;
import com.crm.mall.entity.CrmCouponUser;
import com.crm.mall.mp.MpContext;
import com.crm.mall.service.ICrmCouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "小程序端优惠券")
@RestController
@RequestMapping("/app/coupons")
@RequiredArgsConstructor
public class CrmAppCouponController {

    private final ICrmCouponService couponService;

    @Operation(summary = "小程序端可领取列表")
    @GetMapping
    public R<IPage<CrmCoupon>> available(@RequestParam(defaultValue = "1") long page,
                                          @RequestParam(defaultValue = "20") long size) {
        return R.ok(couponService.page(new Page<>(page, size),
                new LambdaQueryWrapper<CrmCoupon>()
                        .eq(CrmCoupon::getStatus, 1)
                        .orderByDesc(CrmCoupon::getCreatedAt)));
    }

    @Operation(summary = "小程序端可领取列表 (alias)")
    @Deprecated
    @GetMapping("/available")
    public R<List<CrmCoupon>> availableList() {
        List<CrmCoupon> list = couponService.list(
                new LambdaQueryWrapper<CrmCoupon>()
                        .eq(CrmCoupon::getStatus, 1)
                        .orderByDesc(CrmCoupon::getCreatedAt));
        return R.ok(list);
    }

    @Operation(summary = "领取优惠券")
    @PostMapping("/{id}/receive")
    public R<Void> receive(@PathVariable Long id) {
        couponService.receive(id, MpContext.getCustomerId());
        return R.ok();
    }

    @Operation(summary = "领取优惠券 (alias)")
    @Deprecated
    @PostMapping("/{id}/claim")
    public R<Void> claim(@PathVariable Long id) {
        couponService.receive(id, MpContext.getCustomerId());
        return R.ok();
    }

    @Operation(summary = "我的优惠券")
    @GetMapping("/my")
    public R<IPage<CrmCouponUser>> myCoupons(@RequestParam(defaultValue = "1") long page,
                                              @RequestParam(defaultValue = "20") long size) {
        return R.ok(couponService.selectMyCoupons(new Page<>(page, size), MpContext.getCustomerId()));
    }
}
