package com.crm.sales.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.common.model.R;
import com.crm.framework.aspect.OperationLog;
import com.crm.sales.entity.CrmPaymentPlan;
import com.crm.sales.service.ICrmPaymentPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "回款计划", description = "合同回款计划 CRUD、回款登记、批量生成")
@RestController
@RequiredArgsConstructor
public class CrmPaymentPlanController {

    private final ICrmPaymentPlanService paymentPlanService;

    @Operation(summary = "合同回款计划列表")
    @GetMapping("/contracts/{contractId}/payment-plans")
    @PreAuthorize("hasAuthority('sales:payment:list')")
    public R<List<CrmPaymentPlan>> list(@PathVariable Long contractId) {
        return R.ok(paymentPlanService.lambdaQuery()
                .eq(CrmPaymentPlan::getContractId, contractId)
                .orderByAsc(CrmPaymentPlan::getStage)
                .list());
    }

    @Operation(summary = "新增回款计划")
    @PostMapping("/contracts/{contractId}/payment-plans")
    @PreAuthorize("hasAuthority('sales:payment:create')")
    @OperationLog(module = "sales", action = "create", description = "新增回款计划")
    public R<Void> create(@PathVariable Long contractId, @RequestBody CrmPaymentPlan plan) {
        plan.setContractId(contractId);
        paymentPlanService.save(plan);
        return R.ok();
    }

    @Operation(summary = "更新回款计划")
    @PutMapping("/payment-plans/{id}")
    @PreAuthorize("hasAuthority('sales:payment:edit')")
    @OperationLog(module = "sales", action = "update", description = "更新回款计划")
    public R<Void> update(@PathVariable Long id, @RequestBody CrmPaymentPlan plan) {
        plan.setId(id);
        paymentPlanService.updateById(plan);
        return R.ok();
    }

    @Operation(summary = "删除回款计划")
    @DeleteMapping("/payment-plans/{id}")
    @PreAuthorize("hasAuthority('sales:payment:delete')")
    @OperationLog(module = "sales", action = "delete", description = "删除回款计划")
    public R<Void> delete(@PathVariable Long id) {
        paymentPlanService.removeById(id);
        return R.ok();
    }

    @Operation(summary = "回款登记")
    @PutMapping("/payment-plans/{id}/settle")
    @PreAuthorize("hasAuthority('sales:payment:edit')")
    @OperationLog(module = "sales", action = "update", description = "回款登记")
    public R<Void> settle(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        paymentPlanService.settle(id, params);
        return R.ok();
    }

    @Operation(summary = "回款计划批量生成")
    @PostMapping("/contracts/{contractId}/payment-plans/batch")
    @PreAuthorize("hasAuthority('sales:payment:create')")
    @OperationLog(module = "sales", action = "create", description = "批量生成回款计划")
    public R<List<CrmPaymentPlan>> batchGenerate(@PathVariable Long contractId, @RequestBody Map<String, Object> params) {
        return R.ok(paymentPlanService.batchGenerate(contractId, params));
    }
}
