package com.crm.sales.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.model.R;
import com.crm.framework.aspect.OperationLog;
import com.crm.sales.entity.CrmQuotation;
import com.crm.sales.service.ICrmQuotationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "报价管理", description = "报价单 CRUD、审批、转合同")
@RestController
@RequestMapping("/quotations")
@RequiredArgsConstructor
public class CrmQuotationController {

    private final ICrmQuotationService quotationService;

    @Operation(summary = "报价单分页列表")
    @GetMapping
    @PreAuthorize("hasAuthority('sales:quotation:list')")
    public R<IPage<CrmQuotation>> page(@RequestParam(defaultValue = "1") long page,
                                        @RequestParam(defaultValue = "20") long size,
                                        @RequestParam(required = false) String keywords,
                                        @RequestParam(required = false) Long customerId,
                                        @RequestParam(required = false) Long opportunityId,
                                        @RequestParam(required = false) String status,
                                        @RequestParam(required = false) String startDate,
                                        @RequestParam(required = false) String endDate) {
        return R.ok(quotationService.selectPageWithCondition(new Page<>(page, size), keywords, customerId,
                opportunityId, status, startDate, endDate));
    }

    @Operation(summary = "新增报价单")
    @PostMapping
    @PreAuthorize("hasAuthority('sales:quotation:create')")
    @OperationLog(module = "sales", action = "create", description = "新增报价单")
    public R<Void> create(@RequestBody CrmQuotation quotation) {
        quotationService.save(quotation);
        return R.ok();
    }

    @Operation(summary = "报价单详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('sales:quotation:query')")
    public R<CrmQuotation> get(@PathVariable Long id) {
        return R.ok(quotationService.selectWithItems(id));
    }

    @Operation(summary = "更新报价单")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('sales:quotation:edit')")
    @OperationLog(module = "sales", action = "update", description = "更新报价单")
    public R<Void> update(@PathVariable Long id, @RequestBody CrmQuotation quotation) {
        quotation.setId(id);
        quotationService.updateById(quotation);
        return R.ok();
    }

    @Operation(summary = "删除报价单")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('sales:quotation:delete')")
    @OperationLog(module = "sales", action = "delete", description = "删除报价单")
    public R<Void> delete(@PathVariable Long id) {
        quotationService.removeById(id);
        return R.ok();
    }

    @Operation(summary = "提交审批")
    @PostMapping("/{id}/submit-approval")
    @PreAuthorize("hasAuthority('sales:quotation:approve')")
    @OperationLog(module = "sales", action = "update", description = "提交报价审批")
    public R<Void> submitApproval(@PathVariable Long id, @RequestBody Map<String, String> body) {
        quotationService.submitApproval(id, body.get("remark"));
        return R.ok();
    }

    @Operation(summary = "审批通过")
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('sales:quotation:approve')")
    @OperationLog(module = "sales", action = "update", description = "报价审批通过")
    public R<Void> approve(@PathVariable Long id, @RequestBody Map<String, String> body) {
        quotationService.approve(id, body.get("comment"));
        return R.ok();
    }

    @Operation(summary = "审批驳回")
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('sales:quotation:approve')")
    @OperationLog(module = "sales", action = "update", description = "报价审批驳回")
    public R<Void> reject(@PathVariable Long id, @RequestBody Map<String, String> body) {
        quotationService.reject(id, body.get("comment"));
        return R.ok();
    }

    @Operation(summary = "作废报价单")
    @PutMapping("/{id}/void")
    @PreAuthorize("hasAuthority('sales:quotation:edit')")
    @OperationLog(module = "sales", action = "update", description = "作废报价单")
    public R<Void> voidQuotation(@PathVariable Long id, @RequestBody Map<String, String> body) {
        quotationService.voidQuotation(id, body.get("reason"));
        return R.ok();
    }

    @Operation(summary = "报价转合同")
    @PostMapping("/{id}/generate-contract")
    @PreAuthorize("hasAuthority('sales:quotation:edit')")
    @OperationLog(module = "sales", action = "create", description = "报价转合同")
    public R<Long> generateContract(@PathVariable Long id) {
        return R.ok(quotationService.generateContract(id));
    }

    @Operation(summary = "报价单导出")
    @GetMapping("/{id}/export")
    @PreAuthorize("hasAuthority('sales:quotation:export')")
    public R<Void> export(@PathVariable Long id, @RequestParam(defaultValue = "pdf") String format) {
        // TODO: implement export
        return R.ok();
    }
}
