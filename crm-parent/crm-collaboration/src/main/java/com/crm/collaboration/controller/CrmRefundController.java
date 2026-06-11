package com.crm.collaboration.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.model.R;
import com.crm.framework.aspect.OperationLog;
import com.crm.collaboration.entity.CrmRefund;
import com.crm.collaboration.service.ICrmRefundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "退货退款", description = "退款申请、审核、物流、完成")
@RestController
@RequiredArgsConstructor
public class CrmRefundController {

    private final ICrmRefundService refundService;

    @Operation(summary = "发起退款（小程序端）")
    @PostMapping("/app/refunds")
    public R<Void> create(@RequestBody CrmRefund refund) {
        refund.setStatus("pending");
        refundService.save(refund);
        return R.ok();
    }

    @Operation(summary = "退款列表")
    @GetMapping("/refunds")
    @PreAuthorize("hasAuthority('collaboration:refund:list')")
    public R<IPage<CrmRefund>> list(@RequestParam(defaultValue = "1") long page,
                                    @RequestParam(defaultValue = "20") long size,
                                    @RequestParam(required = false) String status,
                                    @RequestParam(required = false) Long orderId) {
        LambdaQueryWrapper<CrmRefund> wrapper = new LambdaQueryWrapper<CrmRefund>()
                .eq(status != null, CrmRefund::getStatus, status)
                .eq(orderId != null, CrmRefund::getOrderId, orderId)
                .orderByDesc(CrmRefund::getCreatedAt);
        return R.ok(refundService.page(new Page<>(page, size), wrapper));
    }

    @Operation(summary = "退款详情")
    @GetMapping("/refunds/{id}")
    @PreAuthorize("hasAuthority('collaboration:refund:query')")
    public R<CrmRefund> get(@PathVariable Long id) {
        return R.ok(refundService.getById(id));
    }

    @Operation(summary = "审核通过")
    @PutMapping("/refunds/{id}/approve")
    @PreAuthorize("hasAuthority('collaboration:refund:approve')")
    @OperationLog(module = "collaboration", action = "approve", description = "退款审核通过")
    public R<Void> approve(@PathVariable Long id) {
        refundService.lambdaUpdate().eq(CrmRefund::getId, id)
                .set(CrmRefund::getStatus, "approved")
                .update();
        return R.ok();
    }

    @Operation(summary = "审核驳回")
    @PutMapping("/refunds/{id}/reject")
    @PreAuthorize("hasAuthority('collaboration:refund:reject')")
    @OperationLog(module = "collaboration", action = "reject", description = "退款审核驳回")
    public R<Void> reject(@PathVariable Long id, @RequestBody CrmRefund refund) {
        refundService.lambdaUpdate().eq(CrmRefund::getId, id)
                .set(CrmRefund::getStatus, "rejected")
                .set(CrmRefund::getRejectReason, refund.getRejectReason())
                .update();
        return R.ok();
    }

    @Operation(summary = "用户寄回")
    @PutMapping("/refunds/{id}/ship-back")
    public R<Void> shipBack(@PathVariable Long id, @RequestBody CrmRefund refund) {
        refundService.lambdaUpdate().eq(CrmRefund::getId, id)
                .set(CrmRefund::getStatus, "shipping")
                .set(CrmRefund::getExpressCompany, refund.getExpressCompany())
                .set(CrmRefund::getExpressNo, refund.getExpressNo())
                .update();
        return R.ok();
    }

    @Operation(summary = "仓库收货")
    @PutMapping("/refunds/{id}/receive")
    @PreAuthorize("hasAuthority('collaboration:refund:receive')")
    @OperationLog(module = "collaboration", action = "update", description = "仓库收货")
    public R<Void> receive(@PathVariable Long id) {
        refundService.lambdaUpdate().eq(CrmRefund::getId, id)
                .set(CrmRefund::getStatus, "received")
                .update();
        return R.ok();
    }

    @Operation(summary = "退款完成")
    @PutMapping("/refunds/{id}/complete")
    @PreAuthorize("hasAuthority('collaboration:refund:complete')")
    @OperationLog(module = "collaboration", action = "update", description = "退款完成")
    public R<Void> complete(@PathVariable Long id) {
        refundService.lambdaUpdate().eq(CrmRefund::getId, id)
                .set(CrmRefund::getStatus, "completed")
                .update();
        return R.ok();
    }
}
